package com.victorcabral.gestorfinanceiro.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Fatura de cartão de um mês específico.
 * Soma das não pagas do mês → card **Faturas** da Home.
 */
@Entity(
    tableName = "card_invoices",
    foreignKeys = [
        ForeignKey(
            entity = PaymentSource::class,
            parentColumns = ["id"],
            childColumns = ["paymentSourceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["paymentSourceId", "year", "month"], unique = true),
        Index("year"),
        Index("month"),
    ],
)
data class CardInvoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paymentSourceId: Long,
    val year: Int,
    /** Calendar.MONTH style: 0 = January */
    val month: Int,
    val amountCents: Long,
    val isPaid: Boolean = false,
)
