package com.maciejtyszczuk.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maciejtyszczuk.expensetracker.data.model.SplitExpense
import com.maciejtyszczuk.expensetracker.viewmodel.ExpenseViewModel

private enum class SplitFilter(val label: String) {
    ALL("Wszystkie"),
    UNPAID("Do zwrotu"),
    PAID("Zwrócone")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitOverviewScreen(viewModel: ExpenseViewModel) {
    val allSplits by viewModel.allSplitExpenses.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf(SplitFilter.ALL) }
    var splitToEdit by remember { mutableStateOf<SplitExpense?>(null) }
    var splitToDelete by remember { mutableStateOf<SplitExpense?>(null) }

    val unpaidTotal = remember(allSplits) { allSplits.filter { !it.isPaid }.sumOf { it.amount } }
    val paidTotal = remember(allSplits) { allSplits.filter { it.isPaid }.sumOf { it.amount } }

    val filteredSplits = remember(allSplits, selectedFilter) {
        when (selectedFilter) {
            SplitFilter.ALL -> allSplits
            SplitFilter.UNPAID -> allSplits.filter { !it.isPaid }
            SplitFilter.PAID -> allSplits.filter { it.isPaid }
        }
    }

    val splitsByPerson = remember(filteredSplits) {
        filteredSplits.groupBy { it.personName }
    }

    // Dialog edycji
    splitToEdit?.let { split ->
        EditSplitDialog(
            split = split,
            onDismiss = { splitToEdit = null },
            onConfirm = { newName, newAmount ->
                viewModel.updateSplit(split, newName, newAmount)
                splitToEdit = null
            }
        )
    }

    // Dialog potwierdzenia usuwania
    splitToDelete?.let { split ->
        AlertDialog(
            onDismissRequest = { splitToDelete = null },
            title = { Text("Usun podzial") },
            text = {
                Text("Usunac podzial ${String.format("%.2f zl", split.amount)} dla ${split.personName}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSplit(split)
                        splitToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Usun")
                }
            },
            dismissButton = {
                TextButton(onClick = { splitToDelete = null }) {
                    Text("Anuluj")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Podzialy wydatkow",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (allSplits.isEmpty()) {
            EmptySplitState()
        } else {
            // Dwie karty podsumowania obok siebie
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SplitSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Do zwrotu",
                    amount = unpaidTotal,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
                SplitSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Zwrocone",
                    amount = paidTotal,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Filtry
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SplitFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.label) }
                    )
                }
            }

            // Lista per osoba
            if (splitsByPerson.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedFilter == SplitFilter.PAID)
                            "Brak zwroconych podzialow"
                        else
                            "Wszystko zwrocone!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    splitsByPerson.forEach { (personName, splits) ->
                        item(key = personName) {
                            PersonDebtCard(
                                personName = personName,
                                splits = splits,
                                showFilter = selectedFilter,
                                onMarkPaid = { viewModel.markSplitAsPaid(it) },
                                onMarkUnpaid = { viewModel.markSplitAsUnpaid(it) },
                                onEdit = { splitToEdit = it },
                                onDelete = { splitToDelete = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditSplitDialog(
    split: SplitExpense,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var personName by remember { mutableStateOf(split.personName) }
    var amount by remember { mutableStateOf(String.format("%.2f", split.amount)) }
    var nameError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj podzial") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = personName,
                    onValueChange = {
                        personName = it
                        nameError = false
                    },
                    label = { Text("Imie osoby") },
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        if (nameError) Text("Podaj imie")
                    }
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        amountError = false
                    },
                    label = { Text("Kwota (zl)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amountError,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        if (amountError) Text("Podaj poprawna kwote")
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = amount.replace(",", ".").toDoubleOrNull()
                    when {
                        personName.isBlank() -> nameError = true
                        parsed == null || parsed <= 0 -> amountError = true
                        else -> onConfirm(personName.trim(), parsed)
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

@Composable
private fun SplitSummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format("%.2f zl", amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun PersonDebtCard(
    personName: String,
    splits: List<SplitExpense>,
    showFilter: SplitFilter,
    onMarkPaid: (SplitExpense) -> Unit,
    onMarkUnpaid: (SplitExpense) -> Unit,
    onEdit: (SplitExpense) -> Unit,
    onDelete: (SplitExpense) -> Unit
) {
    val unpaid = splits.filter { !it.isPaid }
    val paid = splits.filter { it.isPaid }
    val unpaidTotal = unpaid.sumOf { it.amount }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Naglowek z imieniem i kwota
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = personName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (unpaidTotal > 0) {
                    Text(
                        text = String.format("%.2f zl", unpaidTotal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = "Rozliczone",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nieoplacone na gorze
            if (unpaid.isNotEmpty()) {
                unpaid.forEach { split ->
                    SplitRow(
                        split = split,
                        onCheckedChange = { onMarkPaid(split) },
                        onEdit = { onEdit(split) },
                        onDelete = { onDelete(split) }
                    )
                }
            }

            // Separator jesli sa oba typy
            if (unpaid.isNotEmpty() && paid.isNotEmpty() && showFilter == SplitFilter.ALL) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            // Oplacone na dole, przygaszone
            if (paid.isNotEmpty()) {
                paid.forEach { split ->
                    SplitRow(
                        split = split,
                        onCheckedChange = { onMarkUnpaid(split) },
                        onEdit = { onEdit(split) },
                        onDelete = { onDelete(split) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SplitRow(
    split: SplitExpense,
    onCheckedChange: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Checkbox(
                checked = split.isPaid,
                onCheckedChange = { onCheckedChange() }
            )
            Text(
                text = String.format("%.2f zl", split.amount),
                style = MaterialTheme.typography.bodyMedium,
                color = if (split.isPaid)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurface,
                textDecoration = if (split.isPaid) TextDecoration.LineThrough else TextDecoration.None
            )
        }
        Row {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edytuj",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Usun",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptySplitState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "\uD83E\uDD1D",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Brak podzialow",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Podziel wydatek klikajac ikone na liscie wydatkow",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
