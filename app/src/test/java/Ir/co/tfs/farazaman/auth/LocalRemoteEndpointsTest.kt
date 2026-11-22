package Ir.co.tfs.farazaman.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LocalRemoteEndpointsTest {
    @Test
    fun `builds authorization and token endpoints from selected system`() {
        val endpoints = LocalRemoteEndpoints.fromBaseAddress("https://municipality.example.ir/root/")

        assertEquals(
            "https://municipality.example.ir/root/Authorize",
            endpoints.authorizationEndpoint.toString(),
        )
        assertEquals(
            "https://municipality.example.ir/root/api/token",
            endpoints.tokenEndpoint.toString(),
        )
    }

    @Test
    fun `rejects non https base address`() {
        assertThrows(IllegalArgumentException::class.java) {
            LocalRemoteEndpoints.fromBaseAddress("http://municipality.example.ir")
        }
    }

    @Test
    fun `rejects base address with query`() {
        assertThrows(IllegalArgumentException::class.java) {
            LocalRemoteEndpoints.fromBaseAddress("https://municipality.example.ir?next=evil")
        }
    }
}
