package com.smartexpense.presentation.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartexpense.R
import com.smartexpense.domain.model.Expense
import com.smartexpense.presentation.CategorySpecs
import com.smartexpense.presentation.categorySpecById
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Expense list screen with category filters and swipe-to-delete support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onNavigateToExpenseDetail: (Long) -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showFilters by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.expenses_title)) },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(id = R.string.cd_toggle_filters)
                        )
                    }
                }
            )
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showFilters) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.filter.categoryId == null,
                            onClick = { viewModel.onFilterChanged(ExpenseFilter()) },
                            label = { Text(text = stringResource(id = R.string.all)) }
                        )
                        CategorySpecs.forEach { category ->
                            FilterChip(
                                selected = uiState.filter.categoryId == category.id,
                                onClick = {
                                    viewModel.onFilterChanged(
                                        ExpenseFilter(categoryId = category.id)
                                    )
                                },
                                label = { Text(text = stringResource(id = category.nameRes)) }
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.expenses,
                        key = { it.id }
                    ) { expense ->
                        val deletedMessage = stringResource(id = R.string.expense_deleted_amount, expense.amount)
                        val undoLabel = stringResource(id = R.string.undo)
                        DismissibleExpenseCard(
                            expense = expense,
                            onClick = { onNavigateToExpenseDetail(expense.id) },
                            onDelete = {
                                viewModel.onDeleteExpense(expense)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = deletedMessage,
                                        actionLabel = undoLabel
                                    )
                                    if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                                        viewModel.onRestoreExpense(expense)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Swipe-to-dismiss expense card item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissibleExpenseCard(
    expense: Expense,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = {
        if (it != androidx.compose.material3.SwipeToDismissBoxValue.Settled) {
            onDelete()
            true
        } else {
            false
        }
    })

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = stringResource(id = R.string.delete))
            }
        }
    ) {
        val category = categorySpecById(expense.categoryId)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onClick = onClick
        ) {
            ListItem(
                headlineContent = { Text(text = expense.merchantName, fontWeight = FontWeight.Bold) },
                supportingContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = stringResource(
                                id = R.string.expense_list_meta,
                                stringResource(id = category.nameRes),
                                expense.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))
                            )
                        )
                    }
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


