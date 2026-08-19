package com.example.reposcout

import com.example.reposcout.domain.model.Owner
import com.example.reposcout.domain.model.Repository
import com.example.reposcout.domain.repository.GitHubRepository
import com.example.reposcout.ui.details.DetailsUiState
import com.example.reposcout.ui.details.DetailsViewModel
import com.example.reposcout.util.AppResult
import com.example.reposcout.util.ConnectivityObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeConnectivityObserver : ConnectivityObserver {
        override fun observe(): Flow<ConnectivityObserver.Status> = flowOf(ConnectivityObserver.Status.Available)
        override fun isCurrentlyConnected(): Boolean = true
    }

    private class MockGitHubRepository : GitHubRepository {
        var repoResult: AppResult<Repository> = AppResult.Error("Default")

        override suspend fun searchRepositories(
            query: String,
            page: Int,
            perPage: Int,
            sort: String?
        ): AppResult<List<Repository>> = AppResult.Error("Not needed")
        override suspend fun getRepositoryDetails(owner: String, repo: String): AppResult<Repository> = repoResult
        override fun getSavedRepositories(): Flow<List<Repository>> = flowOf(emptyList())
        override fun isRepositorySaved(repositoryId: Long): Flow<Boolean> = flowOf(false)
        override suspend fun toggleBookmark(repository: Repository) {}
        override suspend fun saveRepository(repository: Repository) {}
        override suspend fun deleteRepository(repositoryId: Long) {}
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load repository details success updates state`() = runTest(testDispatcher) {
        val repo = createFakeRepo(1L, "test-repo")
        val fakeRepo = MockGitHubRepository()
        fakeRepo.repoResult = AppResult.Success(repo)

        val viewModel = DetailsViewModel("owner", "repo", fakeRepo, FakeConnectivityObserver())
        val job = launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is DetailsUiState.Success)
        assertEquals(repo, (state as DetailsUiState.Success).repository)
        
        job.cancel()
    }

    private fun createFakeRepo(id: Long, name: String) = Repository(
        id = id,
        name = name,
        fullName = "user/$name",
        owner = Owner(1L, "user", "", ""),
        description = "desc",
        htmlUrl = "https://github.com",
        language = "Kotlin",
        stargazersCount = 10,
        forksCount = 1,
        watchersCount = 10,
        openIssuesCount = 0,
        license = null,
        createdAt = null,
        updatedAt = null
    )
}
