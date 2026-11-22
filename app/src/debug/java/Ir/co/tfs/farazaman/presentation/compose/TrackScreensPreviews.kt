package Ir.co.tfs.farazaman.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import Ir.co.tfs.farazaman.presentation.compose.track.*
import Ir.co.tfs.farazaman.presentation.design.preview.FzLightPreview

@FzLightPreview
@Composable
private fun TrackListPreview() = FaraZamanTheme {
    TrackListScreen(TrackListUiState(tracks = listOf(TrackListItemUiState(1, "مسیر امروز", "۱۴۰۵/۰۶/۱۹", 120))), {})
}

@FzLightPreview
@Composable
private fun TrackDetailPreview() = FaraZamanTheme {
    TrackDetailScreen(TrackDetailUiState(1, "مسیر امروز", "۱۴۰۵/۰۶/۱۹", 120, 3), {})
}

@FzLightPreview
@Composable
private fun WaypointListPreview() = FaraZamanTheme {
    WaypointListScreen(
        listOf(WaypointUiState(1, "نقطه نمونه", "توضیحات نقطه", 35.7, 51.4)),
        {},
    )
}

@FzLightPreview
@Composable
private fun TrackUploadPreview() = FaraZamanTheme {
    TrackUploadScreen(
        TrackUploadUiState(
            trackId = 1,
            name = "مسیر امروز",
            description = "بازدید روزانه محدوده",
            tags = "بازدید، روزانه",
            visibility = "Private",
            visibilityOptions = listOf("Private", "Public", "Trackable", "Identifiable"),
        ),
        {},
    )
}

@FzLightPreview
@Composable
private fun TrackLoggerPreview() = FaraZamanTheme {
    TrackLoggerScreen(
        state = TrackLoggerUiState(trackId = 42, controlsEnabled = true),
        onEvent = {},
        gpsStatus = { Text("GPS: دقت ۵ متر") },
        controls = { Text("دکمه‌های ثبت نقطه، تصویر، متن و صدا") },
    )
}
