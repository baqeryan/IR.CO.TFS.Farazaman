package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Immutable
data class FzStructuredItem<T>(
    val id: T,
    val title: String,
    val subtitle: String? = null,
    val children: List<FzStructuredItem<T>> = emptyList(),
)

@Composable
fun <T> FzTimeline(items: List<FzStructuredItem<T>>, modifier: Modifier = Modifier) = Column(modifier) {
    items.forEach { item ->
        Row(Modifier.fillMaxWidth().padding(vertical = FaraZamanThemeTokens.spacing.sm), verticalAlignment = Alignment.Top) {
            Box(Modifier.padding(top = FaraZamanThemeTokens.spacing.xs).size(10.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
            Spacer(Modifier.width(FaraZamanThemeTokens.spacing.sm))
            Column { Text(item.title, fontWeight = FontWeight.Bold); item.subtitle?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
    }
}

@Composable
fun <T> FzTreeView(
    items: List<FzStructuredItem<T>>,
    expandedIds: Set<T>,
    onExpandedChange: (Set<T>) -> Unit,
    modifier: Modifier = Modifier,
) = Column(modifier) {
    items.forEach { item -> FzTreeNode(item, expandedIds, onExpandedChange, 0) }
}

@Composable
private fun <T> FzTreeNode(item: FzStructuredItem<T>, expanded: Set<T>, onChange: (Set<T>) -> Unit, depth: Int) {
    val isExpanded = item.id in expanded
    Row(Modifier.fillMaxWidth().clickable(enabled = item.children.isNotEmpty()) { onChange(if (isExpanded) expanded - item.id else expanded + item.id) }.padding(start = (depth * 16).dp, top = 8.dp, bottom = 8.dp)) {
        if (item.children.isNotEmpty()) Text(if (isExpanded) "−" else "+")
        Spacer(Modifier.width(FaraZamanThemeTokens.spacing.sm)); Text(item.title)
    }
    if (isExpanded) item.children.forEach { FzTreeNode(it, expanded, onChange, depth + 1) }
}

@Composable
fun FzStepper(items: List<FzStructuredItem<String>>, activeStep: Int, modifier: Modifier = Modifier) = Row(modifier.fillMaxWidth()) {
    items.forEachIndexed { index, item ->
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(28.dp).background(if (index <= activeStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) { Text((index + 1).toString(), color = if (index <= activeStep) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant) }
            Text(item.title, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

@Composable
fun FzHighlightedText(text: String, query: String, modifier: Modifier = Modifier, highlightColor: Color = MaterialTheme.colorScheme.tertiaryContainer) {
    val annotated: AnnotatedString = buildAnnotatedString {
        append(text)
        if (query.isNotBlank()) {
            var start = text.indexOf(query, ignoreCase = true)
            while (start >= 0) { addStyle(SpanStyle(background = highlightColor), start, start + query.length); start = text.indexOf(query, start + query.length, ignoreCase = true) }
        }
    }
    Text(annotated, modifier)
}
