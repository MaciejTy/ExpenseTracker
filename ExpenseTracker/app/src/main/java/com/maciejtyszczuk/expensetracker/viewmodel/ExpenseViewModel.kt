package com.maciejtyszczuk.expensetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.maciejtyszczuk.expensetracker.data.database.ExpenseDatabase
import com.maciejtyszczuk.expensetracker.data.model.Budget
import com.maciejtyszczuk.expensetracker.data.model.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.*

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ExpenseDatabase.getDatabase(application)
    private val dao = database.expenseDao()

    // Lista wszystkich wydatków
    private val allExpensesFlow = dao.getAllExpenses()

    // Funkcja dla statystyk (zawsze wszystkie wydatki)
    fun getAllExpensesForStats() = allExpensesFlow

    // Suma wydatków
    val totalExpenses = dao.getTotalExpenses()

    // Budżet
    private val calendar = Calendar.getInstance()
    private val currentMonth = calendar.get(Calendar.MONTH) + 1
    private val currentYear = calendar.get(Calendar.YEAR)

    val currentBudget = dao.getBudgetForMonth(currentMonth, currentYear)

    // Wydatki w bieżącym miesiącu
    val monthlyExpenses: StateFlow<Double> = allExpensesFlow.map { expenses ->
        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        expenses.filter { it.date >= startOfMonth }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0.0)

    // Stan dialogu dodawania wydatku
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    // Filtrowanie
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedTimeFilter = MutableStateFlow(TimeFilter.ALL)
    val selectedTimeFilter: StateFlow<TimeFilter> = _selectedTimeFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtrowane wydatki
    val filteredExpenses = combine(
        allExpensesFlow,
        _selectedCategory,
        _selectedTimeFilter,
        _searchQuery
    ) { expenses, category, timeFilter, query ->
        expenses
            .filter { expense ->
                // Filtr kategorii
                (category == null || expense.category == category)
            }
            .filter { expense ->
                // Filtr czasu
                val calendar = Calendar.getInstance()
                val now = calendar.timeInMillis

                when (timeFilter) {
                    TimeFilter.ALL -> true
                    TimeFilter.TODAY -> {
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        expense.date >= calendar.timeInMillis
                    }
                    TimeFilter.WEEK -> {
                        calendar.add(Calendar.DAY_OF_YEAR, -7)
                        expense.date >= calendar.timeInMillis
                    }
                    TimeFilter.MONTH -> {
                        calendar.add(Calendar.MONTH, -1)
                        expense.date >= calendar.timeInMillis
                    }
                }
            }
            .filter { expense ->
                // Filtr wyszukiwania
                query.isEmpty() ||
                        expense.description.contains(query, ignoreCase = true) ||
                        expense.category.contains(query, ignoreCase = true)
            }
    }

    fun showAddExpenseDialog() {
        _showAddDialog.value = true
    }

    fun hideAddExpenseDialog() {
        _showAddDialog.value = false
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setTimeFilter(filter: TimeFilter) {
        _selectedTimeFilter.value = filter
    }

    fun clearFilters() {
        _selectedCategory.value = null
        _selectedTimeFilter.value = TimeFilter.ALL
        _searchQuery.value = ""
    }

    // Budżet
    fun setBudget(amount: Double) {
        viewModelScope.launch {
            val budget = Budget(
                amount = amount,
                month = currentMonth,
                year = currentYear
            )
            dao.insertBudget(budget)
        }
    }

    // Dodawanie wydatku
    fun addExpense(amount: Double, category: String, description: String, date: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val expense = Expense(
                amount = amount,
                category = category,
                description = description,
                date = date
            )
            dao.insertExpense(expense)
        }
    }

    // Usuwanie wydatku
    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            dao.deleteExpense(expense)
        }
    }

    // Usuwanie wszystkich wydatków
    fun deleteAllExpenses() {
        viewModelScope.launch {
            dao.deleteAllExpenses()
        }
    }
}

enum class TimeFilter(val displayName: String) {
    ALL("Wszystkie"),
    TODAY("Dziś"),
    WEEK("Tydzień"),
    MONTH("Miesiąc")
}