package com.victorcabral.gestorfinanceiro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.victorcabral.gestorfinanceiro.data.entity.Expense
import kotlinx.coroutines.flow.Flow

data class CategorySpendRow(
    val categoryId: Long?,
    val totalCents: Long,
)

@Dao
interface ExpenseDao {
    @Query(
        """
        SELECT * FROM expenses
        WHERE occurredAtEpochMs BETWEEN :startInclusive AND :endExclusive
        ORDER BY occurredAtEpochMs DESC
        """,
    )
    fun observeBetween(startInclusive: Long, endExclusive: Long): Flow<List<Expense>>

    @Query(
        """
        SELECT COALESCE(SUM(amountCents), 0) FROM expenses
        WHERE occurredAtEpochMs BETWEEN :startInclusive AND :endExclusive
        """,
    )
    fun observeTotalCentsBetween(startInclusive: Long, endExclusive: Long): Flow<Long>

    @Query(
        """
        SELECT categoryId AS categoryId, COALESCE(SUM(amountCents), 0) AS totalCents
        FROM expenses
        WHERE occurredAtEpochMs BETWEEN :startInclusive AND :endExclusive
        GROUP BY categoryId
        ORDER BY totalCents DESC
        """,
    )
    fun observeSpendByCategory(startInclusive: Long, endExclusive: Long): Flow<List<CategorySpendRow>>

    @Query(
        """
        SELECT COUNT(*) FROM expenses
        WHERE occurredAtEpochMs BETWEEN :startInclusive AND :endExclusive
        """,
    )
    fun observeCountBetween(startInclusive: Long, endExclusive: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)
}
