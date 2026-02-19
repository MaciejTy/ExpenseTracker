package com.maciejtyszczuk.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maciejtyszczuk.expensetracker.data.model.Expense
import com.maciejtyszczuk.expensetracker.data.model.SplitExpense
import com.maciejtyszczuk.expensetracker.viewmodel.ExpenseViewModel

@Composable
fun SplitExpenseDialog(
    expense: Expense,
    viewModel: ExpenseViewModel,
    onDismiss: () -> Unit
) {
    val splits by viewModel.getSplitsForExpense(expense.id).collectAsStateWithLifecycle(initialValue = emptyList())

    var personName by remember { mutableStateOf("") }
    var splitAmount by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var useEqualSplit by remember { mutableStateOf(false) }
    var numberOfPeople by remember { mutableStateOf("2") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Podziel wydatek") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
            ) {
                // Info o wydatku
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = expense.category,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = String.format("%.2f zł", expense.amount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Istniejące podziały
                if (splits.isNotEmpty()) {
                    Text(
                        text = "Podziały:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .heightIn(max = 150.dp)
                    ) {
                        items(splits) { split ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = split.isPaid,
                                        onCheckedChange = {
                                            if (it) viewModel.markSplitAsPaid(split)
                                            else viewModel.markSplitAsUnpaid(split)
                                        }
                                    )
                                    Column {
                                        Text(
                                            text = split.personName,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = String.format("%.2f zł", split.amount),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (split.isPaid)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.deleteSplit(split) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Usuń",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Dodawanie nowego podziału
                Text(
                    text = "Dodaj podział:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = personName,
                    onValueChange = {
                        personName = it
                        nameError = false
                    },
                    label = { Text("Imię osoby") },
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        if (nameError) Text("Podaj imię")
                    }
                )

                OutlinedTextField(
                    value = splitAmount,
                    onValueChange = {
                        splitAmount = it
                        amountError = false
                    },
                    label = { Text("Kwota (zł)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amountError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        if (amountError) Text("Podaj poprawną kwotę")
                    }
                )

                Button(
                    onClick = {
                        val amount = splitAmount.replace(",", ".").toDoubleOrNull()
                        when {
                            personName.isBlank() -> nameError = true
                            amount == null || amount <= 0 -> amountError = true
                            else -> {
                                viewModel.addSplit(expense.id, personName.trim(), amount)
                                personName = ""
                                splitAmount = ""
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dodaj podział")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Zamknij")
            }
        }
    )
}
