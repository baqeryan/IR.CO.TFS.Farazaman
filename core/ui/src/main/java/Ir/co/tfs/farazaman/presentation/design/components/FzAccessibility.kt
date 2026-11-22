package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Modifier.fzMinimumTouchTarget(): Modifier = sizeIn(
    minWidth = FaraZamanThemeTokens.sizes.minimumTouchTarget,
    minHeight = FaraZamanThemeTokens.sizes.minimumTouchTarget,
)

