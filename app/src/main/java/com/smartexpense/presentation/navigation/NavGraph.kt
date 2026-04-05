package com.smartexpense.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

/**
 * Root navigation graph with placeholder screens for all routes.
 */
@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            RoutePlaceholder(label = "Dashboard")
        }
        composable(Screen.Scan.route) {
            RoutePlaceholder(label = "Scan")
        }
        composable(
            route = Screen.ExpenseDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) {
            RoutePlaceholder(label = "Expense Detail")
        }
        composable(Screen.AddExpense.route) {
            RoutePlaceholder(label = "Add Expense")
        }
        composable(Screen.Budget.route) {
            RoutePlaceholder(label = "Budget")
        }
        composable(Screen.Alerts.route) {
            RoutePlaceholder(label = "Alerts")
        }
        composable(Screen.Insights.route) {
            RoutePlaceholder(label = "Insights")
        }
        composable(Screen.Settings.route) {
            RoutePlaceholder(label = "Settings")
        }
    }
}

/**
 * Minimal placeholder composable used while feature screens are scaffolded.
 */
@Composable
private fun RoutePlaceholder(label: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label)
    }
}

