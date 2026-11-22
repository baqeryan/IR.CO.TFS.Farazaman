package Ir.co.tfs.farazaman.presentation.design

import Ir.co.tfs.farazaman.presentation.design.components.FzAlert
import Ir.co.tfs.farazaman.presentation.design.components.FzDialog
import Ir.co.tfs.farazaman.presentation.design.components.FzSkeleton
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.design.theme.ThemeConfiguration
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FzFeedbackComponentsTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun dialogDismissCallbackRunsExactlyOnce() {
        var dismissCount = 0
        composeRule.setContent {
            FaraZamanTheme {
                FzDialog(true, "تأیید", "بله", {}, { dismissCount++ }, message = "متن پیام", dismissLabel = "انصراف")
            }
        }
        composeRule.onNodeWithText("انصراف").performClick()
        composeRule.runOnIdle { assertEquals(1, dismissCount) }
    }

    @Test fun reducedMotionFeedbackRendersInRtl() {
        composeRule.setContent {
            FaraZamanTheme(ThemeConfiguration(reduceMotion = true)) {
                Column { FzAlert("هشدار", message = "دقت GPS پایین است"); FzSkeleton() }
            }
        }
        composeRule.onNodeWithText("هشدار").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }
}
