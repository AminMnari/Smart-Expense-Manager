package com.smartexpense.presentation.scan

import android.Manifest
import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartexpense.R
import com.smartexpense.domain.model.ParsedExpense
import com.smartexpense.presentation.expenses.ExpenseViewModel
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

/**
 * Receipt scan screen with camera, gallery, OCR processing, and manual entry form.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel(),
    expenseViewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val categories by expenseViewModel.categories.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val photoUri = remember { mutableStateOf<Uri?>(null) }
    val formatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }

    var merchantName by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var dateText by rememberSaveable { mutableStateOf(LocalDate.now().format(formatter)) }
    var categoryId by rememberSaveable { mutableStateOf(0L) }
    var notes by rememberSaveable { mutableStateOf("") }
    var categoryExpanded by rememberSaveable { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri.value?.let { uri ->
                viewModel.onImageCaptured(uri, context)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            photoUri.value?.let { uri -> cameraLauncher.launch(uri) }
        } else {
            viewModel.resetState()
            coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.camera_permission_required))
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onImageCaptured(it, context) }
    }

    LaunchedEffect(categories) {
        if (categoryId == 0L) {
            categoryId = categories.firstOrNull()?.id ?: 0L
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ScanUiState.Review -> {
                merchantName = state.parsed.merchantName.orEmpty()
                amount = state.parsed.amount?.toString().orEmpty()
                dateText = (state.parsed.date ?: LocalDate.now()).format(formatter)
                categoryId = categories.firstOrNull {
                    it.name.equals(state.parsed.category.orEmpty(), ignoreCase = true)
                }?.id ?: categories.firstOrNull()?.id ?: 0L
            }

            ScanUiState.Saved -> {
                merchantName = ""
                amount = ""
                dateText = LocalDate.now().format(formatter)
                notes = ""
                categoryId = categories.firstOrNull()?.id ?: 0L
                viewModel.resetState()
                onNavigateToDashboard()
            }

            else -> Unit
        }
    }

    LaunchedEffect((uiState as? ScanUiState.Error)?.message) {
        (uiState as? ScanUiState.Error)?.message?.let { snackbarHostState.showSnackbar(it) }
    }

    val selectedDate = runCatching { LocalDate.parse(dateText, formatter) }.getOrElse { LocalDate.now() }
    val datePickerDialog = remember(selectedDate) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                dateText = LocalDate.of(year, month + 1, dayOfMonth).format(formatter)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.scan_receipt_title)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(text = stringResource(id = R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState is ScanUiState.Processing) {
                Text(text = stringResource(id = R.string.analyzing_receipt))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            when (uiState) {
                is ScanUiState.Review -> {
                    Text(
                        text = stringResource(id = R.string.scan_success_banner),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.primary)
                            .padding(12.dp)
                    )
                }

                is ScanUiState.Error -> {
                    Text(
                        text = stringResource(id = R.string.scan_error_banner),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.error)
                            .padding(12.dp)
                    )
                }

                else -> Unit
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        val file = File(context.filesDir, "receipt_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        photoUri.value = uri
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(id = R.string.take_photo))
                }
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(id = R.string.choose_from_gallery))
                }
            }

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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = dateText,
                onValueChange = {},
                readOnly = true,
                label = { Text(text = stringResource(id = R.string.date)) },
                trailingIcon = {
                    TextButton(onClick = { datePickerDialog.show() }) {
                        Text(text = stringResource(id = R.string.pick_date))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = categories.firstOrNull { it.id == categoryId }?.name.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = stringResource(id = R.string.category)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    categories.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option.name) },
                            onClick = {
                                categoryId = option.id
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(text = stringResource(id = R.string.notes)) },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (categoryId == 0L) return@Button
                    val fallbackCategoryName = categories.firstOrNull { it.id == categoryId }?.name
                    val parsedExpense = (uiState as? ScanUiState.Review)?.parsed ?: ParsedExpense(
                        merchantName = merchantName.ifBlank { null },
                        amount = amount.toDoubleOrNull(),
                        date = runCatching { LocalDate.parse(dateText, formatter) }.getOrNull(),
                        category = fallbackCategoryName,
                        confidence = null
                    )
                    viewModel.onConfirmExpense(
                        parsedExpense,
                        mapOf(
                            "merchantName" to merchantName,
                            "amount" to amount,
                            "date" to dateText,
                            "categoryId" to categoryId.toString(),
                            "notes" to notes
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.save_expense))
            }
        }
    }
}

