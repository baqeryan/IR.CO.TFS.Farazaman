package Ir.co.tfs.farazaman.util

import android.util.Log
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import Ir.co.tfs.farazaman.domain.repository.AuthRepository
import Ir.co.tfs.farazaman.util.AuthStateManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val authStateManager: AuthStateManager,
    private val authRepository: AuthRepository,
) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
        private const val AUTHORIZATION_HEADER = "Authorization"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()
        
        Log.d(TAG, "=== INTERCEPTING REQUEST ===")
        Log.d(TAG, "URL: $url")
        Log.d(TAG, "Method: ${originalRequest.method}")
        Log.d(TAG, "Token expired: ${tokenManager.isTokenExpired()}")
        Log.d(TAG, "Has refresh token: ${tokenManager.hasRefreshToken()}")
        
        // Add detailed expiry analysis
        val expiresAt = tokenManager.getTokenExpiresAt()
        val currentTime = System.currentTimeMillis()
        val timeRemaining = expiresAt - currentTime
        Log.d(TAG, "Token expires at: $expiresAt")
        Log.d(TAG, "Current time: $currentTime") 
        Log.d(TAG, "Time remaining: ${timeRemaining}ms (${timeRemaining/1000}s)")
        Log.d(TAG, "Time since login: ${currentTime - 1756488569871}ms")
        
        // Skip token logic for login/refresh endpoints
        if (isAuthEndpoint(originalRequest)) {
            Log.d(TAG, "Skipping auth for endpoint: $url")
            return chain.proceed(originalRequest)
        }

        // Check if token is expired before making the request
        if (tokenManager.isTokenExpired() && tokenManager.hasRefreshToken()) {
            Log.d(TAG, "Token is expired, refreshing before request")
            val refreshResult = refreshTokenBlocking(expectedToken = null, onlyIfExpired = true)
            if (!refreshResult) {
                Log.e(TAG, "Preemptive token refresh failed")
                tokenManager.clearTokens()
                authStateManager.notifyAuthStateChanged(false, AuthStateManager.REASON_REFRESH_FAILED)
            }
        }

        // Add authorization header if available
        val requestWithAuth = addAuthHeader(originalRequest)
        Log.d(TAG, "Making request with auth header: ${requestWithAuth.header(AUTHORIZATION_HEADER) != null}")
        
        var response = chain.proceed(requestWithAuth)
        Log.d(TAG, "Received response code: ${response.code}")

        // If we get 401 Unauthorized, try to refresh the token
        if (response.code == 401 && tokenManager.hasRefreshToken()) {
            Log.w(TAG, "=== 401 UNAUTHORIZED DETECTED ===")
            Log.w(TAG, "Response message: ${response.message}")
            Log.w(TAG, "Request URL: $url")
            Log.w(TAG, "Request method: ${originalRequest.method}")
            Log.w(TAG, "Auth header used: ${requestWithAuth.header(AUTHORIZATION_HEADER) != null}")
            Log.w(TAG, "Attempting token refresh...")
            
            val refreshResult = refreshTokenBlocking(
                expectedToken = getTokenFromRequest(requestWithAuth),
                onlyIfExpired = false,
            )

            if (refreshResult) {
                Log.d(TAG, "Token refresh successful, retrying original request")
                response.close()
                val retryResponse = chain.proceed(addAuthHeader(originalRequest))
                if (retryResponse.code == 401) {
                    Log.e(TAG, "Still getting 401 after token refresh")
                    tokenManager.clearTokens()
                    authStateManager.notifyAuthStateChanged(false, AuthStateManager.REASON_REFRESH_FAILED)
                }
                return retryResponse
            } else {
                Log.e(TAG, "Token refresh failed, clearing tokens")
                tokenManager.clearTokens()
                authStateManager.notifyAuthStateChanged(false, AuthStateManager.REASON_REFRESH_FAILED)
            }
        }

        return response
    }

    private fun isAuthEndpoint(request: Request): Boolean {
        val url = request.url.toString()
        return url.contains("identity.tfs.co.ir", ignoreCase = true) ||
            (url.contains("/token") &&
                request.body?.contentType()?.toString()?.contains("application/x-www-form-urlencoded") == true)
    }

    private fun addAuthHeader(request: Request): Request {
        val authHeader = tokenManager.getAuthorizationHeader()
        return if (authHeader != null) {
            request.newBuilder()
                .header(AUTHORIZATION_HEADER, authHeader)
                .build()
        } else {
            request
        }
    }

    private fun getTokenFromRequest(request: Request): String? {
        val authHeader = request.header(AUTHORIZATION_HEADER)
        return authHeader?.removePrefix("Bearer ")?.trim()
    }

    private fun refreshTokenBlocking(expectedToken: String?, onlyIfExpired: Boolean): Boolean {
        return try {
            runBlocking {
                withTimeout(NetworkTimeouts.TOKEN_REFRESH_TIMEOUT_MS) {
                    tokenManager.withRefreshLock {
                        val currentToken = tokenManager.getAccessToken()
                        when {
                            expectedToken != null && currentToken != null && currentToken != expectedToken -> true
                            onlyIfExpired && !tokenManager.isTokenExpired() -> true
                            else -> performTokenRefresh().let { true }
                        }
                    }
                }
            }
        } catch (error: Exception) {
            Log.e(TAG, "Token refresh failed: ${error.javaClass.simpleName}")
            false
        }
    }

    private suspend fun performTokenRefresh() {
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Log.e(TAG, "No refresh token available")
            throw IllegalStateException("No refresh token available")
        }

        Log.d(TAG, "Performing token refresh...")
        authRepository.refreshToken()
        Log.d(TAG, "Token refresh successful")
    }
}
