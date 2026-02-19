package com.maciejtyszczuk.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maciejtyszczuk.expensetracker.data.model.CustomCategory
import com.maciejtyszczuk.expensetracker.data.model.RecurringExpense
import com.maciejtyszczuk.expensetracker.data.model.RecurringFrequency
import com.maciejtyszczuk.expensetracker.viewmodel.ExpenseViewModel

@Composable
fun RecurringExpenseScreen(viewModel: ExpenseViewModel) {
    val recurringExpenses by viewModel.recurringExpenses.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj wydatek cykliczny")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Wydatki cykliczne",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (recurringExpenses.isEmpty()) {
                EmptyRecurringState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recurringExpenses, key = { it.id }) { recurring ->
                        RecurringExpenseItem(
                            recurring = recurring,
                            categories = categories,
                            onToggle = { viewModel.toggleRecurringExpense(recurring) },
                            onDelete = { viewModel.deleteRecurringExpense(recurring) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddRecurringExpenseDialog(
            categories = categories,
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, category, description, frequency ->
                viewModel.addRecurringExpense(amount, category, description, frequency)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RecurringExpenseItem(
    recurring: RecurringExpense,
    categories: List<CustomCategory>,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val emoji = categories.find { it.name == recurring.category }?.emoji ?: "\uD83D\uDCE6"
    val frequencyDisplay = try {
        RecurringFrequency.valueOf(recurring.frequency).displayName
    } catch (_: Exception) {
        recurring.frequency
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (recurring.isActive)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = recurring.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (recurring.description.isNotEmpty()) {
                        Text(
                            text = recurring.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = frequencyDisplay,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.2f zł", recurring.amount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = recurring.isActive,
                        onCheckedChange = { onToggle() }
                    )
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Usuń",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usuń wydatek cykliczny?") },
            text = { Text("Czy na pewno chcesz usunąć ten wydatek cykliczny?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Usuń", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
fun EmptyRecurringState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "\uD83D\uDD04",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Brak wydatków cyklicznych",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Kliknij + aby dodać wydatek cykliczny",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
