package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Immutable
data class FzTime(val hour: Int, val minute: Int) {
    init { require(hour in 0..23); require(minute in 0..59) }
    override fun toString(): String = "%02d:%02d".format(hour, minute)
}

@Composable
fun FzTimePicker(value: FzTime, onValueChange: (FzTime) -> Unit, modifier: Modifier = Modifier) =
    FzField("زمان", Ir.co.tfs.farazaman.presentation.design.state.FzFieldState(), modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            FzButton("−", { onValueChange(value.copy(hour = (value.hour + 23) % 24)) }, variant = FzButtonVariant.Ghost)
            Text(value.toString())
            FzButton("+", { onValueChange(value.copy(hour = (value.hour + 1) % 24)) }, variant = FzButtonVariant.Ghost)
            FzButton("−۵", { onValueChange(value.copy(minute = (value.minute + 55) % 60)) }, variant = FzButtonVariant.Ghost)
            FzButton("+۵", { onValueChange(value.copy(minute = (value.minute + 5) % 60)) }, variant = FzButtonVariant.Ghost)
        }
    }
