package com.example.reposcout.domain.repository

import com.example.reposcout.domain.model.Repository
import com.example.reposcout.util.AppResult
import kotlinx.coroutines.flow.Flow

interface GitHubRepository {
    suspend fun searchRepositories(
        query: String,
        page: Int,
        perPage: Int = 20,
        sort: String? = null
    ): AppResult<List<Repository>>

    suspend fun getRepositoryDetails(
        owner: String,
        repo: String
    ): AppResult<Repository>

    fun getSavedRepositories(): Flow<List<Repository>>

    fun isRepositorySaved(repositoryId: Long): Flow<Boolean>

    suspend fun toggleBookmark(repository: Repository)

    suspend fun saveRepository(repository: Repository)

    suspend fun deleteRepository(repositoryId: Long)
}
