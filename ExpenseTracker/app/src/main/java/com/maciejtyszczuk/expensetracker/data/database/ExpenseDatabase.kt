package com.maciejtyszczuk.expensetracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.maciejtyszczuk.expensetracker.data.model.Budget
import com.maciejtyszczuk.expensetracker.data.model.CustomCategory
import com.maciejtyszczuk.expensetracker.data.model.Expense
import com.maciejtyszczuk.expensetracker.data.model.RecurringExpense
import com.maciejtyszczuk.expensetracker.data.model.SplitExpense

@Database(
    entities = [
        Expense::class,
        Budget::class,
        CustomCategory::class,
        RecurringExpense::class,
        SplitExpense::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        private val defaultCategories = listOf(
            CustomCategory(name = "Jedzenie", emoji = "\uD83C\uDF55", isDefault = true),
            CustomCategory(name = "Transport", emoji = "\uD83D\uDE97", isDefault = true),
            CustomCategory(name = "Zakupy", emoji = "\uD83D\uDECD\uFE0F", isDefault = true),
            CustomCategory(name = "Rozrywka", emoji = "\uD83C\uDFAC", isDefault = true),
            CustomCategory(name = "Rachunki", emoji = "\uD83D\uDCC4", isDefault = true),
            CustomCategory(name = "Zdrowie", emoji = "\u2695\uFE0F", isDefault = true),
            CustomCategory(name = "Edukacja", emoji = "\uD83D\uDCDA", isDefault = true),
            CustomCategory(name = "Inne", emoji = "\uD83D\uDCE6", isDefault = true)
        )

        fun getDatabase(context: Context): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(PrepopulateCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class PrepopulateCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                defaultCategories.forEach { category ->
                    db.execSQL(
                        "INSERT INTO custom_categories (name, emoji, isDefault) VALUES (?, ?, ?)",
                        arrayOf(category.name, category.emoji, if (category.isDefault) 1 else 0)
                    )
                }
            }
        }
    }
}
