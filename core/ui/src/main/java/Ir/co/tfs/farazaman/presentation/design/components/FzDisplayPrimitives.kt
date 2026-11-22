package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import Ir.co.tfs.farazaman.presentation.design.theme.FzComponentSize
import Ir.co.tfs.farazaman.presentation.design.theme.FzSemanticIntent
import Ir.co.tfs.farazaman.presentation.design.theme.fzIntentColors
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale

@Composable
fun FzSurface(
    modifier: Modifier = Modifier,
    intent: FzSemanticIntent = FzSemanticIntent.Neutral,
    content: @Composable () -> Unit,
) {
    val colors = fzIntentColors(intent)
    Surface(modifier = modifier, color = colors.container, contentColor = colors.content, shape = MaterialTheme.shapes.medium, content = content)
}

@Composable
fun FzDivider(modifier: Modifier = Modifier) = HorizontalDivider(modifier, color = FaraZamanThemeTokens.colors.uiBorder)

@Composable
fun FzAvatar(
    text: String,
    modifier: Modifier = Modifier,
    size: FzComponentSize = FzComponentSize.Medium,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Box(modifier.size(FaraZamanThemeTokens.sizes.buttonHeight(size)).clip(CircleShape).background(backgroundColor), contentAlignment = Alignment.Center) {
        Text(text.take(2), color = contentColor, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun FzBadge(
    badge: String?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) = BadgedBox(modifier = modifier, badge = { if (!badge.isNullOrEmpty()) Badge { Text(badge) } }, content = content)

@Composable
fun FzIcon(painter: Painter, contentDescription: String?, modifier: Modifier = Modifier, tint: Color = Color.Unspecified) =
    Icon(painter, contentDescription, modifier, tint)

@Composable
fun FzImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) = Image(painter, contentDescription, modifier, contentScale = contentScale)
