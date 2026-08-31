package Ir.co.tfs.farazaman.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class AppUpdateJsonParserTest {
    private val parser = AppUpdateJsonParser()

    @Test
    fun `valid response is parsed`() {
        val info = parser.parse(
            """
            {
              "versionCode": 123,
              "versionName": "1.2.3",
              "downloadUrl": "https://ftp.tfs.co.ir/farazaman.apk",
              "mandatory": false,
              "releaseNotes": "Bug fixes",
              "publishedAt": "2026-08-31T00:00:00Z"
            }
            """.trimIndent(),
        )

        assertEquals(123L, info.versionCode)
        assertFalse(info.mandatory)
        assertEquals("Bug fixes", info.releaseNotes)
    }

    @Test
    fun `html or missing fields are rejected as malformed`() {
        assertThrows(Exception::class.java) { parser.parse("<html>not json</html>") }
        assertThrows(IllegalArgumentException::class.java) {
            parser.parse("""{"versionCode":123}""")
        }
    }

    @Test
    fun `non https or untrusted APK host is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            parser.parse(responseWithUrl("http://ftp.tfs.co.ir/farazaman.apk"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            parser.parse(responseWithUrl("https://example.com/farazaman.apk"))
        }
    }

    private fun responseWithUrl(url: String) =
        """{"versionCode":123,"versionName":"1.2.3","downloadUrl":"$url","mandatory":false}"""
}
