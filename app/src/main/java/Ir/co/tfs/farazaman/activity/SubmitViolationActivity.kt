package Ir.co.tfs.farazaman.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import Ir.co.tfs.farazaman.OSMTracker
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.data.model.RoadData
import Ir.co.tfs.farazaman.presentation.viewmodel.SubmitViolationViewModel
import Ir.co.tfs.farazaman.util.AddressLocationHelper
import Ir.co.tfs.farazaman.map.BaranMapStyle
import Ir.co.tfs.farazaman.map.MapLibreOverlayController
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import Ir.co.tfs.farazaman.data.db.TrackContentProvider
import Ir.co.tfs.farazaman.map.ComposeMapHost
import Ir.co.tfs.farazaman.map.MapRoadNameResolver
import Ir.co.tfs.farazaman.presentation.compose.ViolationMapEvent
import Ir.co.tfs.farazaman.presentation.compose.ViolationMapScreen
import Ir.co.tfs.farazaman.presentation.compose.ViolationMapUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme

@AndroidEntryPoint
class SubmitViolationActivity : AppCompatActivity() {
    private lateinit var locationManager: LocationManager
    private lateinit var prefs: SharedPreferences
    private lateinit var mapView: MapView
    private var mapLibreMap: MapLibreMap? = null
    private var mapOverlays: MapLibreOverlayController? = null
    private var centerToGpsPos = true
    private var currentPosition = LatLng(35.7627, 51.3353)
    private var zoomedToTrackAlready = false
    private var trackId: Long = -1
    private var displayedTrackPoints: List<LatLng> = emptyList()
    private var lastRoadAddressCoordinates: Pair<Double, Double>? = null
    private var receivingLocationUpdates = false
    private var composeOwnsMapLifecycle = true

    // ViewModel
    private val viewModel: SubmitViolationViewModel by viewModels()

    private val mapUiState = mutableStateOf(ViolationMapUiState())

    companion object {
        private const val TAG = "SubmitViolationActivity"
        private const val DEFAULT_ZOOM = 16
        private const val CURRENT_CENTER_TO_GPS_POS = "currentCenterToGpsPos"
        private const val CURRENT_LATITUDE = "currentLatitude"
        private const val CURRENT_LONGITUDE = "currentLongitude"
        private const val CENTER_DEFAULT_ZOOM_LEVEL = 18.0
        private const val CURRENT_ZOOM = "currentZoom"
        private const val CURRENT_ZOOMED_TO_TRACK = "currentZoomedToTrack"
        private const val LAST_ZOOM = "lastZoomLevel"
        private const val ANIMATION_DURATION_MS = 1000L
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val EXTRA_TRACK_ID = "track_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        trackId = intent.getLongExtra(EXTRA_TRACK_ID, -1)
        initializePreferences()
        val keepScreenOn = prefs.getBoolean(OSMTracker.Preferences.KEY_UI_DISPLAY_KEEP_ON, OSMTracker.Preferences.VAL_UI_DISPLAY_KEEP_ON)
        restoreMapState(savedInstanceState)
        setContent {
            FaraZamanTheme {
                ViolationMapScreen(
                    state = mapUiState.value,
                    onEvent = ::handleMapEvent,
                    map = {
                        ComposeMapHost(
                            modifier = Modifier.fillMaxSize(),
                            savedInstanceState = savedInstanceState,
                            keepScreenOn = keepScreenOn,
                            onMapViewReady = { createdMap ->
                                mapView = createdMap
                                configureMap(savedInstanceState)
                            },
                        )
                    },
                )
            }
        }
        initializeLocationManager()
        setupViewModelObservers()
        
        if (trackId != -1L) {
            displayTrackPoints()
        }
    }

    private fun handleMapEvent(event: ViolationMapEvent) {
        when (event) {
            ViolationMapEvent.Back -> finish()
            ViolationMapEvent.ZoomIn -> mapOverlays?.zoomIn()
            ViolationMapEvent.ZoomOut -> mapOverlays?.zoomOut()
            ViolationMapEvent.Center -> {
                centerToGpsPos = true
                mapOverlays?.center(currentPosition, CENTER_DEFAULT_ZOOM_LEVEL, ANIMATION_DURATION_MS.toInt())
            }
            ViolationMapEvent.Submit -> startActivity(Intent(this, SubmitViolationFormActivity::class.java))
        }
    }

