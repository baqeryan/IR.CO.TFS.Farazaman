package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

@Immutable
data class FzMenuItem(val id: String, val label: String, val enabled: Boolean = true)

@Composable
fun FzMenu(
    expanded: Boolean,
    items: List<FzMenuItem>,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) = DropdownMenu(expanded, onDismiss, modifier) {
    items.forEach { item ->
        DropdownMenuItem(text = { Text(item.label) }, onClick = { onSelected(item.id) }, enabled = item.enabled)
    }
}

@Composable
fun FzNavigationMenu(
    items: List<FzNavigationItem>,
    selectedId: String?,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) = FzList(modifier) {
    items.forEach { item ->
        FzListItem(
            title = item.label,
            enabled = item.enabled,
            onClick = { onSelected(item.id) },
            trailing = if (selectedId == item.id) ({ Text("✓", color = MaterialTheme.colorScheme.primary) }) else null,
        )
    }
}

@Composable
fun FzLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) =
    Text(
        text,
        modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        style = MaterialTheme.typography.bodyMedium,
    )

@Composable
fun FzBreadcrumbs(
    items: List<FzMenuItem>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) = Row(modifier, horizontalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.xs)) {
    items.forEachIndexed { index, item ->
        FzLink(item.label, { onSelected(item.id) }, enabled = item.enabled)
        if (index != items.lastIndex) Text("‹", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
