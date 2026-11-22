package Ir.co.tfs.farazaman.presentation.compose.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzLoading
import Ir.co.tfs.farazaman.presentation.design.components.FzHeaderSize
import Ir.co.tfs.farazaman.presentation.design.components.FzPageTemplate
import Ir.co.tfs.farazaman.presentation.design.components.FzTextField
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens

data class TrackListItemUiState(val id: Long, val title: String, val date: String, val pointCount: Int)
data class TrackListUiState(val loading: Boolean = false, val tracks: List<TrackListItemUiState> = emptyList(), val error: String? = null)
sealed interface TrackListEvent { data object Back : TrackListEvent; data class Open(val id: Long) : TrackListEvent; data class Delete(val id: Long) : TrackListEvent; data class Upload(val id: Long) : TrackListEvent; data class Export(val id: Long) : TrackListEvent }

@Composable
fun TrackListScreen(state: TrackListUiState, onEvent: (TrackListEvent) -> Unit, modifier: Modifier = Modifier) {
    val spacing = FaraZamanThemeTokens.spacing
    FzPageTemplate(title = "مسیرها", headerSize = FzHeaderSize.Compact, modifier = modifier, onBack = { onEvent(TrackListEvent.Back) }) { padding ->
      Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        if (state.loading) FzLoading("در حال بارگذاری مسیرها")
        else if (state.error != null) Text(state.error, color = MaterialTheme.colorScheme.error)
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            items(state.tracks, key = { it.id }) { track ->
                FzCard(Modifier.fillMaxWidth(), onClick = { onEvent(TrackListEvent.Open(track.id)) }) {
                    Column(Modifier.padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                        Text(track.title, style = MaterialTheme.typography.titleMedium)
                        Text(track.date, style = MaterialTheme.typography.bodySmall)
                        Text("${track.pointCount} نقطه", style = MaterialTheme.typography.bodySmall)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                            FzButton("خروجی GPX", { onEvent(TrackListEvent.Export(track.id)) }, variant = FzButtonVariant.Secondary)
                            FzButton("بارگذاری", { onEvent(TrackListEvent.Upload(track.id)) })
                            FzButton("حذف", { onEvent(TrackListEvent.Delete(track.id)) }, variant = FzButtonVariant.Danger)
                        }
                    }
                }
            }
        }
      }
    }
}

data class TrackDetailUiState(val id: Long, val title: String, val date: String, val pointCount: Int, val waypointCount: Int, val exporting: Boolean = false, val uploading: Boolean = false)
sealed interface TrackDetailEvent { data object Back : TrackDetailEvent; data object OpenMap : TrackDetailEvent; data object Export : TrackDetailEvent; data object Upload : TrackDetailEvent; data object AddWaypoint : TrackDetailEvent }

data class WaypointUiState(val id: Long, val name: String, val description: String, val latitude: Double, val longitude: Double, val uuid: String = "", val link: String? = null)
sealed interface WaypointEvent {
    data object Back : WaypointEvent
    data class OpenOnMap(val latitude: Double, val longitude: Double) : WaypointEvent
    data class Edit(val id: Long) : WaypointEvent
}

@Composable
fun WaypointListScreen(waypoints: List<WaypointUiState>, onEvent: (WaypointEvent) -> Unit, modifier: Modifier = Modifier) {
    val spacing = FaraZamanThemeTokens.spacing
    FzPageTemplate(title = "نقاط ثبت‌شده", headerSize = FzHeaderSize.Compact, modifier = modifier, onBack = { onEvent(WaypointEvent.Back) }) { padding ->
      Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            items(waypoints, key = { it.id }) { point ->
                FzCard(Modifier.fillMaxWidth(), onClick = { onEvent(WaypointEvent.Edit(point.id)) }) {
                    Column(Modifier.padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        Text(point.name)
                        if (point.description.isNotBlank()) Text(point.description, style = MaterialTheme.typography.bodySmall)
                        Text("${point.latitude}, ${point.longitude}", style = MaterialTheme.typography.bodySmall)
                        FzButton("نمایش روی نقشه", { onEvent(WaypointEvent.OpenOnMap(point.latitude, point.longitude)) }, modifier = Modifier.fillMaxWidth(), variant = FzButtonVariant.Ghost)
                    }
                }
            }
        }
      }
    }
}

@Composable
fun WaypointEditDialog(
    name: String,
    hasAttachment: Boolean,
    onNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onPreview: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ویرایش نقطه") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.md)) {
                FzTextField(name, onNameChanged, label = "نام نقطه")
                if (hasAttachment) FzButton("پیش‌نمایش فایل", onPreview, modifier = Modifier.fillMaxWidth(), variant = FzButtonVariant.Ghost)
            }
        },
        confirmButton = { FzButton("ذخیره", onSave) },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.sm)) {
                FzButton("حذف", onDelete, variant = FzButtonVariant.Danger)
                FzButton("انصراف", onDismiss, variant = FzButtonVariant.Ghost)
            }
        },
    )
}

