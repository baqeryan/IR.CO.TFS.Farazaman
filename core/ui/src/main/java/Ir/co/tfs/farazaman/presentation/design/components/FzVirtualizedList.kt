package Ir.co.tfs.farazaman.presentation.design.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T> FzVirtualizedList(
    items: List<T>,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    itemContent: @Composable (T) -> Unit,
) {
    if (items.isEmpty() && emptyContent != null) emptyContent()
    else LazyColumn(modifier) { items(items, key = key) { itemContent(it) } }
}
