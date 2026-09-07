package Ir.co.tfs.farazaman.notification

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object NotificationPermissionHelper {
    fun requestIfNeeded(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (
            ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) return
        val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        if (preferences.getBoolean(PREF_PERMISSION_REQUESTED, false)) return
        preferences.edit().putBoolean(PREF_PERMISSION_REQUESTED, true).apply()
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_CODE,
        )
    }

    private const val PREF_PERMISSION_REQUESTED = "notification_permission_requested"
    private const val REQUEST_CODE = 9042
}
