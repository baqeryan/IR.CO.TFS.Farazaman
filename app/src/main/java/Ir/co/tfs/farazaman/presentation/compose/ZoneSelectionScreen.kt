package Ir.co.tfs.farazaman.presentation.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import Ir.co.tfs.farazaman.data.model.VehicleZoneWorkItem
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzEmptyState
import Ir.co.tfs.farazaman.presentation.design.components.FzLoading
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens

data class ZoneSelectionUiState(
    val loading: Boolean = false,
    val zones: List<VehicleZoneWorkItem> = emptyList(),
)

sealed interface ZoneSelectionEvent {
    data class Select(val zone: VehicleZoneWorkItem) : ZoneSelectionEvent
}

@Composable
fun ZoneSelectionScreen(
    state: ZoneSelectionUiState,
    onEvent: (ZoneSelectionEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    Column(
        modifier = modifier.fillMaxSize().padding(spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Text("انتخاب محدوده کاری", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        when {
            state.loading -> FzLoading("در حال دریافت محدوده‌های کاری...")
            state.zones.isEmpty() -> FzEmptyState(
                title = "هیچ محدوده کاری یافت نشد",
                message = "لطفاً با مدیر سیستم تماس بگیرید",
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
                contentPadding = PaddingValues(bottom = spacing.md),
            ) {
                items(state.zones, key = { it.zoneWorkVehicleID }) { zone ->
                    FzCard(Modifier.fillMaxWidth(), onClick = { onEvent(ZoneSelectionEvent.Select(zone)) }) {
                        Column(Modifier.fillMaxWidth().padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                            Text("منطقه: ${zone.organ.organName ?: "نامشخص"}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Text("پلاک: ${zone.assetVehicle.plakNumber ?: "نامشخص"}")
                            zone.contract?.firstOrNull()?.contractCode?.takeIf { it.isNotBlank() }?.let { Text("قرارداد: $it") }
                            Text("شیفت: ${zone.assetShift?.shiftName ?: "نامشخص"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
