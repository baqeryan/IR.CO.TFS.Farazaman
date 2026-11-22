package Ir.co.tfs.farazaman.presentation.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class FzIntentColors(val container: Color, val content: Color)

@Composable
fun fzIntentColors(intent: FzSemanticIntent): FzIntentColors {
    val scheme = MaterialTheme.colorScheme
    val semantic = FaraZamanThemeTokens.semanticColors
    return when (intent) {
        FzSemanticIntent.Neutral -> FzIntentColors(scheme.surfaceVariant, scheme.onSurfaceVariant)
        FzSemanticIntent.Primary -> FzIntentColors(scheme.primary, scheme.onPrimary)
        FzSemanticIntent.Info -> FzIntentColors(semantic.info, semantic.onInfo)
        FzSemanticIntent.Success -> FzIntentColors(semantic.success, semantic.onSuccess)
        FzSemanticIntent.Warning -> FzIntentColors(semantic.warning, semantic.onWarning)
        FzSemanticIntent.Error -> FzIntentColors(scheme.error, scheme.onError)
    }
}

