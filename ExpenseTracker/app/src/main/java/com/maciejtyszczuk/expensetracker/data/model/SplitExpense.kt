package com.maciejtyszczuk.expensetracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "split_expenses",
    foreignKeys = [
        ForeignKey(
            entity = Expense::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("expenseId")]
)
data class SplitExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expenseId: Long,
    val personName: String,
    val amount: Double,
    val isPaid: Boolean = false
)

data class DebtSummary(
    val personName: String,
    val totalAmount: Double
)
