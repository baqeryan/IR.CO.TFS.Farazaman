package Ir.co.tfs.farazaman.presentation.login

import android.webkit.WebView
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.input.pointer.pointerInteropFilter
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OidcLoginScreen(
    title: String,
    progress: Int,
    onHandleTouch: (Boolean) -> Unit,
    createWebView: (android.content.Context) -> WebView,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = spacing.md)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .pointerInteropFilter { event ->
                        when (event.actionMasked) {
                            MotionEvent.ACTION_DOWN -> onHandleTouch(true)
                            MotionEvent.ACTION_UP -> onHandleTouch(false)
                            // When the parent starts dragging it sends CANCEL to the handle;
                            // keep dragging enabled until BottomSheetBehavior settles.
                            MotionEvent.ACTION_CANCEL -> Unit
                        }
                        false
                    },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .width(80.dp)
                        .height(7.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)),
                )
            }
            Box(Modifier.fillMaxWidth().padding(bottom = spacing.sm)) {
                Text(
                    text = title,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (progress in 1..99) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            AndroidView(
                factory = createWebView,
                modifier = Modifier.fillMaxSize(),
                onRelease = { webView ->
                    webView.stopLoading()
                    webView.webChromeClient = null
                    webView.webViewClient = android.webkit.WebViewClient()
                    webView.destroy()
                },
            )
        }
    }
}
