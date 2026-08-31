package Ir.co.tfs.farazaman.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.webkit.CookieManager
import android.webkit.WebStorage
import Ir.co.tfs.farazaman.di.NetworkModule

/** Clears user/session caches without deleting recorded tracks or other persistent files. */
object LogoutCacheCleaner {

    @JvmStatic
    fun clearAndThen(context: Context, onComplete: Runnable) {
        val applicationContext = context.applicationContext

        runCatching { clearSessionPreferences(applicationContext) }
        runCatching { clearWebSession() }
        runCatching { NetworkModule.RetrofitProvider.reset() }

        Thread({
            try {
                clearDirectoryContents(applicationContext.cacheDir)
                applicationContext.externalCacheDirs
                    .filterNotNull()
                    .forEach(::clearDirectoryContents)
            } finally {
                Handler(Looper.getMainLooper()).post(onComplete)
            }
        }, "logout-cache-cleaner").start()
    }

    private fun clearSessionPreferences(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val dynamicKeys = preferences.all.keys.filter { key ->
            key.startsWith("mission_data_") ||
                key.startsWith("section_expanded_") ||
                key.startsWith("secure_")
        }

        preferences.edit().apply {
            (SESSION_KEYS + dynamicKeys).forEach(::remove)
        }.commit()
    }

    private fun clearWebSession() {
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }
        WebStorage.getInstance().deleteAllData()
    }

    private fun clearDirectoryContents(directory: java.io.File) {
        directory.listFiles()?.forEach { child ->
            runCatching { child.deleteRecursively() }
        }
    }

    private val SESSION_KEYS = setOf(
        "ACCESS_TOKEN",
        "REFRESH_TOKEN",
        "ID_TOKEN",
        "TOKEN_TYPE",
        "TOKEN_EXPIRES_AT",
        "BASE_URL",
        "CACHED_PROVINCES_JSON",
        "LOGIN_MODE",
        "OIDC_CLIENT_ID",
        "OIDC_SCOPE",
        "user_name",
        "display_name",
        "default_subsystem",
        "default_role_name",
        "user_roles",
        "supervisor_violation_history",
        "is_driver_mode",
        "current_zone_work_vehicle_id",
        "current_zone_info_summary",
        "current_track_id_for_zone",
    )
}
