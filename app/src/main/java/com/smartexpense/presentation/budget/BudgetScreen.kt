package com.smartexpense.presentation.budget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartexpense.R
import com.smartexpense.presentation.CategorySpecs
import com.smartexpense.presentation.categorySpecById

/**
 * Budget setup screen for monthly limits and alert thresholds by category.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onNavigateBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val limitDrafts = remember { mutableStateMapOf<Long, String>() }
    val thresholdDrafts = remember { mutableStateMapOf<Long, Float>() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.budget_settings_title)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(text = stringResource(id = R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(CategorySpecs, key = { it.id }) { category ->
                    val currentBudget = uiState.budgets.firstOrNull { it.categoryId == category.id }
                    val limitValue = limitDrafts[category.id] ?: currentBudget?.monthlyLimit?.toString().orEmpty()
                    val thresholdValue = thresholdDrafts[category.id] ?: currentBudget?.alertThresholdPercent?.toFloat() ?: 150f
                    val hasUnsavedChanges =
                        limitValue != (currentBudget?.monthlyLimit?.toString().orEmpty()) ||
                            thresholdValue.toInt() != (currentBudget?.alertThresholdPercent ?: 150)

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (hasUnsavedChanges) {
                                    Modifier.border(
                                        BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                                        RoundedCornerShape(12.dp)
                                    )
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val spec = categorySpecById(category.id)
                            Text(text = stringResource(id = spec.nameRes), style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = if (currentBudget == null) {
                                    stringResource(id = R.string.not_set)
                                } else {
                                    stringResource(id = R.string.current_limit_amount, currentBudget.monthlyLimit)
                                }
                            )
                            OutlinedTextField(
                                value = limitValue,
                                onValueChange = { limitDrafts[category.id] = it },
                                label = { Text(text = stringResource(id = R.string.monthly_limit)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = stringResource(id = R.string.alert_threshold_percent, thresholdValue.toInt())
                            )
                            Slider(
                                value = thresholdValue,
                                onValueChange = { thresholdDrafts[category.id] = it },
                                valueRange = 100f..200f
                            )
                            Button(
                                onClick = {
                                    val parsedLimit = limitValue.toDoubleOrNull() ?: 0.0
                                    viewModel.onSaveBudget(
                                        categoryId = category.id,
                                        limit = parsedLimit,
                                        threshold = thresholdValue.toInt()
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = stringResource(id = R.string.save))
                            }
                        }
                    }
                }
            }
        }
    }
}

