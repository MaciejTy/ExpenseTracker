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
import com.maciejtyszczuk.expensetracker.data.model.CustomCategory

@Composable
fun CategoryManagementDialog(
    categories: List<CustomCategory>,
    onAddCategory: (String, String) -> Unit,
    onDeleteCategory: (CustomCategory) -> Unit,
    onDismiss: () -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newEmoji by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Zarządzaj kategoriami") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                // Lista kategorii
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(categories) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${category.emoji} ${category.name}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (!category.isDefault) {
                                IconButton(
                                    onClick = { onDeleteCategory(category) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Usuń",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Formularz dodawania
                if (showAddForm) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        OutlinedTextField(
                            value = newEmoji,
                            onValueChange = { if (it.length <= 2) newEmoji = it },
                            label = { Text("Emoji") },
                            modifier = Modifier.width(72.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newName,
                            onValueChange = {
                                newName = it
                                nameError = false
                            },
                            label = { Text("Nazwa") },
                            isError = nameError,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            supportingText = {
                                if (nameError) Text("Podaj nazwę")
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            showAddForm = false
                            newName = ""
                            newEmoji = ""
                        }) {
                            Text("Anuluj")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (newName.isNotBlank()) {
                                onAddCategory(newName.trim(), newEmoji.ifBlank { "\uD83D\uDCCC" })
                                newName = ""
                                newEmoji = ""
                                showAddForm = false
                            } else {
                                nameError = true
                            }
                        }) {
                            Text("Dodaj")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showAddForm = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nowa kategoria")
                    }
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
