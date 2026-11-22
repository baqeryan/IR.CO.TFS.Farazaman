package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import Ir.co.tfs.farazaman.presentation.design.components.FzBackButton
import Ir.co.tfs.farazaman.presentation.design.components.FzBottomActionBar
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzIconButton
import Ir.co.tfs.farazaman.presentation.design.components.FzMapControls
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens

data class TrackMapUiState(
    val roadName: String = "",
    val layersExpanded: Boolean = false,
    val hasLayers: Boolean = false,
    val showGpsStatus: Boolean = false,
    val showSubmitViolation: Boolean = false,
    val submittingViolation: Boolean = false,
    val recordedPathVisible: Boolean = true,
)

sealed interface TrackMapEvent {
    data object Back : TrackMapEvent
    data object Center : TrackMapEvent
    data object ZoomIn : TrackMapEvent
    data object ZoomOut : TrackMapEvent
    data object ToggleLayers : TrackMapEvent
    data object ToggleRecordedPath : TrackMapEvent
    data object SubmitViolation : TrackMapEvent
}

@Composable
fun TrackMapScreen(
    state: TrackMapUiState,
    onEvent: (TrackMapEvent) -> Unit,
    map: @Composable () -> Unit,
    gpsStatus: @Composable () -> Unit,
    layers: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    Box(modifier.fillMaxSize()) {
        map()
        FzCard(
            Modifier.align(Alignment.TopCenter).fillMaxWidth()
                .padding(start = spacing.md, top = spacing.md, end = spacing.md),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FzBackButton(onClick = { onEvent(TrackMapEvent.Back) })
                if (state.showGpsStatus) {
                    Spacer(Modifier.size(spacing.sm))
                    Box(Modifier.weight(1f)) { gpsStatus() }
                }
            }
        }
        FzMapControls(
            onZoomIn = { onEvent(TrackMapEvent.ZoomIn) },
            onZoomOut = { onEvent(TrackMapEvent.ZoomOut) },
            onCenter = { onEvent(TrackMapEvent.Center) },
            onLayers = if (state.hasLayers) {{ onEvent(TrackMapEvent.ToggleLayers) }} else null,
            layersExpanded = state.layersExpanded && state.hasLayers,
            layerContent = layers,
            modifier = Modifier.align(Alignment.CenterEnd).padding(spacing.md),
        )
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(spacing.md),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(spacing.sm),
        ) {
            Box(Modifier.fillMaxWidth()) {
                FzIconButton(
                    contentDescription = if (state.recordedPathVisible) "پنهان کردن مسیر GPX" else "نمایش مسیر GPX",
                    onClick = { onEvent(TrackMapEvent.ToggleRecordedPath) },
                    modifier = Modifier.align(Alignment.CenterStart),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_fz_route),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(FaraZamanThemeTokens.sizes.iconMedium),
                    )
                }
            }
            if (state.roadName.isNotBlank()) FzCard(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(spacing.md), horizontalAlignment = Alignment.End) {
                    Text("نام معبر", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Start)
                    Text(state.roadName, Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Start)
                }
            }
            if (state.showSubmitViolation) FzBottomActionBar(
                primaryText = "ثبت نقص",
                onPrimaryClick = { onEvent(TrackMapEvent.SubmitViolation) },
                loading = state.submittingViolation,
                containerColor = Color.Transparent,
            )
        }
    }
}
