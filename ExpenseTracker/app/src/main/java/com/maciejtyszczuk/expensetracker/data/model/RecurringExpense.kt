package com.maciejtyszczuk.expensetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RecurringFrequency(val displayName: String) {
    DAILY("Codziennie"),
    WEEKLY("Co tydzień"),
    MONTHLY("Co miesiąc"),
    YEARLY("Co rok")
}

@Entity(tableName = "recurring_expenses")
data class RecurringExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val description: String,
    val frequency: String,
    val startDate: Long,
    val lastGeneratedDate: Long? = null,
    val isActive: Boolean = true
)
