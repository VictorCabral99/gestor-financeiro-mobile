package com.victorcabral.gestorfinanceiro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.victorcabral.gestorfinanceiro.data.entity.CardInvoice
import kotlinx.coroutines.flow.Flow

@Dao
interface CardInvoiceDao {
    @Query(
        """
        SELECT * FROM card_invoices
        WHERE year = :year AND month = :month
        ORDER BY id ASC
        """,
    )
    fun observeForMonth(year: Int, month: Int): Flow<List<CardInvoice>>

    @Query(
        """
        SELECT COALESCE(SUM(amountCents), 0) FROM card_invoices
        WHERE year = :year AND month = :month AND isPaid = 0
        """,
    )
    fun observeOpenTotalForMonth(year: Int, month: Int): Flow<Long>

    @Query(
        """
        SELECT * FROM card_invoices
        WHERE paymentSourceId = :paymentSourceId AND year = :year AND month = :month
        LIMIT 1
        """,
    )
    suspend fun getForSourceMonth(paymentSourceId: Long, year: Int, month: Int): CardInvoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: CardInvoice): Long

    @Update
    suspend fun update(invoice: CardInvoice)

    @Query("DELETE FROM card_invoices WHERE id = :id")
    suspend fun deleteById(id: Long)
}
