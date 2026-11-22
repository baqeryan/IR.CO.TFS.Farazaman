package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.core.ui.R
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme

enum class FzHeaderSize(val height: Dp, val bodyOffset: Dp) {
    Compact(168.dp, 136.dp),
    Standard(220.dp, 184.dp),
    Expanded(260.dp, 220.dp),
}

/** Common application chrome. It deliberately has no bottom navigation. */
@Composable
fun FzPageTemplate(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    headerSize: FzHeaderSize = FzHeaderSize.Standard,
    onBack: (() -> Unit)? = null,
    headerStart: @Composable () -> Unit = {},
    headerEnd: @Composable () -> Unit = {},
    headerBelowTitle: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val spacing = FaraZamanThemeTokens.spacing
    Box(
        modifier.fillMaxSize().background(
            Brush.verticalGradient(FaraZamanThemeTokens.colors.decorativeGradient),
        ),
    ) {
        Image(
            painter = painterResource(R.drawable.home_header_curved),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxWidth().height(headerSize.height),
        )
        Box(Modifier.fillMaxWidth().height(headerSize.bodyOffset).padding(horizontal = spacing.md, vertical = spacing.md)) {
            Box(
                Modifier.fillMaxWidth().heightIn(min = FaraZamanThemeTokens.sizes.minimumTouchTarget)
                    .align(Alignment.TopCenter),
            ) {
                if (onBack != null) FzBackButton(onBack, Modifier.align(Alignment.CenterStart))
                if (onBack == null) Box(Modifier.align(Alignment.CenterStart)) { headerStart() }
                Box(Modifier.align(Alignment.CenterEnd)) { headerEnd() }
                Text(
                    title,
                    modifier = Modifier.align(Alignment.Center),
                    color = FaraZamanThemeTokens.colors.onDecorative,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
            subtitle?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    color = FaraZamanThemeTokens.colors.onDecorative.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.TopCenter)
                        .padding(top = FaraZamanThemeTokens.sizes.minimumTouchTarget + spacing.sm),
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = FaraZamanThemeTokens.sizes.minimumTouchTarget + spacing.sm),
            ) {
                headerBelowTitle()
            }
        }
        Surface(
            Modifier.fillMaxSize()
                .padding(top = headerSize.bodyOffset)
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)),
            color = FaraZamanThemeTokens.colors.uiBackground,
            contentColor = FaraZamanThemeTokens.colors.textPrimary,
        ) { content(PaddingValues(start = spacing.sm, top = spacing.lg, end = spacing.sm)) }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, locale = "fa")
@Composable
private fun FzPageTemplatePreview() {
    FaraZamanTheme {
        FzPageTemplate(title = "عنوان صفحه", subtitle = "توضیح کوتاه صفحه") { padding ->
            Text("محتوای صفحه", modifier = Modifier.padding(padding))
        }
    }
}
