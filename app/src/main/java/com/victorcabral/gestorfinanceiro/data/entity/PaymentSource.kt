package com.victorcabral.gestorfinanceiro.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment_sources",
    indices = [Index("type"), Index("isHidden")],
)
data class PaymentSource(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: PaymentSourceType,
    /** Banco associado (ex.: Nubank) — opcional. */
    val bankLabel: String? = null,
    val isHidden: Boolean = false,
    val isDefault: Boolean = false,
)
