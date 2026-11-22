package Ir.co.tfs.farazaman.presentation.compose

sealed interface CommonUiState<out T> : UiState {
    data object Loading : CommonUiState<Nothing>
    data class Content<T>(val value: T) : CommonUiState<T>
    data class Empty(val message: String? = null) : CommonUiState<Nothing>
    data class Error(val message: String, val canRetry: Boolean = true) : CommonUiState<Nothing>
}

object FzSemantics {
    const val Loading = "fz_loading"
    const val Error = "fz_error"
    const val Retry = "fz_retry"
    const val Submit = "fz_submit"
}
