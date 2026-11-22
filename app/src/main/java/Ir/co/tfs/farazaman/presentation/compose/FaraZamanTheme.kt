package Ir.co.tfs.farazaman.presentation.compose

import Ir.co.tfs.farazaman.presentation.design.theme.ThemeConfiguration
import Ir.co.tfs.farazaman.presentation.design.theme.ThemeMode
import androidx.compose.runtime.Composable

@Composable
fun FaraZamanTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit,
) = Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme(
    configuration = ThemeConfiguration(
        mode = when (darkTheme) {
            true -> ThemeMode.Dark
            false -> ThemeMode.Light
            null -> ThemeMode.System
        },
    ),
    content = content,
)
