package Ir.co.tfs.farazaman.presentation.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class FzComponentSizes(
    val minimumTouchTarget: Dp = 48.dp,
    val buttonHeight: Dp = 52.dp,
    val iconSmall: Dp = 18.dp,
    val iconMedium: Dp = 24.dp,
    val iconLarge: Dp = 32.dp,
    val cardLeading: Dp = 56.dp,
    val sheetListMaxHeight: Dp = 360.dp,
    val logoLarge: Dp = 88.dp,
) {
    fun buttonHeight(size: FzComponentSize): Dp = when (size) {
        FzComponentSize.Small -> minimumTouchTarget
        FzComponentSize.Medium -> buttonHeight
        FzComponentSize.Large -> 60.dp
    }

    fun iconSize(size: FzComponentSize): Dp = when (size) {
        FzComponentSize.Small -> iconSmall
        FzComponentSize.Medium -> iconMedium
        FzComponentSize.Large -> iconLarge
    }

    fun chipHeight(size: FzComponentSize): Dp = when (size) {
        FzComponentSize.Small -> 28.dp
        FzComponentSize.Medium -> 32.dp
        FzComponentSize.Large -> 40.dp
    }
}

val FzSizes = FzComponentSizes()
