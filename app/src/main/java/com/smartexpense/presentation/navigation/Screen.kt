package com.smartexpense.presentation.navigation

/**
 * Navigation routes used across the app.
 */
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Scan : Screen("scan")
    object ExpenseDetail : Screen("expense/{id}") {
        fun createRoute(id: Long) = "expense/$id"
    }

    object AddExpense : Screen("add_expense")
    object Budget : Screen("budget")
    object Alerts : Screen("alerts")
    object Insights : Screen("insights")
    object Settings : Screen("settings")
}

