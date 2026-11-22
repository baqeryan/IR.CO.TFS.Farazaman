package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier

@Immutable
data class FzTabItem(val id: String, val label: String, val enabled: Boolean = true)

@Composable
fun FzTabs(
    items: List<FzTabItem>,
    selectedId: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val index = items.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
    ScrollableTabRow(index, modifier = modifier, edgePadding = FaraZamanThemeTokens.spacing.sm) {
        items.forEach { item ->
            Tab(item.id == selectedId, { onSelected(item.id) }, enabled = item.enabled, text = { Text(item.label, maxLines = 1) })
        }
    }
}

@Composable
fun FzPagination(
    page: Int,
    pageCount: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(pageCount >= 0) { "pageCount cannot be negative" }
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.xs)) {
        FzButton("قبلی", { onPageChange(page - 1) }, enabled = page > 1)
        Text("$page / $pageCount")
        FzButton("بعدی", { onPageChange(page + 1) }, enabled = page < pageCount)
    }
}

@Composable
fun FzSectionNavigation(
    sections: List<FzTabItem>,
    selectedId: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) = Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.xs)) {
    sections.forEach { section ->
        FzListItem(section.label, enabled = section.enabled, onClick = { onSelected(section.id) }, trailing = if (section.id == selectedId) ({ Text("●") }) else null)
    }
}
