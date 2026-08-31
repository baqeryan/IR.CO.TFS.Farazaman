package Ir.co.tfs.farazaman.security

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import Ir.co.tfs.farazaman.BuildConfig
import Ir.co.tfs.farazaman.activity.DeveloperOptionsWarningActivity

/**
 * Enforces the Developer Options restriction for minified production builds from one place.
 */
object DeveloperOptionsGuard {

    fun enforce(activity: Activity): Boolean {
        if (!shouldBlockCurrentBuild(activity)) {
            return false
        }

        if (activity !is DeveloperOptionsWarningActivity) {
            activity.startActivity(
                Intent(activity, DeveloperOptionsWarningActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
            )
        }
        return true
    }

    fun shouldBlockCurrentBuild(context: Context): Boolean {
        return shouldBlock(
            isDebugBuild = BuildConfig.DEBUG,
            developerOptionsEnabled = isDeveloperOptionsEnabled(context),
        )
    }

    internal fun shouldBlock(
        isDebugBuild: Boolean,
        developerOptionsEnabled: Boolean,
    ): Boolean = !isDebugBuild && developerOptionsEnabled

    private fun isDeveloperOptionsEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0,
            ) != 0
        } catch (_: Exception) {
            false
        }
    }
}
