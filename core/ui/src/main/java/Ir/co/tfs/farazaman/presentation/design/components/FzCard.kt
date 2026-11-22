package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class FzCardVariant { Standard, Elevated, Outlined, Selected, Glass, GlassSuccess, GlassAlert }

@Composable
fun FzCard(
    modifier: Modifier = Modifier,
    variant: FzCardVariant = FzCardVariant.Standard,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val elevation = when (variant) {
        FzCardVariant.Elevated -> FaraZamanThemeTokens.elevation.raised
        FzCardVariant.Glass,
        FzCardVariant.GlassSuccess,
        FzCardVariant.GlassAlert,
        -> FaraZamanThemeTokens.elevation.overlay
        else -> FaraZamanThemeTokens.elevation.resting
    }
    val border = when (variant) {
        FzCardVariant.Outlined -> BorderStroke(FaraZamanThemeTokens.spacing.xs / 4, FaraZamanThemeTokens.colors.uiBorder)
        FzCardVariant.Selected -> BorderStroke(FaraZamanThemeTokens.spacing.xs / 2, FaraZamanThemeTokens.colors.brandSecondary)
        FzCardVariant.GlassAlert -> BorderStroke(
            FaraZamanThemeTokens.spacing.xs / 4,
            FaraZamanThemeTokens.semanticColors.onWarningContainer.copy(alpha = 0.22f),
        )
        FzCardVariant.GlassSuccess -> BorderStroke(
            FaraZamanThemeTokens.spacing.xs / 4,
            FaraZamanThemeTokens.semanticColors.success.copy(alpha = 0.28f),
        )
        else -> null
    }
    val cardColors = CardDefaults.cardColors(
        containerColor = when (variant) {
            FzCardVariant.Glass -> MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
            FzCardVariant.GlassSuccess -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.88f)
            FzCardVariant.GlassAlert -> FaraZamanThemeTokens.semanticColors.warningContainer.copy(alpha = 0.88f)
            else -> FaraZamanThemeTokens.colors.uiFloated
        },
        contentColor = when (variant) {
            FzCardVariant.GlassAlert -> FaraZamanThemeTokens.semanticColors.onWarningContainer
            FzCardVariant.GlassSuccess -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> FaraZamanThemeTokens.colors.textPrimary
        },
    )
    val shape = if (
        variant == FzCardVariant.Glass ||
        variant == FzCardVariant.GlassSuccess ||
        variant == FzCardVariant.GlassAlert
    ) {
        MaterialTheme.shapes.extraLarge
    } else {
        MaterialTheme.shapes.medium
    }
    if (onClick == null) {
        Card(modifier = modifier, shape = shape, border = border, colors = cardColors, elevation = CardDefaults.cardElevation(elevation), content = content)
    } else {
        Card(onClick = onClick, modifier = modifier, shape = shape, border = border, colors = cardColors, elevation = CardDefaults.cardElevation(elevation), content = content)
    }
}
