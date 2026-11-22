package Ir.co.tfs.farazaman.presentation.design.components

import Ir.co.tfs.farazaman.presentation.design.theme.FzSemanticIntent
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier

@Immutable
data class FzSnackbarMessage(
    val message: String,
    val actionLabel: String? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val intent: FzSemanticIntent = FzSemanticIntent.Neutral,
)

suspend fun SnackbarHostState.showFzSnackbar(message: FzSnackbarMessage): SnackbarResult =
    showSnackbar(message.message, message.actionLabel, duration = message.duration)

@Composable
fun FzSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) =
    SnackbarHost(hostState, modifier) { data ->
        Snackbar(
            snackbarData = data,
            actionColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
        )
    }
