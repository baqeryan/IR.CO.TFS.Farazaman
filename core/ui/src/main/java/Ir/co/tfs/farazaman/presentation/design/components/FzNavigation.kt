package Ir.co.tfs.farazaman.presentation.design.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Immutable
data class FzNavigationItem(
    val id: String,
    val label: String,
    val enabled: Boolean = true,
)

@Composable
fun FzBottomNavigation(
    items: List<FzNavigationItem>,
    selectedId: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (FzNavigationItem, Boolean) -> Unit,
) = NavigationBar(modifier) {
    items.forEach { item ->
        val selected = item.id == selectedId
        NavigationBarItem(
            selected = selected,
            onClick = { onSelected(item.id) },
            enabled = item.enabled,
            icon = { icon(item, selected) },
            label = if (selected) ({ Text(item.label, maxLines = 1) }) else null,
            alwaysShowLabel = false,
        )
    }
}

@Composable
fun FzNavigationRail(
    items: List<FzNavigationItem>,
    selectedId: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (FzNavigationItem, Boolean) -> Unit,
) = NavigationRail(modifier) {
    items.forEach { item ->
        val selected = item.id == selectedId
        NavigationRailItem(selected, { onSelected(item.id) }, { icon(item, selected) }, enabled = item.enabled, label = { Text(item.label) })
    }
}

@Composable
fun FzDrawer(
    items: List<FzNavigationItem>,
    selectedId: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
) = ModalDrawerSheet(modifier) {
    if (header != null) header()
    Column {
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                selected = item.id == selectedId,
                onClick = { onSelected(item.id) },
            )
        }
    }
}

@Composable
fun FzNavigationDrawer(
    open: Boolean,
    onDismiss: () -> Unit,
    drawerContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val state = rememberDrawerState(
        initialValue = if (open) DrawerValue.Open else DrawerValue.Closed,
        confirmStateChange = { value ->
            if (value == DrawerValue.Closed && open) onDismiss()
            true
        },
    )
    LaunchedEffect(open) { if (open) state.open() else state.close() }
    ModalNavigationDrawer(
        drawerState = state,
        drawerContent = drawerContent,
        gesturesEnabled = open,
        content = content,
    )
}
