package com.maciejtyszczuk.expensetracker.data.database

import androidx.room.*
import com.maciejtyszczuk.expensetracker.data.model.Budget
import com.maciejtyszczuk.expensetracker.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

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

    // Budget queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year LIMIT 1")
    fun getBudgetForMonth(month: Int, year: Int): Flow<Budget?>

    @Query("SELECT * FROM budgets ORDER BY year DESC, month DESC LIMIT 1")
    fun getLatestBudget(): Flow<Budget?>
}