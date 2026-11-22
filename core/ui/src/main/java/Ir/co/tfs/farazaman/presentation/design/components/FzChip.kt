package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import Ir.co.tfs.farazaman.presentation.design.theme.FzComponentSize
import Ir.co.tfs.farazaman.presentation.design.theme.FzSemanticIntent
import Ir.co.tfs.farazaman.presentation.design.theme.FzSelectionMode
import Ir.co.tfs.farazaman.presentation.design.theme.fzIntentColors
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

@Immutable
data class FzChipItem<T>(
    val value: T,
    val label: String,
    val enabled: Boolean = true,
)

@Composable
fun FzChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    size: FzComponentSize = FzComponentSize.Medium,
    intent: FzSemanticIntent = FzSemanticIntent.Primary,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClose: (() -> Unit)? = null,
) {
    val colors = fzIntentColors(intent)
    val sized = modifier
        .heightIn(min = FaraZamanThemeTokens.sizes.chipHeight(size))
        .semantics { role = Role.Checkbox }
    if (onClose != null) {
        InputChip(
            selected = selected,
            onClick = { if (!readOnly) onClick() },
            modifier = sized,
            enabled = enabled,
            label = { Text(label, maxLines = 1) },
            leadingIcon = leadingIcon,
            trailingIcon = {
                IconButton(onClick = onClose, enabled = enabled && !readOnly) {
                    Text(text = "×")
                }
            },
            colors = InputChipDefaults.inputChipColors(
                selectedContainerColor = colors.container,
                selectedLabelColor = colors.content,
            ),
        )
    } else {
        FilterChip(
            selected = selected,
            onClick = { if (!readOnly) onClick() },
            modifier = sized,
            enabled = enabled,
            label = { Text(label, maxLines = 1) },
            leadingIcon = leadingIcon,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = colors.container,
                selectedLabelColor = colors.content,
            ),
        )
    }
}

@Composable
fun <T> FzChipSet(
    items: List<FzChipItem<T>>,
    selectedValues: Set<T>,
    onSelectionChange: (Set<T>) -> Unit,
    modifier: Modifier = Modifier,
    mode: FzSelectionMode = FzSelectionMode.Multiple,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    size: FzComponentSize = FzComponentSize.Medium,
    intent: FzSemanticIntent = FzSemanticIntent.Primary,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.sm),
    ) {
        items.forEach { item ->
            FzChip(
                label = item.label,
                selected = item.value in selectedValues,
                enabled = enabled && item.enabled,
                readOnly = readOnly,
                size = size,
                intent = intent,
                onClick = {
                    val selected = item.value in selectedValues
                    val next = when (mode) {
                        FzSelectionMode.None -> selectedValues
                        FzSelectionMode.Single -> setOf(item.value)
                        FzSelectionMode.Toggle -> if (selected) emptySet() else setOf(item.value)
                        FzSelectionMode.Multiple -> if (selected) selectedValues - item.value else selectedValues + item.value
                    }
                    if (next != selectedValues) onSelectionChange(next)
                },
            )
        }
    }
}
