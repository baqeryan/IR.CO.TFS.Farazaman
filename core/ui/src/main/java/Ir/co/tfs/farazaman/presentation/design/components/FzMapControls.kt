package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.core.ui.R
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

@Composable
fun FzMapControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onCenter: () -> Unit,
    modifier: Modifier = Modifier,
    onLayers: (() -> Unit)? = null,
    onRecordedPath: (() -> Unit)? = null,
    recordedPathVisible: Boolean = true,
    layersExpanded: Boolean = false,
    layerContent: (@Composable () -> Unit)? = null,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(FaraZamanThemeTokens.spacing.sm)) {
        onLayers?.let { onLayersClick ->
            Box {
                MapControl("لایه‌های نقشه", R.drawable.ic_fz_layers, onLayersClick)
                if (layersExpanded && layerContent != null) {
                    val gap = with(LocalDensity.current) { FaraZamanThemeTokens.spacing.sm.roundToPx() }
                    Popup(
                        popupPositionProvider = AboveAnchorPositionProvider(gap),
                        properties = PopupProperties(focusable = false),
                    ) { layerContent() }
                }
            }
        }
        onRecordedPath?.let { onRecordedPathClick ->
            MapControl(
                if (recordedPathVisible) "پنهان کردن مسیر GPX" else "نمایش مسیر GPX",
                R.drawable.ic_fz_route,
                onRecordedPathClick,
            )
        }
        MapControl("بزرگ‌نمایی", R.drawable.ic_fz_zoom_in, onZoomIn)
        MapControl("کوچک‌نمایی", R.drawable.ic_fz_zoom_out, onZoomOut)
        MapControl("موقعیت من", R.drawable.ic_fz_location, onCenter)
    }
}

private class AboveAnchorPositionProvider(private val gap: Int) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val preferredX = if (layoutDirection == LayoutDirection.Rtl) {
            // Controls sit on the physical left in RTL. Keep the popup aligned with
            // the layer button and let its wider content grow toward the map center.
            anchorBounds.left
        } else {
            anchorBounds.right - popupContentSize.width
        }
        val x = preferredX
            .coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
        val y = (anchorBounds.top - popupContentSize.height - gap).coerceAtLeast(0)
        return IntOffset(x, y)
    }
}

@Composable
private fun MapControl(description: String, icon: Int, onClick: () -> Unit) {
    FzIconButton(description, onClick) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(FaraZamanThemeTokens.sizes.iconMedium),
        )
    }
}
