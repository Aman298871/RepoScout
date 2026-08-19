package com.example.reposcout.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reposcout.domain.model.Repository
import com.example.reposcout.domain.repository.GitHubRepository
import com.example.reposcout.util.AppResult
import com.example.reposcout.util.ConnectivityObserver
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val gitHubRepository: GitHubRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _queryFlow = MutableStateFlow("")
    val queryFlow: StateFlow<String> = _queryFlow

    private val _rawState = MutableStateFlow(SearchUiState())
    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow: SharedFlow<String> = _eventFlow.asSharedFlow()

    private var activeSearchJob: Job? = null

    val isOffline: StateFlow<Boolean> = connectivityObserver.observe()
        .combine(MutableStateFlow(Unit)) { status, _ ->
            status == ConnectivityObserver.Status.Unavailable || status == ConnectivityObserver.Status.Lost
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = !connectivityObserver.isCurrentlyConnected()
        )

    val uiState: StateFlow<SearchUiState> = combine(
        _rawState,
        gitHubRepository.getSavedRepositories()
    ) { state, savedRepos ->
        val savedIds = savedRepos.map { it.id }.toSet()
        state.copy(
            repositories = state.repositories.map { repo ->
                repo.copy(isBookmarked = savedIds.contains(repo.id))
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    init {
        observeQuery()
    }

    private fun observeQuery() {
        viewModelScope.launch {
            _queryFlow
                .debounce(450L)
                .distinctUntilChanged()
                .collect { query ->
                    val trimmed = query.trim()
                    if (trimmed.isEmpty()) {
                        activeSearchJob?.cancel()
                        _rawState.update {
                            it.copy(
                                query = "",
                                repositories = emptyList(),
                                isInitialLoading = false,
                                isSearching = false,
                                errorMessage = null,
                                searchFailedQuery = null,
                                hasSearched = false,
                                canPaginate = false
                            )
                        }
                    } else {
                        performSearch(trimmed)
                    }
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _queryFlow.value = newQuery
        _rawState.update { it.copy(query = newQuery) }
    }

    fun clearQuery() {
        onQueryChange("")
    }

    fun retrySearch() {
        val queryToRetry = _rawState.value.searchFailedQuery ?: _queryFlow.value.trim()
        if (queryToRetry.isNotBlank()) {
            performSearch(queryToRetry)
        }
    }

    fun performSearch(query: String) {
        activeSearchJob?.cancel()
        val hasExistingContent = _rawState.value.repositories.isNotEmpty()

        _rawState.update {
            it.copy(
                isSearching = true,
                isInitialLoading = !hasExistingContent,
                errorMessage = null,
                searchFailedQuery = null,
                hasSearched = true
            )
        }

        activeSearchJob = viewModelScope.launch {
            when (val result = gitHubRepository.searchRepositories(query = query, page = 1, perPage = 20)) {
                is AppResult.Success -> {
                    _rawState.update {
                        it.copy(
                            repositories = result.data,
                            currentPage = 1,
                            canPaginate = result.data.size >= 20,
                            isSearching = false,
                            isInitialLoading = false,
                            errorMessage = null,
                            searchFailedQuery = null,
                            isRateLimited = false
                        )
                    }
                }
                is AppResult.Error -> {
                    _rawState.update {
                        it.copy(
                            isSearching = false,
                            isInitialLoading = false,
                            errorMessage = result.message,
                            searchFailedQuery = query,
                            isRateLimited = result.isRateLimited
                        )
                    }
                }
            }
        }
    }

    fun loadNextPage() {
        val currentState = _rawState.value
        val query = _queryFlow.value.trim()
        if (currentState.isLoadingMore || !currentState.canPaginate || currentState.isSearching || query.isEmpty()) {
            return
        }

        val nextPage = currentState.currentPage + 1
        _rawState.update { it.copy(isLoadingMore = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = gitHubRepository.searchRepositories(query = query, page = nextPage, perPage = 20)) {
                is AppResult.Success -> {
                    val newItems = result.data
                    _rawState.update {
                        it.copy(
                            repositories = it.repositories + newItems,
                            currentPage = nextPage,
                            isLoadingMore = false,
                            canPaginate = newItems.size >= 20,
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _rawState.update {
                        it.copy(
                            isLoadingMore = false,
                            errorMessage = "Failed to load more results: ${result.message}"
                        )
                    }
                }
            }
        }
    }

    fun toggleBookmark(repository: Repository) {
        viewModelScope.launch {
            gitHubRepository.toggleBookmark(repository)
        }
    }

    class Factory(
        private val gitHubRepository: GitHubRepository,
        private val connectivityObserver: ConnectivityObserver
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(gitHubRepository, connectivityObserver) as T
        }
    }
}
