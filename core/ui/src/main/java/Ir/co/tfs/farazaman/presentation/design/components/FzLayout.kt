package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import Ir.co.tfs.farazaman.presentation.design.theme.FzOrientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class FzBreakpoint { Compact, Medium, Expanded }

@Immutable
data class FzGridItem(val span: Int = 1, val content: @Composable () -> Unit)

@Composable
fun FzResponsive(
    modifier: Modifier = Modifier,
    compact: @Composable BoxScope.() -> Unit,
    medium: (@Composable BoxScope.() -> Unit)? = null,
    expanded: (@Composable BoxScope.() -> Unit)? = null,
) {
    BoxWithConstraints(modifier) {
        when {
            maxWidth < 600.dp -> compact()
            maxWidth < 840.dp -> (medium ?: compact)()
            else -> (expanded ?: medium ?: compact)()
        }
    }
}

@Composable
fun FzContainer(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = FaraZamanThemeTokens.spacing.md),
    maxWidth: Dp = 1200.dp,
    content: @Composable BoxScope.() -> Unit,
) = Box(modifier.fillMaxWidth().padding(contentPadding), contentAlignment = androidx.compose.ui.Alignment.TopCenter) {
    Box(Modifier.fillMaxWidth().widthIn(max = maxWidth), content = content)
}

@Composable
fun FzRow(
    modifier: Modifier = Modifier,
    spacing: Dp = FaraZamanThemeTokens.spacing.sm,
    content: @Composable RowScope.() -> Unit,
) = Row(modifier, horizontalArrangement = Arrangement.spacedBy(spacing), content = content)

@Composable
fun FzColumn(
    modifier: Modifier = Modifier,
    spacing: Dp = FaraZamanThemeTokens.spacing.sm,
    content: @Composable ColumnScope.() -> Unit,
) = Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing), content = content)

@Composable
fun FzSpacer(size: Dp = FaraZamanThemeTokens.spacing.md) = Spacer(Modifier.size(size))

@Composable
fun FzGrid(
    columns: Int,
    items: List<FzGridItem>,
    modifier: Modifier = Modifier,
    spacing: Dp = FaraZamanThemeTokens.spacing.sm,
) {
    require(columns > 0) { "columns must be positive" }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing)) {
        items.chunked(columns).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing)) {
                rowItems.forEach { item -> Box(Modifier.weight(item.span.coerceAtLeast(1).toFloat())) { item.content() } }
                repeat((columns - rowItems.size).coerceAtLeast(0)) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun FzStack(
    orientation: FzOrientation,
    modifier: Modifier = Modifier,
    spacing: Dp = FaraZamanThemeTokens.spacing.sm,
    content: @Composable () -> Unit,
) {
    if (orientation == FzOrientation.Horizontal) Row(modifier, horizontalArrangement = Arrangement.spacedBy(spacing)) { content() }
    else Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing)) { content() }
}
