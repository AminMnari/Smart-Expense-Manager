package com.smartexpense.presentation.settings

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.smartexpense.BuildConfig
import com.smartexpense.MainActivity
import com.smartexpense.R
import com.smartexpense.utils.CurrencyHelper
import com.smartexpense.utils.LanguageHelper

/**
 * Settings screen for app-level preferences and maintenance actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBudget: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(LanguageHelper.getSavedLanguage(context)) }
    var selectedCurrency by remember { mutableStateOf(CurrencyHelper.getSavedCurrency(context)) }
    var currencyExpanded by remember { mutableStateOf(false) }

    fun restartApp() {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        (context as? Activity)?.finish()
    }

    fun onLanguageSelected(languageCode: String) {
        selectedLanguage = languageCode
        LanguageHelper.saveLanguage(context, languageCode)
        LanguageHelper.applyLanguage(context, languageCode)
        if (languageCode == "ar") {
            (context as? Activity)?.window?.decorView?.layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        restartApp()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings_title)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(text = stringResource(id = R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item {
                ListItem(
                    headlineContent = { Text(text = stringResource(id = R.string.language)) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilterChip(
                                selected = selectedLanguage == "en",
                                onClick = { onLanguageSelected("en") },
                                label = { Text("EN") },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            FilterChip(
                                selected = selectedLanguage == "fr",
                                onClick = { onLanguageSelected("fr") },
                                label = { Text("FR") },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            FilterChip(
                                selected = selectedLanguage == "ar",
                                onClick = { onLanguageSelected("ar") },
                                label = { Text("AR") }
                            )
                        }
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text(text = stringResource(id = R.string.currency)) },
                    trailingContent = {
                        ExposedDropdownMenuBox(
                            expanded = currencyExpanded,
                            onExpandedChange = { currencyExpanded = !currencyExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedCurrency,
                                onValueChange = {},
                                readOnly = true,
                                singleLine = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .width(120.dp)
                                    .heightIn(min = 56.dp)
                            )
                            DropdownMenu(
                                expanded = currencyExpanded,
                                onDismissRequest = { currencyExpanded = false }
                            ) {
                                CurrencyHelper.supportedCurrencies.forEach { currency ->
                                    DropdownMenuItem(
                                        text = { Text(currency) },
                                        onClick = {
                                            selectedCurrency = currency
                                            CurrencyHelper.saveCurrency(context, currency)
                                            currencyExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text(text = stringResource(id = R.string.anomaly_alert_threshold)) },
                    supportingContent = { Text(text = stringResource(id = R.string.configure_budget_threshold)) }
                )
            }

            item {
                TextButton(onClick = onNavigateToBudget, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text(text = stringResource(id = R.string.open_budget_settings))
                }
            }

            item {
                ListItem(
                    headlineContent = { Text(text = stringResource(id = R.string.clear_all_data)) },
                    supportingContent = { Text(text = stringResource(id = R.string.clear_all_data_desc)) }
                )
            }

            item {
                TextButton(onClick = { showClearDialog = true }, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text(text = stringResource(id = R.string.clear_data_now))
                }
            }

            item {
                ListItem(
                    headlineContent = { Text(text = stringResource(id = R.string.about)) },
                    supportingContent = { Text(text = stringResource(id = R.string.about_desc)) }
                )
            }

            item {
                TextButton(onClick = { showAboutDialog = true }, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Text(text = stringResource(id = R.string.view_about))
                }
            }
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text(text = stringResource(id = R.string.clear_all_data)) },
                text = { Text(text = stringResource(id = R.string.clear_all_data_confirm)) },
                confirmButton = {
                    TextButton(onClick = {
                        showClearDialog = false
                        viewModel.clearAllData(context) {
                            restartApp()
                        }
                    }) {
                        Text(text = stringResource(id = R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                }
            )
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text(text = stringResource(id = R.string.app_name)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(id = R.string.about_description_full),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(id = R.string.about_version, BuildConfig.VERSION_NAME),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = stringResource(id = R.string.about_platform),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = stringResource(id = R.string.about_ai),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            )
        }
    }
}

