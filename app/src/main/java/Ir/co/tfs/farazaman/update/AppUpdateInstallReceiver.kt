package Ir.co.tfs.farazaman.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build

class AppUpdateInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(
            PackageInstaller.EXTRA_STATUS,
            PackageInstaller.STATUS_FAILURE,
        )
        val statusMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmationIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_INTENT)
                }
                if (confirmationIntent == null) {
                    AppUpdateInstallStatusBus.publish(
                        AppUpdateInstallStatus.Failure("پنجره تأیید نصب در دسترس نیست"),
                    )
                    return
                }
                AppUpdateInstallStatusBus.publish(AppUpdateInstallStatus.AwaitingUserConfirmation)
                try {
                    confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(confirmationIntent)
                } catch (_: Exception) {
                    AppUpdateInstallStatusBus.publish(
                        AppUpdateInstallStatus.Failure("باز کردن تأیید نصب ناموفق بود"),
                    )
                }
            }

            PackageInstaller.STATUS_SUCCESS -> {
                AppUpdateInstallStatusBus.publish(AppUpdateInstallStatus.Success)
            }

            else -> {
                AppUpdateInstallStatusBus.publish(
                    AppUpdateInstallStatus.Failure(
                        statusMessage?.takeIf { it.isNotBlank() }
                            ?: "نصب نسخه جدید ناموفق بود",
                    ),
                )
            }
        }
    }

    companion object {
        const val ACTION_INSTALL_STATUS = "Ir.co.tfs.farazaman.action.APP_UPDATE_INSTALL_STATUS"
    }
}
