package Ir.co.tfs.farazaman.presentation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun <E : UiEffect> RouteEffectHandler(
    effects: Flow<E>,
    onEffect: suspend (E) -> Unit,
) {
    LaunchedEffect(effects) {
        effects.collectLatest(onEffect)
    }
}
