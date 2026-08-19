package com.example.reposcout.ui.details

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

class DetailsViewModel(
    private val owner: String,
    private val repo: String,
    private val gitHubRepository: GitHubRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val _rawState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow: SharedFlow<String> = _eventFlow.asSharedFlow()

    val isOffline: StateFlow<Boolean> = connectivityObserver.observe()
        .combine(MutableStateFlow(Unit)) { status, _ ->
            status == ConnectivityObserver.Status.Unavailable || status == ConnectivityObserver.Status.Lost
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = !connectivityObserver.isCurrentlyConnected()
        )

    val uiState: StateFlow<DetailsUiState> = combine(
        _rawState,
        gitHubRepository.getSavedRepositories()
    ) { state, savedRepos ->
        val savedIds = savedRepos.map { it.id }.toSet()
        when (state) {
            is DetailsUiState.Success -> {
                val isSaved = savedIds.contains(state.repository.id)
                state.copy(repository = state.repository.copy(isBookmarked = isSaved))
            }
            else -> state
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DetailsUiState.Loading
    )

    init {
        loadRepositoryDetails()
    }

    fun loadRepositoryDetails() {
        viewModelScope.launch {
            _rawState.value = DetailsUiState.Loading
            when (val result = gitHubRepository.getRepositoryDetails(owner = owner, repo = repo)) {
                is AppResult.Success -> {
                    _rawState.value = DetailsUiState.Success(result.data)
                }
                is AppResult.Error -> {
                    _rawState.value = DetailsUiState.Error(
                        message = result.message,
                        isRateLimited = result.isRateLimited
                    )
                }
            }
        }
    }

    fun toggleBookmark() {
        val currentState = _rawState.value
        if (currentState is DetailsUiState.Success) {
            viewModelScope.launch {
                val repo = currentState.repository
                gitHubRepository.toggleBookmark(repo)
                val newStatus = !repo.isBookmarked
                _eventFlow.emit(if (newStatus) "Repository saved to bookmarks" else "Repository removed from bookmarks")
            }
        }
    }

    class Factory(
        private val owner: String,
        private val repo: String,
        private val gitHubRepository: GitHubRepository,
        private val connectivityObserver: ConnectivityObserver
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DetailsViewModel(owner, repo, gitHubRepository, connectivityObserver) as T
        }
    }
}
