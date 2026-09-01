package Ir.co.tfs.farazaman.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import Ir.co.tfs.farazaman.presentation.compose.AppUpdateUi
import Ir.co.tfs.farazaman.presentation.compose.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.compose.SplashScreen
import Ir.co.tfs.farazaman.presentation.compose.SplashUiState
import dagger.hilt.android.AndroidEntryPoint
import Ir.co.tfs.farazaman.presentation.login.LoginActivity
import Ir.co.tfs.farazaman.util.TokenManager
import Ir.co.tfs.farazaman.presentation.viewmodel.SplashUpdateState
import Ir.co.tfs.farazaman.presentation.viewmodel.SplashViewModel
import Ir.co.tfs.farazaman.update.AppUpdateInstaller
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var updateInstaller: AppUpdateInstaller

    private val splashViewModel: SplashViewModel by viewModels()
    private var navigationStarted = false
    private var installPermissionRequestInFlight = false

    private val installPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        installPermissionRequestInFlight = false
        splashViewModel.onInstallPermissionResult()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            FaraZamanTheme {
                val updateState by splashViewModel.updateState.collectAsState()
                LaunchedEffect(updateState) {
                    if (updateState == SplashUpdateState.Continue && !isFinishing) {
                        navigateToNextScreen()
                    }
                }
                LaunchedEffect(updateState) {
                    if (updateState is SplashUpdateState.InstallPermissionRequired &&
                        !installPermissionRequestInFlight && !isFinishing
                    ) {
                        openInstallPermissionSettings()
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    SplashScreen(
                        state = SplashUiState(slogan = "تحولی نوین در شهر"),
                    )
                    AppUpdateUi(
                        state = updateState,
                        onDownload = splashViewModel::downloadUpdate,
                        onInstall = splashViewModel::installUpdate,
                        onOpenInstallSettings = {
                            openInstallPermissionSettings()
                        },
                        onRetry = splashViewModel::retry,
                        onLater = splashViewModel::continueWithoutUpdate,
                    )
                }
            }
        }
    }

    private fun openInstallPermissionSettings() {
        if (installPermissionRequestInFlight) return
        installPermissionRequestInFlight = true
        installPermissionLauncher.launch(updateInstaller.createInstallPermissionIntent())
    }

    private fun navigateToNextScreen() {
        if (navigationStarted) return
        navigationStarted = true
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
}
