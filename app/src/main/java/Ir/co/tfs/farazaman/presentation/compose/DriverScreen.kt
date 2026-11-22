package Ir.co.tfs.farazaman.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzCardVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzHeaderSize
import Ir.co.tfs.farazaman.presentation.design.components.FzPageTemplate
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens

data class DriverUiState(
    val hasActiveMission: Boolean = false,
    val isTracking: Boolean = false,
    val zoneSummary: String = "",
    val waypointsCount: Int = 0,
    val trackpointsCount: Int = 0,
    val isUploading: Boolean = false,
)

sealed interface DriverEvent {
    data object Back : DriverEvent
    data object StartMission : DriverEvent
    data object StopOrResumeGps : DriverEvent
    data object EndAndUpload : DriverEvent
    data object OpenOptions : DriverEvent
}

@Composable
fun DriverScreen(
    state: DriverUiState,
    onEvent: (DriverEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    FzPageTemplate(
        title = "ماموریت راننده",
        headerSize = FzHeaderSize.Standard,
        modifier = modifier,
        onBack = { onEvent(DriverEvent.Back) },
    ) { padding ->
      Column(
        modifier = Modifier.fillMaxSize().padding(padding),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
      ) {

        if (!state.hasActiveMission) {
            Spacer(Modifier.weight(1f))
            Text(
                "هنوز مأموریتی شروع نشده است",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            FzButton(
                text = "شروع مأموریت",
                onClick = { onEvent(DriverEvent.StartMission) },
                modifier = Modifier.fillMaxWidth(),
            )
            return@Column
        }

        FzCard(Modifier.fillMaxWidth(), variant = FzCardVariant.Elevated) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("ماموریت فعال", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    FzButton("گزینه‌ها", { onEvent(DriverEvent.OpenOptions) }, variant = FzButtonVariant.Ghost)
                }
                if (state.zoneSummary.isNotBlank()) {
                    Text(state.zoneSummary, color = MaterialTheme.colorScheme.primary)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DriverMetric("نقاط مسیر", state.trackpointsCount)
                    DriverMetric("نقاط ثبت‌شده", state.waypointsCount)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    FzButton(
                        text = if (state.isTracking) "توقف" else "ادامه",
                        onClick = { onEvent(DriverEvent.StopOrResumeGps) },
                        modifier = Modifier.weight(1f),
                        variant = if (state.isTracking) FzButtonVariant.Danger else FzButtonVariant.Primary,
                        enabled = !state.isUploading,
                    )
                    FzButton(
                        text = "اتمام و بارگذاری",
                        onClick = { onEvent(DriverEvent.EndAndUpload) },
                        modifier = Modifier.weight(1f),
                        variant = FzButtonVariant.Secondary,
                        loading = state.isUploading,
                    )
                }
            }
        }
      }
    }
}

@Composable
private fun DriverMetric(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.coerceAtLeast(0).toString(), style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
