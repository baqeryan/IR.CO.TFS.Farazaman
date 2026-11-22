package Ir.co.tfs.farazaman.presentation.design

import Ir.co.tfs.farazaman.presentation.design.components.FzBottomNavigation
import Ir.co.tfs.farazaman.presentation.design.components.FzNavigationItem
import Ir.co.tfs.farazaman.presentation.design.components.FzResponsive
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FzLayoutComponentsTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun compactRtlNavigationDoesNotHideSelectedLabel() {
        composeRule.setContent {
            FaraZamanTheme {
                Column(Modifier.width(360.dp)) {
                    FzResponsive(compact = { Text("فشرده") }, expanded = { Text("گسترده") })
                    FzBottomNavigation(
                        listOf(FzNavigationItem("home", "خانه"), FzNavigationItem("today", "امروز")),
                        "today",
                        {},
                    ) { item, _ -> Text(item.label.take(1)) }
                }
            }
        }
        composeRule.onNodeWithText("فشرده").assertIsDisplayed()
        composeRule.onNodeWithText("امروز").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage()
    }
}
