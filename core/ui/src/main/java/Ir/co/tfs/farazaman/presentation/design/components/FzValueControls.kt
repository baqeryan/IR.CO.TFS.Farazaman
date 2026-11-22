package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import Ir.co.tfs.farazaman.presentation.design.theme.FzComponentSize
import Ir.co.tfs.farazaman.presentation.design.theme.FzSelectionMode
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun FzToggle(
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: FzComponentSize = FzComponentSize.Medium,
) = FzChip(
    label = label,
    selected = selected,
    onClick = { onSelectedChange(!selected) },
    modifier = modifier,
    enabled = enabled,
    size = size,
)

@Composable
fun <T> FzToggleGroup(
    items: List<FzChipItem<T>>,
    selectedValues: Set<T>,
    onSelectionChange: (Set<T>) -> Unit,
    modifier: Modifier = Modifier,
    mode: FzSelectionMode = FzSelectionMode.Single,
    enabled: Boolean = true,
) = FzChipSet(items, selectedValues, onSelectionChange, modifier, mode, enabled)

@Composable
fun FzSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
) = Slider(value, onValueChange, modifier, enabled, valueRange, steps, onValueChangeFinished)

@Composable
fun FzRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
) = RangeSlider(value, onValueChange, modifier, enabled, valueRange, steps, onValueChangeFinished)

@Composable
fun FzRating(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    maximum: Int = 5,
    enabled: Boolean = true,
    symbolSize: TextUnit = 28.sp,
) {
    require(maximum > 0) { "maximum must be positive" }
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.xs)) {
        repeat(maximum) { index ->
            val rating = index + 1
            val isSelected = rating <= value.roundToInt()
            TextButton(
                onClick = { onValueChange(rating.toFloat()) },
                enabled = enabled,
                modifier = Modifier
                    .fzMinimumTouchTarget()
                    .semantics { role = Role.RadioButton; selected = isSelected },
            ) {
                Text(if (isSelected) "★" else "☆", fontSize = symbolSize)
            }
        }
    }
}
