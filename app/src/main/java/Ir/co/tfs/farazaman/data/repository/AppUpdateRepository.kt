package Ir.co.tfs.farazaman.data.repository

import Ir.co.tfs.farazaman.data.model.AppUpdateInfo
import Ir.co.tfs.farazaman.data.update.ApkFileValidator
import Ir.co.tfs.farazaman.data.update.AppUpdateService
import Ir.co.tfs.farazaman.domain.update.AppUpdateDecision
import Ir.co.tfs.farazaman.domain.update.AppUpdatePolicy
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed interface UpdateCheckResult {
    data object UpToDate : UpdateCheckResult
    data class Available(val info: AppUpdateInfo) : UpdateCheckResult
    data class Failed(val cause: Throwable) : UpdateCheckResult
}

@Singleton
class AppUpdateRepository @Inject constructor(
    private val service: AppUpdateService,
    private val apkValidator: ApkFileValidator,
    @ApplicationContext private val context: Context,
) {
    @Volatile
    private var mandatoryUpdateForSession: AppUpdateInfo? = null

    suspend fun checkForUpdate(installedVersionCode: Long): UpdateCheckResult {
        mandatoryUpdateForSession?.let { cached ->
            if (cached.versionCode > installedVersionCode) {
                return UpdateCheckResult.Available(cached)
            }
        }
        return try {
            val info = service.fetchLatestVersion()
            when (AppUpdatePolicy.decide(installedVersionCode, info)) {
                AppUpdateDecision.UpToDate -> UpdateCheckResult.UpToDate
                is AppUpdateDecision.Optional -> UpdateCheckResult.Available(info)
                is AppUpdateDecision.Mandatory -> {
                    mandatoryUpdateForSession = info
                    UpdateCheckResult.Available(info)
                }
            }
        } catch (e: Exception) {
            UpdateCheckResult.Failed(e)
        }
    }

    suspend fun download(
        info: AppUpdateInfo,
        onProgress: (Int) -> Unit,
    ): File {
        val updateDirectory = File(context.cacheDir, UPDATE_DIRECTORY).apply { mkdirs() }
        cleanupDownloads(updateDirectory)
        val partial = File(updateDirectory, "farazaman-${info.versionCode}.apk.part")
        val completed = File(updateDirectory, "farazaman-${info.versionCode}.apk")

        return try {
            service.downloadApk(info.downloadUrl, partial, onProgress)
            apkValidator.validate(partial, info)
            check(partial.renameTo(completed)) { "Unable to finalize downloaded APK" }
            completed
        } catch (e: Exception) {
            partial.delete()
            completed.delete()
            throw e
        }
    }

    fun cleanupAllDownloads() {
        cleanupDownloads(File(context.cacheDir, UPDATE_DIRECTORY))
    }

    private fun cleanupDownloads(directory: File) {
        directory.listFiles()?.forEach { file ->
            if (file.isFile && (file.name.endsWith(".apk") || file.name.endsWith(".part"))) {
                file.delete()
            }
        }
    }

    private companion object {
        const val UPDATE_DIRECTORY = "app_updates"
    }
}
