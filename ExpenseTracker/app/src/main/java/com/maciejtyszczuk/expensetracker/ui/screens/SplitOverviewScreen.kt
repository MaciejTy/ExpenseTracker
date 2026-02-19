package com.maciejtyszczuk.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maciejtyszczuk.expensetracker.data.model.DebtSummary
import com.maciejtyszczuk.expensetracker.data.model.SplitExpense
import com.maciejtyszczuk.expensetracker.viewmodel.ExpenseViewModel

@Composable
fun SplitOverviewScreen(viewModel: ExpenseViewModel) {
    val allSplits by viewModel.allSplitExpenses.collectAsStateWithLifecycle()
    val unpaidSplits by viewModel.allUnpaidSplits.collectAsStateWithLifecycle()

    // Podsumowanie długów per osoba (tylko nieopłacone)
    val debtSummaries = remember(unpaidSplits) {
        unpaidSplits
            .groupBy { it.personName }
            .map { (name, splits) ->
                DebtSummary(
                    personName = name,
                    totalAmount = splits.sumOf { it.amount }
                )
            }
            .sortedByDescending { it.totalAmount }
    }

    // Wszystkie podziały pogrupowane per osoba
    val allSplitsByPerson = remember(allSplits) {
        allSplits.groupBy { it.personName }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Podziały wydatków",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (allSplits.isEmpty()) {
            EmptySplitState()
        } else {
            // Podsumowanie nieopłaconych długów
            if (debtSummaries.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Do zapłaty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format("%.2f zł", debtSummaries.sumOf { it.totalAmount }),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Lista per osoba
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allSplitsByPerson.forEach { (personName, splits) ->
                    item {
                        PersonDebtCard(
                            personName = personName,
                            splits = splits,
                            onMarkPaid = { viewModel.markSplitAsPaid(it) },
                            onMarkUnpaid = { viewModel.markSplitAsUnpaid(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonDebtCard(
    personName: String,
    splits: List<SplitExpense>,
    onMarkPaid: (SplitExpense) -> Unit,
    onMarkUnpaid: (SplitExpense) -> Unit
) {
    val unpaidTotal = splits.filter { !it.isPaid }.sumOf { it.amount }
    val paidTotal = splits.filter { it.isPaid }.sumOf { it.amount }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        text = String.format("%.2f zł", unpaidTotal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (paidTotal > 0 && unpaidTotal > 0) {
                Text(
                    text = String.format("Zapłacono: %.2f zł", paidTotal),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            splits.forEach { split ->
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
                                if (it) onMarkPaid(split) else onMarkUnpaid(split)
                            }
                        )
                        Text(
                            text = String.format("%.2f zł", split.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (split.isPaid)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (split.isPaid) {
                        Text(
                            text = "Zapłacono",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
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
                text = "Brak podziałów",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Podziel wydatek klikając ikonę na liście wydatków",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
