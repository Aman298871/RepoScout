package com.example.reposcout

import com.example.reposcout.domain.model.Owner
import com.example.reposcout.domain.model.Repository
import com.example.reposcout.domain.repository.GitHubRepository
import com.example.reposcout.ui.search.SearchViewModel
import com.example.reposcout.util.AppResult
import com.example.reposcout.util.ConnectivityObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchDebounceTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeConnectivityObserver : ConnectivityObserver {
        override fun observe(): Flow<ConnectivityObserver.Status> = flowOf(ConnectivityObserver.Status.Available)
        override fun isCurrentlyConnected(): Boolean = true
    }

    private class TrackingGitHubRepository : GitHubRepository {
        val requestedQueries = mutableListOf<String>()

        override suspend fun searchRepositories(
            query: String, 
            page: Int, 
            perPage: Int,
            sort: String?
        ): AppResult<List<Repository>> {
            requestedQueries.add(query)
            return AppResult.Success(
                listOf(
                    Repository(
                        id = 1L,
                        name = query,
                        fullName = "user/$query",
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
                )
            )
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
    fun `rapid typing within 450ms debounces and makes only one API call`() = runTest(testDispatcher) {
        val fakeRepo = TrackingGitHubRepository()
        val fakeConn = FakeConnectivityObserver()
        val viewModel = SearchViewModel(fakeRepo, fakeConn)

        // Rapid keystrokes: a -> an -> and -> andro -> android
        viewModel.onQueryChange("a")
        advanceTimeBy(100)
        viewModel.onQueryChange("an")
        advanceTimeBy(100)
        viewModel.onQueryChange("and")
        advanceTimeBy(100)
        viewModel.onQueryChange("andro")
        advanceTimeBy(100)
        viewModel.onQueryChange("android")

        // Advance beyond 450ms debounce threshold
        advanceTimeBy(500)
        advanceUntilIdle()

        // Only the final query "android" should have triggered an API request
        assertEquals(1, fakeRepo.requestedQueries.size)
        assertEquals("android", fakeRepo.requestedQueries.first())
    }
}
