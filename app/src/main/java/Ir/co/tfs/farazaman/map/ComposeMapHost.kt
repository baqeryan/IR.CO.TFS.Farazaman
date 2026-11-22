package Ir.co.tfs.farazaman.map

import android.os.Bundle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.maplibre.android.maps.MapView

/**
 * Lifecycle boundary for the native MapLibre surface used by migrated Compose routes.
 * Map loading, overlays, permissions and location remain responsibilities of the route.
 */
@Composable
fun ComposeMapHost(
    modifier: Modifier = Modifier,
    savedInstanceState: Bundle? = null,
    keepScreenOn: Boolean = true,
    onMapViewReady: (MapView) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember(context) {
        MapView(context).apply {
            this.keepScreenOn = keepScreenOn
            onCreate(savedInstanceState)
            onMapViewReady(this)
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
    )

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            if (lifecycle.currentState != Lifecycle.State.DESTROYED) mapView.onDestroy()
        }
    }
}

/** A network- and MapLibre-independent renderer for previews and deterministic screenshots. */
@Composable
fun FakeComposeMapHost(
    modifier: Modifier = Modifier,
    routeColor: Color = Color(0xFF006C4C),
) {
    Box(modifier = modifier.background(Color(0xFFE5EDE9))) {
        Canvas(Modifier.fillMaxSize()) {
            val grid = Color(0xFFB8C8C0)
            repeat(5) { index ->
                val fraction = (index + 1) / 6f
                drawLine(grid, Offset(size.width * fraction, 0f), Offset(size.width * fraction, size.height))
                drawLine(grid, Offset(0f, size.height * fraction), Offset(size.width, size.height * fraction))
            }
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width * .12f, size.height * .78f)
                cubicTo(
                    size.width * .28f,
                    size.height * .55f,
                    size.width * .62f,
                    size.height * .68f,
                    size.width * .86f,
                    size.height * .25f,
                )
            }
            drawPath(path, routeColor, style = Stroke(width = 8f, cap = StrokeCap.Round))
            drawCircle(Color(0xFFB3261E), radius = 12f, center = Offset(size.width * .86f, size.height * .25f))
        }
    }
}
