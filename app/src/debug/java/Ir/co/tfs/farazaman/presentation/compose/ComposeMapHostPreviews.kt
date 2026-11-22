package Ir.co.tfs.farazaman.presentation.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import Ir.co.tfs.farazaman.map.FakeComposeMapHost
import Ir.co.tfs.farazaman.presentation.design.preview.FzDarkPreview
import Ir.co.tfs.farazaman.presentation.design.preview.FzLightPreview

@FzLightPreview
@FzDarkPreview
@Composable
private fun MapHostPreview() {
    FaraZamanTheme {
        FakeComposeMapHost(Modifier.fillMaxSize())
    }
}

@FzLightPreview
@FzDarkPreview
@Composable
private fun ViolationMapPreview() {
    FaraZamanTheme {
        ViolationMapScreen(
            state = ViolationMapUiState("خیابان آزادی", "معبر اصلی", "حداکثر سرعت ۶۰ km/h"),
            onEvent = {},
            map = { FakeComposeMapHost(Modifier.fillMaxSize()) },
        )
    }
}
