package com.maciejtyszczuk.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maciejtyszczuk.expensetracker.ui.screens.BudgetScreen
import com.maciejtyszczuk.expensetracker.ui.screens.MainScreen
import com.maciejtyszczuk.expensetracker.ui.screens.StatisticsScreen
import com.maciejtyszczuk.expensetracker.ui.theme.ExpenseTrackerTheme
import com.maciejtyszczuk.expensetracker.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: ExpenseViewModel = viewModel()
                    MainNavigationScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainNavigationScreen(viewModel: ExpenseViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Lista") },
                    label = { Text("Lista") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.QueryStats, contentDescription = "Statystyki") },
                    label = { Text("Statystyki") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Budżet") },
                    label = { Text("Budżet") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> MainScreen(viewModel = viewModel)
                1 -> StatisticsScreen(viewModel = viewModel)
                2 -> BudgetScreen(viewModel = viewModel)
            }
        }
    }
}