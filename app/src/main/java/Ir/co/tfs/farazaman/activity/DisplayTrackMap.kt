package Ir.co.tfs.farazaman.activity

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.Gravity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import Ir.co.tfs.farazaman.OSMTracker
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.viewmodel.DisplayTrackMapViewModel
import Ir.co.tfs.farazaman.util.AddressLocationHelper
import Ir.co.tfs.farazaman.data.db.TrackContentProvider
import Ir.co.tfs.farazaman.layout.GpsStatusRecordDisplay
import Ir.co.tfs.farazaman.listener.SensorListener
import Ir.co.tfs.farazaman.map.BaranMapStyle
import Ir.co.tfs.farazaman.map.MapLibreOverlayController
import Ir.co.tfs.farazaman.map.MapRoadNameResolver
import Ir.co.tfs.farazaman.service.gps.GPSLogger
import Ir.co.tfs.farazaman.service.gps.GPSLoggerServiceConnectionDisplay
import Ir.co.tfs.farazaman.util.GpsAccuracyHelper
import Ir.co.tfs.farazaman.service.remote.ApiService
import com.google.gson.Gson
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import javax.inject.Inject
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.decode.SvgDecoder
import android.graphics.drawable.Drawable
import java.io.File
import java.io.FileOutputStream
import android.content.ContentValues
import Ir.co.tfs.farazaman.data.model.GisLayerIndexResponse
import Ir.co.tfs.farazaman.domain.road.RoadWidthCategory
import Ir.co.tfs.farazaman.supervisor.SupervisorMissionSetup
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import Ir.co.tfs.farazaman.map.ComposeMapHost
import Ir.co.tfs.farazaman.presentation.compose.TrackMapEvent
import Ir.co.tfs.farazaman.presentation.compose.TrackMapScreen
import Ir.co.tfs.farazaman.presentation.compose.TrackMapUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme

@AndroidEntryPoint
class DisplayTrackMap : AppCompatActivity() {

    companion object {
        private const val TAG = "DisplayTrackMap"
        private const val KEY_PLAYBACK_MODE = "playback_mode"
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
        const val EXTRA_FROM_NEW_SUPERVISOR = "from_new_supervisor"
        private const val ROAD_INFO_UPDATE_INTERVAL_MS = 1_000L
        private const val LOCATION_ERROR_FLASH_MS = 900L
        private const val MIN_HEADING_DISTANCE_METERS = 0.75f
        private const val VIOLATION_WAYPOINT_NAME = "نقص ثبت شده"
        private const val LEGACY_VIOLATION_WAYPOINT_NAME = "\u062A\u062E\u0644\u0641 \u062B\u0628\u062A \u0634\u062F\u0647"
    }

    private lateinit var mapView: MapView
    private var mapLibreMap: MapLibreMap? = null
    private var mapOverlays: MapLibreOverlayController? = null
    private lateinit var locationManager: LocationManager
    private var currentMapLocation: LatLng? = null
    private var lastHeadingLocation: Location? = null
    private var currentMovementHeading: Float? = null
    private var receivingMapLocationUpdates = false
    private var followUserLocation = true
    private val pendingFencePolygons = mutableListOf<List<LatLng>>()
    private var pendingFenceCamera: List<LatLng>? = null
    private var displayedViolationWaypoints: List<LatLng> = emptyList()
    private var displayedLayerWaypoints: List<LatLng> = emptyList()
    private val displayedWaypoints: List<LatLng>
        get() = displayedViolationWaypoints + displayedLayerWaypoints
    private lateinit var prefs: SharedPreferences
    private var currentTrackId: Long = 0
    private var zoomedToTrackAlready = false
    private var centeredOnOrganFence = false
    private var isPlaybackMode = false
    private var disableGpsService = false
    private lateinit var trackpointContentObserver: ContentObserver
    private var gpsLoggerServiceIntent: Intent? = null
    private var sensorListener: SensorListener? = null
    private var gpsLogger: GPSLogger? = null
    private var gpsLoggerConnection: ServiceConnection = GPSLoggerServiceConnectionDisplay(this)

    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var formDataApiService: Ir.co.tfs.farazaman.data.api.FormDataApiService
    private var composeOwnsMapLifecycle = false
    
    // ViewModel
    private val viewModel: DisplayTrackMapViewModel by viewModels()
    private val mapUiState = mutableStateOf(TrackMapUiState())
    private lateinit var layerItemsContainer: LinearLayout
    private lateinit var gpsStatusRecord: GpsStatusRecordDisplay
    private val roadInfoUpdateHandler = Handler(Looper.getMainLooper())
    private lateinit var roadInfoRunnable: Runnable
    private var lastRoadInfoCoordinates: Pair<Double, Double>? = null
    private var pathUpdateJob: Job? = null
    private var locationErrorFlashJob: Job? = null
    private var pathUpdatePending = false
    private var lastLoadedTrackPointId = 0L
    private val displayedTrackPoints = mutableListOf<LatLng>()
    private var layersExpanded = false
    private var cachedLayers: List<Ir.co.tfs.farazaman.presentation.viewmodel.SpeedDialLayerItem> = emptyList()

    private var isFromDriver: Boolean = false
    private var isFromNewSupervisor: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        supportActionBar?.hide()

        // Setup ViewModel observers
        setupViewModelObservers()

