package com.maciejtyszczuk.expensetracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.maciejtyszczuk.expensetracker.data.database.ExpenseDatabase
import com.maciejtyszczuk.expensetracker.notification.NotificationHelper
import java.util.Calendar

class BudgetCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dao = ExpenseDatabase.getDatabase(applicationContext).expenseDao()

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)

        val budget = dao.getBudgetForMonthSuspend(currentMonth, currentYear) ?: return Result.success()

        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfMonth = System.currentTimeMillis()
        val totalSpent = dao.getTotalExpensesByDateRangeSuspend(startOfMonth, endOfMonth) ?: 0.0

        if (totalSpent > budget.amount) {
            NotificationHelper.showBudgetExceededNotification(
                applicationContext,
                totalSpent,
                budget.amount
            )
        }

        return Result.success()
    }
}
