package com.smartexpense.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.View
import java.util.Locale

/**
 * Persists and applies app language preferences.
 */
object LanguageHelper {
    fun applyLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        if (languageCode == "ar") {
            (context as? Activity)?.window?.decorView?.layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
    }

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("language", "en") ?: "en"
    }

    fun saveLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putString("language", languageCode)
            .apply()
    }
}

