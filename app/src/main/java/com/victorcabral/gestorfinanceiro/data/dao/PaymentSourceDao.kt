package com.victorcabral.gestorfinanceiro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.victorcabral.gestorfinanceiro.data.entity.PaymentSource
import com.victorcabral.gestorfinanceiro.data.entity.PaymentSourceType
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentSourceDao {
    @Query(
        """
        SELECT * FROM payment_sources
        WHERE isHidden = 0
        ORDER BY type ASC, name COLLATE NOCASE ASC
        """,
    )
    fun observeVisible(): Flow<List<PaymentSource>>

    @Query(
        """
        SELECT * FROM payment_sources
        ORDER BY isHidden ASC, type ASC, name COLLATE NOCASE ASC
        """,
    )
    fun observeAll(): Flow<List<PaymentSource>>

    @Query(
        """
        SELECT * FROM payment_sources
        WHERE isHidden = 0 AND type = :type
        ORDER BY name COLLATE NOCASE ASC
        """,
    )
    fun observeVisibleByType(type: PaymentSourceType): Flow<List<PaymentSource>>

    @Query("SELECT * FROM payment_sources WHERE id = :id")
    suspend fun getById(id: Long): PaymentSource?

    @Query("SELECT COUNT(*) FROM payment_sources")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(source: PaymentSource): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(sources: List<PaymentSource>): List<Long>

    @Update
    suspend fun update(source: PaymentSource)
}
