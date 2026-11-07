package com.victorcabral.gestorfinanceiro.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Receita (ex.: salário) — aumenta o Caixa. */
@Entity(
    tableName = "incomes",
    foreignKeys = [
        ForeignKey(
            entity = PaymentSource::class,
            parentColumns = ["id"],
            childColumns = ["paymentSourceId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("occurredAtEpochMs"), Index("paymentSourceId")],
)
data class Income(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountCents: Long,
    val description: String,
    val paymentSourceId: Long? = null,
    val occurredAtEpochMs: Long,
    val createdAtEpochMs: Long,
)