        isPlaybackMode = intent.getBooleanExtra(KEY_PLAYBACK_MODE, false)
        currentTrackId = intent.extras?.getLong(TrackContentProvider.Schema.COL_TRACK_ID) ?: 0
        disableGpsService = intent.getBooleanExtra("disable_gps_service", false)
        isFromDriver = intent.getBooleanExtra("from_driver", false)
        isFromNewSupervisor = intent.getBooleanExtra(EXTRA_FROM_NEW_SUPERVISOR, false)
        
        // Check if we have zone encryption for zone display
        val zoneEncryption = intent.getStringExtra("zone_encryption")
        Log.d(TAG, "Zone encryption from intent: $zoneEncryption")
        if (zoneEncryption.isNullOrEmpty()) {
            Log.w(TAG, "No zone encryption provided in intent")
        }

        if (currentTrackId <= 0) {
            finish()
            return
        }

        if (!isPlaybackMode && !disableGpsService) {
            gpsLoggerServiceIntent = Intent(this, GPSLogger::class.java).apply {
                action = OSMTracker.INTENT_START_TRACKING
                putExtra(TrackContentProvider.Schema.COL_TRACK_ID, currentTrackId)
            }
            sensorListener = SensorListener()
        }

        title = when {
            isPlaybackMode -> "نمایش مسیر: #$currentTrackId"
            disableGpsService -> "نمایش نقشه و موقعیت زنده: #$currentTrackId"
            else -> "ردیابی مسیر: #$currentTrackId"
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateBackToTrackManager()
            }
        })

        val keepScreenOn = prefs.getBoolean(OSMTracker.Preferences.KEY_UI_DISPLAY_KEEP_ON, OSMTracker.Preferences.VAL_UI_DISPLAY_KEEP_ON)
        layerItemsContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        gpsStatusRecord = GpsStatusRecordDisplay(this, null)
        gpsStatusRecord.background = null
        gpsStatusRecord.setPadding(0, 0, 0, 0)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        trackpointContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) = pathChanged()
        }
        composeOwnsMapLifecycle = true
        mapUiState.value = mapUiState.value.copy(
            showGpsStatus = !isPlaybackMode,
            showSubmitViolation = !isPlaybackMode,
        )
        setContent {
            FaraZamanTheme {
                TrackMapScreen(
                    state = mapUiState.value,
                    onEvent = ::handleMapEvent,
                    map = {
                        ComposeMapHost(
                            modifier = Modifier.fillMaxSize(),
                            savedInstanceState = savedInstanceState,
                            keepScreenOn = keepScreenOn,
                            onMapViewReady = { createdMap -> mapView = createdMap; configureMap() },
                        )
                    },
                    gpsStatus = { AndroidView(factory = { gpsStatusRecord }) },
                    layers = { AndroidView(factory = { layerItemsContainer }) },
                )
            }
        }
        if (!zoneEncryption.isNullOrEmpty()) fetchAndDisplayZone(zoneEncryption)
        initializeRoadInfoUpdater()

        fetchMapLayersFromApi()
    }

    /**
     * Setup ViewModel observers for MVVM architecture
     */
    private fun setupViewModelObservers() {
        // Observe road information
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.roadInfo.collect { roadInfo ->
                    updateRoadInfoUI(roadInfo)
                }
            }
        }
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.gisLayers.collect { layers ->
                    updateLayerControlsWithLayers(layers)
                }
            }
        }
        
        // Observe loading states
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoadingRoadInfo.collect { isLoading ->
                    // Periodic refreshes must not hide an address that is already shown.
                    if (isLoading && viewModel.roadInfo.value.name.isBlank()) {
                        mapUiState.value = mapUiState.value.copy(roadName = getString(R.string.map_loading_address))
                    }
                }
            }
        }
        
        // Observe error messages
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.collect { error ->
                    error?.let {
                        Toast.makeText(this@DisplayTrackMap, it, Toast.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.roadLookupFailures.collect {
                    flashLocationLookupError()
                }
            }
        }
        
        // Observe mission details
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.missionDetails.collect { details ->
                    details?.let {
                        val gson = com.google.gson.Gson()
                        val responseJson = gson.toJson(it)
                        parseAndDisplayZone(responseJson)
                        viewModel.clearMissionDetails()
                    }
                }
            }
        }
    }
    
    /**
     * Update road info UI elements
     */
    private fun updateRoadInfoUI(roadInfo: Ir.co.tfs.farazaman.presentation.viewmodel.RoadInfo) {
        val roadName = roadInfo.name.ifBlank { getString(R.string.map_address_unknown) }
        val label = if (roadInfo.isValid) {
            getString(
                R.string.map_road_name_with_width,
                roadName,
                getString(RoadWidthCategory.fromFclass(roadInfo.fclass).labelResource()),
            )
        } else {
            roadName
        }
        mapUiState.value = mapUiState.value.copy(roadName = label)
    }

    private fun flashLocationLookupError() {
        locationErrorFlashJob?.cancel()
        mapOverlays?.setCurrentLocationError(true)
        locationErrorFlashJob = lifecycleScope.launch {
            delay(LOCATION_ERROR_FLASH_MS)
            mapOverlays?.setCurrentLocationError(false)
        }
    }

    private fun RoadWidthCategory.labelResource(): Int = when (this) {
        RoadWidthCategory.UP_TO_15_METERS -> R.string.road_width_up_to_15
        RoadWidthCategory.OVER_15_TO_25_METERS -> R.string.road_width_over_15_to_25
        RoadWidthCategory.OVER_25_TO_45_METERS -> R.string.road_width_over_25_to_45
        RoadWidthCategory.OVER_45_METERS -> R.string.road_width_over_45
    }

    private fun updateLayerControlsWithLayers(
        layers: List<Ir.co.tfs.farazaman.presentation.viewmodel.SpeedDialLayerItem>
    ) {
        cachedLayers = layers
        mapUiState.value = mapUiState.value.copy(hasLayers = layers.isNotEmpty())
        rebuildLayerItems()
    }

    private fun rebuildLayerItems() {
        layerItemsContainer.removeAllViews()
        if (cachedLayers.isEmpty()) return

        lifecycleScope.launch {
            cachedLayers.forEach { layer ->
                val density = resources.displayMetrics.density
                fun dp(value: Int): Int = (value * density).toInt()
                val row = LinearLayout(this@DisplayTrackMap).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    layoutDirection = View.LAYOUT_DIRECTION_RTL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).apply { bottomMargin = dp(10) }
                }
                val label = TextView(this@DisplayTrackMap).apply {
                    text = layer.name
                    setTextColor(getColor(R.color.white))
                    textSize = 12f
                    setBackgroundResource(R.drawable.map_layer_pill_bg)
                    setPadding(dp(14), dp(8), dp(14), dp(8))
                }
                val button = ImageButton(this@DisplayTrackMap).apply {
                    contentDescription = layer.name
                    setBackgroundResource(R.drawable.map_control_btn_bg)
                    setPadding(dp(12), dp(12), dp(12), dp(12))
                    layoutParams = LinearLayout.LayoutParams(dp(48), dp(48)).apply { marginStart = dp(8) }
                }
                row.addView(label)
                row.addView(button)
                var iconDrawable: Drawable? = null
                if (!layer.iconUrl.isNullOrBlank()) {
                    iconDrawable = svgStringToDrawable(layer.iconUrl)
                }
                if (iconDrawable != null) {
                    button.setImageDrawable(iconDrawable)
                } else {
                    button.setImageResource(R.drawable.ic_fab_add_track)
                    button.setColorFilter(resources.getColor(R.color.app_primary, theme))
                }

                button.setOnClickListener {
                    val location = currentMapLocation
                    if (location != null) {
                        saveWaypointToTrack(
                            trackId = currentTrackId,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            name = layer.name,
                            link = layer.id.toString()
                        )
                    } else {
                        Toast.makeText(this@DisplayTrackMap, "موقعیت مکانی یافت نشد", Toast.LENGTH_SHORT).show()
                    }
                }
                layerItemsContainer.addView(row)
            }
        }
    }

    private fun setLayersExpanded(expanded: Boolean) {
        layersExpanded = expanded
        mapUiState.value = mapUiState.value.copy(layersExpanded = expanded)
    }

