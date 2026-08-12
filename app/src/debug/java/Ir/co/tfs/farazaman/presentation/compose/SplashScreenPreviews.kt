package Ir.co.tfs.farazaman.presentation.compose

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection

@Preview(
    name = "Splash - Normal",
    showBackground = true,
    locale = "fa",
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun SplashScreenNormalPreview() {
    SplashPreviewContent(darkTheme = false)
}

@Preview(
    name = "Splash - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "fa",
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun SplashScreenDarkPreview() {
    SplashPreviewContent(darkTheme = true)
}

@Composable
private fun SplashPreviewContent(darkTheme: Boolean) {
    FaraZamanTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            SplashScreen(
                state = SplashUiState(
                    slogan = "تحولی نوین در شهر",
                    animate = false,
                ),
            )
        }
    }
}
