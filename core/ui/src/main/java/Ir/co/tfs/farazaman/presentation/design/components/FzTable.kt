package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Immutable
data class FzTableColumn<T>(val id: String, val title: String, val weight: Float = 1f, val value: (T) -> String)

@Composable
fun FzSimpleTable(headers: List<String>, rows: List<List<String>>, modifier: Modifier = Modifier) {
    require(rows.all { it.size == headers.size }) { "Every row must match the header count" }
    androidx.compose.foundation.layout.Column(modifier) {
        FzStringTableRow(headers, true)
        rows.forEach { FzStringTableRow(it, false) }
    }
}

@Composable
private fun FzStringTableRow(values: List<String>, header: Boolean) {
    Row(Modifier.fillMaxWidth().then(if (header) Modifier.background(MaterialTheme.colorScheme.surfaceVariant) else Modifier)) {
        values.forEach { Text(it, Modifier.weight(1f).padding(FaraZamanThemeTokens.spacing.sm), fontWeight = if (header) FontWeight.Bold else null) }
    }
    FzDivider()
}

@Composable
fun <T> FzTable(columns: List<FzTableColumn<T>>, rows: List<T>, modifier: Modifier = Modifier) =
    androidx.compose.foundation.layout.Column(modifier) {
        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)) {
            columns.forEach { Text(it.title, Modifier.weight(it.weight).padding(FaraZamanThemeTokens.spacing.sm), fontWeight = FontWeight.Bold) }
        }
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth()) { columns.forEach { Text(it.value(row), Modifier.weight(it.weight).padding(FaraZamanThemeTokens.spacing.sm)) } }
            FzDivider()
        }
    }
