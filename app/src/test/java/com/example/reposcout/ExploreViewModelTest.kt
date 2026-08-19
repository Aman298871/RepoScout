package com.example.reposcout

import com.example.reposcout.domain.model.Owner
import com.example.reposcout.domain.model.Repository
import com.example.reposcout.domain.repository.GitHubRepository
import com.example.reposcout.ui.explore.ExploreUiState
import com.example.reposcout.ui.explore.ExploreViewModel
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
class ExploreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeConnectivityObserver : ConnectivityObserver {
        override fun observe(): Flow<ConnectivityObserver.Status> = flowOf(ConnectivityObserver.Status.Available)
        override fun isCurrentlyConnected(): Boolean = true
    }

    private class FakeGitHubRepository(val result: AppResult<List<Repository>>) : GitHubRepository {
        var callCount = 0
        override suspend fun searchRepositories(
            query: String,
            page: Int,
            perPage: Int,
            sort: String?
        ): AppResult<List<Repository>> {
            callCount++
            return result
        }

        override suspend fun getRepositoryDetails(owner: String, repo: String): AppResult<Repository> = AppResult.Error("Not needed")
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
    fun `initial load success updates state to Success`() = runTest(testDispatcher) {
        val repos = listOf(createFakeRepo(1L, "android-1"))
        val fakeRepo = FakeGitHubRepository(AppResult.Success(repos))
        val viewModel = ExploreViewModel(fakeRepo, FakeConnectivityObserver())

        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ExploreUiState.Success)
        assertEquals(repos, (state as ExploreUiState.Success).repositories)
        assertEquals(1, fakeRepo.callCount)
        job.cancel()
    }

    @Test
    fun `initial load empty updates state to Empty`() = runTest(testDispatcher) {
        val fakeRepo = FakeGitHubRepository(AppResult.Success(emptyList()))
        val viewModel = ExploreViewModel(fakeRepo, FakeConnectivityObserver())

        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ExploreUiState.Empty)
        job.cancel()
    }

    @Test
    fun `initial load error updates state to Error`() = runTest(testDispatcher) {
        val errorMessage = "API Error"
        val fakeRepo = FakeGitHubRepository(AppResult.Error(errorMessage))
        val viewModel = ExploreViewModel(fakeRepo, FakeConnectivityObserver())

        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ExploreUiState.Error)
        assertEquals(errorMessage, (state as ExploreUiState.Error).message)
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
