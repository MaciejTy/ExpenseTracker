package com.maciejtyszczuk.expensetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.maciejtyszczuk.expensetracker.data.database.ExpenseDatabase
import com.maciejtyszczuk.expensetracker.data.model.Budget
import com.maciejtyszczuk.expensetracker.data.model.CustomCategory
import com.maciejtyszczuk.expensetracker.data.model.Expense
import com.maciejtyszczuk.expensetracker.data.model.RecurringExpense
import com.maciejtyszczuk.expensetracker.data.model.SplitExpense
import com.maciejtyszczuk.expensetracker.notification.NotificationHelper
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

    // Suma wydatków (z uwzględnieniem zwrotów z dzielonych rachunków)
    val totalExpenses = combine(
        dao.getTotalExpenses(),
        dao.getTotalPaidSplits()
    ) { total, paidBack ->
        (total ?: 0.0) - paidBack
    }

    // Budżet
    private val calendar = Calendar.getInstance()
    private val currentMonth = calendar.get(Calendar.MONTH) + 1
    private val currentYear = calendar.get(Calendar.YEAR)

    val currentBudget = dao.getBudgetForMonth(currentMonth, currentYear)

    // Wydatki w bieżącym miesiącu (z uwzględnieniem zwrotów z dzielonych rachunków)
    val monthlyExpenses: StateFlow<Double> = combine(
        allExpensesFlow,
        dao.getAllSplitExpenses()
    ) { expenses, splits ->
        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val monthlyExpensesList = expenses.filter { it.date >= startOfMonth }
        val monthlyExpenseIds = monthlyExpensesList.map { it.id }.toSet()
        val expenseTotal = monthlyExpensesList.sumOf { it.amount }

        val paidBackTotal = splits
            .filter { it.isPaid && it.expenseId in monthlyExpenseIds }
            .sumOf { it.amount }

        expenseTotal - paidBackTotal
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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
            checkBudgetExceeded()
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

    // ===== Własne kategorie =====

    val categories: StateFlow<List<CustomCategory>> = dao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String, emoji: String) {
        viewModelScope.launch {
            val category = CustomCategory(name = name, emoji = emoji)
            dao.insertCategory(category)
        }
    }

    fun deleteCategory(category: CustomCategory) {
        viewModelScope.launch {
            dao.deleteCategory(category)
        }
    }

    // ===== Edycja wydatków =====

    private val _expenseToEdit = MutableStateFlow<Expense?>(null)
    val expenseToEdit: StateFlow<Expense?> = _expenseToEdit.asStateFlow()

    fun startEditExpense(expense: Expense) {
        _expenseToEdit.value = expense
        _showAddDialog.value = true
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            dao.updateExpense(expense)
            _expenseToEdit.value = null
            checkBudgetExceeded()
        }
    }

    fun clearEditExpense() {
        _expenseToEdit.value = null
    }

    // ===== Wydatki cykliczne =====

    val recurringExpenses: StateFlow<List<RecurringExpense>> = dao.getAllRecurringExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRecurringExpense(
        amount: Double,
        category: String,
        description: String,
        frequency: String
    ) {
        viewModelScope.launch {
            val recurring = RecurringExpense(
                amount = amount,
                category = category,
                description = description,
                frequency = frequency,
                startDate = System.currentTimeMillis()
            )
            dao.insertRecurringExpense(recurring)
        }
    }

    fun updateRecurringExpense(recurringExpense: RecurringExpense) {
        viewModelScope.launch {
            dao.updateRecurringExpense(recurringExpense)
        }
    }

    fun deleteRecurringExpense(recurringExpense: RecurringExpense) {
        viewModelScope.launch {
            dao.deleteRecurringExpense(recurringExpense)
        }
    }

    fun toggleRecurringExpense(recurringExpense: RecurringExpense) {
        viewModelScope.launch {
            dao.updateRecurringExpense(
                recurringExpense.copy(isActive = !recurringExpense.isActive)
            )
        }
    }

    // ===== Podział wydatków =====

    val allUnpaidSplits: StateFlow<List<SplitExpense>> = dao.getAllUnpaidSplits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSplitExpenses: StateFlow<List<SplitExpense>> = dao.getAllSplitExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getSplitsForExpense(expenseId: Long): Flow<List<SplitExpense>> {
        return dao.getSplitsForExpense(expenseId)
    }

    fun addSplit(expenseId: Long, personName: String, amount: Double) {
        viewModelScope.launch {
            val split = SplitExpense(
                expenseId = expenseId,
                personName = personName,
                amount = amount
            )
            dao.insertSplitExpense(split)
        }
    }

    fun markSplitAsPaid(splitExpense: SplitExpense) {
        viewModelScope.launch {
            dao.updateSplitExpense(splitExpense.copy(isPaid = true))
        }
    }

    fun markSplitAsUnpaid(splitExpense: SplitExpense) {
        viewModelScope.launch {
            dao.updateSplitExpense(splitExpense.copy(isPaid = false))
        }
    }

    fun updateSplit(splitExpense: SplitExpense, newPersonName: String, newAmount: Double) {
        viewModelScope.launch {
            dao.updateSplitExpense(splitExpense.copy(personName = newPersonName, amount = newAmount))
        }
    }

    fun deleteSplit(splitExpense: SplitExpense) {
        viewModelScope.launch {
            dao.deleteSplitExpense(splitExpense)
        }
    }

    // ===== Sprawdzanie budżetu =====

    private suspend fun checkBudgetExceeded() {
        val budget = dao.getBudgetForMonthSuspend(currentMonth, currentYear) ?: return

        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val now = System.currentTimeMillis()
        val totalSpent = dao.getTotalExpensesByDateRangeSuspend(startOfMonth, now) ?: 0.0
        val paidBack = dao.getTotalPaidSplitsByDateRangeSuspend(startOfMonth, now)
        val effectiveSpent = totalSpent - paidBack

        if (effectiveSpent > budget.amount) {
            NotificationHelper.showBudgetExceededNotification(
                getApplication(),
                effectiveSpent,
                budget.amount
            )
        }
    }
}

enum class TimeFilter(val displayName: String) {
    ALL("Wszystkie"),
    TODAY("Dziś"),
    WEEK("Tydzień"),
    MONTH("Miesiąc")
}
