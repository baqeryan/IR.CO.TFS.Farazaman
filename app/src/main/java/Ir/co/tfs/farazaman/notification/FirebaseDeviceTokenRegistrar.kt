package Ir.co.tfs.farazaman.notification

import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import Ir.co.tfs.farazaman.BuildConfig
import Ir.co.tfs.farazaman.util.SelectedServerUrl
import Ir.co.tfs.farazaman.util.TokenManager
import Ir.co.tfs.farazaman.util.UserManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDeviceTokenRegistrar @Inject constructor(
    private val preferences: SharedPreferences,
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
    private val okHttpClient: OkHttpClient,
) {
    fun syncCurrentToken() {
        if (!tokenManager.isLoggedIn()) return
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Unable to obtain the Firebase messaging token", task.exception)
                return@addOnCompleteListener
            }
            task.result?.takeIf(String::isNotBlank)?.let(::register)
        }
    }

    fun register(token: String) {
        if (token.isBlank() || !tokenManager.isLoggedIn()) return

        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            registerPayload(
                token = token,
                installationId = task.takeIf { it.isSuccessful }?.result,
            )
        }
    }

    private fun registerPayload(token: String, installationId: String?) {
        if (!tokenManager.isLoggedIn()) return

        val endpoint = SelectedServerUrl.apiUrl(
            preferences,
            "api/AssetPersonNotify/FirebaseDeviceToken",
        )
        val identity = userManager.getUserName().orEmpty()
        val firebaseOptions = FirebaseApp.getInstance().options
        val json = JSONObject().apply {
            put("firebaseDeviceToken", token)
            installationId?.takeIf(String::isNotBlank)?.let {
                put("firebaseInstallationId", it)
            }
            put("firebaseApplicationId", firebaseOptions.applicationId)
            firebaseOptions.projectId?.takeIf(String::isNotBlank)?.let {
                put("firebaseProjectId", it)
            }
            firebaseOptions.gcmSenderId?.takeIf(String::isNotBlank)?.let {
                put("firebaseSenderId", it)
            }
            put("packageName", BuildConfig.APPLICATION_ID)
            put("platform", "Android")
        }.toString()
        val registrationKey = sha256("$endpoint|$identity|$json")
        if (preferences.getString(PREF_REGISTERED_TOKEN_KEY, null) == registrationKey) return

        val request = runCatching {
            Request.Builder()
                .url(endpoint)
                .post(json.toRequestBody(JSON_MEDIA_TYPE))
                .build()
        }.getOrElse {
            Log.w(TAG, "Firebase token endpoint URL is invalid")
            return
        }

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.w(TAG, "Firebase device token registration failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        preferences.edit().putString(PREF_REGISTERED_TOKEN_KEY, registrationKey).apply()
                    } else {
                        Log.w(TAG, "Firebase device token registration returned HTTP ${it.code}")
                    }
                }
            }
        })
    }

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    private companion object {
        const val TAG = "FirebaseTokenRegistrar"
        const val PREF_REGISTERED_TOKEN_KEY = "firebase_registered_token_key"
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
