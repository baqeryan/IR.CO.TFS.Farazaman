package Ir.co.tfs.farazaman.presentation.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

enum class FzChartType { Line, Bar, Pie, Donut, Sparkline }

@Immutable
data class FzChartPoint(val label: String, val value: Float)

@Composable
fun FzChart(points: List<FzChartPoint>, type: FzChartType, modifier: Modifier = Modifier, colors: List<Color>? = null) {
    val scheme = androidx.compose.material3.MaterialTheme.colorScheme
    val palette = colors ?: listOf(scheme.primary, scheme.secondary, scheme.tertiary, scheme.error)
    Canvas(modifier.fillMaxWidth().height(180.dp)) {
        if (points.isEmpty()) return@Canvas
        val max = points.maxOf { it.value }.coerceAtLeast(1f)
        when (type) {
            FzChartType.Line, FzChartType.Sparkline -> {
                val path = Path()
                points.forEachIndexed { index, point ->
                    val x = if (points.size == 1) size.width / 2 else index * size.width / (points.size - 1)
                    val y = size.height - point.value / max * size.height
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, palette.first(), style = Stroke(if (type == FzChartType.Sparkline) 4f else 6f, cap = StrokeCap.Round))
            }
            FzChartType.Bar -> {
                val slot = size.width / points.size
                points.forEachIndexed { index, point ->
                    val barHeight = point.value / max * size.height
                    drawRect(palette[index % palette.size], Offset(index * slot + slot * 0.15f, size.height - barHeight), Size(slot * 0.7f, barHeight))
                }
            }
            FzChartType.Pie, FzChartType.Donut -> {
                val total = points.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)
                var start = -90f
                points.forEachIndexed { index, point ->
                    val sweep = point.value / total * 360f
                    drawArc(palette[index % palette.size], start, sweep, type == FzChartType.Pie, style = if (type == FzChartType.Donut) Stroke(size.minDimension * 0.18f) else Fill)
                    start += sweep
                }
            }
        }
    }
}
