package com.maciejtyszczuk.expensetracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.maciejtyszczuk.expensetracker.R

object NotificationHelper {

    const val CHANNEL_BUDGET = "budget_alerts"
    const val CHANNEL_REMINDERS = "daily_reminders"
    const val CHANNEL_RECURRING = "recurring_expenses"

    private const val NOTIFICATION_ID_BUDGET = 1001
    private const val NOTIFICATION_ID_REMINDER = 1002
    private const val NOTIFICATION_ID_RECURRING = 1003

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET,
                "Alerty budżetowe",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Powiadomienia o przekroczeniu budżetu"
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Przypomnienia",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Codzienne przypomnienia o dodaniu wydatków"
            }

            val recurringChannel = NotificationChannel(
                CHANNEL_RECURRING,
                "Wydatki cykliczne",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Powiadomienia o automatycznie dodanych wydatkach cyklicznych"
            }

            manager.createNotificationChannels(
                listOf(budgetChannel, reminderChannel, recurringChannel)
            )
        }
    }

    fun showBudgetExceededNotification(context: Context, spent: Double, budget: Double) {
        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Przekroczono budżet!")
            .setContentText(
                String.format("Wydano %.2f zł z %.2f zł budżetu", spent, budget)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BUDGET, notification)
        } catch (_: SecurityException) {
            // Brak uprawnień POST_NOTIFICATIONS
        }
    }

    fun showDailyReminderNotification(context: Context) {
        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Pamiętaj o wydatkach!")
            .setContentText("Czy dodałeś dzisiaj swoje wydatki?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_REMINDER, notification)
        } catch (_: SecurityException) {
            // Brak uprawnień POST_NOTIFICATIONS
        }
    }

    fun showRecurringExpenseNotification(context: Context, description: String, amount: Double) {
        val notification = NotificationCompat.Builder(context, CHANNEL_RECURRING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Dodano wydatek cykliczny")
            .setContentText(
                String.format("%s — %.2f zł", description, amount)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_RECURRING + System.currentTimeMillis().toInt(),
                notification
            )
        } catch (_: SecurityException) {
            // Brak uprawnień POST_NOTIFICATIONS
        }
    }
}
