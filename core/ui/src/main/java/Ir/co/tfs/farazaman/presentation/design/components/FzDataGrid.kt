package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics

enum class FzSortDirection { Ascending, Descending }

@Immutable
data class FzDataGridState<K>(
    val query: String = "",
    val sortColumnId: String? = null,
    val sortDirection: FzSortDirection = FzSortDirection.Ascending,
    val page: Int = 1,
    val pageSize: Int = 20,
    val selectedKeys: Set<K> = emptySet(),
) {
    init { require(page > 0); require(pageSize > 0) }
    fun toggleSelection(key: K) = copy(selectedKeys = if (key in selectedKeys) selectedKeys - key else selectedKeys + key)
    fun selectSort(id: String) = if (sortColumnId == id) copy(
        sortDirection = if (sortDirection == FzSortDirection.Ascending) FzSortDirection.Descending else FzSortDirection.Ascending,
        page = 1,
    ) else copy(sortColumnId = id, sortDirection = FzSortDirection.Ascending, page = 1)
}

fun <T, K> FzDataGridState<K>.resolveRows(rows: List<T>, columns: List<FzTableColumn<T>>): List<T> {
    val filtered = if (query.isBlank()) rows else rows.filter { row -> columns.any { it.value(row).contains(query, true) } }
    val column = columns.firstOrNull { it.id == sortColumnId }
    val sorted = if (column == null) filtered else filtered.sortedBy { column.value(it) }.let { if (sortDirection == FzSortDirection.Descending) it.reversed() else it }
    return sorted.drop((page - 1) * pageSize).take(pageSize)
}

@Composable
fun <T, K> FzDataGrid(
    rows: List<T>, columns: List<FzTableColumn<T>>, state: FzDataGridState<K>, key: (T) -> K,
    onStateChange: (FzDataGridState<K>) -> Unit, modifier: Modifier = Modifier,
) = androidx.compose.foundation.layout.Column(modifier) {
    Row(Modifier.fillMaxWidth()) {
        columns.forEach { column ->
            val marker = if (state.sortColumnId == column.id) if (state.sortDirection == FzSortDirection.Ascending) " ↑" else " ↓" else ""
            Text(column.title + marker, Modifier.weight(column.weight).clickable { onStateChange(state.selectSort(column.id)) }.padding(FaraZamanThemeTokens.spacing.sm), fontWeight = FontWeight.Bold)
        }
    }
    state.resolveRows(rows, columns).forEach { row ->
        val rowKey = key(row)
        Row(Modifier.fillMaxWidth().semantics { selected = rowKey in state.selectedKeys }.clickable { onStateChange(state.toggleSelection(rowKey)) }
            .then(if (rowKey in state.selectedKeys) Modifier.background(MaterialTheme.colorScheme.primaryContainer) else Modifier)) {
            columns.forEach { Text(it.value(row), Modifier.weight(it.weight).padding(FaraZamanThemeTokens.spacing.sm)) }
        }
        FzDivider()
    }
}
