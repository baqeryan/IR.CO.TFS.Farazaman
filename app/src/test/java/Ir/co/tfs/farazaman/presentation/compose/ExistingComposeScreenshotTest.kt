package Ir.co.tfs.farazaman.presentation.compose

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
class ExistingComposeScreenshotTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun splashContractBaseline() {
        composeRule.setContent { Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme { SplashScreen(SplashUiState("فرازمان", animate = false)) } }
        composeRule.onRoot().captureRoboImage()
    }
}
