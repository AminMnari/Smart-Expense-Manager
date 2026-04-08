package com.smartexpense.presentation.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.smartexpense.R
import com.smartexpense.presentation.categorySpecById
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Expense details screen with edit and delete actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expenseId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val expense = uiState.expenses.firstOrNull { it.id == expenseId }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.expense_detail_title)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(text = stringResource(id = R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(id = R.string.cd_edit_expense))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(id = R.string.cd_delete_expense))
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
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(horizontal = 16.dp))
            }
        } else if (expense == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text(text = stringResource(id = R.string.expense_not_found), style = MaterialTheme.typography.titleMedium)
            }
        } else {
            val category = categorySpecById(expense.categoryId)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = stringResource(id = R.string.merchant_name_value, expense.merchantName))
                        Text(text = stringResource(id = R.string.amount_value, expense.amount))
                        Text(text = stringResource(id = R.string.category_value, stringResource(id = category.nameRes)))
                        Text(
                            text = stringResource(
                                id = R.string.date_value,
                                expense.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))
                            )
                        )
                        Text(text = stringResource(id = R.string.notes_value, expense.notes.ifBlank { stringResource(id = R.string.not_available) }))
                        if (expense.isAiCategorized) {
                            AssistChip(onClick = {}, label = { Text(text = stringResource(id = R.string.ai_categorized)) })
                        }
                    }
                }

                expense.receiptImagePath?.let { path ->
                    AsyncImage(
                        model = path,
                        contentDescription = stringResource(id = R.string.cd_receipt_image),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                        error = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                }
            }
        }

        if (showDeleteDialog && expense != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(text = stringResource(id = R.string.delete_expense)) },
                text = { Text(text = stringResource(id = R.string.delete_expense_confirm)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.onDeleteExpense(expense)
                            onNavigateBack()
                        }
                    ) {
                        Text(text = stringResource(id = R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                }
            )
        }
    }
}

