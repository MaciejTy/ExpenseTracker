package com.maciejtyszczuk.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maciejtyszczuk.expensetracker.data.model.CustomCategory
import com.maciejtyszczuk.expensetracker.data.model.Expense
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
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val expenseToEdit by viewModel.expenseToEdit.collectAsStateWithLifecycle()

    var showCategoryDialog by remember { mutableStateOf(false) }
    var expenseToSplit by remember { mutableStateOf<Expense?>(null) }

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
            // Tytuł z przyciskiem zarządzania kategoriami
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Moje Wydatki",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showCategoryDialog = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Zarządzaj kategoriami")
                }
            }

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

            // Filtry kategorii (z bazy danych)
            CategoryFilterChips(
                categories = categories,
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
                    categories = categories,
                    onDeleteExpense = { viewModel.deleteExpense(it) },
                    onEditExpense = { viewModel.startEditExpense(it) },
                    onSplitExpense = { expenseToSplit = it }
                )
            }
        }
    }

    // Dialog dodawania/edycji wydatku
    if (showDialog) {
        AddExpenseDialog(
            categories = categories,
            expenseToEdit = expenseToEdit,
            onDismiss = {
                viewModel.hideAddExpenseDialog()
                viewModel.clearEditExpense()
            },
            onConfirm = { amount, category, description ->
                viewModel.addExpense(amount, category, description)
                viewModel.hideAddExpenseDialog()
            },
            onUpdate = { expense ->
                viewModel.updateExpense(expense)
                viewModel.hideAddExpenseDialog()
            }
        )
    }

    // Dialog zarządzania kategoriami
    if (showCategoryDialog) {
        CategoryManagementDialog(
            categories = categories,
            onAddCategory = { name, emoji -> viewModel.addCategory(name, emoji) },
            onDeleteCategory = { viewModel.deleteCategory(it) },
            onDismiss = { showCategoryDialog = false }
        )
    }

    // Dialog podziału wydatku
    expenseToSplit?.let { expense ->
        SplitExpenseDialog(
            expense = expense,
            viewModel = viewModel,
            onDismiss = { expenseToSplit = null }
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
    categories: List<CustomCategory>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories.size) { index ->
            val category = categories[index]
            FilterChip(
                selected = selectedCategory == category.name,
                onClick = {
                    if (selectedCategory == category.name) {
                        onCategorySelected(null)
                    } else {
                        onCategorySelected(category.name)
                    }
                },
                label = { Text("${category.emoji} ${category.name}") }
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
                text = "\uD83D\uDCDD",
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
    categories: List<CustomCategory>,
    onDeleteExpense: (Expense) -> Unit,
    onEditExpense: (Expense) -> Unit,
    onSplitExpense: (Expense) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(expenses, key = { it.id }) { expense ->
            ExpenseItem(
                expense = expense,
                categories = categories,
                onDelete = { onDeleteExpense(expense) },
                onEdit = { onEditExpense(expense) },
                onSplit = { onSplitExpense(expense) }
            )
        }
    }
}
