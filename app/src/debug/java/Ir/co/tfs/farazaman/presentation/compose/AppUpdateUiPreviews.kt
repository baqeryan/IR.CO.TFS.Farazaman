package Ir.co.tfs.farazaman.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import Ir.co.tfs.farazaman.data.model.AppUpdateInfo
import Ir.co.tfs.farazaman.presentation.viewmodel.SplashUpdateState

@Preview(name = "بروزرسانی - نسخه جدید", showBackground = true, locale = "fa", widthDp = 360, heightDp = 800)
@Composable
private fun AppUpdateAvailablePreview() {
    FaraZamanTheme {
        AppUpdateUi(
            state = SplashUpdateState.Available(
                AppUpdateInfo(
                    versionCode = 116,
                    versionName = "2026.09.08",
                    downloadUrl = "https://example.invalid/app.apk",
                    releaseNotes = "بهبود عملکرد و رفع اشکالات",
                    mandatory = false,
                    publishedAt = null,
                ),
            ),
            onEvent = {},
        )
    }
}
