package com.victorcabral.gestorfinanceiro.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_suggestions",
    indices = [
        Index(value = ["dedupeKey"], unique = true),
        Index("receivedAtEpochMs"),
    ],
)
data class PendingSuggestion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountCents: Long?,
    val origin: String?,
    val rawText: String,
    val bankPackage: String,
    val bankLabel: String,
    /** Hash of package + normalized text + coarse timestamp window for dedupe. */
    val dedupeKey: String,
    val receivedAtEpochMs: Long,
    val parseConfidence: String = "stub",
)
