package Ir.co.tfs.farazaman.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import Ir.co.tfs.farazaman.map.BaranMapStyle
import Ir.co.tfs.farazaman.map.MapLibreOverlayController
import Ir.co.tfs.farazaman.presentation.base.BaseActivity
import Ir.co.tfs.farazaman.presentation.compose.ContractorViolationLocationEvent
import Ir.co.tfs.farazaman.presentation.compose.ContractorViolationLocationMutation
import Ir.co.tfs.farazaman.presentation.compose.ContractorViolationLocationReducer
import Ir.co.tfs.farazaman.presentation.compose.ContractorViolationLocationScreen
import Ir.co.tfs.farazaman.presentation.compose.ContractorViolationLocationUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

/** Compose route for the read-only violation map; MapView lifecycle remains Activity-owned. */
@AndroidEntryPoint
class ContractorViolationLocationActivity : BaseActivity() {
    private lateinit var mapView: MapView
    private var state by mutableStateOf(ContractorViolationLocationUiState())
    private var point: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView = MapView(this).also { it.onCreate(savedInstanceState) }

        val latitude = intent.getDoubleExtra(EXTRA_LATITUDE, Double.NaN)
        val longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, Double.NaN)
        if (coordinatesAreUsable(latitude, longitude)) {
            point = LatLng(latitude, longitude)
            state = state.copy(latitude = latitude, longitude = longitude)
            loadMap()
        } else {
            state = ContractorViolationLocationReducer.reduce(
                state,
                ContractorViolationLocationMutation.Failed("مختصات نقص معتبر نیست."),
            )
        }

        setContent {
            FaraZamanTheme {
                ContractorViolationLocationScreen(
                    state = state,
                    onEvent = { event ->
                        when (event) {
                            ContractorViolationLocationEvent.Back -> finish()
                            ContractorViolationLocationEvent.Retry -> loadMap()
                        }
                    },
                    mapContent = {
                        AndroidView(
                            factory = { mapView },
                            modifier = Modifier.fillMaxSize(),
                        )
                    },
                )
            }
        }
    }

    private fun loadMap() {
        val target = point ?: return
        state = ContractorViolationLocationReducer.reduce(state, ContractorViolationLocationMutation.Loading)
        mapView.getMapAsync { map ->
            map.uiSettings.isCompassEnabled = false
            map.uiSettings.isLogoEnabled = false
            lifecycleScope.launch {
                runCatching { BaranMapStyle.load(this@ContractorViolationLocationActivity) }
                    .onSuccess { styleJson ->
                        map.setStyle(Style.Builder().fromJson(styleJson)) { style ->
                            MapLibreOverlayController(map, style).apply {
                                updateCurrentLocation(target)
                                center(target)
                            }
                            state = ContractorViolationLocationReducer.reduce(
                                state,
                                ContractorViolationLocationMutation.Ready(target.latitude, target.longitude),
                            )
                        }
                    }
                    .onFailure { error ->
                        state = ContractorViolationLocationReducer.reduce(
                            state,
                            ContractorViolationLocationMutation.Failed(
                                error.message ?: "بارگذاری نقشه انجام نشد.",
                            ),
                        )
                    }
            }
        }
    }

    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { mapView.onPause(); super.onPause() }
    override fun onStop() { mapView.onStop(); super.onStop() }
    override fun onDestroy() { mapView.onDestroy(); super.onDestroy() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    private fun coordinatesAreUsable(latitude: Double, longitude: Double): Boolean =
        latitude.isFinite() && longitude.isFinite() &&
            latitude in -90.0..90.0 && longitude in -180.0..180.0 &&
            !(latitude == 0.0 && longitude == 0.0)

    companion object {
        const val EXTRA_LATITUDE = "contractor_violation_latitude"
        const val EXTRA_LONGITUDE = "contractor_violation_longitude"
    }
}
