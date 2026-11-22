package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import Ir.co.tfs.farazaman.presentation.design.theme.FzSemanticIntent
import Ir.co.tfs.farazaman.presentation.design.theme.fzIntentColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics

@Composable
fun FzAlert(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    intent: FzSemanticIntent = FzSemanticIntent.Info,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) {
    val colors = fzIntentColors(intent)
    Surface(modifier.semantics { liveRegion = LiveRegionMode.Polite }, color = colors.container, contentColor = colors.content, shape = MaterialTheme.shapes.medium) {
        Row(Modifier.fillMaxWidth().padding(FaraZamanThemeTokens.spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.xs)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
            if (actionLabel != null && onAction != null) FzButton(actionLabel, onAction, variant = FzButtonVariant.Ghost)
            if (onDismiss != null) FzButton("بستن", onDismiss, variant = FzButtonVariant.Ghost)
        }
    }
}

@Composable
fun FzMessageBox(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String? = null,
    onDismiss: (() -> Unit)? = null,
) = FzAlert(title, modifier, message, FzSemanticIntent.Neutral, confirmLabel, onConfirm, if (dismissLabel != null) onDismiss else null)
