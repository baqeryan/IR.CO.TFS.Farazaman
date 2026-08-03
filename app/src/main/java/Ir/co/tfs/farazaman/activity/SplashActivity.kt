package Ir.co.tfs.farazaman.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.LaunchedEffect
import Ir.co.tfs.farazaman.presentation.compose.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.compose.SplashScreen
import Ir.co.tfs.farazaman.presentation.compose.SplashUiState
import dagger.hilt.android.AndroidEntryPoint
import Ir.co.tfs.farazaman.presentation.login.LoginActivity
import Ir.co.tfs.farazaman.util.TokenManager
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            FaraZamanTheme {
                LaunchedEffect(Unit) {
                    delay(1_200)
                    if (!isFinishing) {
                        navigateToNextScreen()
                    }
                }
                SplashScreen(
                    state = SplashUiState(slogan = "تحولی نوین در شهر"),
                )
            }
        }
    }

    private fun navigateToNextScreen() {
        // First, check if Developer Options is enabled
//        if (isDeveloperOptionsEnabled()) {
//            // Developer Options is enabled, show warning screen
//            val intent = Intent(this, DeveloperOptionsWarningActivity::class.java)
//            startActivity(intent)
//            finish()
//            return
//        }

        // Use TokenManager to check authentication status
        val intent: Intent

        if (tokenManager.isLoggedIn()) {
            // User is logged in, check if token is expired
            if (tokenManager.isTokenExpired() && !tokenManager.hasRefreshToken()) {
                // Token expired and no refresh token, redirect to login
                intent = Intent(this, LoginActivity::class.java)
            } else {
                // User has valid tokens (or can refresh), go to RoleSelectionActivity
                intent = Intent(this, RoleSelectionActivity::class.java)
            }
        } else {
            // User is not logged in, go to LoginActivity
            intent = Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish() // Clear this activity from memory
    }

    /**
     * Check if Developer Options is enabled on the device
     */
    private fun isDeveloperOptionsEnabled(): Boolean {
        return try {
            Settings.Secure.getInt(
                contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) != 0
        } catch (e: Exception) {
            // If we can't determine, assume it's disabled
            false
        }
    }
}
