package Ir.co.tfs.farazaman.util

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Requests the optional "allow all the time" access separately from foreground location.
 * Tracking starts as a location foreground service first, so declining this prompt does not
 * interrupt the current visit.
 */
object BackgroundLocationPermissionHelper {
    private const val BACKGROUND_LOCATION_REQUEST_CODE = 7301

    fun promptIfNeeded(activity: AppCompatActivity): Boolean {
        if (hasPermission(activity)) return false

        AlertDialog.Builder(activity)
            .setTitle("دسترسی موقعیت مکانی در پس‌زمینه")
            .setMessage(
                "برای ادامه مطمئن ثبت مسیر وقتی برنامه روی صفحه نیست، دسترسی موقعیت مکانی را روی «همیشه مجاز» قرار دهید.",
            )
            .setNegativeButton("فعلاً نه", null)
            .setPositiveButton(if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) "ادامه" else "باز کردن تنظیمات") { _, _ ->
                requestPermission(activity)
            }
            .show()
        return true
    }

    fun hasPermission(activity: AppCompatActivity): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    fun requestPermission(activity: AppCompatActivity) {
        if (hasPermission(activity)) return
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                BACKGROUND_LOCATION_REQUEST_CODE,
            )
        } else {
            openLocationPermissionSettings(activity)
        }
    }

    fun isBatteryOptimizationIgnored(activity: AppCompatActivity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        val powerManager = activity.getSystemService(PowerManager::class.java)
        return powerManager?.isIgnoringBatteryOptimizations(activity.packageName) == true
    }

    fun promptBatteryOptimizationIfNeeded(activity: AppCompatActivity): Boolean {
        if (isBatteryOptimizationIgnored(activity)) return false
        AlertDialog.Builder(activity)
            .setTitle("اجرای مطمئن در پس‌زمینه")
            .setMessage(
                "برای جلوگیری از توقف ثبت مسیر هنگام خاموش شدن صفحه یا قرار گرفتن برنامه در پس‌زمینه، در تنظیمات باتری فرازمان را روی «بدون محدودیت» قرار دهید.",
            )
            .setNegativeButton("فعلاً نه", null)
            .setPositiveButton("تنظیمات باتری") { _, _ -> openBatteryOptimizationSettings(activity) }
            .show()
        return true
    }

    fun openBatteryOptimizationSettings(activity: AppCompatActivity) {
        val directRequest = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.fromParts("package", activity.packageName, null),
        )
        try {
            activity.startActivity(directRequest)
        } catch (_: ActivityNotFoundException) {
            openBatteryOptimizationList(activity)
        } catch (_: SecurityException) {
            openBatteryOptimizationList(activity)
        }
    }

    private fun openBatteryOptimizationList(activity: AppCompatActivity) {
        try {
            activity.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        } catch (_: ActivityNotFoundException) {
            openAppDetails(activity)
        } catch (_: SecurityException) {
            openAppDetails(activity)
        }
    }

    /** Opens the public app-details screen where Android 11+ exposes "Allow all the time". */
    private fun openLocationPermissionSettings(activity: AppCompatActivity) {
        openAppDetails(activity)
    }

    private fun openAppDetails(activity: AppCompatActivity) {
        val appDetailsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", activity.packageName, null),
        )
        try {
            activity.startActivity(appDetailsIntent)
        } catch (_: ActivityNotFoundException) {
            activity.startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS))
        } catch (_: SecurityException) {
            activity.startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS))
        }
    }
}
