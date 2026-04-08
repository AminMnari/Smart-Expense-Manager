package com.smartexpense.presentation.scan

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartexpense.R
import com.smartexpense.domain.model.ParsedExpense
import com.smartexpense.presentation.CategorySpecs
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Receipt scan screen with camera, gallery, OCR processing, and review form.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var latestCaptureUri by remember { mutableStateOf(createTempImageUri(context)) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            viewModel.onImageCaptured(latestCaptureUri, context)
            latestCaptureUri = createTempImageUri(context)
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onImageCaptured(it, context) }
    }

    LaunchedEffect(uiState) {
        if (uiState is ScanUiState.Saved) {
            viewModel.resetState()
            onNavigateToDashboard()
        }
    }

    LaunchedEffect((uiState as? ScanUiState.Error)?.message) {
        (uiState as? ScanUiState.Error)?.message?.let { snackbarHostState.showSnackbar(it) }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { cameraLauncher.launch(latestCaptureUri) },
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

                when (val state = uiState) {
                    ScanUiState.Idle,
                    ScanUiState.CameraOpen -> {
                        Text(text = stringResource(id = R.string.scan_hint))
                    }

                    is ScanUiState.Review -> {
                        ScanReviewForm(
                            parsed = state.parsed,
                            onSave = { parsedExpense, edits -> viewModel.onConfirmExpense(parsedExpense, edits) }
                        )
                    }

                    is ScanUiState.Error -> {
                        Button(onClick = onNavigateToAddExpense) {
                            Text(text = stringResource(id = R.string.enter_manually))
                        }
                    }

                    ScanUiState.Saved,
                    ScanUiState.Processing -> Unit
                }
            }

            if (uiState is ScanUiState.Processing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator()
                        Text(text = stringResource(id = R.string.analyzing_receipt))
                    }
                }
            }
        }
    }
}

/**
 * Editable review form used after parsing receipt text.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScanReviewForm(
    parsed: ParsedExpense,
    onSave: (ParsedExpense, Map<String, String>) -> Unit
) {
    var merchantName by remember(parsed) { mutableStateOf(parsed.merchantName.orEmpty()) }
    var amount by remember(parsed) { mutableStateOf(parsed.amount?.toString().orEmpty()) }
    var date by remember(parsed) { mutableStateOf(parsed.date ?: LocalDate.now()) }
    var categoryId by remember(parsed) {
        mutableStateOf(
            CategorySpecs.firstOrNull {
                it.toGeminiName().equals(parsed.category ?: "", ignoreCase = true)
            }?.id ?: CategorySpecs.last().id
        )
    }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val formatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }

    val datePicker = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth -> date = LocalDate.of(year, month + 1, dayOfMonth) },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                value = stringResource(id = CategorySpecs.first { it.id == categoryId }.nameRes),
                onValueChange = {},
                readOnly = true,
                label = { Text(text = stringResource(id = R.string.category)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                CategorySpecs.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = option.nameRes)) },
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
                onSave(
                    parsed,
                    mapOf(
                        "merchantName" to merchantName,
                        "amount" to amount,
                        "date" to date.format(formatter),
                        "categoryId" to categoryId.toString(),
                        "notes" to notes
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.save_expense))
        }
        TextButton(onClick = {
            merchantName = ""
            amount = ""
            date = LocalDate.now()
            notes = ""
            categoryId = CategorySpecs.last().id
        }) {
            Text(text = stringResource(id = R.string.edit_manually))
        }
    }
}

private fun createTempImageUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "receipt_images").apply { mkdirs() }
    val imageFile = File.createTempFile("receipt_", ".jpg", imagesDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
}

private fun com.smartexpense.presentation.CategoryUiSpec.toGeminiName(): String = when (id) {
    1L -> "Food"
    2L -> "Transport"
    3L -> "Shopping"
    4L -> "Health"
    5L -> "Utilities"
    6L -> "Entertainment"
    7L -> "Education"
    else -> "Other"
}
