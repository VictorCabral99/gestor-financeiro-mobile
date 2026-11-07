package com.victorcabral.gestorfinanceiro.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["subcategoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = PaymentSource::class,
            parentColumns = ["id"],
            childColumns = ["paymentSourceId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("categoryId"),
        Index("subcategoryId"),
        Index("paymentSourceId"),
        Index("occurredAtEpochMs"),
    ],
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Amount in BRL cents to avoid floating-point drift. */
    val amountCents: Long,
    /** Estabelecimento / merchant (texto da notificação ou digitado). */
    val origin: String,
    val description: String,
    val categoryId: Long?,
    val subcategoryId: Long? = null,
    /** Conta (débito) ou cartão (crédito) — o tipo da fonte já implica a modalidade. */
    val paymentSourceId: Long? = null,
    val bankPackage: String? = null,
    val occurredAtEpochMs: Long,
    val createdAtEpochMs: Long,
)
