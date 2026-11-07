package com.victorcabral.gestorfinanceiro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.victorcabral.gestorfinanceiro.data.dao.CardInvoiceDao
import com.victorcabral.gestorfinanceiro.data.dao.CategoryDao
import com.victorcabral.gestorfinanceiro.data.dao.ExpenseDao
import com.victorcabral.gestorfinanceiro.data.dao.FixedExpenseDao
import com.victorcabral.gestorfinanceiro.data.dao.IncomeDao
import com.victorcabral.gestorfinanceiro.data.dao.PaymentSourceDao
import com.victorcabral.gestorfinanceiro.data.dao.PendingSuggestionDao
import com.victorcabral.gestorfinanceiro.data.entity.CardInvoice
import com.victorcabral.gestorfinanceiro.data.entity.Category
import com.victorcabral.gestorfinanceiro.data.entity.Expense
import com.victorcabral.gestorfinanceiro.data.entity.FixedExpense
import com.victorcabral.gestorfinanceiro.data.entity.Income
import com.victorcabral.gestorfinanceiro.data.entity.PaymentSource
import com.victorcabral.gestorfinanceiro.data.entity.PendingSuggestion

@Database(
    entities = [
        Expense::class,
        Category::class,
        PendingSuggestion::class,
        PaymentSource::class,
        Income::class,
        FixedExpense::class,
        CardInvoice::class,
    ],
    version = 4,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun pendingSuggestionDao(): PendingSuggestionDao
    abstract fun paymentSourceDao(): PaymentSourceDao
    abstract fun incomeDao(): IncomeDao
    abstract fun fixedExpenseDao(): FixedExpenseDao
    abstract fun cardInvoiceDao(): CardInvoiceDao

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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS payment_sources (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL,
                        bankLabel TEXT,
                        isHidden INTEGER NOT NULL,
                        isDefault INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payment_sources_type ON payment_sources(type)")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_payment_sources_isHidden ON payment_sources(isHidden)",
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS categories_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        parentId INTEGER,
                        isDefault INTEGER NOT NULL,
                        isHidden INTEGER NOT NULL,
                        FOREIGN KEY(parentId) REFERENCES categories_new(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO categories_new (id, name, parentId, isDefault, isHidden)
                    SELECT id, name, NULL, isDefault, isHidden FROM categories
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE categories")
                db.execSQL("ALTER TABLE categories_new RENAME TO categories")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_categories_parentId ON categories(parentId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_categories_isHidden ON categories(isHidden)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS expenses_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amountCents INTEGER NOT NULL,
                        origin TEXT NOT NULL,
                        description TEXT NOT NULL,
                        categoryId INTEGER,
                        subcategoryId INTEGER,
                        paymentSourceId INTEGER,
                        bankPackage TEXT,
                        occurredAtEpochMs INTEGER NOT NULL,
                        createdAtEpochMs INTEGER NOT NULL,
                        FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE SET NULL,
                        FOREIGN KEY(subcategoryId) REFERENCES categories(id) ON DELETE SET NULL,
                        FOREIGN KEY(paymentSourceId) REFERENCES payment_sources(id) ON DELETE SET NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO expenses_new (
                        id, amountCents, origin, description, categoryId,
                        subcategoryId, paymentSourceId, bankPackage,
                        occurredAtEpochMs, createdAtEpochMs
                    )
                    SELECT id, amountCents, origin, description, categoryId,
                        NULL, NULL, bankPackage, occurredAtEpochMs, createdAtEpochMs
                    FROM expenses
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE expenses")
                db.execSQL("ALTER TABLE expenses_new RENAME TO expenses")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_categoryId ON expenses(categoryId)")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_expenses_subcategoryId ON expenses(subcategoryId)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_expenses_paymentSourceId ON expenses(paymentSourceId)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_expenses_occurredAtEpochMs ON expenses(occurredAtEpochMs)",
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS incomes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        amountCents INTEGER NOT NULL,
                        description TEXT NOT NULL,
                        paymentSourceId INTEGER,
                        occurredAtEpochMs INTEGER NOT NULL,
                        createdAtEpochMs INTEGER NOT NULL,
                        FOREIGN KEY(paymentSourceId) REFERENCES payment_sources(id) ON DELETE SET NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_incomes_occurredAtEpochMs ON incomes(occurredAtEpochMs)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_incomes_paymentSourceId ON incomes(paymentSourceId)",
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS fixed_expenses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        amountCents INTEGER NOT NULL,
                        dueDayOfMonth INTEGER,
                        isActive INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_fixed_expenses_isActive ON fixed_expenses(isActive)",
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS card_invoices (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        paymentSourceId INTEGER NOT NULL,
                        year INTEGER NOT NULL,
                        month INTEGER NOT NULL,
                        amountCents INTEGER NOT NULL,
                        isPaid INTEGER NOT NULL,
                        FOREIGN KEY(paymentSourceId) REFERENCES payment_sources(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_card_invoices_paymentSourceId_year_month
                    ON card_invoices(paymentSourceId, year, month)
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_card_invoices_year ON card_invoices(year)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_card_invoices_month ON card_invoices(month)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gestor_financeiro.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
