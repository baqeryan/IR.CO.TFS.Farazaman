package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

@Composable
fun FzCollapse(visible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) =
    AnimatedVisibility(visible = visible, modifier = modifier, content = { content() })

@Composable
fun FzExpansionPanel(
    title: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(modifier, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
        Column {
            Row(
                Modifier.fillMaxWidth().clickable(enabled = enabled, role = Role.Button) { onExpandedChange(!expanded) }
                    .padding(FaraZamanThemeTokens.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Text(if (expanded) "−" else "+", style = MaterialTheme.typography.titleLarge)
            }
            FzCollapse(expanded) { Column(Modifier.padding(FaraZamanThemeTokens.spacing.md), content = content) }
        }
    }
}
