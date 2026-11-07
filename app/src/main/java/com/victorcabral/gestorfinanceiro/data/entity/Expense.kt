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
    ],
    indices = [Index("categoryId"), Index("occurredAtEpochMs")],
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Amount in BRL cents to avoid floating-point drift. */
    val amountCents: Long,
    val origin: String,
    val description: String,
    val categoryId: Long?,
    val bankPackage: String? = null,
    val occurredAtEpochMs: Long,
    val createdAtEpochMs: Long,
)
