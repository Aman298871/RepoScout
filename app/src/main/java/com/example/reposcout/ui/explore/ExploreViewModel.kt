package com.example.reposcout.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reposcout.domain.model.Repository
import com.example.reposcout.domain.repository.GitHubRepository
import com.example.reposcout.util.AppResult
import com.example.reposcout.util.ConnectivityObserver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val gitHubRepository: GitHubRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _rawState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow: SharedFlow<String> = _eventFlow.asSharedFlow()

    private var currentSort: String? = null

    val isOffline: StateFlow<Boolean> = connectivityObserver.observe()
        .combine(MutableStateFlow(Unit)) { status, _ ->
            status == ConnectivityObserver.Status.Unavailable || status == ConnectivityObserver.Status.Lost
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = !connectivityObserver.isCurrentlyConnected()
        )

    val uiState: StateFlow<ExploreUiState> = combine(
        _rawState,
        gitHubRepository.getSavedRepositories()
    ) { state, savedRepos ->
        val savedIds = savedRepos.map { it.id }.toSet()
        when (state) {
            is ExploreUiState.Success -> {
                state.copy(
                    repositories = state.repositories.map { repo ->
                        repo.copy(isBookmarked = savedIds.contains(repo.id))
                    }
                )
            }
            else -> state
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExploreUiState.Loading
    )

    init {
        loadInitialRepositories()
    }

    fun loadInitialRepositories(query: String = "android", sort: String? = null) {
        currentSort = sort
        viewModelScope.launch {
            _rawState.value = ExploreUiState.Loading
            when (val result = gitHubRepository.searchRepositories(query = query, page = 1, perPage = 20, sort = sort)) {
                is AppResult.Success -> {
                    if (result.data.isEmpty()) {
                        _rawState.value = ExploreUiState.Empty
                    } else {
                        _rawState.value = ExploreUiState.Success(
                            repositories = result.data,
                            currentPage = 1,
                            canPaginate = result.data.size >= 20
                        )
                    }
                }
                is AppResult.Error -> {
                    _rawState.value = ExploreUiState.Error(
                        message = result.message,
                        isRateLimited = result.isRateLimited
                    )
                }
            }
        }
    }

    fun refresh(query: String = "android") {
        val currentState = _rawState.value
        viewModelScope.launch {
            if (currentState is ExploreUiState.Success) {
                _rawState.value = currentState.copy(isRefreshing = true, paginationError = null)
            }

            when (val result = gitHubRepository.searchRepositories(query = query, page = 1, perPage = 20, sort = currentSort)) {
                is AppResult.Success -> {
                    if (result.data.isEmpty()) {
                        _rawState.value = ExploreUiState.Empty
                    } else {
                        _rawState.value = ExploreUiState.Success(
                            repositories = result.data,
                            currentPage = 1,
                            canPaginate = result.data.size >= 20,
                            isRefreshing = false
                        )
                    }
                }
                is AppResult.Error -> {
                    if (currentState is ExploreUiState.Success) {
                        _rawState.value = currentState.copy(isRefreshing = false)
                        _eventFlow.emit("Refresh failed: ${result.message}")
                    } else {
                        _rawState.value = ExploreUiState.Error(
                            message = result.message,
                            isRateLimited = result.isRateLimited
                        )
                    }
                }
            }
        }
    }

    fun loadNextPage(query: String = "android") {
        val currentState = _rawState.value as? ExploreUiState.Success ?: return
        if (currentState.isLoadingMore || !currentState.canPaginate || currentState.isRefreshing) {
            return
        }

        val nextPage = currentState.currentPage + 1
        _rawState.value = currentState.copy(isLoadingMore = true, paginationError = null)

        viewModelScope.launch {
            when (val result = gitHubRepository.searchRepositories(query = query, page = nextPage, perPage = 20, sort = currentSort)) {
                is AppResult.Success -> {
                    val newItems = result.data
                    val updatedList = currentState.repositories + newItems
                    _rawState.value = currentState.copy(
                        repositories = updatedList,
                        currentPage = nextPage,
                        isLoadingMore = false,
                        canPaginate = newItems.size >= 20,
                        paginationError = null
                    )
                }
                is AppResult.Error -> {
                    _rawState.value = currentState.copy(
                        isLoadingMore = false,
                        paginationError = result.message
                    )
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
            return ExploreViewModel(gitHubRepository, connectivityObserver) as T
        }
    }
}
