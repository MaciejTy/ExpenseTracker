package com.maciejtyszczuk.expensetracker.data.database

import androidx.room.*
import com.maciejtyszczuk.expensetracker.data.model.Budget
import com.maciejtyszczuk.expensetracker.data.model.CustomCategory
import com.maciejtyszczuk.expensetracker.data.model.Expense
import com.maciejtyszczuk.expensetracker.data.model.RecurringExpense
import com.maciejtyszczuk.expensetracker.data.model.SplitExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    // ===== Expenses =====

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getExpensesByCategory(category: String): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenses(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalExpensesByDateRange(startDate: Long, endDate: Long): Flow<Double?>

    // ===== Budget =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year LIMIT 1")
    fun getBudgetForMonth(month: Int, year: Int): Flow<Budget?>

    @Query("SELECT * FROM budgets ORDER BY year DESC, month DESC LIMIT 1")
    fun getLatestBudget(): Flow<Budget?>

    // ===== Custom Categories =====

    @Query("SELECT * FROM custom_categories ORDER BY isDefault DESC, name ASC")
    fun getAllCategories(): Flow<List<CustomCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CustomCategory)

    @Delete
    suspend fun deleteCategory(category: CustomCategory)

    @Query("SELECT * FROM custom_categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): CustomCategory?

    // ===== Recurring Expenses =====

    @Query("SELECT * FROM recurring_expenses ORDER BY startDate DESC")
    fun getAllRecurringExpenses(): Flow<List<RecurringExpense>>

    @Query("SELECT * FROM recurring_expenses WHERE isActive = 1")
    suspend fun getActiveRecurringExpenses(): List<RecurringExpense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringExpense(recurringExpense: RecurringExpense)

    @Update
    suspend fun updateRecurringExpense(recurringExpense: RecurringExpense)

    @Delete
    suspend fun deleteRecurringExpense(recurringExpense: RecurringExpense)

    @Query("SELECT * FROM recurring_expenses WHERE id = :id")
    suspend fun getRecurringExpenseById(id: Long): RecurringExpense?

    // ===== Split Expenses =====

    @Query("SELECT * FROM split_expenses WHERE expenseId = :expenseId")
    fun getSplitsForExpense(expenseId: Long): Flow<List<SplitExpense>>

    @Query("SELECT * FROM split_expenses WHERE isPaid = 0")
    fun getAllUnpaidSplits(): Flow<List<SplitExpense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSplitExpense(splitExpense: SplitExpense)

    @Update
    suspend fun updateSplitExpense(splitExpense: SplitExpense)

    @Delete
    suspend fun deleteSplitExpense(splitExpense: SplitExpense)

    @Query("SELECT * FROM split_expenses")
    fun getAllSplitExpenses(): Flow<List<SplitExpense>>

    // ===== Suspend versions for Workers =====

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year LIMIT 1")
    suspend fun getBudgetForMonthSuspend(month: Int, year: Int): Budget?

    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalExpensesByDateRangeSuspend(startDate: Long, endDate: Long): Double?
}
