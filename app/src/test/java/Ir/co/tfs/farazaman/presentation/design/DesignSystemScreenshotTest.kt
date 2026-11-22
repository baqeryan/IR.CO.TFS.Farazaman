package Ir.co.tfs.farazaman.presentation.design

import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DesignSystemScreenshotTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun foundationLightRtl() {
        composeRule.setContent { FaraZamanTheme { FzButton("تأیید", {}) } }
        composeRule.onRoot().captureRoboImage()
    }
}