    private fun initializeLocationManager() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (hasLocationPermission()) {
            startGettingLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun initializePreferences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
    }

    private fun restoreMapState(savedInstanceState: Bundle?) {
        centerToGpsPos = savedInstanceState?.getBoolean(
            CURRENT_CENTER_TO_GPS_POS,
            centerToGpsPos,
        ) ?: centerToGpsPos
        zoomedToTrackAlready = savedInstanceState?.getBoolean(
            CURRENT_ZOOMED_TO_TRACK,
            zoomedToTrackAlready,
        ) ?: zoomedToTrackAlready

    }

    private fun configureMap(savedInstanceState: Bundle?) {
        mapView.getMapAsync { map ->
            mapLibreMap = map
            map.uiSettings.isCompassEnabled = false
            map.uiSettings.isLogoEnabled = false
            val target = if (savedInstanceState != null) {
                LatLng(
                    savedInstanceState.getDouble(CURRENT_LATITUDE, currentPosition.latitude),
                    savedInstanceState.getDouble(CURRENT_LONGITUDE, currentPosition.longitude),
                )
            } else {
                currentPosition
            }
            val zoom = savedInstanceState?.getDouble(CURRENT_ZOOM, DEFAULT_ZOOM.toDouble())
                ?: getPreferences(MODE_PRIVATE).getInt(LAST_ZOOM, DEFAULT_ZOOM).toDouble()
            map.cameraPosition = CameraPosition.Builder().target(target).zoom(zoom).build()
            map.addOnCameraMoveStartedListener { reason ->
                if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) {
                    centerToGpsPos = false
                }
            }
            lifecycleScope.launch {
                try {
                    val styleJson = BaranMapStyle.load(this@SubmitViolationActivity)
                    map.setStyle(Style.Builder().fromJson(styleJson)) { style ->
                        mapOverlays = MapLibreOverlayController(map, style).also { overlays ->
                            overlays.updateRoute(displayedTrackPoints)
                            overlays.updateCurrentLocation(currentPosition)
                        }
                        if (displayedTrackPoints.isNotEmpty() && !zoomedToTrackAlready) {
                            mapOverlays?.fit(displayedTrackPoints)
                            zoomedToTrackAlready = true
                        }
                    }
                } catch (error: Exception) {
                    Log.e(TAG, "Unable to initialize Baran MapLibre style", error)
                }
            }
        }
    }


    private fun startGettingLocation() {
        try {
            if (hasLocationPermission()) {
                if (receivingLocationUpdates) return
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    0f,
                    locationListener
                )
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000L,
                    0f,
                    locationListener
                )
                receivingLocationUpdates = true
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            currentPosition = LatLng(location.latitude, location.longitude)
            mapOverlays?.updateCurrentLocation(currentPosition)

            if (centerToGpsPos) {
                mapOverlays?.center(
                    currentPosition,
                    CENTER_DEFAULT_ZOOM_LEVEL,
                    ANIMATION_DURATION_MS.toInt(),
                )
            }

            val coordinates = location.latitude to location.longitude
            if (AddressLocationHelper.shouldUpdateAddress(lastRoadAddressCoordinates, coordinates)) {
                fetchRoadData(location.latitude, location.longitude, location.accuracy, coordinates)
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    /**
     * Setup ViewModel observers for MVVM architecture
     */
    private fun setupViewModelObservers() {
        // Observe road data
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.roadData.collect { roadData ->
                    roadData?.let { updateRoadInfo(it) }
                }
            }
        }
        
        // Observe loading state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoadingRoadData.collect { isLoading ->
                    // Keep the last resolved address visible during GPS refreshes.
                    // Otherwise every new location update replaces it with loading.
                    if (isLoading && viewModel.roadData.value == null) {
                        mapUiState.value = ViolationMapUiState(roadName = "در حال دریافت...")
                    }
                }
            }
        }
        
        // Observe error messages
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.collect { error ->
                    error?.let {
                        Log.e("SubmitViolationActivity", "ViewModel error: $it")
                        mapUiState.value = ViolationMapUiState(roadName = "خطا در دریافت اطلاعات")
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun fetchRoadData(
        latitude: Double,
        longitude: Double,
        accuracyMeters: Float,
        requestedCoordinates: Pair<Double, Double>,
    ) {
        val mapRoad = mapLibreMap?.let { map ->
            MapRoadNameResolver.resolve(map, LatLng(latitude, longitude))
        }
        if (mapRoad != null) {
            mapUiState.value = ViolationMapUiState(
                roadName = mapRoad.name,
                roadType = mapRoad.roadClass,
                speedLimit = "",
            )
            lastRoadAddressCoordinates = requestedCoordinates
            return
        }

        // Fall back to the selected municipality service until the required vector tile is
        // rendered, or when the nearby map features do not contain a usable street name.
        val buffer = Ir.co.tfs.farazaman.util.RoadQueryBuffer.forAccuracy(
            accuracyMeters.takeIf { it > 0f },
        )
        viewModel.fetchRoadData(latitude, longitude, buffer) { completed ->
            if (completed) lastRoadAddressCoordinates = requestedCoordinates
        }
    }

    private fun updateRoadInfo(roadData: RoadData) {
        mapUiState.value = ViolationMapUiState(roadData.name, roadData.fclass, "${roadData.maxspeed} km/h")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startGettingLocation()
            } else {
                Log.w("TAG", "Location permission denied")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!composeOwnsMapLifecycle) mapView.onStart()
    }

    override fun onPause() {
        stopGettingLocation()
        if (!composeOwnsMapLifecycle) mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        if (!composeOwnsMapLifecycle) mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        stopGettingLocation()
        if (!composeOwnsMapLifecycle && ::mapView.isInitialized) mapView.onDestroy()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (!composeOwnsMapLifecycle) mapView.onResume()
        if (hasLocationPermission()) startGettingLocation()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (!composeOwnsMapLifecycle && ::mapView.isInitialized) mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mapLibreMap?.cameraPosition?.let { camera ->
            outState.putDouble(CURRENT_LATITUDE, camera.target?.latitude ?: currentPosition.latitude)
            outState.putDouble(CURRENT_LONGITUDE, camera.target?.longitude ?: currentPosition.longitude)
            outState.putDouble(CURRENT_ZOOM, camera.zoom)
        }
        outState.putBoolean(CURRENT_CENTER_TO_GPS_POS, centerToGpsPos)
        outState.putBoolean(CURRENT_ZOOMED_TO_TRACK, zoomedToTrackAlready)
        super.onSaveInstanceState(outState)
        if (!composeOwnsMapLifecycle && ::mapView.isInitialized) mapView.onSaveInstanceState(outState)
    }

    private fun stopGettingLocation() {
        if (!receivingLocationUpdates) return
        try {
            locationManager.removeUpdates(locationListener)
        } catch (error: SecurityException) {
            Log.w(TAG, "Location permission was revoked before listener cleanup", error)
        } finally {
            receivingLocationUpdates = false
        }
    }

    private fun displayTrackPoints() {
        lifecycleScope.launch {
            val points = withContext(Dispatchers.IO) {
                val result = mutableListOf<LatLng>()
                contentResolver.query(
                    TrackContentProvider.trackPointsUri(trackId),
                    arrayOf(
                        TrackContentProvider.Schema.COL_LATITUDE,
                        TrackContentProvider.Schema.COL_LONGITUDE,
                    ),
                    null,
                    null,
                    null,
                )?.use { cursor ->
                    val latitudeColumn = cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_LATITUDE)
                    val longitudeColumn = cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_LONGITUDE)
                    while (cursor.moveToNext()) {
                        result += LatLng(
                            cursor.getDouble(latitudeColumn),
                            cursor.getDouble(longitudeColumn),
                        )
                    }
                }
                result
            }
            displayedTrackPoints = points
            mapOverlays?.updateRoute(points)
            if (points.isNotEmpty() && !zoomedToTrackAlready) {
                mapOverlays?.fit(points)
                zoomedToTrackAlready = mapOverlays != null
            }
        }
    }
} 
