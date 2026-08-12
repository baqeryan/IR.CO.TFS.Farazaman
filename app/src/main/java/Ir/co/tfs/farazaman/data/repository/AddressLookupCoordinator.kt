package Ir.co.tfs.farazaman.data.repository

import android.os.SystemClock
import Ir.co.tfs.farazaman.data.model.RoadData
import Ir.co.tfs.farazaman.service.remote.RoadService
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressLookupCoordinator @Inject constructor(
    private val roadService: RoadService,
) {
    private val requestMutex = Mutex()

    @Volatile
    private var blockedUntilElapsedMs = 0L

    fun isCoolingDown(): Boolean = SystemClock.elapsedRealtime() < blockedUntilElapsedMs

    suspend fun getRoadData(
        latitude: Double,
        longitude: Double,
        buffer: Int,
    ): List<RoadData> = requestMutex.withLock {
        if (isCoolingDown()) {
            throw AddressLookupCooldownException()
        }

        try {
            withTimeout(REQUEST_TIMEOUT_MS) {
                roadService.getRoadData(latitude, longitude, buffer)
            }
        } catch (timeout: TimeoutCancellationException) {
            blockedUntilElapsedMs = SystemClock.elapsedRealtime() + TIMEOUT_COOLDOWN_MS
            throw AddressLookupTimeoutException(timeout)
        }
    }

    private companion object {
        const val REQUEST_TIMEOUT_MS = 2_000L
        const val TIMEOUT_COOLDOWN_MS = 10_000L
    }
}

class AddressLookupTimeoutException(cause: Throwable) : IOException("Address lookup timed out", cause)

class AddressLookupCooldownException : IOException("Address lookup is temporarily paused")
