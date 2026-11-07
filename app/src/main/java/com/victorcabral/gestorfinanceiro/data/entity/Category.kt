package com.victorcabral.gestorfinanceiro.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Categoria (parentId == null) ou subcategoria de 1 nível (parentId → categoria raiz).
 * Não há subníveis mais profundos.
 */
@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("parentId"), Index("isHidden")],
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentId: Long? = null,
    val isDefault: Boolean = false,
    /** Soft-hide from pickers; retained for historical expenses. */
    val isHidden: Boolean = false,
) {
    val isRoot: Boolean get() = parentId == null
}
