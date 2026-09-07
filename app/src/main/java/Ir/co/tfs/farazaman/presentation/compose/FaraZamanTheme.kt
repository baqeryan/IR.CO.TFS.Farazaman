package Ir.co.tfs.farazaman.presentation.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val FaraZamanColors = lightColorScheme(
    primary = Color(0xFF006B5B),
    onPrimary = Color.White,
    secondary = Color(0xFF00A884),
    background = Color(0xFFF6F8F7),
    onBackground = Color(0xFF212121),
    surface = Color.White,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFE8F5F2),
    onSurfaceVariant = Color(0xFF757575),
    outline = Color(0xFFE0E0E0),
    primaryContainer = Color(0xFFD5F5ED),
    onPrimaryContainer = Color(0xFF00382F),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
)

private val FaraZamanDarkColors = darkColorScheme(
    primary = Color(0xFF72D8C8),
    onPrimary = Color(0xFF003730),
    secondary = Color(0xFF56DBC0),
    background = Color(0xFF101413),
    onBackground = Color(0xFFE1E3E1),
    surface = Color(0xFF191C1B),
    onSurface = Color(0xFFE1E3E1),
    surfaceVariant = Color(0xFF24312E),
    onSurfaceVariant = Color(0xFFBFC9C6),
    outline = Color(0xFF54615E),
    primaryContainer = Color(0xFF005144),
    onPrimaryContainer = Color(0xFFD5F5ED),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun FaraZamanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = if (darkTheme) FaraZamanDarkColors else FaraZamanColors,
            content = content,
        )
    }
}
