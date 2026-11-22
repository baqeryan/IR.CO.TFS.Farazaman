package Ir.co.tfs.farazaman.presentation.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

val FzShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(percent = 50),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Immutable data class FzExtendedShapes(val curvedHeader: RoundedCornerShape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
