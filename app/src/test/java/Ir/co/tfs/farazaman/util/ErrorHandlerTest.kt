package Ir.co.tfs.farazaman.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorHandlerTest {
    @Test
    fun `400 plain text body is returned`() {
        assertEquals(
            "خطای 400: مقدار واردشده معتبر نیست",
            ErrorHandler.messageFromBody(400, "مقدار واردشده معتبر نیست", "خطا"),
        )
    }

    @Test
    fun `400 validation errors are flattened`() {
        val message = ErrorHandler.messageFromBody(
            400,
            """{"title":"Validation failed","errors":{"Number":["مقدار باید عددی باشد"]}}""",
            "خطا",
        )
        assertTrue(message.startsWith("خطای 400:"))
        assertTrue(message.contains("Validation failed"))
        assertTrue(message.contains("مقدار باید عددی باشد"))
    }

    @Test
    fun `non 400 response keeps fallback`() {
        assertEquals("خطای پیش‌فرض", ErrorHandler.messageFromBody(500, "server detail", "خطای پیش‌فرض"))
    }
}
