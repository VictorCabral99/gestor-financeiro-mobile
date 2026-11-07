package com.victorcabral.gestorfinanceiro.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Despesa fixa mensal (aluguel, internet, etc.).
 * A soma das ativas alimenta o card **Pendentes** da Home
 * (“pendentes até o fim do mês”).
 */
@Entity(
    tableName = "fixed_expenses",
    indices = [Index("isActive")],
)
data class FixedExpense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amountCents: Long,
    /** Dia do mês em que costuma vencer (1–31), opcional. */
    val dueDayOfMonth: Int? = null,
    val isActive: Boolean = true,
)
