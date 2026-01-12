package com.maciejtyszczuk.expensetracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.maciejtyszczuk.expensetracker.data.model.ExpenseCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.OTHER) }
    var expanded by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj wydatek") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Kwota
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        amountError = false
                    },
                    label = { Text("Kwota (zł)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amountError,
                    supportingText = {
                        if (amountError) {
                            Text("Podaj poprawną kwotę")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Kategoria (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = "${selectedCategory.emoji} ${selectedCategory.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ExpenseCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text("${category.emoji} ${category.displayName}") },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Opis
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountDouble = amount.replace(",", ".").toDoubleOrNull()
                    if (amountDouble != null && amountDouble > 0) {
                        onConfirm(amountDouble, selectedCategory.displayName, description)
                    } else {
                        amountError = true
                    }
                }
            ) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}