package Ir.co.tfs.farazaman.supervisor

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class BillSystemCodeExtractorTest {
    @Test
    fun `extracts lower-case code from nested response`() {
        val response = JSONObject(
            """{"billOriginCleaningItem":{"billCleaningItem":{"bilsystemcode":"120301"}}}""",
        )

        assertEquals("120301", response.extractBillSystemCode())
    }

    @Test
    fun `accepts bill spelling and mixed case`() {
        val response = JSONObject(
            """{"payload":{"billSystemCode":7654}}""",
        )

        assertEquals("7654", response.extractBillSystemCode())
    }
}
