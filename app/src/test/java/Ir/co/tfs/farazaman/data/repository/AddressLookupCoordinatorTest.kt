package Ir.co.tfs.farazaman.data.repository

import Ir.co.tfs.farazaman.data.model.RoadData
import Ir.co.tfs.farazaman.service.remote.RoadService
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class AddressLookupCoordinatorTest {
    @Test(timeout = 5_000L)
    fun timeoutStartsCooldownAndPreventsAnotherNetworkCall() = runBlocking {
        var networkCalls = 0
        val slowService = object : RoadService {
            override suspend fun getRoadData(
                latitude: Double,
                longitude: Double,
                buffer: Int,
            ): List<RoadData> {
                networkCalls++
                delay(10_000L)
                return emptyList()
            }
        }
        val coordinator = AddressLookupCoordinator(slowService)

        try {
            coordinator.getRoadData(35.7, 51.4, 30)
            fail("Expected address timeout")
        } catch (_: AddressLookupTimeoutException) {
            // Expected.
        }

        try {
            coordinator.getRoadData(35.7, 51.4, 30)
            fail("Expected cooldown")
        } catch (_: AddressLookupCooldownException) {
            // Expected: no second network request is started.
        }

        assertEquals(1, networkCalls)
    }
}
