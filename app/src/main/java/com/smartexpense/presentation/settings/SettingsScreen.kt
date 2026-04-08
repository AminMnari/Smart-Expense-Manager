package com.smartexpense.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.smartexpense.BuildConfig
import com.smartexpense.R

/**
 * Settings screen for app-level preferences and maintenance actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBudget: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

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
        Column(modifier = Modifier.padding(innerPadding)) {
            ListItem(
                headlineContent = { Text(text = stringResource(id = R.string.currency)) },
                supportingContent = { Text(text = stringResource(id = R.string.currency_tnd)) }
            )
            ListItem(
                headlineContent = { Text(text = stringResource(id = R.string.anomaly_alert_threshold)) },
                supportingContent = { Text(text = stringResource(id = R.string.configure_budget_threshold)) }
            )
            TextButton(onClick = onNavigateToBudget) {
                Text(text = stringResource(id = R.string.open_budget_settings))
            }
            ListItem(
                headlineContent = { Text(text = stringResource(id = R.string.clear_all_data)) },
                supportingContent = { Text(text = stringResource(id = R.string.clear_all_data_desc)) }
            )
            TextButton(onClick = { showClearDialog = true }) {
                Text(text = stringResource(id = R.string.clear_data_now))
            }
            ListItem(
                headlineContent = { Text(text = stringResource(id = R.string.about)) },
                supportingContent = { Text(text = stringResource(id = R.string.about_desc)) }
            )
            TextButton(onClick = { showAboutDialog = true }) {
                Text(text = stringResource(id = R.string.view_about))
            }
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text(text = stringResource(id = R.string.clear_all_data)) },
                text = { Text(text = stringResource(id = R.string.clear_all_data_confirm)) },
                confirmButton = {
                    TextButton(onClick = { showClearDialog = false }) {
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
                title = { Text(text = stringResource(id = R.string.about)) },
                text = {
                    Text(
                        text = stringResource(
                            id = R.string.about_body,
                            stringResource(id = R.string.app_name),
                            BuildConfig.VERSION_NAME
                        )
                    )
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
