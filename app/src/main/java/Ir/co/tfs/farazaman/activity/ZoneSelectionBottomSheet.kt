package Ir.co.tfs.farazaman.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.data.model.VehicleZoneWorkItem
import Ir.co.tfs.farazaman.presentation.compose.ZoneSelectionEvent
import Ir.co.tfs.farazaman.presentation.compose.ZoneSelectionScreen
import Ir.co.tfs.farazaman.presentation.compose.ZoneSelectionUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.service.remote.VehicleZoneWorkService
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ZoneSelectionBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val TAG = "ZoneSelectionBottomSheet"
        private const val SHEET_HEIGHT_RATIO = 0.75f
        
        fun newInstance(onZoneSelected: (VehicleZoneWorkItem) -> Unit): ZoneSelectionBottomSheet {
            val fragment = ZoneSelectionBottomSheet()
            fragment.onZoneSelectedListener = onZoneSelected
            return fragment
        }
    }

    @Inject
    lateinit var vehicleZoneWorkService: VehicleZoneWorkService

    private var onZoneSelectedListener: ((VehicleZoneWorkItem) -> Unit)? = null
    private var uiState by mutableStateOf(ZoneSelectionUiState())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            FaraZamanTheme {
                ZoneSelectionScreen(uiState, onEvent = { event ->
                    when (event) {
                        is ZoneSelectionEvent.Select -> {
                            Log.d(TAG, "Zone selected: ${event.zone.zoneWorkVehicleID}")
                            onZoneSelectedListener?.invoke(event.zone)
                            dismiss()
                        }
                    }
                })
            }
        }
        post { fetchVehicleZones() }
    }

    override fun onStart() {
        super.onStart()
        val sheet = (dialog as? BottomSheetDialog)
            ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?: return
        sheet.layoutParams = sheet.layoutParams.apply {
            height = (resources.displayMetrics.heightPixels * SHEET_HEIGHT_RATIO).toInt()
        }
        BottomSheetBehavior.from(sheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    private fun fetchVehicleZones() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Fetching vehicle zones...")
                val response = vehicleZoneWorkService.getVehicleZoneWork()

                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d(TAG, "Response successful: ${data != null}")

                    if (data != null) {
                        val zones = data.assetVehicleZoneWorkUseCase.data
                        Log.d(TAG, "Zones count: ${zones.size}")

                        if (zones.isNotEmpty()) {
                            showZones(zones)
                        } else {
                            showEmpty()
                        }
                    } else {
                        showEmpty()
                    }
                } else {
                    Log.e(TAG, "Response error: ${response.code()} - ${response.message()}")
                    val errorMessage = Ir.co.tfs.farazaman.util.ErrorHandler.retrofitErrorMessage(
                        response,
                        "خطا در دریافت محدوده‌ها: ${response.message()}",
                    )
                    Toast.makeText(
                        requireContext(),
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    showEmpty()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching zones", e)
                Toast.makeText(
                    requireContext(),
                    "خطا در دریافت محدوده‌ها: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                showEmpty()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showZones(zones: List<VehicleZoneWorkItem>) {
        uiState = ZoneSelectionUiState(zones = zones)
    }

    private fun showEmpty() {
        uiState = ZoneSelectionUiState()
    }

    private fun showLoading(isLoading: Boolean) {
        uiState = uiState.copy(loading = isLoading)
    }
}

