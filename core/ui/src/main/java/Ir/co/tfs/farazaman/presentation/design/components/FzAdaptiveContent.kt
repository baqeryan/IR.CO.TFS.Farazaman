package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> FzCarousel(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = FaraZamanThemeTokens.spacing.sm,
    key: ((T) -> Any)? = null,
    itemContent: @Composable (index: Int, item: T) -> Unit,
) {
    LazyRow(modifier, horizontalArrangement = Arrangement.spacedBy(itemSpacing)) {
        itemsIndexed(items, key = if (key == null) null else { _, item -> key(item) }) { index, item ->
            itemContent(index, item)
        }
    }
}

@Composable
fun FzSplitPanel(
    modifier: Modifier = Modifier,
    splitAt: Dp = 840.dp,
    primary: @Composable BoxScope.() -> Unit,
    secondary: @Composable BoxScope.() -> Unit,
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier) {
        if (maxWidth >= splitAt) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.md)) {
                Box(Modifier.weight(1f), content = primary)
                Box(Modifier.weight(1f), content = secondary)
            }
        } else {
            androidx.compose.foundation.layout.Column {
                Box(Modifier.fillMaxWidth(), content = primary)
                Box(Modifier.fillMaxWidth().padding(top = FaraZamanThemeTokens.spacing.md), content = secondary)
            }
        }
    }
}

@Composable
fun FzSwipeArea(
    onSwipeStart: () -> Unit,
    onSwipeEnd: () -> Unit,
    modifier: Modifier = Modifier,
    threshold: Dp = 48.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    var distance = 0f
    Box(
        modifier.pointerInput(threshold) {
            detectHorizontalDragGestures(
                onDragStart = { distance = 0f },
                onHorizontalDrag = { _, amount -> distance += amount },
                onDragEnd = {
                    when {
                        distance > threshold.toPx() -> onSwipeEnd()
                        distance < -threshold.toPx() -> onSwipeStart()
                    }
                },
            )
        },
        content = content,
    )
}
