package com.victorcabral.gestorfinanceiro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.victorcabral.gestorfinanceiro.data.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query(
        """
        SELECT * FROM categories
        WHERE isHidden = 0 AND parentId IS NULL
        ORDER BY name COLLATE NOCASE ASC
        """,
    )
    fun observeVisibleRoots(): Flow<List<Category>>

    @Query(
        """
        SELECT * FROM categories
        WHERE isHidden = 0 AND parentId = :parentId
        ORDER BY name COLLATE NOCASE ASC
        """,
    )
    fun observeVisibleChildren(parentId: Long): Flow<List<Category>>

    @Query(
        """
        SELECT * FROM categories
        WHERE isHidden = 0 AND parentId = :parentId
        ORDER BY name COLLATE NOCASE ASC
        """,
    )
    suspend fun getVisibleChildren(parentId: Long): List<Category>

    @Query("SELECT * FROM categories ORDER BY parentId IS NOT NULL, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<Category>>

    @Query(
        """
        SELECT * FROM categories
        WHERE parentId IS NULL
        ORDER BY isHidden ASC, name COLLATE NOCASE ASC
        """,
    )
    fun observeRoots(): Flow<List<Category>>

    @Query(
        """
        SELECT * FROM categories
        WHERE parentId = :parentId
        ORDER BY isHidden ASC, name COLLATE NOCASE ASC
        """,
    )
    fun observeChildren(parentId: Long): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): Category?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Category>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)
}
