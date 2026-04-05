package com.smartexpense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.smartexpense.presentation.navigation.NavGraph
import com.smartexpense.ui.theme.SmartExpenseManagerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity hosting the app navigation graph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartExpenseManagerTheme {
                NavGraph()
            }
        }
    }
}
