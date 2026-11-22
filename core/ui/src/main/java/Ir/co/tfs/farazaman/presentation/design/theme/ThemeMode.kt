package Ir.co.tfs.farazaman.presentation.design.theme

import androidx.compose.runtime.Immutable

enum class ThemeMode { System, Light, Dark }

@Immutable
data class ThemeConfiguration(
    val mode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = false,
    val reduceMotion: Boolean = false,
)
