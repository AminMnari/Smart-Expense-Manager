package com.smartexpense.presentation.expenses

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartexpense.R
import com.smartexpense.domain.model.Expense
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Manual entry screen for creating a new expense.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var merchantName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var categoryId by remember { mutableStateOf(0L) }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val formatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }

    LaunchedEffect(categories) {
        if (categoryId == 0L || categories.none { it.id == categoryId }) {
            categoryId = categories.firstOrNull()?.id ?: 0L
        }
    }

    val datePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth -> date = LocalDate.of(year, month + 1, dayOfMonth) },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        )
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.add_expense_title)) },
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
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = merchantName,
                onValueChange = { merchantName = it },
                label = { Text(text = stringResource(id = R.string.merchant_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text(text = stringResource(id = R.string.amount)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = date.format(formatter),
                onValueChange = {},
                readOnly = true,
                label = { Text(text = stringResource(id = R.string.date)) },
                trailingIcon = {
                    TextButton(onClick = { datePicker.show() }) {
                        Text(text = stringResource(id = R.string.pick_date))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = categories.firstOrNull { it.id == categoryId }?.name.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = stringResource(id = R.string.category)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option.name) },
                            onClick = {
                                categoryId = option.id
                                expanded = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(text = stringResource(id = R.string.notes)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Button(
                onClick = {
                    if (categoryId == 0L) return@Button
                    val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                    viewModel.onSaveExpense(
                        Expense(
                            amount = parsedAmount,
                            merchantName = merchantName,
                            categoryId = categoryId,
                            date = date,
                            notes = notes,
                            createdAt = LocalDateTime.now()
                        ),
                        onSuccess = onNavigateBack
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        }
    }
}

