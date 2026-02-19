package com.maciejtyszczuk.expensetracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis()
)

// Predefiniowane kategorie — zastąpione przez CustomCategory z bazy danych
@Deprecated("Użyj CustomCategory z bazy danych zamiast tego enuma")
enum class ExpenseCategory(val displayName: String, val emoji: String) {
    FOOD("Jedzenie", "🍕"),
    TRANSPORT("Transport", "🚗"),
    SHOPPING("Zakupy", "🛍️"),
    ENTERTAINMENT("Rozrywka", "🎬"),
    BILLS("Rachunki", "📄"),
    HEALTH("Zdrowie", "⚕️"),
    EDUCATION("Edukacja", "📚"),
    OTHER("Inne", "📦");

    companion object {
        fun fromString(category: String): ExpenseCategory {
            return values().find { it.displayName == category } ?: OTHER
        }
    }
}