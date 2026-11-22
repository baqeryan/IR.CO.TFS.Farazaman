package Ir.co.tfs.farazaman.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzChoice
import Ir.co.tfs.farazaman.presentation.design.components.FzHeaderSize
import Ir.co.tfs.farazaman.presentation.design.components.FzLoading
import Ir.co.tfs.farazaman.presentation.design.components.FzPageTemplate
import Ir.co.tfs.farazaman.presentation.design.components.FzSelect
import Ir.co.tfs.farazaman.presentation.design.components.FzTextField
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens

data class LoginUiState(
    val cities: List<LoginActivity.ProvinceInstance> = emptyList(),
    val selectedCityUid: Long? = null,
    val citiesLoading: Boolean = false,
    val webLogin: Boolean = true,
    val username: String = "",
    val password: String = "",
    val loginEnabled: Boolean = false,
)

sealed interface LoginUiEvent {
    data class CitySelected(val city: LoginActivity.ProvinceInstance) : LoginUiEvent
    data object Login : LoginUiEvent
    data object About : LoginUiEvent
    data class UsernameChanged(val value: String) : LoginUiEvent
    data class PasswordChanged(val value: String) : LoginUiEvent
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEvent: (LoginUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    FzPageTemplate(
        title = stringResource(R.string.login_title),
        headerSize = FzHeaderSize.Standard,
        modifier = modifier,
        headerBelowTitle = {
            Image(
                painter = painterResource(R.drawable.farazaman_log_),
                contentDescription = stringResource(R.string.login_logo_description),
                modifier = Modifier.size(FaraZamanThemeTokens.sizes.logoLarge),
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.sm)
                    .padding(bottom = spacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                Spacer(Modifier.size(spacing.lg))

                if (state.citiesLoading) {
                    FzLoading(stringResource(R.string.login_cities_loading))
                }

                FzSelect(
                    value = state.selectedCityUid,
                    options = state.cities.map {
                        FzChoice(
                            value = it.uid,
                            label = it.title,
                            supportingText = it.baseAddress,
                        )
                    },
                    onValueChange = { uid ->
                        state.cities.firstOrNull { it.uid == uid }?.let {
                            onEvent(LoginUiEvent.CitySelected(it))
                        }
                    },
                    label = stringResource(R.string.login_city_label),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.citiesLoading && state.cities.isNotEmpty(),
                )

                if (!state.webLogin) {
                    FzTextField(
                        value = state.username,
                        onValueChange = { onEvent(LoginUiEvent.UsernameChanged(it)) },
                        label = stringResource(R.string.login_username_label),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    FzTextField(
                        value = state.password,
                        onValueChange = { onEvent(LoginUiEvent.PasswordChanged(it)) },
                        label = stringResource(R.string.login_password_label),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                    )
                }
                FzButton(
                    text = stringResource(R.string.login_button),
                    onClick = { onEvent(LoginUiEvent.Login) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.loginEnabled,
                )
            }

            Text(
                text = stringResource(R.string.login_footer),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .heightIn(min = FaraZamanThemeTokens.sizes.minimumTouchTarget)
                    .clickable { onEvent(LoginUiEvent.About) }
                    .padding(bottom = spacing.sm),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}
