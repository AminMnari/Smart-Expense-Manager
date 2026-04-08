package com.smartexpense.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smartexpense.R
import com.smartexpense.presentation.alerts.AlertsScreen
import com.smartexpense.presentation.budget.BudgetScreen
import com.smartexpense.presentation.dashboard.DashboardScreen
import com.smartexpense.presentation.expenses.AddExpenseScreen
import com.smartexpense.presentation.expenses.ExpenseDetailScreen
import com.smartexpense.presentation.insights.InsightScreen
import com.smartexpense.presentation.scan.ScanScreen
import com.smartexpense.presentation.settings.SettingsScreen

/**
 * Root navigation graph for all app routes.
 */
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val tabRoutes = setOf(
        Screen.Dashboard.route,
        Screen.Scan.route,
        Screen.Alerts.route,
        Screen.Insights.route
    )
    val tabs = listOf(
        BottomNavItem(Screen.Dashboard.route, R.string.dashboard_title, Icons.Default.Home, R.string.cd_tab_dashboard),
        BottomNavItem(Screen.Scan.route, R.string.scan_receipt_title, Icons.Default.QrCodeScanner, R.string.cd_tab_scan),
        BottomNavItem(Screen.Alerts.route, R.string.spending_alerts_title, Icons.Default.Notifications, R.string.cd_tab_alerts),
        BottomNavItem(Screen.Insights.route, R.string.ai_insights_title, Icons.Default.Lightbulb, R.string.cd_tab_insights)
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in tabRoutes) {
                NavigationBar {
                    tabs.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Dashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = stringResource(id = item.contentDescription)
                                )
                            },
                            label = { Text(text = stringResource(id = item.label)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToBudget = { navController.navigate(Screen.Budget.route) },
                    onNavigateToScan = { navController.navigate(Screen.Scan.route) }
                )
            }
            composable(Screen.Scan.route) {
                ScanScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) },
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = Screen.ExpenseDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getLong("id") ?: 0L
                ExpenseDetailScreen(
                    expenseId = expenseId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { navController.navigate(Screen.AddExpense.route) }
                )
            }
            composable(Screen.AddExpense.route) {
                AddExpenseScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Budget.route) {
                BudgetScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Alerts.route) {
                AlertsScreen()
            }
            composable(Screen.Insights.route) {
                InsightScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBudget = { navController.navigate(Screen.Budget.route) }
                )
            }
        }
    }
}

/**
 * Bottom navigation item metadata.
 */
private data class BottomNavItem(
    val route: String,
    val label: Int,
    val icon: ImageVector,
    val contentDescription: Int
)
