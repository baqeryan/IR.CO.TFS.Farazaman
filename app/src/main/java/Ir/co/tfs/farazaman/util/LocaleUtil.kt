package Ir.co.tfs.farazaman.util

import android.content.Context
import java.util.Locale

object LocaleUtil {
    fun setLocale(context: Context, language: String): Context {
        val locale=Locale(language)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }
}
