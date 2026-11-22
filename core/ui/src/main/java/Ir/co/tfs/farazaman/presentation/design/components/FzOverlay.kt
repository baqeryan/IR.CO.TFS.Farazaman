package Ir.co.tfs.farazaman.presentation.design.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.unit.dp

@Composable
fun FzOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissible: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    if (!visible) return
    BackHandler(enabled = dismissible, onBack = onDismiss)
    Box(
        modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f))
            .clickable(enabled = dismissible, onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.clickable(onClick = {}), content = content)
    }
}

@Composable
fun FzPopover(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (visible) Popup(onDismissRequest = onDismiss, properties = PopupProperties(focusable = true)) {
        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 8.dp, content = content)
    }
}

@Composable
fun FzTooltip(text: String, visible: Boolean, onDismiss: () -> Unit = {}) {
    if (visible) Popup(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.inverseSurface) {
            Text(text, color = MaterialTheme.colorScheme.inverseOnSurface)
        }
    }
}
