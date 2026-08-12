package Ir.co.tfs.farazaman.presentation.compose

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.LayoutDirection

private class RoleSelectionStateProvider : PreviewParameterProvider<RoleSelectionUiState> {
    override val values = sequenceOf(
        RoleSelectionUiState(
            showSupervisor = true,
            showDriver = true,
            selectedRole = RoleOption.SUPERVISOR,
        ),
        RoleSelectionUiState(
            showSupervisor = true,
            showDriver = true,
            selectedRole = RoleOption.DRIVER,
            showBack = true,
        ),
        RoleSelectionUiState(
            showSupervisor = true,
            showDriver = false,
            selectedRole = RoleOption.SUPERVISOR,
        ),
    )
}

@Preview(
    name = "Role Selection - Content",
    showBackground = true,
    locale = "fa",
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun RoleSelectionContentPreview(
    @PreviewParameter(RoleSelectionStateProvider::class) state: RoleSelectionUiState,
) {
    RoleSelectionPreviewContent(state = state, darkTheme = false)
}

@Preview(
    name = "Role Selection - Disabled",
    showBackground = true,
    locale = "fa",
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun RoleSelectionDisabledPreview() {
    RoleSelectionPreviewContent(
        state = RoleSelectionUiState(
            showSupervisor = false,
            showDriver = false,
        ),
        darkTheme = false,
    )
}

@Preview(
    name = "Role Selection - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "fa",
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun RoleSelectionDarkPreview() {
    RoleSelectionPreviewContent(
        state = RoleSelectionUiState(
            selectedRole = RoleOption.DRIVER,
            showBack = true,
        ),
        darkTheme = true,
    )
}

@Composable
private fun RoleSelectionPreviewContent(
    state: RoleSelectionUiState,
    darkTheme: Boolean,
) {
    FaraZamanTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            RoleSelectionScreen(
                state = state,
                onRoleSelected = {},
                onBack = {},
                onProfile = {},
                onEnter = {},
            )
        }
    }
}
