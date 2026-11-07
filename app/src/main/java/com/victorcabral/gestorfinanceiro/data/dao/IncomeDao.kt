package com.victorcabral.gestorfinanceiro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.victorcabral.gestorfinanceiro.data.entity.Income
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes ORDER BY occurredAtEpochMs DESC")
    fun observeAll(): Flow<List<Income>>

    @Query(
        """
        SELECT COALESCE(SUM(amountCents), 0) FROM incomes
        WHERE occurredAtEpochMs BETWEEN :startInclusive AND :endExclusive
        """,
    )
    fun observeTotalBetween(startInclusive: Long, endExclusive: Long): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: Income): Long

    @Query("DELETE FROM incomes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