@Composable
fun TrackDetailScreen(state: TrackDetailUiState, onEvent: (TrackDetailEvent) -> Unit, modifier: Modifier = Modifier) {
    val spacing = FaraZamanThemeTokens.spacing
    FzPageTemplate(title = state.title, headerSize = FzHeaderSize.Compact, modifier = modifier, onBack = { onEvent(TrackDetailEvent.Back) }) { padding ->
      Column(Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        FzCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(spacing.md), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                Text(state.date); Text("نقاط مسیر: ${state.pointCount}"); Text("نقاط ثبت‌شده: ${state.waypointCount}")
                FzButton("نمایش روی نقشه", { onEvent(TrackDetailEvent.OpenMap) }, Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    FzButton("افزودن نقطه", { onEvent(TrackDetailEvent.AddWaypoint) }, Modifier.weight(1f), FzButtonVariant.Secondary)
                    FzButton("خروجی GPX", { onEvent(TrackDetailEvent.Export) }, Modifier.weight(1f), FzButtonVariant.Secondary, loading = state.exporting)
                }
                FzButton("بارگذاری مسیر", { onEvent(TrackDetailEvent.Upload) }, Modifier.fillMaxWidth(), loading = state.uploading)
            }
        }
      }
    }
}

data class TrackUploadUiState(
    val trackId: Long,
    val name: String = "",
    val description: String = "",
    val tags: String = "",
    val visibility: String = "private",
    val visibilityOptions: List<String> = listOf("private", "public", "trackable", "identifiable"),
    val uploading: Boolean = false,
    val error: String? = null,
)

sealed interface TrackUploadEvent {
    data object Back : TrackUploadEvent
    data class NameChanged(val value: String) : TrackUploadEvent
    data class DescriptionChanged(val value: String) : TrackUploadEvent
    data class TagsChanged(val value: String) : TrackUploadEvent
    data class VisibilityChanged(val value: String) : TrackUploadEvent
    data object Submit : TrackUploadEvent
}

@Composable
fun TrackUploadScreen(
    state: TrackUploadUiState,
    onEvent: (TrackUploadEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    FzPageTemplate(
        title = "بارگذاری مسیر",
        headerSize = FzHeaderSize.Compact,
        modifier = modifier,
        onBack = { onEvent(TrackUploadEvent.Back) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            item { FzTextField(state.name, { onEvent(TrackUploadEvent.NameChanged(it)) }, label = "نام مسیر") }
            item { FzTextField(state.description, { onEvent(TrackUploadEvent.DescriptionChanged(it)) }, label = "توضیحات", errorText = state.error) }
            item { FzTextField(state.tags, { onEvent(TrackUploadEvent.TagsChanged(it)) }, label = "برچسب‌ها") }
            item {
                Text("سطح دسترسی", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    state.visibilityOptions.forEach { option ->
                        FzButton(
                            text = option,
                            onClick = { onEvent(TrackUploadEvent.VisibilityChanged(option)) },
                            variant = if (state.visibility == option) FzButtonVariant.Primary else FzButtonVariant.Ghost,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            item {
                FzButton(
                    text = "ذخیره و بارگذاری",
                    onClick = { onEvent(TrackUploadEvent.Submit) },
                    modifier = Modifier.fillMaxWidth(),
                    loading = state.uploading,
                    enabled = !state.uploading,
                )
            }
        }
    }
}

data class TrackLoggerUiState(
    val trackId: Long,
    val controlsEnabled: Boolean = false,
)

sealed interface TrackLoggerEvent {
    data object Stop : TrackLoggerEvent
    data object Settings : TrackLoggerEvent
    data object Waypoints : TrackLoggerEvent
    data object About : TrackLoggerEvent
    data object Map : TrackLoggerEvent
}

@Composable
fun TrackLoggerScreen(
    state: TrackLoggerUiState,
    onEvent: (TrackLoggerEvent) -> Unit,
    gpsStatus: @Composable () -> Unit,
    controls: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    FzPageTemplate(
        title = "ثبت مسیر #${state.trackId}",
        headerSize = FzHeaderSize.Compact,
        modifier = modifier,
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            gpsStatus()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                FzButton("نقشه", { onEvent(TrackLoggerEvent.Map) }, Modifier.weight(1f), FzButtonVariant.Ghost)
                FzButton("نقاط", { onEvent(TrackLoggerEvent.Waypoints) }, Modifier.weight(1f), FzButtonVariant.Ghost)
                FzButton("تنظیمات", { onEvent(TrackLoggerEvent.Settings) }, Modifier.weight(1f), FzButtonVariant.Ghost)
                FzButton("درباره", { onEvent(TrackLoggerEvent.About) }, Modifier.weight(1f), FzButtonVariant.Ghost)
            }
            FzCard(Modifier.fillMaxWidth().weight(1f)) { controls() }
            FzButton(
                text = "توقف ثبت مسیر",
                onClick = { onEvent(TrackLoggerEvent.Stop) },
                modifier = Modifier.fillMaxWidth(),
                variant = FzButtonVariant.Danger,
                enabled = state.controlsEnabled,
            )
        }
    }
}
