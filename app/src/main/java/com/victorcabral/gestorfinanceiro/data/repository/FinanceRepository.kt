package com.victorcabral.gestorfinanceiro.data.repository

import com.victorcabral.gestorfinanceiro.data.dao.CategoryDao
import com.victorcabral.gestorfinanceiro.data.dao.ExpenseDao
import com.victorcabral.gestorfinanceiro.data.dao.PendingSuggestionDao
import com.victorcabral.gestorfinanceiro.data.entity.Category
import com.victorcabral.gestorfinanceiro.data.entity.Expense
import com.victorcabral.gestorfinanceiro.data.entity.PendingSuggestion
import com.victorcabral.gestorfinanceiro.util.MonthRange
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val pendingSuggestionDao: PendingSuggestionDao,
) {
    fun observeMonthExpenses(range: MonthRange): Flow<List<Expense>> =
        expenseDao.observeBetween(range.startInclusive, range.endExclusive)

    fun observeMonthTotalCents(range: MonthRange): Flow<Long> =
        expenseDao.observeTotalCentsBetween(range.startInclusive, range.endExclusive)

    fun observePending(): Flow<List<PendingSuggestion>> = pendingSuggestionDao.observeAll()

    fun observePendingCount(): Flow<Int> = pendingSuggestionDao.observeCount()

    fun observeVisibleCategories(): Flow<List<Category>> = categoryDao.observeVisible()

    fun observeAllCategories(): Flow<List<Category>> = categoryDao.observeAll()

    suspend fun getVisibleCategories(): List<Category> = categoryDao.getVisible()

    suspend fun getPending(id: Long): PendingSuggestion? = pendingSuggestionDao.getById(id)

    /**
     * Inserts a pending suggestion if [dedupeKey] is new.
     * @return row id, or -1 when ignored as duplicate.
     */
    suspend fun tryInsertPending(suggestion: PendingSuggestion): Long =
        pendingSuggestionDao.insertIgnore(suggestion)

    suspend fun confirmExpense(
        amountCents: Long,
        origin: String,
        description: String,
        categoryId: Long?,
        bankPackage: String?,
        occurredAtEpochMs: Long,
        pendingIdToDismiss: Long?,
    ): Long {
        val now = System.currentTimeMillis()
        val id = expenseDao.insert(
            Expense(
                amountCents = amountCents,
                origin = origin.trim(),
                description = description.trim(),
                categoryId = categoryId,
                bankPackage = bankPackage,
                occurredAtEpochMs = occurredAtEpochMs,
                createdAtEpochMs = now,
            ),
        )
        if (pendingIdToDismiss != null) {
            pendingSuggestionDao.deleteById(pendingIdToDismiss)
        }
        return id
    }

    suspend fun dismissPending(id: Long) {
        pendingSuggestionDao.deleteById(id)
    }

    suspend fun addCategory(name: String): Long =
        categoryDao.insert(Category(name = name.trim(), isDefault = false))

    suspend fun renameCategory(category: Category, newName: String) {
        categoryDao.update(category.copy(name = newName.trim()))
    }

    suspend fun setCategoryHidden(category: Category, hidden: Boolean) {
        categoryDao.update(category.copy(isHidden = hidden))
    }

    suspend fun ensureDefaultCategoriesSeeded() {
        if (categoryDao.count() == 0) {
            categoryDao.insertAll(
                listOf(
                    Category(name = "alimentação", isDefault = true),
                    Category(name = "transporte", isDefault = true),
                    Category(name = "moradia", isDefault = true),
                    Category(name = "lazer", isDefault = true),
                    Category(name = "outros", isDefault = true),
                ),
            )
        }
    }
}
