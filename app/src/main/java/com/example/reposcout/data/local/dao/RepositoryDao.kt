package com.example.reposcout.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.reposcout.data.local.entity.RepositoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepositoryDao {

    @Query("SELECT * FROM saved_repositories ORDER BY savedAt DESC")
    fun getAllSavedRepositories(): Flow<List<RepositoryEntity>>

    @Query("SELECT * FROM saved_repositories WHERE id = :id LIMIT 1")
    suspend fun getSavedRepositoryById(id: Long): RepositoryEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM saved_repositories WHERE id = :id)")
    fun isRepositorySaved(id: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_repositories WHERE id = :id)")
    suspend fun isRepositorySavedSync(id: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepository(repository: RepositoryEntity)

    @Query("DELETE FROM saved_repositories WHERE id = :id")
    suspend fun deleteRepositoryById(id: Long)

    @Query("DELETE FROM saved_repositories")
    suspend fun clearAll()
}
