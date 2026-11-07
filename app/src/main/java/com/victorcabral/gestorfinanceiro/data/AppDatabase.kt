package com.victorcabral.gestorfinanceiro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.victorcabral.gestorfinanceiro.data.dao.CategoryDao
import com.victorcabral.gestorfinanceiro.data.dao.ExpenseDao
import com.victorcabral.gestorfinanceiro.data.dao.PendingSuggestionDao
import com.victorcabral.gestorfinanceiro.data.entity.Category
import com.victorcabral.gestorfinanceiro.data.entity.Expense
import com.victorcabral.gestorfinanceiro.data.entity.PendingSuggestion

@Database(
    entities = [Expense::class, Category::class, PendingSuggestion::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun pendingSuggestionDao(): PendingSuggestionDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE categories ADD COLUMN isHidden INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    "ALTER TABLE pending_suggestions ADD COLUMN needsManualReview INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gestor_financeiro.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
