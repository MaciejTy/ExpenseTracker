package com.maciejtyszczuk.expensetracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.maciejtyszczuk.expensetracker.notification.NotificationHelper
import com.maciejtyszczuk.expensetracker.ui.screens.BudgetScreen
import com.maciejtyszczuk.expensetracker.ui.screens.MainScreen
import com.maciejtyszczuk.expensetracker.ui.screens.RecurringExpenseScreen
import com.maciejtyszczuk.expensetracker.ui.screens.SplitOverviewScreen
import com.maciejtyszczuk.expensetracker.ui.screens.StatisticsScreen
import com.maciejtyszczuk.expensetracker.ui.theme.ExpenseTrackerTheme
import com.maciejtyszczuk.expensetracker.viewmodel.ExpenseViewModel
import com.maciejtyszczuk.expensetracker.worker.BudgetCheckWorker
import com.maciejtyszczuk.expensetracker.worker.DailyReminderWorker
import com.maciejtyszczuk.expensetracker.worker.RecurringExpenseWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Tworzenie kanałów powiadomień
        NotificationHelper.createNotificationChannels(this)

        // Żądanie uprawnień POST_NOTIFICATIONS (API 33+)
        requestNotificationPermission()

        // Planowanie workerów
        scheduleWorkers()

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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun scheduleWorkers() {
        val workManager = WorkManager.getInstance(this)

        // Wydatki cykliczne — co 1 godzinę
        val recurringWork = PeriodicWorkRequestBuilder<RecurringExpenseWorker>(
            1, TimeUnit.HOURS
        ).build()
        workManager.enqueueUniquePeriodicWork(
            "recurring_expenses",
            ExistingPeriodicWorkPolicy.KEEP,
            recurringWork
        )

        // Sprawdzanie budżetu — co 6 godzin
        val budgetWork = PeriodicWorkRequestBuilder<BudgetCheckWorker>(
            6, TimeUnit.HOURS
        ).build()
        workManager.enqueueUniquePeriodicWork(
            "budget_check",
            ExistingPeriodicWorkPolicy.KEEP,
            budgetWork
        )

        // Codzienne przypomnienie — co 24 godziny
        val reminderWork = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS
        ).build()
        workManager.enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderWork
        )
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
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Repeat, contentDescription = "Cykliczne") },
                    label = { Text("Cykliczne") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.AutoMirrored.Filled.CallSplit, contentDescription = "Podziały") },
                    label = { Text("Podziały") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> MainScreen(viewModel = viewModel)
                1 -> StatisticsScreen(viewModel = viewModel)
                2 -> BudgetScreen(viewModel = viewModel)
                3 -> RecurringExpenseScreen(viewModel = viewModel)
                4 -> SplitOverviewScreen(viewModel = viewModel)
            }
        }
    }
}
