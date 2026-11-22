package Ir.co.tfs.farazaman.presentation.viewmodel

import Ir.co.tfs.farazaman.data.model.AppUpdateInfo
import Ir.co.tfs.farazaman.data.repository.AppUpdateRepository
import Ir.co.tfs.farazaman.data.repository.UpdateCheckResult
import Ir.co.tfs.farazaman.data.update.ApkValidationException
import Ir.co.tfs.farazaman.data.update.ApkValidationFailure
import Ir.co.tfs.farazaman.data.update.InstalledVersionProvider
import Ir.co.tfs.farazaman.update.AppUpdateInstallStatus
import Ir.co.tfs.farazaman.update.AppUpdateInstallStatusBus
import Ir.co.tfs.farazaman.update.AppUpdateInstaller
import Ir.co.tfs.farazaman.update.isUnknownSourceInstallBlock
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val updateRepository: AppUpdateRepository,
    private val installedVersionProvider: InstalledVersionProvider,
    private val updateInstaller: AppUpdateInstaller,
) : ViewModel() {

    private val mutableUpdateState = MutableStateFlow<SplashUpdateState>(SplashUpdateState.Checking)
    val updateState: StateFlow<SplashUpdateState> = mutableUpdateState.asStateFlow()

    private var downloadedApk: File? = null

    init {
        checkForUpdate()
        collectInstallStatus()
    }

    fun checkForUpdate() {
        mutableUpdateState.value = SplashUpdateState.Checking
        viewModelScope.launch {
            val startedAt = SystemClock.elapsedRealtime()
            val result = updateRepository.checkForUpdate(installedVersionProvider.getVersionCode())
            val remainingSplashTime = MINIMUM_SPLASH_MS - (SystemClock.elapsedRealtime() - startedAt)
            if (remainingSplashTime > 0) delay(remainingSplashTime)

            mutableUpdateState.value = when (result) {
                UpdateCheckResult.UpToDate -> SplashUpdateState.Continue
                is UpdateCheckResult.Available -> SplashUpdateState.Available(result.info)
                is UpdateCheckResult.Failed -> {
                    // Update discovery is fail-open. A temporary outage must not block field work.
                    Log.w(TAG, "Update check failed; continuing startup", result.cause)
                    SplashUpdateState.Continue
                }
            }
        }
    }

    fun downloadUpdate() {
        val info = mutableUpdateState.value.info ?: return
        mutableUpdateState.value = SplashUpdateState.Downloading(info, 0)
        viewModelScope.launch {
            try {
                downloadedApk = updateRepository.download(info) { progress ->
                    mutableUpdateState.value = SplashUpdateState.Downloading(info, progress)
                }
                mutableUpdateState.value = SplashUpdateState.ReadyToInstall(info)
            } catch (e: Exception) {
                Log.e(TAG, "APK download or validation failed", e)
                mutableUpdateState.value = SplashUpdateState.Error(
                    info = info,
                    message = downloadFailureMessage(e),
                    retryAction = UpdateRetryAction.DOWNLOAD,
                )
            }
        }
    }

    fun installUpdate() {
        val info = mutableUpdateState.value.info ?: return
        val apk = downloadedApk
        if (apk == null || !apk.isFile) {
            mutableUpdateState.value = SplashUpdateState.Error(
                info = info,
                message = "فایل بروزرسانی در دسترس نیست.",
                retryAction = UpdateRetryAction.DOWNLOAD,
            )
            return
        }
        if (!updateInstaller.canInstallPackages()) {
            mutableUpdateState.value = SplashUpdateState.InstallPermissionRequired(info)
            return
        }

        mutableUpdateState.value = SplashUpdateState.Installing(info)
        viewModelScope.launch {
            try {
                updateInstaller.install(apk)
            } catch (e: Exception) {
                Log.e(TAG, "Unable to initiate package installation", e)
                mutableUpdateState.value = SplashUpdateState.Error(
                    info = info,
                    message = "شروع نصب نسخه جدید ناموفق بود.",
                    retryAction = UpdateRetryAction.INSTALL,
                )
            }
        }
    }

    fun onInstallPermissionResult() {
        if (updateInstaller.canInstallPackages()) {
            installUpdate()
        } else {
            val info = mutableUpdateState.value.info ?: return
            mutableUpdateState.value = SplashUpdateState.Error(
                info = info,
                message = "برای نصب نسخه جدید، اجازه نصب از این برنامه لازم است.",
                retryAction = UpdateRetryAction.PERMISSION,
            )
        }
    }

    fun onSecuritySettingsResult() {
        val info = mutableUpdateState.value.info ?: return
        mutableUpdateState.value = if (updateInstaller.canInstallPackages()) {
            SplashUpdateState.ReadyToInstall(info)
        } else {
            SplashUpdateState.InstallPermissionRequired(info)
        }
    }

    fun retry() {
        when (val state = mutableUpdateState.value) {
            is SplashUpdateState.Error -> when (state.retryAction) {
                UpdateRetryAction.CHECK -> checkForUpdate()
                UpdateRetryAction.DOWNLOAD -> downloadUpdate()
                UpdateRetryAction.INSTALL -> installUpdate()
                UpdateRetryAction.PERMISSION -> {
                    mutableUpdateState.value = SplashUpdateState.InstallPermissionRequired(state.info)
                }
            }
            else -> Unit
        }
    }

    fun continueWithoutUpdate() {
        val info = mutableUpdateState.value.info ?: return
        if (!info.mandatory) {
            updateRepository.cleanupAllDownloads()
            downloadedApk = null
            mutableUpdateState.value = SplashUpdateState.Continue
        }
    }

    private fun collectInstallStatus() {
        viewModelScope.launch {
            AppUpdateInstallStatusBus.events.collect { status ->
                val info = mutableUpdateState.value.info ?: return@collect
                mutableUpdateState.value = when (status) {
                    AppUpdateInstallStatus.AwaitingUserConfirmation -> SplashUpdateState.Installing(info)
                    AppUpdateInstallStatus.Success -> SplashUpdateState.Installing(info)
                    is AppUpdateInstallStatus.Failure -> {
                        if (isUnknownSourceInstallBlock(status.message)) {
                            SplashUpdateState.InstallSecuritySettingsRequired(info)
                        } else {
                            SplashUpdateState.Error(
                                info = info,
                                message = status.message,
                                retryAction = UpdateRetryAction.INSTALL,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun downloadFailureMessage(error: Exception): String {
        if (error is ApkValidationException) {
            return when (error.failure) {
                ApkValidationFailure.EMPTY_FILE -> "فایل بروزرسانی خالی دریافت شد. دوباره تلاش کنید."
                ApkValidationFailure.INVALID_ARCHIVE ->
                    "فایل دریافت‌شده APK معتبر نیست یا کامل دانلود نشده است."
                ApkValidationFailure.PACKAGE_MISMATCH ->
                    "فایل بروزرسانی متعلق به برنامه فرازمان نیست."
                ApkValidationFailure.VERSION_MISMATCH ->
                    "نسخه فایل دانلودشده با اطلاعات نسخه روی سرور مطابقت ندارد."
                ApkValidationFailure.SIGNATURE_UNAVAILABLE ->
                    "اندروید نتوانست امضای امنیتی فایل بروزرسانی را بخواند."
                ApkValidationFailure.SIGNATURE_MISMATCH ->
                    "امضای نسخه جدید با برنامه نصب‌شده یکسان نیست و امکان بروزرسانی مستقیم وجود ندارد."
            }
        }
        return if (error is IOException) {
            "دریافت فایل بروزرسانی ناموفق بود. اتصال اینترنت را بررسی و دوباره تلاش کنید."
        } else {
            "آماده‌سازی نسخه جدید ناموفق بود. دوباره تلاش کنید."
        }
    }

    private companion object {
        const val TAG = "SplashViewModel"
        const val MINIMUM_SPLASH_MS = 1_200L
    }
}

enum class UpdateRetryAction {
    CHECK,
    DOWNLOAD,
    INSTALL,
    PERMISSION,
}

sealed interface SplashUpdateState {
    val info: AppUpdateInfo?
        get() = null

    data object Checking : SplashUpdateState
    data object Continue : SplashUpdateState
    data class Available(override val info: AppUpdateInfo) : SplashUpdateState
    data class Downloading(override val info: AppUpdateInfo, val progress: Int) : SplashUpdateState
    data class ReadyToInstall(override val info: AppUpdateInfo) : SplashUpdateState
    data class InstallPermissionRequired(override val info: AppUpdateInfo) : SplashUpdateState
    data class InstallSecuritySettingsRequired(override val info: AppUpdateInfo) : SplashUpdateState
    data class Installing(override val info: AppUpdateInfo) : SplashUpdateState
    data class Error(
        override val info: AppUpdateInfo,
        val message: String,
        val retryAction: UpdateRetryAction,
    ) : SplashUpdateState
}
