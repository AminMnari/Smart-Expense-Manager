package com.smartexpense.presentation.dashboard

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartexpense.R
import com.smartexpense.domain.model.Budget
import com.smartexpense.presentation.CategorySpecs
import com.smartexpense.presentation.budget.BudgetViewModel
import com.smartexpense.presentation.categorySpecById
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Dashboard screen showing monthly totals, category spend, budget progress, and recent expenses.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToScan: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
    budgetViewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val budgetState by budgetViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.dashboard_title)) },
                actions = {
                    IconButton(onClick = onNavigateToBudget) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = stringResource(id = R.string.cd_budget_settings)
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(id = R.string.cd_open_settings)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToScan) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.cd_add_expense)
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val recentExpenses = uiState.expenses.sortedByDescending { it.date }.take(5)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    MonthSelector(
                        selectedLabel = uiState.selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                        onPrevious = { viewModel.onMonthChanged(uiState.selectedMonth.minusMonths(1)) },
                        onNext = { viewModel.onMonthChanged(uiState.selectedMonth.plusMonths(1)) }
                    )
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = stringResource(id = R.string.total_spent))
                            Text(
                                text = stringResource(id = R.string.currency_amount_tnd, uiState.totalSpent),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = stringResource(id = R.string.category_breakdown), style = MaterialTheme.typography.titleMedium)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CategorySpecs.forEach { spec ->
                                Card(modifier = Modifier.width(144.dp)) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = stringResource(id = spec.nameRes), style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = stringResource(
                                                id = R.string.currency_amount_tnd,
                                                uiState.categoryTotals["Category ${spec.id}"] ?: 0.0
                                            ),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(id = R.string.budget_progress),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                items(budgetState.budgets, key = { it.id }) { budget ->
                    BudgetProgressItem(
                        budget = budget,
                        spent = uiState.categoryTotals["Category ${budget.categoryId}"] ?: 0.0
                    )
                }

                item {
                    Text(
                        text = stringResource(id = R.string.recent_expenses),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                items(recentExpenses, key = { it.id }) { expense ->
                    val spec = categorySpecById(expense.categoryId)
                    ListItem(
                        headlineContent = { Text(text = expense.merchantName, fontWeight = FontWeight.SemiBold) },
                        supportingContent = {
                            Text(
                                text = stringResource(
                                    id = R.string.expense_list_meta,
                                    stringResource(id = spec.nameRes),
                                    expense.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))
                                )
                            )
                        },
                        trailingContent = {
                            Text(
                                text = stringResource(id = R.string.currency_amount_tnd, expense.amount),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetProgressItem(
    budget: Budget,
    spent: Double
) {
    val categorySpec = categorySpecById(budget.categoryId)
    val progress = if (budget.monthlyLimit == 0.0) 0f else (spent / budget.monthlyLimit).toFloat()
    val progressColor = if (progress > 0.8f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = categorySpec.icon, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = categorySpec.nameRes),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Text(
                text = stringResource(id = R.string.spent_over_limit, spent, budget.monthlyLimit),
                style = MaterialTheme.typography.bodySmall
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = progressColor
            )
        }
    }
}

/**
 * Shared month selector row with previous and next controls.
 */
@Composable
fun MonthSelector(
    selectedLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(id = R.string.month_previous)
            )
        }
        Text(text = selectedLabel, style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(id = R.string.month_next)
            )
        }
    }
}

