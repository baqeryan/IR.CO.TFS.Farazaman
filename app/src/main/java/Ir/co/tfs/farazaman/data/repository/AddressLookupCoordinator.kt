package Ir.co.tfs.farazaman.data.repository

import android.content.SharedPreferences
import android.os.SystemClock
import android.util.Log
import Ir.co.tfs.farazaman.data.model.RoadData
import Ir.co.tfs.farazaman.service.remote.RoadService
import Ir.co.tfs.farazaman.util.SelectedServerUrl
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
    private val preferences: SharedPreferences? = null,
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
            Log.e(
                TAG,
                "Address lookup timed out after ${REQUEST_TIMEOUT_MS} ms. " +
                    "Reproduce against the selected server (replace the token placeholder locally):\n" +
                    buildCurlCommand(latitude, longitude, buffer),
                timeout,
            )
            throw AddressLookupTimeoutException(timeout)
        }
    }

    private fun buildCurlCommand(latitude: Double, longitude: Double, buffer: Int): String {
        val endpoint = preferences?.let {
            SelectedServerUrl.apiUrl(it, ROAD_LOOKUP_PATH)
        } ?: ROAD_LOOKUP_PATH
        val url = "$endpoint?lat=$latitude&lng=$longitude&buffer=$buffer"
        return "curl --request GET --url '$url' " +
            "--header 'Accept: application/json' " +
            "--header 'Authorization: Bearer <BEARER_TOKEN>' " +
            "--connect-timeout 10 --max-time 30"
    }

    private companion object {
        const val TAG = "AddressLookup"
        const val ROAD_LOOKUP_PATH = "api/GisLayer/DataLayerRoadByPoint"
        const val REQUEST_TIMEOUT_MS = 2_000L
        const val TIMEOUT_COOLDOWN_MS = 10_000L
    }
}

class AddressLookupTimeoutException(cause: Throwable) : IOException("Address lookup timed out", cause)

class AddressLookupCooldownException : IOException("Address lookup is temporarily paused")
