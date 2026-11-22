package Ir.co.tfs.farazaman.presentation.design

import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzStatusVariant
import Ir.co.tfs.farazaman.presentation.design.theme.FzDarkColors
import Ir.co.tfs.farazaman.presentation.design.theme.FzLightColors
import Ir.co.tfs.farazaman.presentation.design.theme.FzLightExpressiveColors
import Ir.co.tfs.farazaman.presentation.design.theme.FzDarkExpressiveColors
import Ir.co.tfs.farazaman.presentation.design.theme.FzSizes
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignTokenContractTest {
    @Test fun `interactive touch target is at least 48dp`() =
        assertTrue(FzSizes.minimumTouchTarget.value >= 48f)

    @Test fun `semantic variants do not expose raw styling parameters`() {
        assertEquals(setOf("Primary", "Secondary", "Danger", "Ghost"), FzButtonVariant.entries.map { it.name }.toSet())
        assertEquals(setOf("Default", "Info", "Success", "Warning", "Error"), FzStatusVariant.entries.map { it.name }.toSet())
        assertFalse(FzButtonVariant::class.java.declaredFields.any { it.type == Color::class.java })
    }

    @Test fun `core text pairs meet WCAG AA contrast`() {
        listOf(FzLightColors, FzDarkColors).forEach { colors ->
            assertTrue(contrast(colors.onBackground, colors.background) >= 4.5)
            assertTrue(contrast(colors.onSurface, colors.surface) >= 4.5)
            assertTrue(contrast(colors.onPrimary, colors.primary) >= 4.5)
        }
    }

    @Test fun `expressive palette keeps Farazaman identity in both themes`() {
        listOf(FzLightExpressiveColors, FzDarkExpressiveColors).forEach { colors ->
            assertTrue(colors.interactivePrimary.size >= 2)
            assertTrue(colors.decorativeGradient.size >= 3)
            assertTrue(colors.brand.green > colors.brand.red)
            assertTrue(colors.brand.green > colors.brand.blue)
        }
    }

    private fun contrast(foreground: Color, background: Color): Double {
        fun luminance(color: Color): Double {
            fun channel(value: Float): Double {
                val v = value.toDouble()
                return if (v <= 0.03928) v / 12.92 else Math.pow((v + 0.055) / 1.055, 2.4)
            }
            return 0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)
        }
        val first = luminance(foreground)
        val second = luminance(background)
        return (maxOf(first, second) + 0.05) / (minOf(first, second) + 0.05)
    }
}
