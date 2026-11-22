package Ir.co.tfs.farazaman.presentation.compose

import androidx.compose.runtime.Composable
import Ir.co.tfs.farazaman.presentation.design.preview.FzDarkPreview
import Ir.co.tfs.farazaman.presentation.design.preview.FzLightPreview

@FzLightPreview
@Composable
private fun EmptyDriverPreview() = FaraZamanTheme {
    DriverScreen(DriverUiState(), {})
}

@FzDarkPreview
@Composable
private fun ActiveDriverPreview() = FaraZamanTheme {
    DriverScreen(
        DriverUiState(true, true, "منطقه مرکزی | خودرو ۱۲", 4, 182),
        {},
    )
}