//    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
//        menuInflater.inflate(R.menu.displaytrackmap_menu, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.menu_export_gpx -> {
//                exportTrackToGpx(currentTrackId)
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    private fun navigateBackToTrackManager() {
        when {
            isFromNewSupervisor -> finish()
            isFromDriver -> {
                val intent = Intent(this, DriverActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            else -> {
                val intent = Intent(this, NewSupervisorActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    // این متد حالا مسئول شروع تایمر است
    fun setGpsLogger(l: GPSLogger) {
        gpsLogger = l
        // بعد از اینکه سرویس متصل شد، بررسی می‌کنیم که آیا باید تایمر را روشن کنیم
        if (!isPlaybackMode && !disableGpsService && gpsLogger?.isTracking == true) {
            // هر فراخوانی قبلی را حذف می‌کنیم تا تایمر چندبار اجرا نشود
            roadInfoUpdateHandler.removeCallbacks(roadInfoRunnable)
            // تایمر را برای اولین بار فعال می‌کنیم
            roadInfoUpdateHandler.post(roadInfoRunnable)
        } else if (!isPlaybackMode && disableGpsService) {
            // Live mode without GPS tracking - start road info updates immediately
            roadInfoUpdateHandler.removeCallbacks(roadInfoRunnable)
            roadInfoUpdateHandler.post(roadInfoRunnable)
        }
    }

    fun getGpsLogger(): GPSLogger? = gpsLogger
    fun getCurrentTrackId(): Long = currentTrackId

    private fun configureMap() {
        mapView.getMapAsync { map ->
            mapLibreMap = map
            map.uiSettings.isCompassEnabled = false
            map.uiSettings.isLogoEnabled = false
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(32.0, 53.0))
                .zoom(4.0)
                .build()
            map.addOnCameraMoveStartedListener { reason ->
                if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE) {
                    followUserLocation = false
                }
            }
            lifecycleScope.launch {
                try {
                    val styleJson = BaranMapStyle.load(this@DisplayTrackMap)
                    map.setStyle(Style.Builder().fromJson(styleJson)) { style ->
                        mapOverlays = MapLibreOverlayController(map, style).also { overlays ->
                            if (mapUiState.value.recordedPathVisible) {
                                overlays.updateRoute(displayedTrackPoints)
                                overlays.updateTrackPoints(displayedTrackPoints)
                                overlays.updateWaypoints(displayedViolationWaypoints, displayedLayerWaypoints)
                            }
                            overlays.updateCurrentLocation(currentMapLocation, currentMovementHeading)
                            pendingFencePolygons.forEach(overlays::addFence)
                            pendingFenceCamera?.let(overlays::fit)
                        }
                        pendingFencePolygons.clear()
                        pendingFenceCamera = null
                        displayStoredOrganFence()
                        refreshWaypoints()
                        zoomToTrackOnce()
                    }
                } catch (error: Exception) {
                    Log.e(TAG, "Unable to initialize Baran MapLibre style", error)
                    Toast.makeText(this@DisplayTrackMap, "خطا در بارگذاری نقشه", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun handleMapEvent(event: TrackMapEvent) {
        when (event) {
            TrackMapEvent.Back -> navigateBackToTrackManager()
            TrackMapEvent.Center -> if (!isPlaybackMode) {
                currentMapLocation?.let {
                    followUserLocation = true
                    mapOverlays?.center(it)
                } ?: Toast.makeText(this, "در حال یافتن موقعیت مکانی...", Toast.LENGTH_SHORT).show()
            }
            TrackMapEvent.ZoomIn -> {
                followUserLocation = false
                mapOverlays?.zoomIn()
            }
            TrackMapEvent.ZoomOut -> {
                followUserLocation = false
                mapOverlays?.zoomOut()
            }
            TrackMapEvent.ToggleLayers -> {
                if (cachedLayers.isEmpty()) Toast.makeText(this, "لایه‌ای برای نمایش وجود ندارد", Toast.LENGTH_SHORT).show()
                else setLayersExpanded(!layersExpanded)
            }
            TrackMapEvent.ToggleRecordedPath -> toggleRecordedPath()
            TrackMapEvent.SubmitViolation -> submitViolation()
        }
    }

    private fun toggleRecordedPath() {
        val visible = !mapUiState.value.recordedPathVisible
        mapUiState.value = mapUiState.value.copy(recordedPathVisible = visible)
        val overlays = mapOverlays ?: return
        if (visible) {
            overlays.updateRoute(displayedTrackPoints)
            overlays.updateTrackPoints(displayedTrackPoints)
            overlays.updateWaypoints(displayedViolationWaypoints, displayedLayerWaypoints)
            val allPoints = displayedTrackPoints + displayedWaypoints
            when {
                allPoints.size > 1 -> overlays.fit(allPoints, paddingPx = 80)
                allPoints.size == 1 -> overlays.center(allPoints.first())
                else -> Toast.makeText(this, "هنوز نقطه‌ای برای نمایش و خروجی GPX ثبت نشده است", Toast.LENGTH_SHORT).show()
            }
        } else {
            overlays.updateRoute(emptyList())
            overlays.updateTrackPoints(emptyList())
            overlays.updateWaypoints(emptyList(), emptyList())
        }
    }

    private fun initializeRoadInfoUpdater() {
        roadInfoRunnable = Runnable {
            if (!isPlaybackMode) {
                val loc = currentMapLocation
                if (loc != null) {
                    val coordinates = loc.latitude to loc.longitude
                    if (AddressLocationHelper.shouldUpdateAddress(lastRoadInfoCoordinates, coordinates)) {
                        Log.d(TAG, "RoadInfoUpdater location changed: lat=${loc.latitude}, lon=${loc.longitude}")
                        fetchAndDisplayRoadInfo(loc.latitude, loc.longitude, coordinates)
                    }
                } else {
                    Log.d(TAG, "RoadInfoUpdater tick: location is null; will retry")
                }
                // Continue updating road info in both live mode and tracking mode
                roadInfoUpdateHandler.postDelayed(roadInfoRunnable, ROAD_INFO_UPDATE_INTERVAL_MS)
            }
        }
        // Ensure the updater starts even if GPSLogger hasn't posted it yet
        if (!isPlaybackMode) {
            roadInfoUpdateHandler.removeCallbacks(roadInfoRunnable)
            roadInfoUpdateHandler.post(roadInfoRunnable)
            Log.d(TAG, "RoadInfoUpdater: initial post() from initializeRoadInfoUpdater")
        }
    }

    private fun fetchAndDisplayRoadInfo(
        lat: Double,
        lon: Double,
        requestedCoordinates: Pair<Double, Double>,
    ) {
        val mapRoad = mapLibreMap?.let { map ->
            MapRoadNameResolver.resolve(map, LatLng(lat, lon))
        }
        if (mapRoad != null) {
            viewModel.useMapRoadInfo(mapRoad.name, mapRoad.roadClass)
            lastRoadInfoCoordinates = requestedCoordinates
            return
        }

        // Vector data can be unavailable while tiles/style are still loading or when the
        // location is outside the rendered viewport. Keep the selected-server API as fallback.
        val buffer = GpsAccuracyHelper.resolveBuffer(this, currentTrackId)
        viewModel.fetchRoadInfo(lat, lon, buffer) { completed ->
            if (completed) lastRoadInfoCoordinates = requestedCoordinates
        }
    }

    private fun roadQueryBuffer(): Int = GpsAccuracyHelper.resolveBuffer(this, currentTrackId)

    private val mapLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val point = LatLng(location.latitude, location.longitude)
            currentMovementHeading = resolveMovementHeading(location)
            currentMapLocation = point
            mapOverlays?.updateCurrentLocation(point, currentMovementHeading)
            if (followUserLocation && !isPlaybackMode) mapOverlays?.follow(point)
        }

        override fun onProviderEnabled(provider: String) = Unit
        override fun onProviderDisabled(provider: String) = Unit
        @Deprecated("Deprecated in Android")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }

    private fun resolveMovementHeading(location: Location): Float? {
        val previous = lastHeadingLocation
        val distanceFromPrevious = previous?.distanceTo(location) ?: 0f
        val sensorHeading = sensorListener
            ?.azimuth
            ?.takeIf { it.isFinite() && it in 0f..360f }
        val resolved = when {
            location.hasBearing() && location.bearing.isFinite() ->
                location.bearing
            previous != null && distanceFromPrevious >= MIN_HEADING_DISTANCE_METERS ->
                previous.bearingTo(location)
            sensorHeading != null -> sensorHeading
            else -> currentMovementHeading
        }
        if (previous == null || distanceFromPrevious >= MIN_HEADING_DISTANCE_METERS) {
            lastHeadingLocation = Location(location)
        }
        return resolved?.let { ((it % 360f) + 360f) % 360f }
    }

    private fun startMapLocationUpdates() {
        if (isPlaybackMode || receivingMapLocationUpdates) return
        val fine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) return
        runCatching {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1_000L, 0f, mapLocationListener)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1_000L, 0f, mapLocationListener)
            receivingMapLocationUpdates = true
        }.onFailure { Log.w(TAG, "Unable to start map location updates", it) }
    }

    private fun stopMapLocationUpdates() {
        if (!receivingMapLocationUpdates) return
        runCatching { locationManager.removeUpdates(mapLocationListener) }
            .onFailure { Log.w(TAG, "Unable to stop map location updates", it) }
        receivingMapLocationUpdates = false
    }

    override fun onStart() {
        super.onStart()
        if (!composeOwnsMapLifecycle) mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (!composeOwnsMapLifecycle) mapView.onResume()
        
        if (isPlaybackMode) {
            // Playback mode - show track only, no GPS
            mapUiState.value = mapUiState.value.copy(showGpsStatus = false)
        } else if (disableGpsService) {
            // Live mode without GPS tracking - show current location but don't start tracking service
            gpsStatusRecord.requestLocationUpdates(true)
            startMapLocationUpdates()
            sensorListener?.register(this)
            // Don't start GPS service or bind to it
        } else {
            // Full tracking mode - start GPS service and tracking
            gpsStatusRecord.requestLocationUpdates(true)
            startMapLocationUpdates()
            startService(gpsLoggerServiceIntent)
            gpsLoggerServiceIntent?.let { bindService(it, gpsLoggerConnection, 0) }
            sensorListener?.register(this)
        }
        
        contentResolver.registerContentObserver(TrackContentProvider.trackPointsUri(currentTrackId), true, trackpointContentObserver)
        zoomedToTrackAlready = centeredOnOrganFence
        pathChanged(reset = true)
        refreshWaypoints()
    }

    override fun onPause() {
        super.onPause()
        if (!composeOwnsMapLifecycle) mapView.onPause()
        stopMapLocationUpdates()
        roadInfoUpdateHandler.removeCallbacks(roadInfoRunnable)
        pathUpdateJob?.cancel()
        pathUpdateJob = null
        locationErrorFlashJob?.cancel()
        locationErrorFlashJob = null
        mapOverlays?.setCurrentLocationError(false)
        pathUpdatePending = false
        contentResolver.unregisterContentObserver(trackpointContentObserver)
        
        if (isPlaybackMode) {
            // Playback mode - nothing to clean up
        } else if (disableGpsService) {
            // Live mode without GPS tracking - just disable location updates
            gpsStatusRecord.requestLocationUpdates(false)
        } else {
            // Full tracking mode - clean up GPS service
            try {
                if (gpsLogger != null) unbindService(gpsLoggerConnection)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Service was not registered or already unbound.")
            }
            gpsStatusRecord.requestLocationUpdates(false)
            sensorListener?.unregister()
        }
    }

    private fun pathChanged(reset: Boolean = false) {
        if (isFinishing) return
        if (reset) {
            pathUpdateJob?.cancel()
            pathUpdateJob = null
            lastLoadedTrackPointId = 0L
            displayedTrackPoints.clear()
            mapOverlays?.updateRoute(emptyList())
            mapOverlays?.updateTrackPoints(emptyList())
        }

        pathUpdatePending = true
        if (pathUpdateJob?.isActive == true) return
        pathUpdateJob = lifecycleScope.launch {
            do {
                pathUpdatePending = false
                val afterId = lastLoadedTrackPointId
                val newPoints = withContext(Dispatchers.IO) { loadTrackPointsAfter(afterId) }
                if (newPoints.isNotEmpty()) {
                    lastLoadedTrackPointId = newPoints.last().id
                    newPoints.forEach { loadedPoint ->
                        displayedTrackPoints += loadedPoint.point
                    }
                    if (mapUiState.value.recordedPathVisible) {
                        mapOverlays?.updateRoute(displayedTrackPoints)
                        mapOverlays?.updateTrackPoints(displayedTrackPoints)
                    }
                    zoomToTrackOnce()
                }
            } while (pathUpdatePending)
        }
    }

    override fun onStop() {
        if (!composeOwnsMapLifecycle) mapView.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (::mapView.isInitialized) mapView.onLowMemory()
    }

    override fun onDestroy() {
        if (!composeOwnsMapLifecycle && ::mapView.isInitialized) mapView.onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::mapView.isInitialized) mapView.onSaveInstanceState(outState)
    }

    private fun loadTrackPointsAfter(afterId: Long): List<LoadedTrackPoint> {
        val projection = arrayOf(
            TrackContentProvider.Schema.COL_ID,
            TrackContentProvider.Schema.COL_LATITUDE,
            TrackContentProvider.Schema.COL_LONGITUDE,
        )
        val selection = if (afterId > 0L) "${TrackContentProvider.Schema.COL_ID} > ?" else null
        val selectionArgs = if (afterId > 0L) arrayOf(afterId.toString()) else null
        val result = mutableListOf<LoadedTrackPoint>()
        contentResolver.query(
            TrackContentProvider.trackPointsUri(currentTrackId),
            projection,
            selection,
            selectionArgs,
            "${TrackContentProvider.Schema.COL_ID} asc",
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_ID)
            val latColumn = cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_LATITUDE)
            val lonColumn = cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_LONGITUDE)
            while (cursor.moveToNext()) {
                result += LoadedTrackPoint(
                    id = cursor.getLong(idColumn),
                    point = LatLng(cursor.getDouble(latColumn), cursor.getDouble(lonColumn)),
                )
            }
        }
        return result
    }

    private fun zoomToTrackOnce() {
        if (zoomedToTrackAlready || displayedTrackPoints.isEmpty()) return
        val overlays = mapOverlays ?: return
        if (displayedTrackPoints.size == 1) overlays.center(displayedTrackPoints.first())
        else overlays.fit(displayedTrackPoints, paddingPx = 50)
        zoomedToTrackAlready = true
    }

    private data class LoadedTrackPoint(val id: Long, val point: LatLng)

    private fun submitViolation() {
        if (!isPlaybackMode && !mapUiState.value.submittingViolation) {
                android.util.Log.d("DisplayTrackMap", "=== SUBMIT_VIOLATION BUTTON CLICKED ===")
                android.util.Log.d("DisplayTrackMap", "currentTrackId = $currentTrackId")
                
                // دریافت موقعیت مکانی فعلی از لایه نقشه
                val currentLocation = currentMapLocation
                android.util.Log.d("DisplayTrackMap", "currentLocation = $currentLocation")

                if (currentLocation != null) {
                    val openViolationForm = {
                        // Fetch address from RoadService for the current location
                        fetchAddressForViolation(currentLocation.latitude, currentLocation.longitude) { address ->
                        android.util.Log.d("DisplayTrackMap", "Address fetched: $address")
                        
                        // Get mission data from SharedPreferences for this track
                        val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this)
                        val missionDataKey = "mission_data_$currentTrackId"
                        val missionDataJson = prefs.getString(missionDataKey, null)
                        
                        android.util.Log.d("DisplayTrackMap", "Looking for mission data with key: $missionDataKey")
                        android.util.Log.d("DisplayTrackMap", "Mission data JSON: $missionDataJson")
                        
                        // ارسال هر سه مقدار به اکتیویتی فرم
                        val intent = Intent(this, SubmitViolationFormActivity::class.java).apply {
                            android.util.Log.d("DisplayTrackMap", "Creating intent with currentTrackId: $currentTrackId")
                            putExtra(EXTRA_LATITUDE, currentLocation.latitude)
                            putExtra(EXTRA_LONGITUDE, currentLocation.longitude)
                            putExtra("extra_address", address) // ارسال آدرس به عنوان یک رشته
                            putExtra(TrackContentProvider.Schema.COL_TRACK_ID, currentTrackId)
                            android.util.Log.d("DisplayTrackMap", "Added TRACK_ID extra: $currentTrackId")
                            
                            // Add mission data if available (organId, contractId, etc.)
                            if (missionDataJson != null) {
                                try {
                                    val missionData = org.json.JSONObject(missionDataJson)
                                    val organId = missionData.optInt("organId", 0)
                                    val contractId = missionData.optInt("contractId", 0)
                                    val organTitle = missionData.optString("organTitle", "")
                                    val contractTitle = missionData.optString("contractTitle", "")
                                    val visitDate = missionData.optString("visitDate", "")
                                    
                                    putExtra("organ_id", organId)
                                    putExtra("contract_id", contractId)
                                    putExtra("organ_title", organTitle)
                                    putExtra("contract_title", contractTitle)
                                    putExtra("visit_date", visitDate)
                                    
                                    android.util.Log.d("DisplayTrackMap", "✓ Added mission data: organId=$organId, contractId=$contractId")
                                } catch (e: Exception) {
                                    android.util.Log.e("DisplayTrackMap", "✗ Error parsing mission data: ${e.message}", e)
                                }
                            } else {
                                android.util.Log.w("DisplayTrackMap", "✗ No mission data found for track $currentTrackId")
                            }
                            
                            // Mark this as coming from submit_violation button in map
                            putExtra("from_submit_violation_card", true)
                            android.util.Log.d("DisplayTrackMap", "Added from_submit_violation_card flag")
                        }
                        
                        android.util.Log.d("DisplayTrackMap", "=== FINAL INTENT SUMMARY ===")
                        android.util.Log.d("DisplayTrackMap", "Track ID: ${intent.getLongExtra(TrackContentProvider.Schema.COL_TRACK_ID, -999)}")
                        android.util.Log.d("DisplayTrackMap", "organ_id: ${intent.getIntExtra("organ_id", -999)}")
                        android.util.Log.d("DisplayTrackMap", "contract_id: ${intent.getIntExtra("contract_id", -999)}")
                        android.util.Log.d("DisplayTrackMap", "from_submit_violation_card: ${intent.getBooleanExtra("from_submit_violation_card", false)}")
                        android.util.Log.d("DisplayTrackMap", "Starting SubmitViolationFormActivity...")
                        
                            startActivity(intent)
                        }
                    }
                    if (
                        isFromNewSupervisor &&
                        !SupervisorMissionSetup.isMissionRegisteredForDate(this, currentTrackId)
                    ) {
                        mapUiState.value = mapUiState.value.copy(submittingViolation = true)
                        Toast.makeText(this, R.string.mission_day_registration_loading, Toast.LENGTH_LONG).show()
                        lifecycleScope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    SupervisorMissionSetup.refreshMissionForDateIfNeeded(
                                        context = applicationContext,
                                        service = formDataApiService,
                                        trackId = currentTrackId,
                                    )
                                }
                                Toast.makeText(
                                    this@DisplayTrackMap,
                                    R.string.mission_day_registration_success,
                                    Toast.LENGTH_LONG,
                                ).show()
                                openViolationForm()
                            } catch (cancellation: CancellationException) {
                                throw cancellation
                            } catch (error: Exception) {
                                Log.e(TAG, "Unable to register current-day mission before violation", error)
                                Toast.makeText(
                                    this@DisplayTrackMap,
                                    R.string.mission_day_registration_failed,
                                    Toast.LENGTH_LONG,
                                ).show()
                            } finally {
                                mapUiState.value = mapUiState.value.copy(submittingViolation = false)
                            }
                        }
                    } else {
                        openViolationForm()
                    }
                } else {
                    Toast.makeText(this, "موقعیت مکانی هنوز یافت نشده است.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun getGpsStatusRecord(): GpsStatusRecordDisplay = gpsStatusRecord

    private fun fetchAddressForViolation(lat: Double, lon: Double, onAddressFetched: (String) -> Unit) {
        viewModel.fetchAddressForViolation(lat, lon, roadQueryBuffer(), onAddressFetched)
    }

    private suspend fun svgStringToDrawable(svgString: String): Drawable? {
        // Save SVG string to a temporary file
        val tempFile = File.createTempFile("icon_", ".svg", cacheDir)
        FileOutputStream(tempFile).use { it.write(svgString.toByteArray()) }
        val loader = ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .build()
        val request = ImageRequest.Builder(this)
            .data(tempFile)
            .size(96, 96) // Set explicit size for SVG rendering
            .build()
        val result = loader.execute(request)
        tempFile.delete()
        val drawable = (result as? SuccessResult)?.drawable
        // Set proper bounds for the drawable
        drawable?.setBounds(0, 0, 96, 96)
        return drawable
    }

    private fun fetchMapLayersFromApi() {
        viewModel.fetchGisLayers()
    }

    private fun saveWaypointToTrack(trackId: Long, latitude: Double, longitude: Double, name: String, link: String? = null) {
        val values = ContentValues().apply {
            put(Ir.co.tfs.farazaman.data.db.TrackContentProvider.Schema.COL_TRACK_ID, trackId)
            put(Ir.co.tfs.farazaman.data.db.TrackContentProvider.Schema.COL_LATITUDE, latitude)
            put(Ir.co.tfs.farazaman.data.db.TrackContentProvider.Schema.COL_LONGITUDE, longitude)
            put(Ir.co.tfs.farazaman.data.db.TrackContentProvider.Schema.COL_NAME, name)
            put(Ir.co.tfs.farazaman.data.db.TrackContentProvider.Schema.COL_LINK, link)
            put(Ir.co.tfs.farazaman.data.db.TrackContentProvider.Schema.COL_TIMESTAMP, System.currentTimeMillis())
            put(Ir.co.tfs.farazaman.data.db.TrackContentProvider.Schema.COL_NBSATELLITES, 0)
        }
        val uri = contentResolver.insert(Ir.co.tfs.farazaman.data.db.TrackContentProvider.waypointsUri(trackId), values)
        if (uri != null) {
            Toast.makeText(this, "نقطه ثبت شد", Toast.LENGTH_SHORT).show()
            refreshWaypoints()
        } else {
            Toast.makeText(this, "خطا در ثبت نقطه", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun exportTrackToGpx(trackId: Long) {
//        // Request permission if needed
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
//            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1001)
//            return
//        }
//        try {
//            val gpxString = generateGpxForTrack(trackId)
//            val dir = java.io.File(android.os.Environment.getExternalStorageDirectory(), "faraZaman")
//            if (!dir.exists()) dir.mkdirs()
//            val gpxFile = java.io.File(dir, "track_${trackId}.gpx")
//            java.io.FileWriter(gpxFile).use { it.write(gpxString) }
//            Toast.makeText(this, "GPX ذخیره شد: ${gpxFile.absolutePath}", Toast.LENGTH_LONG).show()
//        } catch (e: Exception) {
//            Toast.makeText(this, "خطا در ذخیره GPX: ${e.message}", Toast.LENGTH_LONG).show()
//        }
//    }

    private fun generateGpxForTrack(trackId: Long): String {
        // Use your existing GPX writer logic here
        // This is a placeholder. Replace with actual GPX generation.
        // For example, use GpxWriter or similar from your codebase.
        return "<gpx><trk><name>Track $trackId</name></trk></gpx>"
    }

    private fun fetchAndDisplayZone(encryption: String) {
        Log.d(TAG, "Fetching zone data for encryption: $encryption")
        // Use ViewModel instead of direct API call
        viewModel.fetchMissionDetails(encryption)
    }

    private fun parseAndDisplayZone(responseBody: String) {
        try {
            Log.d(TAG, "Parsing response: $responseBody")
            val jsonResponse = org.json.JSONObject(responseBody)
            
            // Check if response has the expected structure
            if (!jsonResponse.has("dataModel")) {
                Log.e(TAG, "Response does not contain 'dataModel' field")
                Toast.makeText(this, "پاسخ سرور دارای ساختار نامعتبر است", Toast.LENGTH_SHORT).show()
                return
            }
            
            val dataModel = jsonResponse.getJSONObject("dataModel")
            
            if (!dataModel.has("gisLayersData")) {
                Log.e(TAG, "dataModel does not contain 'gisLayersData' field")
                Toast.makeText(this, "داده‌های نقشه در پاسخ یافت نشد", Toast.LENGTH_SHORT).show()
                return
            }
            
            val gisLayersData = dataModel.getJSONArray("gisLayersData")
            Log.d(TAG, "Found ${gisLayersData.length()} GIS layers")
            
            var polygonsDisplayed = 0
            for (i in 0 until gisLayersData.length()) {
                val layerData = gisLayersData.getJSONObject(i)
                Log.d(TAG, "Processing layer $i: ${layerData.toString()}")
                
                if (layerData.has("gisLayersDataWellKnownText")) {
                    val wellKnownText = layerData.getJSONObject("gisLayersDataWellKnownText")
                    if (wellKnownText.has("wellKnownText")) {
                        val wkt = wellKnownText.getString("wellKnownText")
                        Log.d(TAG, "WKT data: $wkt")
                        
                        // Parse WKT and display on map
                        if (displayPolygonOnMap(wkt)) {
                            polygonsDisplayed++
                        }
                    } else {
                        Log.w(TAG, "gisLayersDataWellKnownText does not contain 'wellKnownText' field")
                    }
                } else {
                    Log.w(TAG, "Layer does not contain 'gisLayersDataWellKnownText' field")
                }
            }
            
            if (polygonsDisplayed == 0) {
                Toast.makeText(this, "هیچ منطقه‌ای برای نمایش یافت نشد", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "$polygonsDisplayed منطقه روی نقشه نمایش داده شد", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing zone data", e)
            Toast.makeText(this, "خطا در پردازش اطلاعات منطقه: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayStoredOrganFence() {
        val missionDataJson = prefs.getString("mission_data_$currentTrackId", null) ?: return
        try {
            val wkt = org.json.JSONObject(missionDataJson)
                .optString("organFenceWkt")
                .trim()
            if (wkt.isNotEmpty()) {
                centeredOnOrganFence = displayPolygonOnMap(wkt, zoomToPolygon = true)
                if (centeredOnOrganFence) {
                    followUserLocation = false
                    zoomedToTrackAlready = true
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to display stored organ fence for trackId=$currentTrackId", e)
        }
    }

    private fun refreshWaypoints() {
        lifecycleScope.launch {
            val points = withContext(Dispatchers.IO) {
                val violationPoints = mutableListOf<LatLng>()
                val layerPoints = mutableListOf<LatLng>()
                contentResolver.query(
                    TrackContentProvider.waypointsUri(currentTrackId),
                    arrayOf(
                        TrackContentProvider.Schema.COL_LATITUDE,
                        TrackContentProvider.Schema.COL_LONGITUDE,
                        TrackContentProvider.Schema.COL_NAME,
                    ),
                    null,
                    null,
                    "${TrackContentProvider.Schema.COL_TIMESTAMP} asc",
                )?.use { cursor ->
                    val latitudeColumn = cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_LATITUDE)
                    val longitudeColumn = cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_LONGITUDE)
                    val nameColumn = cursor.getColumnIndexOrThrow(TrackContentProvider.Schema.COL_NAME)
                    while (cursor.moveToNext()) {
                        val point = LatLng(
                            cursor.getDouble(latitudeColumn),
                            cursor.getDouble(longitudeColumn),
                        )
                        if (cursor.getString(nameColumn) in setOf(VIOLATION_WAYPOINT_NAME, LEGACY_VIOLATION_WAYPOINT_NAME)) {
                            violationPoints += point
                        } else {
                            layerPoints += point
                        }
                    }
                }
                violationPoints to layerPoints
            }
            displayedViolationWaypoints = points.first
            displayedLayerWaypoints = points.second
            if (mapUiState.value.recordedPathVisible) {
                mapOverlays?.updateWaypoints(displayedViolationWaypoints, displayedLayerWaypoints)
            }
        }
    }

    private fun displayPolygonOnMap(
        wkt: String,
        zoomToPolygon: Boolean = true,
    ): Boolean {
        try {
            Log.d(TAG, "Displaying polygon with WKT: $wkt")
            
            // Parse WKT POLYGON format: "POLYGON ((lon1 lat1, lon2 lat2, ...))"
            if (wkt.startsWith("POLYGON ((") && wkt.endsWith("))")) {
                val coordinates = wkt.substring(10, wkt.length - 2) // Remove "POLYGON ((" and "))"
                Log.d(TAG, "Coordinates string: $coordinates")
                
                val points = coordinates.split(",").map { coord ->
                    val parts = coord.trim().split(" ")
                    if (parts.size >= 2) {
                        val lon = parts[0].toDouble()
                        val lat = parts[1].toDouble()
                        Log.d(TAG, "Parsed point: lat=$lat, lon=$lon")
                        LatLng(lat, lon)
                    } else {
                        Log.w(TAG, "Invalid coordinate format: $coord")
                        null
                    }
                }.filterNotNull()

                Log.d(TAG, "Parsed ${points.size} valid points")
                
                if (points.isNotEmpty()) {
                    val overlays = mapOverlays
                    if (overlays == null) {
                        pendingFencePolygons += points
                        if (zoomToPolygon) pendingFenceCamera = points
                    } else {
                        overlays.addFence(points)
                        if (zoomToPolygon) overlays.fit(points)
                    }
                    
                    Log.d(TAG, "Polygon successfully displayed on map")
                    return true
                } else {
                    Log.w(TAG, "No valid points found in WKT")
                    return false
                }
            } else {
                Log.w(TAG, "WKT does not match expected POLYGON format")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying polygon on map", e)
            return false
        }
    }
}
