package com.smartexpense.presentation.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartexpense.R
import com.smartexpense.presentation.dashboard.MonthSelector
import com.smartexpense.utils.CurrencyHelper
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Insight screen that generates and displays AI monthly spending insights.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightScreen(viewModel: InsightViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currency = CurrencyHelper.getSavedCurrency(context)
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(id = R.string.ai_insights_title)) })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MonthSelector(
                    selectedLabel = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                    onPrevious = { selectedMonth = selectedMonth.minusMonths(1) },
                    onNext = { selectedMonth = selectedMonth.plusMonths(1) }
                )
            }
            item {
                ElevatedButton(
                    onClick = { viewModel.onGenerateInsight(selectedMonth) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.generate_insight))
                }
            }

            uiState.error?.let { errorMessage ->
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                TextButton(onClick = viewModel::clearError) {
                                    Text(text = stringResource(id = R.string.dismiss))
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(text = stringResource(id = R.string.analyzing_spending))
                    }
                }
            }

            uiState.insight?.let { insight ->
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { (insight.budgetScore.coerceIn(0, 100) / 100f) },
                                    modifier = Modifier.size(96.dp),
                                    strokeWidth = 8.dp
                                )
                                Text(
                                    text = insight.budgetScore.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(text = stringResource(id = R.string.budget_score))
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(id = R.string.summary),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = insight.summary)
                        }
                    }
                }

                item {
                    Text(text = stringResource(id = R.string.top_categories), style = MaterialTheme.typography.titleMedium)
                }
                itemsIndexed(insight.topCategories.take(3)) { _, categorySpend ->
                    val icon = when (categorySpend.trend.lowercase(Locale.getDefault())) {
                        "up" -> Icons.Default.ArrowUpward
                        "down" -> Icons.Default.ArrowDownward
                        else -> Icons.Default.Remove
                    }
                    val tint = when (categorySpend.trend.lowercase(Locale.getDefault())) {
                        "up" -> MaterialTheme.colorScheme.error
                        "down" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    ListItem(
                        headlineContent = { Text(text = categorySpend.category) },
                        supportingContent = {
                            Text(text = CurrencyHelper.formatAmount(categorySpend.amount, currency))
                        },
                        trailingContent = { Icon(imageVector = icon, contentDescription = null, tint = tint) }
                    )
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = stringResource(id = R.string.savings_tips), style = MaterialTheme.typography.titleMedium)
                            insight.savingsTips.forEachIndexed { index, tip ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = stringResource(id = R.string.tip_number, index + 1))
                                    Text(text = tip)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

