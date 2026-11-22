package Ir.co.tfs.farazaman.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzEmptyState
import Ir.co.tfs.farazaman.presentation.design.components.FzLoading
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens

data class CitySelectionUiState(
    val cities: List<LoginActivity.ProvinceInstance> = emptyList(),
    val loading: Boolean = false,
)

sealed interface CitySelectionEvent {
    data class Select(val city: LoginActivity.ProvinceInstance) : CitySelectionEvent
}

@Composable
fun CitySelectionScreen(
    state: CitySelectionUiState,
    onEvent: (CitySelectionEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = FaraZamanThemeTokens.spacing
    Column(
        modifier = modifier.fillMaxSize().padding(spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Text(
            text = stringResource(R.string.login_city_hint),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        when {
            state.loading -> FzLoading(stringResource(R.string.login_cities_loading))
            state.cities.isEmpty() -> FzEmptyState(
                title = stringResource(R.string.login_city_hint),
                message = stringResource(R.string.login_cities_unavailable),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
                contentPadding = PaddingValues(bottom = spacing.md),
            ) {
                items(state.cities, key = { it.uid }) { city ->
                    FzCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onEvent(CitySelectionEvent.Select(city)) },
                    ) {
                        Text(
                            text = city.title,
                            modifier = Modifier.fillMaxWidth().padding(spacing.md),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}
