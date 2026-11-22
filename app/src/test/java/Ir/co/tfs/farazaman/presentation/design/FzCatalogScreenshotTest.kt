package Ir.co.tfs.farazaman.presentation.design

import Ir.co.tfs.farazaman.presentation.design.catalog.FzCatalogScreen
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.design.theme.ThemeConfiguration
import Ir.co.tfs.farazaman.presentation.design.theme.ThemeMode
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FzCatalogScreenshotTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun lightRtlAtCompactWidth() = captureCatalog(ThemeMode.Light, 1f)

    @Test fun darkRtlAtCompactWidth() = captureCatalog(ThemeMode.Dark, 1f)

    @Test fun lightRtlAtLargeFont() = captureCatalog(ThemeMode.Light, 2f)

    @Test fun darkRtlAtLargeFont() = captureCatalog(ThemeMode.Dark, 2f)

    private fun captureCatalog(mode: ThemeMode, fontScale: Float) {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = fontScale)) {
                FaraZamanTheme(ThemeConfiguration(mode = mode)) {
                    FzCatalogScreen()
                }
            }
        }
        composeRule.onNodeWithText("کاتالوگ رابط فرازمان").assertIsDisplayed()
        composeRule.onNodeWithText("کنترل‌ها").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }
}
