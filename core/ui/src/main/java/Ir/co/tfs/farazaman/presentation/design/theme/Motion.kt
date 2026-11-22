package Ir.co.tfs.farazaman.presentation.design.theme

import androidx.compose.runtime.Immutable

@Immutable
data class FzMotion(
    val shortMillis: Int = 150,
    val mediumMillis: Int = 300,
    val longMillis: Int = 700,
    val reduceMotion: Boolean = false,
) {
    fun duration(normalMillis: Int): Int = if (reduceMotion) 0 else normalMillis
}
