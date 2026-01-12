package com.maciejtyszczuk.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maciejtyszczuk.expensetracker.data.model.Expense
import com.maciejtyszczuk.expensetracker.data.model.ExpenseCategory
import com.maciejtyszczuk.expensetracker.ui.components.ExpenseItem
import com.maciejtyszczuk.expensetracker.viewmodel.ExpenseViewModel
import com.maciejtyszczuk.expensetracker.viewmodel.TimeFilter

@Composable
fun MainScreen(viewModel: ExpenseViewModel) {
    val expenses by viewModel.filteredExpenses.collectAsStateWithLifecycle(initialValue = emptyList())
    val totalExpenses by viewModel.totalExpenses.collectAsStateWithLifecycle(initialValue = 0.0)
    val showDialog by viewModel.showAddDialog.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedTimeFilter by viewModel.selectedTimeFilter.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddExpenseDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj wydatek")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            // Tytuł
            Text(
                text = "Moje Wydatki",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Pasek wyszukiwania
            SearchTextField(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                onClearQuery = { viewModel.setSearchQuery("") },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filtry czasowe
            TimeFilterChips(
                selectedFilter = selectedTimeFilter,
                onFilterSelected = { viewModel.setTimeFilter(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filtry kategorii
            CategoryFilterChips(
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.setSelectedCategory(it) }
            )

            // Przycisk czyszczenia filtrów
            if (searchQuery.isNotEmpty() || selectedCategory != null || selectedTimeFilter != TimeFilter.ALL) {
                TextButton(
                    onClick = { viewModel.clearFilters() },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Wyczyść filtry")
                }
            }

            // Karta z podsumowaniem
            SummaryCard(total = totalExpenses ?: 0.0)

            // Lista wydatków
            if (expenses.isEmpty()) {
                EmptyState()
            } else {
                ExpensesList(
                    expenses = expenses,
                    onDeleteExpense = { viewModel.deleteExpense(it) }
                )
            }
        }
    }

    // Dialog dodawania wydatku
    if (showDialog) {
        AddExpenseDialog(
            onDismiss = { viewModel.hideAddExpenseDialog() },
            onConfirm = { amount, category, description ->
                viewModel.addExpense(amount, category, description)
                viewModel.hideAddExpenseDialog()
            }
        )
    }
}

@Composable
fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Szukaj po opisie...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Szukaj")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(Icons.Default.Clear, contentDescription = "Wyczyść")
                }
            }
        },
        singleLine = true
    )
}

@Composable
fun TimeFilterChips(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TimeFilter.values().size) { index ->
            val filter = TimeFilter.values()[index]
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.displayName) }
            )
        }
    }
}

@Composable
fun CategoryFilterChips(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ExpenseCategory.values().size) { index ->
            val category = ExpenseCategory.values()[index]
            FilterChip(
                selected = selectedCategory == category.displayName,
                onClick = {
                    if (selectedCategory == category.displayName) {
                        onCategorySelected(null)
                    } else {
                        onCategorySelected(category.displayName)
                    }
                },
                label = { Text("${category.emoji} ${category.displayName}") }
            )
        }
    }
}

@Composable
fun SummaryCard(total: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Suma wydatków",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format("%.2f zł", total),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "📝",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Brak wydatków",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Kliknij + aby dodać pierwszy wydatek",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ExpensesList(
    expenses: List<Expense>,
    onDeleteExpense: (Expense) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(expenses, key = { it.id }) { expense ->
            ExpenseItem(
                expense = expense,
                onDelete = { onDeleteExpense(expense) }
            )
        }
    }
}