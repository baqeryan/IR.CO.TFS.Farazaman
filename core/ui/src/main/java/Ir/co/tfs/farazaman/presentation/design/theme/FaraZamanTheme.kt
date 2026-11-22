package Ir.co.tfs.farazaman.presentation.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val LocalFzSpacing = staticCompositionLocalOf { FzSpacing() }
private val LocalFzElevation = staticCompositionLocalOf { FzElevation() }
private val LocalFzMotion = staticCompositionLocalOf { FzMotion() }
private val LocalFzSizes = staticCompositionLocalOf { FzSizes }
private val LocalFzSemanticColors = staticCompositionLocalOf { FzLightSemanticColors }
private val LocalFzExtendedShapes = staticCompositionLocalOf { FzExtendedShapes() }
private val LocalFzExpressiveColors = staticCompositionLocalOf { FzLightExpressiveColors }

object FaraZamanThemeTokens {
    val spacing: FzSpacing @Composable @ReadOnlyComposable get() = LocalFzSpacing.current
    val elevation: FzElevation @Composable @ReadOnlyComposable get() = LocalFzElevation.current
    val motion: FzMotion @Composable @ReadOnlyComposable get() = LocalFzMotion.current
    val sizes: FzComponentSizes @Composable @ReadOnlyComposable get() = LocalFzSizes.current
    val semanticColors: FzSemanticColors @Composable @ReadOnlyComposable get() = LocalFzSemanticColors.current
    val extendedShapes: FzExtendedShapes @Composable @ReadOnlyComposable get() = LocalFzExtendedShapes.current
    val colors: FzExpressiveColors @Composable @ReadOnlyComposable get() = LocalFzExpressiveColors.current
}

@Composable
fun FaraZamanTheme(configuration: ThemeConfiguration = ThemeConfiguration(), content: @Composable () -> Unit) {
    val dark = when (configuration.mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalFzSpacing provides FzSpacing(), LocalFzElevation provides FzElevation(),
        LocalFzMotion provides FzMotion(reduceMotion = configuration.reduceMotion),
        LocalFzSizes provides FzSizes,
        LocalFzSemanticColors provides if (dark) FzDarkSemanticColors else FzLightSemanticColors,
        LocalFzExtendedShapes provides FzExtendedShapes(),
        LocalFzExpressiveColors provides if (dark) FzDarkExpressiveColors else FzLightExpressiveColors,
    ) {
        MaterialTheme(
            colorScheme = if (dark) FzDarkColors else FzLightColors,
            typography = FzTypography,
            shapes = FzShapes,
            content = content,
        )
    }
}
