package Ir.co.tfs.farazaman.util

import android.content.SharedPreferences
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureTokenStorage @Inject constructor(
    private val preferences: SharedPreferences,
    private val cipher: AndroidKeyStoreCipher,
) {

    fun read(key: String): String? {
        val secureKey = secureKey(key)
        preferences.getString(secureKey, null)?.let { encrypted ->
            return try {
                cipher.decrypt(encrypted)
            } catch (error: Exception) {
                Log.e(TAG, "Unable to decrypt stored credential; clearing it", error)
                preferences.edit().remove(secureKey).apply()
                null
            }
        }

        val legacyValue = preferences.getString(key, null) ?: return null
        write(key, legacyValue)
        preferences.edit().remove(key).apply()
        return legacyValue
    }

    fun write(values: Map<String, String?>) {
        val editor = preferences.edit()
        values.forEach { (key, value) ->
            if (value.isNullOrEmpty()) {
                editor.remove(secureKey(key))
            } else {
                editor.putString(secureKey(key), cipher.encrypt(value))
            }
            editor.remove(key)
        }
        editor.apply()
    }

    fun write(key: String, value: String?) = write(mapOf(key to value))

    fun clear(vararg keys: String) {
        preferences.edit().apply {
            keys.forEach { key ->
                remove(key)
                remove(secureKey(key))
            }
        }.apply()
    }

    private fun secureKey(key: String) = "secure_$key"

    private companion object {
        const val TAG = "SecureTokenStorage"
    }
}
