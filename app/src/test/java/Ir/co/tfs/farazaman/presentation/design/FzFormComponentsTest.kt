package Ir.co.tfs.farazaman.presentation.design

import Ir.co.tfs.farazaman.presentation.design.components.FzNumericField
import Ir.co.tfs.farazaman.presentation.design.components.FzTextField
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FzFormComponentsTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun requiredAndInvalidFieldsExposePersianMessages() {
        composeRule.setContent {
            FaraZamanTheme {
                Column {
                    FzTextField("", {}, "نام معبر", required = true, errorText = "این فیلد الزامی است")
                    FzNumericField("12", {}, "عرض معبر")
                }
            }
        }
        composeRule.onNodeWithText("نام معبر *").assertIsDisplayed()
        composeRule.onNodeWithText("این فیلد الزامی است").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }
}
