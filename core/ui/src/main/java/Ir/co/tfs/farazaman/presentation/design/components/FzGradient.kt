package Ir.co.tfs.farazaman.presentation.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.fzHorizontalGradient(colors: List<Color>): Modifier =
    background(Brush.horizontalGradient(colors))

fun Modifier.fzDiagonalGradient(colors: List<Color>): Modifier =
    background(Brush.linearGradient(colors))

fun Modifier.fzGradientBorder(
    colors: List<Color>,
    shape: Shape,
    width: Dp = 1.dp,
): Modifier = border(width = width, brush = Brush.linearGradient(colors), shape = shape)
