package com.victorcabral.gestorfinanceiro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.victorcabral.gestorfinanceiro.data.entity.FixedExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface FixedExpenseDao {
    @Query("SELECT * FROM fixed_expenses ORDER BY isActive DESC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<FixedExpense>>

    @Query(
        """
        SELECT COALESCE(SUM(amountCents), 0) FROM fixed_expenses
        WHERE isActive = 1
        """,
    )
    fun observeActiveTotalCents(): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: FixedExpense): Long

    @Update
    suspend fun update(expense: FixedExpense)

    @Query("DELETE FROM fixed_expenses WHERE id = :id")
    suspend fun deleteById(id: Long)
}
