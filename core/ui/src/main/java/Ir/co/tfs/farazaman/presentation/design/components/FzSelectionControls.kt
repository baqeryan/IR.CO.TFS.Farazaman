package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun FzCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit, label: String? = null, modifier: Modifier = Modifier, enabled: Boolean = true) {
    FzLabeledControl(label, modifier, enabled) { Checkbox(checked, onCheckedChange, enabled = enabled) }
}

@Composable
fun FzRadio(selected: Boolean, onClick: () -> Unit, label: String? = null, modifier: Modifier = Modifier, enabled: Boolean = true) {
    FzLabeledControl(label, modifier.clickable(enabled = enabled, onClick = onClick), enabled) { RadioButton(selected, onClick, enabled = enabled) }
}

@Composable
fun FzSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, label: String? = null, modifier: Modifier = Modifier, enabled: Boolean = true) {
    FzLabeledControl(label, modifier, enabled) { Switch(checked, onCheckedChange, enabled = enabled) }
}

@Composable
fun <T> FzRadioGroup(options: List<FzChipItem<T>>, selected: T?, onSelected: (T) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.xs)) {
        options.forEach { option -> FzRadio(option.value == selected, { onSelected(option.value) }, option.label, enabled = option.enabled) }
    }
}

@Composable
private fun FzLabeledControl(label: String?, modifier: Modifier, enabled: Boolean, control: @Composable RowScope.() -> Unit) {
    Row(modifier = modifier.fzMinimumTouchTarget(), verticalAlignment = Alignment.CenterVertically) {
        control()
        if (label != null) Text(label, color = if (enabled) androidx.compose.ui.graphics.Color.Unspecified else androidx.compose.ui.graphics.Color.Gray)
    }
}
