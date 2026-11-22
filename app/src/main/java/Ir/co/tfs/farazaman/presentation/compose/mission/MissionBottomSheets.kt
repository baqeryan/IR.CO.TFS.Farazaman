package Ir.co.tfs.farazaman.presentation.compose.mission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.activity.MissionSelectionOption
import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzLoading
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens

@Composable
fun EndMissionSheetContent(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    Column(
        modifier = modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surface)
            .padding(spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Text(
            stringResource(R.string.end_mission_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            stringResource(R.string.end_mission_message_remove),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.end_mission_message_confirm),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            FzButton(
                text = stringResource(R.string.end_mission_cancel),
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                variant = FzButtonVariant.Secondary,
            )
            FzButton(
                text = stringResource(R.string.end_mission_confirm),
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                variant = FzButtonVariant.Primary,
            )
        }
    }
}

@Composable
fun MissionItemSelectionSheetContent(
    title: String,
    options: List<MissionSelectionOption>,
    onSelect: (MissionSelectionOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    Column(
        modifier = modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Text(
            text = title,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            contentPadding = PaddingValues(bottom = spacing.md),
        ) {
            items(options, key = { it.id }) { option ->
                FzCard(modifier = Modifier.fillMaxWidth(), onClick = { onSelect(option) }) {
                    Text(
                        text = option.title,
                        modifier = Modifier.fillMaxWidth().padding(spacing.md),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

data class NewMissionUiState(
    val date: String = "",
    val organTitle: String? = null,
    val contractTitle: String? = null,
    val organLoading: Boolean = true,
    val contractLoading: Boolean = false,
    val submitting: Boolean = false,
)

sealed interface NewMissionEvent {
    data object SelectOrgan : NewMissionEvent
    data object SelectContract : NewMissionEvent
    data object Submit : NewMissionEvent
    data object Cancel : NewMissionEvent
}

@Composable
fun NewMissionSheetContent(
    state: NewMissionUiState,
    onEvent: (NewMissionEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    Column(
        modifier = modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Text(
            "دریافت برنامه کاری",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            state.date,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (state.organLoading) FzLoading("در حال دریافت کارفرماها") else FzButton(
            text = state.organTitle ?: "انتخاب کارفرما",
            onClick = { onEvent(NewMissionEvent.SelectOrgan) },
            modifier = Modifier.fillMaxWidth(),
            variant = FzButtonVariant.Secondary,
        )
        if (state.contractLoading) FzLoading("در حال دریافت قراردادها") else FzButton(
            text = state.contractTitle ?: "انتخاب قرارداد",
            onClick = { onEvent(NewMissionEvent.SelectContract) },
            modifier = Modifier.fillMaxWidth(),
            variant = FzButtonVariant.Secondary,
            enabled = state.organTitle != null,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            FzButton("انصراف", { onEvent(NewMissionEvent.Cancel) }, Modifier.weight(1f), FzButtonVariant.Ghost)
            FzButton(
                text = if (state.submitting) "در حال ثبت" else "دریافت برنامه",
                onClick = { onEvent(NewMissionEvent.Submit) },
                modifier = Modifier.weight(1f),
                loading = state.submitting,
                enabled = state.organTitle != null && state.contractTitle != null && !state.submitting,
            )
        }
    }
}
