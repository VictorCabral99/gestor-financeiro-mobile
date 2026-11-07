package com.victorcabral.gestorfinanceiro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.victorcabral.gestorfinanceiro.data.entity.PendingSuggestion
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingSuggestionDao {
    @Query("SELECT * FROM pending_suggestions ORDER BY receivedAtEpochMs DESC")
    fun observeAll(): Flow<List<PendingSuggestion>>

    @Query("SELECT COUNT(*) FROM pending_suggestions")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM pending_suggestions WHERE id = :id")
    suspend fun getById(id: Long): PendingSuggestion?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(suggestion: PendingSuggestion): Long

    @Query("DELETE FROM pending_suggestions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM pending_suggestions WHERE dedupeKey = :dedupeKey)")
    suspend fun existsByDedupeKey(dedupeKey: String): Boolean
}
