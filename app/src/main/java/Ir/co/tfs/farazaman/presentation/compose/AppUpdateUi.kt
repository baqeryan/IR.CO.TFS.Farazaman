package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.viewmodel.SplashUpdateState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AppUpdateUi(
    state: SplashUpdateState,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onOpenInstallSettings: () -> Unit,
    onRetry: () -> Unit,
    onLater: () -> Unit,
) {
    when (state) {
        SplashUpdateState.Checking -> CheckingUpdateIndicator()
        SplashUpdateState.Continue -> Unit
        is SplashUpdateState.Available -> UpdateDialog(
            state = state,
            confirmText = stringResource(R.string.app_update_download),
            onConfirm = onDownload,
            onLater = onLater,
        )
        is SplashUpdateState.Downloading -> UpdateDialog(
            state = state,
            confirmText = null,
            onConfirm = onDownload,
            onLater = onLater,
        )
        is SplashUpdateState.ReadyToInstall -> UpdateDialog(
            state = state,
            confirmText = stringResource(R.string.app_update_install),
            onConfirm = onInstall,
            onLater = onLater,
        )
        is SplashUpdateState.InstallPermissionRequired -> UpdateDialog(
            state = state,
            confirmText = stringResource(R.string.app_update_open_settings),
            onConfirm = onOpenInstallSettings,
            onLater = onLater,
        )
        is SplashUpdateState.Installing -> UpdateDialog(
            state = state,
            confirmText = null,
            onConfirm = onInstall,
            onLater = onLater,
        )
        is SplashUpdateState.Error -> UpdateDialog(
            state = state,
            confirmText = stringResource(R.string.app_update_retry),
            onConfirm = onRetry,
            onLater = onLater,
        )
    }
}

@Composable
private fun CheckingUpdateIndicator() {
    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 40.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.app_update_checking))
        }
    }
}

@Composable
private fun UpdateDialog(
    state: SplashUpdateState,
    confirmText: String?,
    onConfirm: () -> Unit,
    onLater: () -> Unit,
) {
    val info = requireNotNull(state.info)
    AlertDialog(
        onDismissRequest = { if (!info.mandatory) onLater() },
        title = {
            Text(
                text = stringResource(R.string.app_update_title, info.versionName),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right,
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                val message = when (state) {
                    is SplashUpdateState.Available -> info.releaseNotes
                        ?: stringResource(R.string.app_update_default_notes)
                    is SplashUpdateState.Downloading -> stringResource(
                        R.string.app_update_downloading,
                        state.progress,
                    )
                    is SplashUpdateState.ReadyToInstall -> stringResource(R.string.app_update_ready)
                    is SplashUpdateState.InstallPermissionRequired -> stringResource(
                        R.string.app_update_permission_explanation,
                    )
                    is SplashUpdateState.Installing -> stringResource(R.string.app_update_installing)
                    is SplashUpdateState.Error -> state.message
                    else -> ""
                }
                Text(
                    text = message,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right,
                )
                if (state is SplashUpdateState.Downloading) {
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { state.progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (state is SplashUpdateState.Installing) {
                    Spacer(Modifier.height(16.dp))
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        },
        confirmButton = {
            if (confirmText != null) {
                Button(onClick = onConfirm) { Text(confirmText) }
            }
        },
        dismissButton = {
            if (!info.mandatory && state !is SplashUpdateState.Downloading &&
                state !is SplashUpdateState.Installing
            ) {
                TextButton(onClick = onLater) { Text(stringResource(R.string.app_update_later)) }
            }
        },
    )
}
