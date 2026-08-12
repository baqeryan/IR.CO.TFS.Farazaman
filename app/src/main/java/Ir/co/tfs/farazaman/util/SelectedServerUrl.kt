package Ir.co.tfs.farazaman.util

import android.content.SharedPreferences
import Ir.co.tfs.farazaman.AppConstants

object SelectedServerUrl {
    @JvmStatic
    fun apiUrl(preferences: SharedPreferences, relativePath: String): String {
        val selectedBaseUrl = preferences.getString("BASE_URL", AppConstants.BASE_URL)
            ?.takeIf { it.isNotBlank() }
            ?: AppConstants.BASE_URL
        return "${selectedBaseUrl.trimEnd('/')}/${relativePath.trimStart('/')}"
    }
}
