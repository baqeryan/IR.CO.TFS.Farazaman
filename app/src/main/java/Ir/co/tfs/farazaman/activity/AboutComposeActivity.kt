package Ir.co.tfs.farazaman.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.compose.AboutScreen
import Ir.co.tfs.farazaman.presentation.compose.AboutUiState
import Ir.co.tfs.farazaman.presentation.compose.AboutEvent
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme

class AboutComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val version = runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull() ?: "-"
        setContent {
            FaraZamanTheme {
                val state = AboutUiState(version, getString(R.string.about_text))
                AboutScreen(state = state, onEvent = { event ->
                    when (event) {
                        AboutEvent.Back -> finish()
                        AboutEvent.OpenWebsite -> {
                            runCatching {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(state.websiteUrl)))
                            }
                        }
                        AboutEvent.DebugInfo, AboutEvent.ExportDatabase -> Unit
                    }
                })
            }
        }
    }
}
