package Ir.co.tfs.farazaman.presentation.design.state

import androidx.compose.runtime.Immutable

@Immutable
data class FzFieldState(
    val enabled: Boolean = true,
    val readOnly: Boolean = false,
    val required: Boolean = false,
    val errorMessage: String? = null,
)

@Immutable
data class FzOverlayState(
    val visible: Boolean = false,
    val dismissible: Boolean = true,
    val modal: Boolean = false,
)

@Immutable
data class FzCollectionState<T>(
    val items: List<T> = emptyList(),
    val loading: Boolean = false,
    val errorMessage: String? = null,
)

