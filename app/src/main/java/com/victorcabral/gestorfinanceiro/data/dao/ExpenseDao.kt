package com.victorcabral.gestorfinanceiro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.victorcabral.gestorfinanceiro.data.entity.Expense
import kotlinx.coroutines.flow.Flow

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)
}
