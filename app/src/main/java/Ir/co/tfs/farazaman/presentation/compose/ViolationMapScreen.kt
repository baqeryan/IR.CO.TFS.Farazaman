package Ir.co.tfs.farazaman.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import Ir.co.tfs.farazaman.presentation.design.components.FzBackButton
import Ir.co.tfs.farazaman.presentation.design.components.FzBottomActionBar
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzMapControls
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens

data class ViolationMapUiState(
    val roadName: String = "نام خیابان: -",
    val roadType: String = "",
    val speedLimit: String = "",
)

sealed interface ViolationMapEvent {
    data object Back : ViolationMapEvent
    data object ZoomIn : ViolationMapEvent
    data object ZoomOut : ViolationMapEvent
    data object Center : ViolationMapEvent
    data object Submit : ViolationMapEvent
}

@Composable
fun ViolationMapScreen(
    state: ViolationMapUiState,
    onEvent: (ViolationMapEvent) -> Unit,
    map: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    Box(modifier.fillMaxSize()) {
        map()
        FzBackButton(
            onClick = { onEvent(ViolationMapEvent.Back) },
            modifier = Modifier.align(Alignment.TopStart).padding(spacing.md),
        )
        FzMapControls(
            onZoomIn = { onEvent(ViolationMapEvent.ZoomIn) },
            onZoomOut = { onEvent(ViolationMapEvent.ZoomOut) },
            onCenter = { onEvent(ViolationMapEvent.Center) },
            modifier = Modifier.align(Alignment.CenterEnd).padding(spacing.md),
        )
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            FzCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    Text(state.roadName, style = MaterialTheme.typography.titleMedium)
                    if (state.roadType.isNotBlank()) Text(state.roadType)
                    if (state.speedLimit.isNotBlank()) Text(state.speedLimit)
                }
            }
            FzBottomActionBar(
                primaryText = "ثبت نقص",
                onPrimaryClick = { onEvent(ViolationMapEvent.Submit) },
                containerColor = Color.Transparent,
            )
        }
    }
}
