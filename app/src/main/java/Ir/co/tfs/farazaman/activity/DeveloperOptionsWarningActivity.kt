package Ir.co.tfs.farazaman.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import Ir.co.tfs.farazaman.presentation.compose.DeveloperOptionsWarningEvent
import Ir.co.tfs.farazaman.presentation.compose.DeveloperOptionsWarningScreen
import Ir.co.tfs.farazaman.presentation.compose.DeveloperOptionsWarningUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.security.DeveloperOptionsGuard

/**
 * Activity that displays a warning when Developer Options is enabled
 * and prevents the user from accessing the app until it's disabled.
 */
class DeveloperOptionsWarningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val message = mutableStateOf(defaultWarningMessage())
        setContent {
            FaraZamanTheme {
                DeveloperOptionsWarningScreen(
                    state = DeveloperOptionsWarningUiState(message.value),
                    onEvent = { event ->
                    when (event) {
                        DeveloperOptionsWarningEvent.OpenSettings -> openDeveloperSettings()
                        DeveloperOptionsWarningEvent.Recheck -> {
                            if (!DeveloperOptionsGuard.shouldBlockCurrentBuild(this)) finish()
                            else message.value = "گزینه‌های توسعه‌دهنده هنوز فعال است. لطفاً آن را خاموش کنید و مجدداً امتحان کنید."
                        }
                    }
                    },
                )
            }
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = Unit
            },
        )

    }

    private fun defaultWarningMessage() =
        "برای استفاده از این برنامه، لطفاً گزینه‌های توسعه‌دهنده (Developer Options) را خاموش کنید.\n\nفعال بودن این گزینه می‌تواند امنیت اطلاعات شما را به خطر بیندازد."

    private fun openDeveloperSettings() {
        try {
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        if (!DeveloperOptionsGuard.shouldBlockCurrentBuild(this)) {
            finish()
        }
    }
}

