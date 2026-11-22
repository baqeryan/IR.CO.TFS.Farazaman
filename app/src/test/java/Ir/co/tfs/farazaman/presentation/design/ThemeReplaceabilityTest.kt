package Ir.co.tfs.farazaman.presentation.design

import Ir.co.tfs.farazaman.presentation.design.theme.FzShapes
import Ir.co.tfs.farazaman.presentation.design.theme.FzSpacing
import Ir.co.tfs.farazaman.presentation.design.theme.ThemeConfiguration
import Ir.co.tfs.farazaman.presentation.design.theme.ThemeMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeReplaceabilityTest {
    @Test fun `configuration selects a replaceable mode without feature inputs`() {
        assertTrue(ThemeConfiguration(ThemeMode.Dark).mode == ThemeMode.Dark)
        assertFalse(ThemeConfiguration().dynamicColor)
    }

    @Test fun `tokens have semantic scales`() {
        val spacing = FzSpacing()
        assertTrue(spacing.xs < spacing.sm && spacing.sm < spacing.md && spacing.md < spacing.lg)
        assertTrue(FzShapes.medium != FzShapes.extraSmall)
    }
}
