package Ir.co.tfs.farazaman.util

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import Ir.co.tfs.farazaman.domain.location.LocationFix
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

class LocationAcquisitionCoordinatorTest {

    @Test
    fun `enabled provider returns fresh fix`() = runBlocking {
        val locationManager = locationManager(gpsEnabled = true, networkEnabled = false)
        val fresh = location(36.56, 52.68, 8f)
        Mockito.doAnswer { invocation ->
            invocation.getArgument<LocationListener>(3).onLocationChanged(fresh)
            null
        }.`when`(locationManager).requestLocationUpdates(
            Mockito.eq(LocationManager.GPS_PROVIDER),
            Mockito.eq(0L),
            Mockito.eq(0f),
            ArgumentMatchers.any(LocationListener::class.java),
            ArgumentMatchers.nullable(Looper::class.java),
        )
        val coordinator = coordinator(locationManager, timeoutMillis = 100L)

        val result = coordinator.acquire()

        assertTrue(result is LocationAcquisitionResult.Available)
        assertTrue((result as LocationAcquisitionResult.Available).fix is LocationFix.Fresh)
        assertSame(fresh, result.fix.location)
    }

    @Test
    fun `fresh timeout falls back to best last known fix`() = runBlocking {
        val locationManager = locationManager(gpsEnabled = true, networkEnabled = true)
        val gps = location(36.56, 52.68, 9f)
        val network = location(36.57, 52.69, 25f)
        Mockito.`when`(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(gps)
        Mockito.`when`(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)).thenReturn(network)

        val result = coordinator(locationManager, timeoutMillis = 5L).acquire()

        assertTrue(result is LocationAcquisitionResult.Available)
        assertTrue((result as LocationAcquisitionResult.Available).fix is LocationFix.LastKnown)
        assertSame(gps, result.fix.location)
    }

    @Test
    fun `disabled providers return services disabled`() = runBlocking {
        val result = coordinator(
            locationManager(gpsEnabled = false, networkEnabled = false),
            timeoutMillis = 5L,
        ).acquire()

        assertTrue(result is LocationAcquisitionResult.ServicesDisabled)
    }

    @Test
    fun `cancelling acquisition removes registered listener`() = runBlocking {
        val locationManager = locationManager(gpsEnabled = true, networkEnabled = false)
        val coordinator = coordinator(locationManager, timeoutMillis = 5_000L)

        val job = launch { coordinator.acquire() }
        yield()
        job.cancelAndJoin()

        Mockito.verify(locationManager, Mockito.atLeastOnce())
            .removeUpdates(ArgumentMatchers.any(LocationListener::class.java))
    }

    @Test
    fun `missing permission exits before provider access`() = runBlocking {
        val context = Mockito.mock(Context::class.java)
        val manager = Mockito.mock(LocationManager::class.java)
        val coordinator = LocationAcquisitionCoordinator(
            applicationContext = context,
            locationManager = manager,
            permissionChecker = { false },
            freshTimeoutMillis = 5L,
        )

        assertTrue(coordinator.acquire() is LocationAcquisitionResult.PermissionMissing)
        Mockito.verifyNoInteractions(manager)
    }

    private fun coordinator(
        locationManager: LocationManager,
        timeoutMillis: Long,
    ): LocationAcquisitionCoordinator {
        val context = Mockito.mock(Context::class.java)
        Mockito.`when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(locationManager)
        return LocationAcquisitionCoordinator(
            applicationContext = context,
            locationManager = locationManager,
            permissionChecker = { true },
            freshTimeoutMillis = timeoutMillis,
        )
    }

    private fun locationManager(gpsEnabled: Boolean, networkEnabled: Boolean): LocationManager {
        val manager = Mockito.mock(LocationManager::class.java)
        Mockito.`when`(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(gpsEnabled)
        Mockito.`when`(manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(networkEnabled)
        return manager
    }

    private fun location(latitude: Double, longitude: Double, accuracy: Float): Location {
        val location = Mockito.mock(Location::class.java)
        Mockito.`when`(location.latitude).thenReturn(latitude)
        Mockito.`when`(location.longitude).thenReturn(longitude)
        Mockito.`when`(location.hasAccuracy()).thenReturn(true)
        Mockito.`when`(location.accuracy).thenReturn(accuracy)
        return location
    }
}
