package Ir.co.tfs.farazaman.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppUpdateInstaller @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun canInstallPackages(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()
    }

    fun createInstallPermissionIntent(): Intent {
        return Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}"),
        )
    }

    fun createSecuritySettingsIntent(): Intent = Intent(Settings.ACTION_SECURITY_SETTINGS)

    suspend fun install(apkFile: File) = withContext(Dispatchers.IO) {
        check(canInstallPackages()) { "Install permission has not been granted" }
        check(apkFile.isFile && apkFile.length() > 0) { "Downloaded APK is missing" }

        val packageInstaller = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(context.packageName)
            setSize(apkFile.length())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_REQUIRED)
            }
        }
        val sessionId = packageInstaller.createSession(params)

        try {
            packageInstaller.openSession(sessionId).use { session ->
                session.openWrite("base.apk", 0, apkFile.length()).use { output ->
                    FileInputStream(apkFile).use { input -> input.copyTo(output) }
                    session.fsync(output)
                }

                val statusIntent = Intent(context, AppUpdateInstallReceiver::class.java).apply {
                    action = AppUpdateInstallReceiver.ACTION_INSTALL_STATUS
                }
                val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_MUTABLE
                    } else {
                        0
                    }
                val statusReceiver = PendingIntent.getBroadcast(
                    context,
                    sessionId,
                    statusIntent,
                    flags,
                )
                session.commit(statusReceiver.intentSender)
            }
        } catch (e: Exception) {
            runCatching { packageInstaller.abandonSession(sessionId) }
            throw e
        }
    }
}
