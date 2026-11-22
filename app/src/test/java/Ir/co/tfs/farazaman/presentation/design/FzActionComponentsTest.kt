package Ir.co.tfs.farazaman.presentation.design

import Ir.co.tfs.farazaman.presentation.design.components.FzChip
import Ir.co.tfs.farazaman.presentation.design.components.FzRating
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertIsSelected
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
class FzActionComponentsTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun selectedChipAndRatingExposeRtlVisualState() {
        composeRule.setContent {
            FaraZamanTheme { Column { FzChip("فعال", true, {}); FzRating(3f, {}) } }
        }
        composeRule.onNodeWithText("فعال").assertIsSelected()
        composeRule.onRoot().captureRoboImage()
    }
}
