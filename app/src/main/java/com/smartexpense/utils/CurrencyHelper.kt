package com.smartexpense.utils

import android.content.Context
import java.util.Locale

/**
 * Persists user currency preference and formats amounts.
 */
object CurrencyHelper {
    val supportedCurrencies = listOf(
        "TND", "USD", "EUR", "GBP", "SAR", "AED", "MAD", "DZD"
    )

    fun getSavedCurrency(context: Context): String {
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("currency", "TND") ?: "TND"
    }

    fun saveCurrency(context: Context, currency: String) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putString("currency", currency)
            .apply()
    }

    fun formatAmount(amount: Double, currency: String): String {
        return String.format(Locale.getDefault(), "%.2f %s", amount, currency)
    }
}

