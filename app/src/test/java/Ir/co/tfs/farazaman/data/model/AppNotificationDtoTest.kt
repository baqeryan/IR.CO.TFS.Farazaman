package Ir.co.tfs.farazaman.data.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNotificationDtoTest {
    @Test
    fun `missing LastModifiedTime means unread`() {
        val item = Gson().fromJson(
            """{"AppNotificationID":125,"Title":"test","CreateDate":"2026-09-06T14:30:00+03:30"}""",
            AppNotificationDto::class.java,
        )

        assertEquals(125, item.id)
        assertFalse(item.isRead)
    }

    @Test
    fun `present LastModifiedTime means read`() {
        val item = Gson().fromJson(
            """{"AppNotificationID":125,"Title":"test","CreateDate":"2026-09-06T14:30:00+03:30","LastModifiedTime":"2026-09-06T14:31:00+03:30"}""",
            AppNotificationDto::class.java,
        )

        assertTrue(item.isRead)
    }
}
