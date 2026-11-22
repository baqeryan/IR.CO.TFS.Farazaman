package Ir.co.tfs.farazaman.support

import Ir.co.tfs.farazaman.data.model.AppNotificationDto
import Ir.co.tfs.farazaman.presentation.compose.NotificationsScreen
import Ir.co.tfs.farazaman.presentation.compose.NotificationsUiEvent
import Ir.co.tfs.farazaman.presentation.compose.NotificationsUiState
import Ir.co.tfs.farazaman.presentation.compose.SupervisorProfileEvent
import Ir.co.tfs.farazaman.presentation.compose.SupervisorProfileScreen
import Ir.co.tfs.farazaman.presentation.compose.SupervisorProfileUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SupportScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val notification = AppNotificationDto(
        id = 125,
        title = "ماموریت جدید برای شما ثبت شد",
        createDate = "2026-09-10T10:00:00+03:30",
    )

    @Test
    fun unreadNotificationExposesOpenAndDeleteActions() {
        val events = mutableListOf<NotificationsUiEvent>()
        composeRule.setContent {
            FaraZamanTheme {
                NotificationsScreen(
                    NotificationsUiState(items = listOf(notification), hasMore = false),
                    events::add,
                )
            }
        }

        composeRule.onNodeWithText("ماموریت جدید برای شما ثبت شد").assertIsDisplayed()
        composeRule.onNodeWithText("مشاهده (جدید)").performClick()
        composeRule.onNodeWithText("حذف").performClick()
        composeRule.runOnIdle {
            assertEquals(NotificationsUiEvent.Open(notification), events[0])
            assertEquals(NotificationsUiEvent.Delete(notification), events[1])
        }
    }

    @Test
    fun profileHidesChangeRoleWhenUserHasSingleRole() {
        composeRule.setContent {
            FaraZamanTheme {
                SupervisorProfileScreen(
                    SupervisorProfileUiState("کاربر ناظر", "supervisor", "ناظر قرارداد", false),
                    {},
                )
            }
        }

        composeRule.onNodeWithText("کاربر ناظر").assertIsDisplayed()
        composeRule.onAllNodesWithText("تغییر نقش").assertCountEquals(0)
        composeRule.onNodeWithText("خروج").assertIsDisplayed()
    }

    @Test
    fun profileEmitsChangeRoleEventWhenAllowed() {
        var event: SupervisorProfileEvent? = null
        composeRule.setContent {
            FaraZamanTheme {
                SupervisorProfileScreen(
                    SupervisorProfileUiState("مدیر سامانه", "admin", "مدیر", true),
                    { event = it },
                )
            }
        }

        composeRule.onNodeWithText("تغییر نقش").performClick()
        composeRule.runOnIdle { assertEquals(SupervisorProfileEvent.ChangeRole, event) }
    }
}
