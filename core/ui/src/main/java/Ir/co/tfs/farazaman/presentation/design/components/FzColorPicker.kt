package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics

@Composable
fun FzColorPicker(
    value: Color,
    onValueChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
    colors: List<Color>? = null,
    enabled: Boolean = true,
) = Row(modifier, horizontalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.sm)) {
    val palette = colors ?: listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer,
    )
    palette.forEach { color ->
        val isSelected = color == value
        androidx.compose.foundation.layout.Box(
            Modifier.size(FaraZamanThemeTokens.sizes.minimumTouchTarget).clip(CircleShape)
                .background(color)
                .then(if (isSelected) Modifier.border(FaraZamanThemeTokens.spacing.xs / 2, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                .clickable(enabled = enabled, role = Role.RadioButton) { onValueChange(color) }
                .semantics { selected = isSelected; contentDescription = "رنگ ${color.value}" },
        )
    }
}
