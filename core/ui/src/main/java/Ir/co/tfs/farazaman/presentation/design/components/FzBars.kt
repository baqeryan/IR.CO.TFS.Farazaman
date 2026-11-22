package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun FzTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigation: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Surface(modifier, color = MaterialTheme.colorScheme.surface) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = FaraZamanThemeTokens.sizes.buttonHeight)
                .padding(horizontal = FaraZamanThemeTokens.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (navigation != null) navigation()
            Text(title, Modifier.weight(1f).padding(horizontal = FaraZamanThemeTokens.spacing.sm), style = MaterialTheme.typography.titleLarge)
            Row(content = actions)
        }
    }
}

@Composable
fun FzToolbar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) = Surface(modifier, color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
    Row(
        Modifier.fillMaxWidth().padding(FaraZamanThemeTokens.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
fun FzScrollToTop(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.animation.AnimatedVisibility(visible = visible, modifier = modifier) {
        FzFloatingActionButton(onClick = onClick) { Text("↑", style = MaterialTheme.typography.titleLarge) }
    }
}
