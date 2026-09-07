package Ir.co.tfs.farazaman.activity

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.databinding.ActivityContractorViolationLocationBinding
import Ir.co.tfs.farazaman.map.BaranMapStyle
import Ir.co.tfs.farazaman.map.MapLibreOverlayController
import Ir.co.tfs.farazaman.presentation.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style

/** Read-only map destination. It accepts coordinates only; no backend DTO is assumed here. */
@AndroidEntryPoint
class ContractorViolationLocationActivity : BaseActivity() {

    private lateinit var binding: ActivityContractorViolationLocationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContractorViolationLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mapView.onCreate(savedInstanceState)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
            title = ""
        }
        binding.toolbar.setNavigationOnClickListener { finish() }

        val latitude = intent.getDoubleExtra(EXTRA_LATITUDE, Double.NaN)
        val longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, Double.NaN)
        if (!coordinatesAreUsable(latitude, longitude)) {
            binding.mapView.visibility = View.GONE
            binding.locationUnavailable.visibility = View.VISIBLE
            return
        }

        val point = LatLng(latitude, longitude)
        binding.mapView.getMapAsync { map ->
            map.uiSettings.isCompassEnabled = false
            map.uiSettings.isLogoEnabled = false
            lifecycleScope.launch {
                runCatching { BaranMapStyle.load(this@ContractorViolationLocationActivity) }
                    .onSuccess { styleJson ->
                        map.setStyle(Style.Builder().fromJson(styleJson)) { style ->
                            MapLibreOverlayController(map, style).apply {
                                updateCurrentLocation(point)
                                center(point)
                            }
                        }
                    }
                    .onFailure {
                        binding.locationUnavailable.visibility = View.VISIBLE
                    }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        binding.mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
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
