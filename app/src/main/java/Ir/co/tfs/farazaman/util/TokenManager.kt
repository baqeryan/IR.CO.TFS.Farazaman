package Ir.co.tfs.farazaman.util

import android.content.SharedPreferences
import Ir.co.tfs.farazaman.auth.TokenExpiryPolicy
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val secureTokenStorage: SecureTokenStorage,
) {

    private val refreshMutex = Mutex()

    fun getAccessToken(): String? = secureTokenStorage.read(ACCESS_TOKEN_KEY)

    fun getRefreshToken(): String? = secureTokenStorage.read(REFRESH_TOKEN_KEY)

    fun getIdToken(): String? = secureTokenStorage.read(ID_TOKEN_KEY)

    fun saveIdToken(idToken: String?) {
        secureTokenStorage.write(ID_TOKEN_KEY, idToken)
    }

    fun getTokenType(): String = secureTokenStorage.read(TOKEN_TYPE_KEY) ?: "Bearer"

    fun saveTokens(
        accessToken: String,
        refreshToken: String,
        tokenType: String = "Bearer",
        expiresIn: Int,
    ) {
        val expiresAt = System.currentTimeMillis() + expiresIn.coerceAtLeast(0) * 1_000L
        secureTokenStorage.write(
            mapOf(
                ACCESS_TOKEN_KEY to accessToken,
                REFRESH_TOKEN_KEY to refreshToken,
                TOKEN_TYPE_KEY to tokenType,
            ),
        )
        sharedPreferences.edit().putLong(TOKEN_EXPIRES_AT_KEY, expiresAt).apply()
    }

    fun isTokenExpired(): Boolean {
        return TokenExpiryPolicy.isExpired(getTokenExpiresAt(), System.currentTimeMillis())
    }

    fun hasRefreshToken(): Boolean = !getRefreshToken().isNullOrEmpty()

    fun hasAccessToken(): Boolean = !getAccessToken().isNullOrEmpty()

    fun getTokenExpiresAt(): Long = sharedPreferences.getLong(TOKEN_EXPIRES_AT_KEY, 0L)

    /**
     * A refresh token is optional in valid OAuth/OIDC responses. Requiring it here caused a
     * freshly installed app to reject a successfully stored access token, while an upgraded
     * debug installation appeared to work because it could still have an older refresh token.
     */
    fun isLoggedIn(): Boolean = hasAccessToken()

    fun clearTokens() {
        secureTokenStorage.clear(ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY, ID_TOKEN_KEY, TOKEN_TYPE_KEY)
        sharedPreferences.edit().remove(TOKEN_EXPIRES_AT_KEY).apply()
    }

    fun getAuthorizationHeader(): String? = getAccessToken()?.let { "Bearer $it" }

    suspend fun <T> withRefreshLock(operation: suspend () -> T): T {
        return refreshMutex.withLock { operation() }
    }

    fun getTimeUntilExpiry(): Long {
        val expiresAt = getTokenExpiresAt()
        return if (expiresAt > 0L) expiresAt - System.currentTimeMillis() else 0L
    }

    private companion object {
        const val ACCESS_TOKEN_KEY = "ACCESS_TOKEN"
        const val REFRESH_TOKEN_KEY = "REFRESH_TOKEN"
        const val ID_TOKEN_KEY = "ID_TOKEN"
        const val TOKEN_TYPE_KEY = "TOKEN_TYPE"
        const val TOKEN_EXPIRES_AT_KEY = "TOKEN_EXPIRES_AT"
    }
}
