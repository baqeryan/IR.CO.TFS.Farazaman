package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class FzStatusVariant { Default, Info, Success, Warning, Error }

@Composable
fun FzStatusChip(text: String, modifier: Modifier = Modifier, variant: FzStatusVariant = FzStatusVariant.Default) {
    val semantic = FaraZamanThemeTokens.semanticColors
    val colors = when (variant) {
        FzStatusVariant.Default -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        FzStatusVariant.Info -> semantic.info to semantic.onInfo
        FzStatusVariant.Success -> semantic.success to semantic.onSuccess
        FzStatusVariant.Warning -> semantic.warning to semantic.onWarning
        FzStatusVariant.Error -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    }
    Surface(
        modifier = modifier.defaultMinSize(minHeight = 32.dp),
        color = colors.first,
        contentColor = colors.second,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
