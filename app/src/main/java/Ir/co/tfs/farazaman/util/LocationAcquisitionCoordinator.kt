package Ir.co.tfs.farazaman.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import Ir.co.tfs.farazaman.domain.location.LocationFix
import Ir.co.tfs.farazaman.domain.location.LocationFixPolicy
import Ir.co.tfs.farazaman.domain.location.LocationFixPolicy.isUsable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

sealed interface LocationAcquisitionResult {
    data class Available(val fix: LocationFix) : LocationAcquisitionResult
    data object PermissionMissing : LocationAcquisitionResult
    data object ServicesDisabled : LocationAcquisitionResult
    data object Unavailable : LocationAcquisitionResult
}

class LocationAcquisitionCoordinator internal constructor(
    private val applicationContext: Context,
    private val locationManager: LocationManager?,
    private val permissionChecker: () -> Boolean,
    private val freshTimeoutMillis: Long = DEFAULT_FRESH_TIMEOUT_MILLIS,
) {
    constructor(
        context: Context,
        freshTimeoutMillis: Long = DEFAULT_FRESH_TIMEOUT_MILLIS,
    ) : this(
        applicationContext = context.applicationContext,
        locationManager = context.applicationContext
            .getSystemService(Context.LOCATION_SERVICE) as? LocationManager,
        permissionChecker = {
            ContextCompat.checkSelfPermission(
                context.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context.applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED
        },
        freshTimeoutMillis = freshTimeoutMillis,
    )

    suspend fun acquire(): LocationAcquisitionResult {
        if (!permissionChecker()) return LocationAcquisitionResult.PermissionMissing
        val manager = locationManager ?: return LocationAcquisitionResult.Unavailable
        val enabledProviders = ENABLED_PROVIDERS.filter { provider ->
            runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false)
        }
        if (enabledProviders.isEmpty()) return LocationAcquisitionResult.ServicesDisabled

        val fresh = withTimeoutOrNull(freshTimeoutMillis) {
            awaitFreshLocation(manager, enabledProviders)
        }
        val selected = LocationFixPolicy.select(
            fresh = fresh,
            lastKnown = GpsAccuracyHelper.lastKnownLocations(applicationContext),
        )
        return selected?.let(LocationAcquisitionResult::Available)
            ?: LocationAcquisitionResult.Unavailable
    }

    @SuppressLint("MissingPermission")
    private suspend fun awaitFreshLocation(
        manager: LocationManager,
        enabledProviders: List<String>,
    ): Location? = suspendCancellableCoroutine { continuation ->
        var listener: LocationListener? = null
        var registeredProviderCount = 0

        fun removeListener() {
            listener?.let { registeredListener ->
                runCatching { manager.removeUpdates(registeredListener) }
            }
        }

        val registeredListener = LocationListener { location ->
            if (location.isUsable() && continuation.isActive) {
                removeListener()
                continuation.resume(location)
            }
        }
        listener = registeredListener
        continuation.invokeOnCancellation { removeListener() }

        enabledProviders.forEach { provider ->
            val registered = runCatching {
                manager.requestLocationUpdates(
                    provider,
                    0L,
                    0f,
                    registeredListener,
                    Looper.getMainLooper(),
                )
            }.isSuccess
            if (registered) registeredProviderCount++
        }
        if (registeredProviderCount == 0 && continuation.isActive) {
            removeListener()
            continuation.resume(null)
        }
    }

    companion object {
        const val DEFAULT_FRESH_TIMEOUT_MILLIS = 10_000L
        private val ENABLED_PROVIDERS = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
        )
    }
}
