package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp

@Composable
fun FzSkeleton(modifier: Modifier = Modifier, height: Dp = FaraZamanThemeTokens.sizes.buttonHeight) {
    val motion = FaraZamanThemeTokens.motion
    val alpha = if (motion.reduceMotion) 0.6f else {
        val transition = rememberInfiniteTransition(label = "fz-skeleton")
        val value by transition.animateFloat(0.35f, 0.75f, infiniteRepeatable(tween(motion.longMillis), RepeatMode.Reverse), label = "fz-skeleton-alpha")
        value
    }
    Box(modifier.fillMaxWidth().height(height).alpha(alpha).background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium))
}

@Composable
fun FzLinearProgress(progress: Float? = null, modifier: Modifier = Modifier) {
    if (progress == null) LinearProgressIndicator(modifier) else LinearProgressIndicator({ progress.coerceIn(0f, 1f) }, modifier)
}

@Composable
fun FzCircularProgress(progress: Float? = null, modifier: Modifier = Modifier) {
    if (progress == null) CircularProgressIndicator(modifier) else CircularProgressIndicator({ progress.coerceIn(0f, 1f) }, modifier)
}
