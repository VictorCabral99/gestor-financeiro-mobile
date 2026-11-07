package com.victorcabral.gestorfinanceiro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.victorcabral.gestorfinanceiro.data.dao.CategoryDao
import com.victorcabral.gestorfinanceiro.data.dao.ExpenseDao
import com.victorcabral.gestorfinanceiro.data.dao.PendingSuggestionDao
import com.victorcabral.gestorfinanceiro.data.entity.Category
import com.victorcabral.gestorfinanceiro.data.entity.Expense
import com.victorcabral.gestorfinanceiro.data.entity.PendingSuggestion

@Database(
    entities = [Expense::class, Category::class, PendingSuggestion::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun pendingSuggestionDao(): PendingSuggestionDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gestor_financeiro.db",
                ).build().also { instance = it }
            }
        }
    }
}
