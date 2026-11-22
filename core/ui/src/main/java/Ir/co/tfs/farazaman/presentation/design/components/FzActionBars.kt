package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun FzBottomActionBar(
    primaryText: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryVariant: FzButtonVariant = FzButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        tonalElevation = if (containerColor == Color.Transparent) FaraZamanThemeTokens.spacing.none else FaraZamanThemeTokens.spacing.xs,
    ) {
        val contentModifier = if (containerColor == Color.Transparent) {
            Modifier.fillMaxWidth().navigationBarsPadding()
        } else {
            Modifier.fillMaxWidth().navigationBarsPadding().padding(FaraZamanThemeTokens.spacing.md)
        }
        Column(
            contentModifier,
            verticalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.sm),
        ) {
            supportingContent?.invoke()
            FzButton(
                text = primaryText,
                onClick = onPrimaryClick,
                modifier = Modifier.fillMaxWidth(),
                variant = primaryVariant,
                enabled = enabled,
                loading = loading,
                leadingIcon = leadingIcon,
            )
        }
    }
}

@Composable
fun FzDialogActions(
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.sm),
    ) {
        FzButton(dismissText, onDismiss, Modifier.weight(1f), FzButtonVariant.Secondary)
        FzButton(
            confirmText,
            onConfirm,
            Modifier.weight(1f),
            if (destructive) FzButtonVariant.Danger else FzButtonVariant.Primary,
        )
    }
}
