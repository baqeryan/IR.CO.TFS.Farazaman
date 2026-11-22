package Ir.co.tfs.farazaman.presentation.design.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

val FzLightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF006B5B), onPrimary = Color.White,
    secondary = Color(0xFF006B55), onSecondary = Color.White,
    background = Color(0xFFF6F8F7), onBackground = Color(0xFF212121),
    surface = Color.White, onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFE8F5F2), onSurfaceVariant = Color(0xFF4F5B58),
    outline = Color(0xFF737D7A), primaryContainer = Color(0xFFD5F5ED),
    onPrimaryContainer = Color(0xFF00382F), error = Color(0xFFBA1A1A),
    onError = Color.White, errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF93000A),
)

val FzDarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF72D8C8), onPrimary = Color(0xFF003730),
    secondary = Color(0xFF56DBC0), onSecondary = Color(0xFF00382E),
    background = Color(0xFF101413), onBackground = Color(0xFFE1E3E1),
    surface = Color(0xFF191C1B), onSurface = Color(0xFFE1E3E1),
    surfaceVariant = Color(0xFF24312E), onSurfaceVariant = Color(0xFFBFC9C6),
    outline = Color(0xFF899592), primaryContainer = Color(0xFF005144),
    onPrimaryContainer = Color(0xFFD5F5ED), error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005), errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
)

@Immutable
data class FzSemanticColors(
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
)

/** Jetsnack-inspired expressive colors, derived only from the FaraZaman green palette. */
@Immutable
data class FzExpressiveColors(
    val brand: Color,
    val brandSecondary: Color,
    val uiBackground: Color,
    val uiBorder: Color,
    val uiFloated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textHelp: Color,
    /** Foreground used on the persistent dark brand gradients. */
    val onDecorative: Color,
    val interactivePrimary: List<Color>,
    val interactiveSecondary: List<Color>,
    val decorativeGradient: List<Color>,
    val isDark: Boolean,
)

val FzLightExpressiveColors = FzExpressiveColors(
    brand = Color(0xFF005F50),
    brandSecondary = Color(0xFF00A387),
    uiBackground = Color(0xFFF6F8F7),
    uiBorder = Color(0xFFD7E2DF),
    uiFloated = Color.White,
    textPrimary = Color(0xFF15312B),
    textSecondary = Color(0xFF46615B),
    textHelp = Color(0xFF71827E),
    onDecorative = Color.White,
    interactivePrimary = listOf(Color(0xFF006B5B), Color(0xFF00A387)),
    interactiveSecondary = listOf(Color(0xFFD5F5ED), Color(0xFFB7E8DC)),
    decorativeGradient = listOf(Color(0xFF004C40), Color(0xFF006B5B), Color(0xFF00A387)),
    isDark = false,
)

val FzDarkExpressiveColors = FzExpressiveColors(
    brand = Color(0xFF72D8C8),
    brandSecondary = Color(0xFF4DBEAA),
    uiBackground = Color(0xFF101413),
    uiBorder = Color(0xFF334540),
    uiFloated = Color(0xFF202725),
    textPrimary = Color(0xFFE1F1ED),
    textSecondary = Color(0xFFBFC9C6),
    textHelp = Color(0xFF899592),
    onDecorative = Color(0xFFF1FFFB),
    interactivePrimary = listOf(Color(0xFF006B5B), Color(0xFF3DB9A3)),
    interactiveSecondary = listOf(Color(0xFF24413A), Color(0xFF315A51)),
    decorativeGradient = listOf(Color(0xFF003D34), Color(0xFF00594C), Color(0xFF168F7A)),
    isDark = true,
)

val FzLightSemanticColors = FzSemanticColors(
    success = Color(0xFF176B45), onSuccess = Color.White,
    warning = Color(0xFF855400), onWarning = Color.White,
    warningContainer = Color(0xFFFFF1CF), onWarningContainer = Color(0xFF3C2A00),
    info = Color(0xFF315DA8), onInfo = Color.White,
)
val FzDarkSemanticColors = FzSemanticColors(
    success = Color(0xFF76D7A4), onSuccess = Color(0xFF003921),
    warning = Color(0xFFFFBA43), onWarning = Color(0xFF452B00),
    warningContainer = Color(0xFF493700), onWarningContainer = Color(0xFFFFDEA1),
    info = Color(0xFFAEC6FF), onInfo = Color(0xFF002E69),
)
