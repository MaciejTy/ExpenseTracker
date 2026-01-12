package com.maciejtyszczuk.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maciejtyszczuk.expensetracker.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BudgetScreen(viewModel: ExpenseViewModel) {
    val budget by viewModel.currentBudget.collectAsStateWithLifecycle(initialValue = null)
    val monthlyExpenses by viewModel.monthlyExpenses.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Budżet",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Karta miesiąca
        MonthCard()

        if (budget != null) {
            // Karta budżetu z paskiem postępu
            BudgetCard(
                budgetAmount = budget!!.amount,
                spentAmount = monthlyExpenses,
                onEditClick = { showDialog = true }
            )

            // Statystyki
            BudgetStats(
                budgetAmount = budget!!.amount,
                spentAmount = monthlyExpenses
            )
        } else {
            // Brak budżetu - zachęta do ustawienia
            NoBudgetState(onSetBudget = { showDialog = true })
        }
    }

    // Dialog ustawiania budżetu
    if (showDialog) {
        SetBudgetDialog(
            currentBudget = budget?.amount,
            onDismiss = { showDialog = false },
            onConfirm = { amount ->
                viewModel.setBudget(amount)
                showDialog = false
            }
        )
    }
}

@Composable
fun MonthCard() {
    val dateFormat = SimpleDateFormat("LLLL yyyy", Locale("pl", "PL"))
    val currentMonth = dateFormat.format(Date())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = currentMonth.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun BudgetCard(
    budgetAmount: Double,
    spentAmount: Double,
    onEditClick: () -> Unit
) {
    val progress = (spentAmount / budgetAmount).toFloat().coerceIn(0f, 1f)
    val isOverBudget = spentAmount > budgetAmount

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Twój budżet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edytuj budżet")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kwoty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Wydano",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.2f zł", spentAmount),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Budżet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.2f zł", budgetAmount),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pasek postępu
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Procent
            Text(
                text = String.format("%.1f%% wykorzystane", progress * 100),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isOverBudget) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ Przekroczono budżet!",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun BudgetStats(
    budgetAmount: Double,
    spentAmount: Double
) {
    val remaining = budgetAmount - spentAmount

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Pozostało do wydania",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format("%.2f zł", remaining.coerceAtLeast(0.0)),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (remaining >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun NoBudgetState(onSetBudget: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💰",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Brak budżetu",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ustaw miesięczny budżet aby kontrolować wydatki",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onSetBudget) {
                Text("Ustaw budżet")
            }
        }
    }
}

@Composable
fun SetBudgetDialog(
    currentBudget: Double?,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf(currentBudget?.toString() ?: "") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (currentBudget != null) "Edytuj budżet" else "Ustaw budżet") },
        text = {
            Column {
                Text(
                    text = "Podaj miesięczny limit wydatków",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        error = false
                    },
                    label = { Text("Kwota (zł)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error,
                    supportingText = {
                        if (error) {
                            Text("Podaj poprawną kwotę")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountDouble = amount.replace(",", ".").toDoubleOrNull()
                    if (amountDouble != null && amountDouble > 0) {
                        onConfirm(amountDouble)
                    } else {
                        error = true
                    }
                }
            ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}