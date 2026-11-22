package Ir.co.tfs.farazaman.presentation.login

import androidx.compose.runtime.Composable
import Ir.co.tfs.farazaman.presentation.design.preview.FzDarkPreview
import Ir.co.tfs.farazaman.presentation.design.preview.FzLightPreview
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme

@FzLightPreview
@Composable
private fun LoginWebPreview() {
    FaraZamanTheme {
        LoginScreen(
            state = LoginUiState(
                cities = previewCities,
                selectedCityUid = previewCities.first().uid,
                webLogin = true,
                loginEnabled = true,
            ),
            onEvent = {},
        )
    }
}

@FzDarkPreview
@Composable
private fun LoginPasswordPreview() {
    FaraZamanTheme {
        LoginScreen(
            state = LoginUiState(
                cities = previewCities,
                selectedCityUid = previewCities.last().uid,
                webLogin = false,
                username = "operator",
                password = "password",
                loginEnabled = true,
            ),
            onEvent = {},
        )
    }
}

private val previewCities = listOf(
    LoginActivity.ProvinceInstance(
        uid = 1,
        title = "بابل",
        baseAddress = "https://babol.example.ir/",
        enabled = true,
    ),
    LoginActivity.ProvinceInstance(
        uid = 2,
        title = "تهران",
        baseAddress = "https://tehran.example.ir/",
        enabled = true,
        loginMode = LoginModeHelper.MODE_PASSWORD,
    ),
)

@FzLightPreview
@Composable
private fun LoginLoadingCitiesPreview() {
    FaraZamanTheme {
        LoginScreen(state = LoginUiState(citiesLoading = true), onEvent = {})
    }
}

@FzLightPreview
@Composable
private fun CitySelectionMatrixPreview() {
    FaraZamanTheme { CitySelectionScreen(CitySelectionUiState(), {}) }
}

@FzLightPreview
@Composable
private fun OidcLoginMatrixPreview() {
    FaraZamanTheme {
        OidcLoginScreen(
            title = "ورود امن",
            progress = 45,
            onHandleTouch = {},
            createWebView = { android.webkit.WebView(it) },
        )
    }
}
