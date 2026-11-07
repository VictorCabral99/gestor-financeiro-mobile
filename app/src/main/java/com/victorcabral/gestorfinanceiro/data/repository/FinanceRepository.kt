package com.victorcabral.gestorfinanceiro.data.repository

import com.victorcabral.gestorfinanceiro.data.DefaultCategories
import com.victorcabral.gestorfinanceiro.data.DefaultPaymentSources
import com.victorcabral.gestorfinanceiro.data.dao.CategoryDao
import com.victorcabral.gestorfinanceiro.data.dao.CategorySpendRow
import com.victorcabral.gestorfinanceiro.data.dao.ExpenseDao
import com.victorcabral.gestorfinanceiro.data.dao.PaymentSourceDao
import com.victorcabral.gestorfinanceiro.data.dao.PendingSuggestionDao
import com.victorcabral.gestorfinanceiro.data.entity.Category
import com.victorcabral.gestorfinanceiro.data.entity.Expense
import com.victorcabral.gestorfinanceiro.data.entity.PaymentSource
import com.victorcabral.gestorfinanceiro.data.entity.PaymentSourceType
import com.victorcabral.gestorfinanceiro.data.entity.PendingSuggestion
import com.victorcabral.gestorfinanceiro.util.MonthRange
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val pendingSuggestionDao: PendingSuggestionDao,
    private val paymentSourceDao: PaymentSourceDao,
) {
    fun observeMonthExpenses(range: MonthRange): Flow<List<Expense>> =
        expenseDao.observeBetween(range.startInclusive, range.endExclusive)

    fun observeMonthTotalCents(range: MonthRange): Flow<Long> =
        expenseDao.observeTotalCentsBetween(range.startInclusive, range.endExclusive)

    fun observeMonthCount(range: MonthRange): Flow<Int> =
        expenseDao.observeCountBetween(range.startInclusive, range.endExclusive)

    fun observeSpendByCategory(range: MonthRange): Flow<List<CategorySpendRow>> =
        expenseDao.observeSpendByCategory(range.startInclusive, range.endExclusive)

    fun observePending(): Flow<List<PendingSuggestion>> = pendingSuggestionDao.observeAll()

    fun observePendingCount(): Flow<Int> = pendingSuggestionDao.observeCount()

    fun observeVisibleRootCategories(): Flow<List<Category>> = categoryDao.observeVisibleRoots()

    fun observeVisibleSubcategories(parentId: Long): Flow<List<Category>> =
        categoryDao.observeVisibleChildren(parentId)

    fun observeAllCategories(): Flow<List<Category>> = categoryDao.observeAll()

    fun observeRootCategories(): Flow<List<Category>> = categoryDao.observeRoots()

    fun observeChildCategories(parentId: Long): Flow<List<Category>> =
        categoryDao.observeChildren(parentId)

    fun observeVisiblePaymentSources(): Flow<List<PaymentSource>> = paymentSourceDao.observeVisible()

    fun observeAllPaymentSources(): Flow<List<PaymentSource>> = paymentSourceDao.observeAll()

    suspend fun getPending(id: Long): PendingSuggestion? = pendingSuggestionDao.getById(id)

    suspend fun getCategory(id: Long): Category? = categoryDao.getById(id)

    suspend fun getPaymentSource(id: Long): PaymentSource? = paymentSourceDao.getById(id)

    suspend fun tryInsertPending(suggestion: PendingSuggestion): Long =
        pendingSuggestionDao.insertIgnore(suggestion)

    suspend fun confirmExpense(
        amountCents: Long,
        origin: String,
        description: String,
        categoryId: Long?,
        subcategoryId: Long?,
        paymentSourceId: Long?,
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
                subcategoryId = subcategoryId,
                paymentSourceId = paymentSourceId,
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

    suspend fun addRootCategory(name: String): Long =
        categoryDao.insert(Category(name = name.trim(), parentId = null, isDefault = false))

    suspend fun addSubcategory(parentId: Long, name: String): Long {
        val parent = categoryDao.getById(parentId)
            ?: error("Categoria pai não encontrada")
        require(parent.parentId == null) { "Subcategoria só pode ficar sob categoria raiz" }
        return categoryDao.insert(
            Category(name = name.trim(), parentId = parentId, isDefault = false),
        )
    }

    suspend fun renameCategory(category: Category, newName: String) {
        categoryDao.update(category.copy(name = newName.trim()))
    }

    suspend fun setCategoryHidden(category: Category, hidden: Boolean) {
        categoryDao.update(category.copy(isHidden = hidden))
    }

    suspend fun addPaymentSource(
        name: String,
        type: PaymentSourceType,
        bankLabel: String?,
    ): Long = paymentSourceDao.insert(
        PaymentSource(
            name = name.trim(),
            type = type,
            bankLabel = bankLabel?.trim()?.ifEmpty { null },
            isDefault = false,
        ),
    )

    suspend fun renamePaymentSource(source: PaymentSource, newName: String) {
        paymentSourceDao.update(source.copy(name = newName.trim()))
    }

    suspend fun setPaymentSourceHidden(source: PaymentSource, hidden: Boolean) {
        paymentSourceDao.update(source.copy(isHidden = hidden))
    }

    suspend fun ensureDefaultsSeeded() {
        if (categoryDao.count() == 0) {
            val rootIds = categoryDao.insertAll(DefaultCategories.seedRoots)
            DefaultCategories.seedSubcategoriesByRoot.forEach { (rootName, subs) ->
                val rootIndex = DefaultCategories.rootNames.indexOf(rootName)
                if (rootIndex < 0) return@forEach
                val rootId = rootIds.getOrNull(rootIndex) ?: return@forEach
                if (rootId <= 0L) return@forEach
                categoryDao.insertAll(
                    subs.map { Category(name = it, parentId = rootId, isDefault = true) },
                )
            }
        }
        if (paymentSourceDao.count() == 0) {
            paymentSourceDao.insertAll(DefaultPaymentSources.seed)
        }
    }
}
