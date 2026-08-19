package com.example.reposcout.data.repository

import com.example.reposcout.data.local.dao.RepositoryDao
import com.example.reposcout.data.mapper.toDomain
import com.example.reposcout.data.mapper.toEntity
import com.example.reposcout.data.remote.api.GitHubApiService
import com.example.reposcout.domain.model.Repository
import com.example.reposcout.domain.repository.GitHubRepository
import com.example.reposcout.util.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class GitHubRepositoryImpl(
    private val apiService: GitHubApiService,
    private val repositoryDao: RepositoryDao
) : GitHubRepository {

    override suspend fun searchRepositories(
        query: String,
        page: Int,
        perPage: Int,
        sort: String?
    ): AppResult<List<Repository>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchRepositories(query = query, page = page, perPage = perPage, sort = sort)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val repositories = body.items.map { dto ->
                        val isSaved = repositoryDao.isRepositorySavedSync(dto.id)
                        dto.toDomain(isBookmarked = isSaved)
                    }
                    AppResult.Success(repositories)
                } else {
                    AppResult.Success(emptyList())
                }
            } else {
                val code = response.code()
                val isRateLimit = code == 403 || code == 429
                val errorMessage = when (code) {
                    403, 429 -> "GitHub API rate limit exceeded. Please wait a minute and try again."
                    404 -> "Repositories not found."
                    422 -> "Validation failed for search query."
                    in 500..599 -> "GitHub server error ($code). Please try again later."
                    else -> "Failed to load repositories (HTTP $code)."
                }
                AppResult.Error(errorMessage, isRateLimited = isRateLimit)
            }
        } catch (e: UnknownHostException) {
            AppResult.Error("No internet connection. Please check your network.", e)
        } catch (e: SocketTimeoutException) {
            AppResult.Error("Connection timed out. Please try again.", e)
        } catch (e: IOException) {
            AppResult.Error("Network error occurred: ${e.localizedMessage ?: "Unable to connect"}", e)
        } catch (e: HttpException) {
            AppResult.Error("HTTP error: ${e.message()}", e)
        } catch (e: Exception) {
            AppResult.Error("An unexpected error occurred: ${e.localizedMessage ?: "Unknown"}", e)
        }
    }

    override suspend fun getRepositoryDetails(
        owner: String,
        repo: String
    ): AppResult<Repository> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRepositoryDetails(owner = owner, repo = repo)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val isSaved = repositoryDao.isRepositorySavedSync(body.id)
                    AppResult.Success(body.toDomain(isBookmarked = isSaved))
                } else {
                    AppResult.Error("Repository details not found.")
                }
            } else {
                val code = response.code()
                val isRateLimit = code == 403 || code == 429
                val errorMessage = when (code) {
                    403, 429 -> "GitHub API rate limit exceeded. Please wait a minute and try again."
                    404 -> "Repository '$owner/$repo' not found."
                    in 500..599 -> "GitHub server error ($code)."
                    else -> "Failed to fetch repository details (HTTP $code)."
                }
                AppResult.Error(errorMessage, isRateLimited = isRateLimit)
            }
        } catch (e: UnknownHostException) {
            AppResult.Error("No internet connection. Please check your network.", e)
        } catch (e: SocketTimeoutException) {
            AppResult.Error("Connection timed out. Please try again.", e)
        } catch (e: IOException) {
            AppResult.Error("Network error occurred: ${e.localizedMessage ?: "Unable to connect"}", e)
        } catch (e: Exception) {
            AppResult.Error("An unexpected error occurred: ${e.localizedMessage ?: "Unknown"}", e)
        }
    }

    override fun getSavedRepositories(): Flow<List<Repository>> {
        return repositoryDao.getAllSavedRepositories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun isRepositorySaved(repositoryId: Long): Flow<Boolean> {
        return repositoryDao.isRepositorySaved(repositoryId)
    }

    override suspend fun toggleBookmark(repository: Repository) = withContext(Dispatchers.IO) {
        val isSaved = repositoryDao.isRepositorySavedSync(repository.id)
        if (isSaved) {
            repositoryDao.deleteRepositoryById(repository.id)
        } else {
            repositoryDao.insertRepository(repository.toEntity())
        }
    }

    override suspend fun saveRepository(repository: Repository) = withContext(Dispatchers.IO) {
        repositoryDao.insertRepository(repository.toEntity())
    }

    override suspend fun deleteRepository(repositoryId: Long) = withContext(Dispatchers.IO) {
        repositoryDao.deleteRepositoryById(repositoryId)
    }
}
