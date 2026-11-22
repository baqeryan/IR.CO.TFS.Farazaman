package Ir.co.tfs.farazaman.presentation.login

import android.os.Bundle
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import Ir.co.tfs.farazaman.auth.OidcAuthManager
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import org.json.JSONArray
import org.json.JSONObject

class CitySelectionBottomSheet : BottomSheetDialogFragment() {

    var onCitySelected: ((LoginActivity.ProvinceInstance) -> Unit)? = null
    private var pendingSelectedCity: LoginActivity.ProvinceInstance? = null

    companion object {
        private const val ARG_CITIES_JSON = "cities_json"
        private const val ARG_IS_LOADING = "is_loading"
        private const val SHEET_HEIGHT_RATIO = 0.75f

        fun newInstance(
            cities: List<LoginActivity.ProvinceInstance>,
            isLoading: Boolean,
        ): CitySelectionBottomSheet {
            val citiesJson = JSONArray().apply {
                cities.forEach { city ->
                    put(
                        JSONObject().apply {
                            put("uid", city.uid)
                            put("title", city.title)
                            put("baseAddress", city.baseAddress)
                            put("enabled", city.enabled)
                            put("clientId", city.clientId)
                            put("scope", city.scope)
                            put("loginMode", city.loginMode)
                        },
                    )
                }
            }.toString()
            return CitySelectionBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_CITIES_JSON, citiesJson)
                    putBoolean(ARG_IS_LOADING, isLoading)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        val isLoading = arguments?.getBoolean(ARG_IS_LOADING, false) == true
        val cities = parseCities(arguments?.getString(ARG_CITIES_JSON).orEmpty())
        setContent {
            FaraZamanTheme {
                CitySelectionScreen(
                    state = CitySelectionUiState(cities = cities, loading = isLoading),
                    onEvent = { event ->
                        when (event) {
                            is CitySelectionEvent.Select -> {
                                pendingSelectedCity = event.city
                                dismiss()
                            }
                        }
                    },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?: return
        bottomSheet.layoutParams = bottomSheet.layoutParams.apply {
            height = (resources.displayMetrics.heightPixels * SHEET_HEIGHT_RATIO).toInt()
        }
        BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val selectedCity = pendingSelectedCity ?: return
        pendingSelectedCity = null
        onCitySelected?.invoke(selectedCity)
    }

    private fun parseCities(json: String): List<LoginActivity.ProvinceInstance> {
        if (json.isBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    add(
                        LoginActivity.ProvinceInstance(
                            uid = obj.optLong("uid"),
                            title = obj.optString("title"),
                            baseAddress = obj.optString("baseAddress"),
                            enabled = obj.optBoolean("enabled", true),
                            clientId = obj.optString("clientId", OidcAuthManager.DEFAULT_CLIENT_ID),
                            scope = obj.optString("scope", OidcAuthManager.DEFAULT_SCOPES),
                            loginMode = obj.optInt("loginMode", LoginModeHelper.MODE_WEB),
                        ),
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
