package com.victorcabral.gestorfinanceiro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false,
    /** Soft-hide from pickers; retained for historical expenses. */
    val isHidden: Boolean = false,
)
