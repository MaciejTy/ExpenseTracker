package com.maciejtyszczuk.expensetracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.maciejtyszczuk.expensetracker.data.database.ExpenseDatabase
import com.maciejtyszczuk.expensetracker.data.model.Expense
import com.maciejtyszczuk.expensetracker.data.model.RecurringFrequency
import com.maciejtyszczuk.expensetracker.notification.NotificationHelper
import java.util.Calendar

class RecurringExpenseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dao = ExpenseDatabase.getDatabase(applicationContext).expenseDao()
        val activeExpenses = dao.getActiveRecurringExpenses()
        val now = System.currentTimeMillis()

        for (recurring in activeExpenses) {
            val shouldGenerate = shouldGenerateExpense(recurring.lastGeneratedDate, recurring.frequency, recurring.startDate, now)

            if (shouldGenerate) {
                val expense = Expense(
                    amount = recurring.amount,
                    category = recurring.category,
                    description = recurring.description,
                    date = now
                )
                dao.insertExpense(expense)
                dao.updateRecurringExpense(recurring.copy(lastGeneratedDate = now))

                NotificationHelper.showRecurringExpenseNotification(
                    applicationContext,
                    recurring.description.ifEmpty { recurring.category },
                    recurring.amount
                )
            }
        }

        return Result.success()
    }

    private fun shouldGenerateExpense(
        lastGenerated: Long?,
        frequency: String,
        startDate: Long,
        now: Long
    ): Boolean {
        val referenceDate = lastGenerated ?: (startDate - getIntervalMillis(frequency))
        val elapsed = now - referenceDate
        return elapsed >= getIntervalMillis(frequency)
    }

    private fun getIntervalMillis(frequency: String): Long {
        return when (frequency) {
            RecurringFrequency.DAILY.name -> 24L * 60 * 60 * 1000
            RecurringFrequency.WEEKLY.name -> 7L * 24 * 60 * 60 * 1000
            RecurringFrequency.MONTHLY.name -> 30L * 24 * 60 * 60 * 1000
            RecurringFrequency.YEARLY.name -> 365L * 24 * 60 * 60 * 1000
            else -> 30L * 24 * 60 * 60 * 1000
        }
    }
}
