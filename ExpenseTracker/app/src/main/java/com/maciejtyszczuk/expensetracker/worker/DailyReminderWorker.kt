package com.maciejtyszczuk.expensetracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.maciejtyszczuk.expensetracker.notification.NotificationHelper

class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        NotificationHelper.showDailyReminderNotification(applicationContext)
        return Result.success()
    }
}
