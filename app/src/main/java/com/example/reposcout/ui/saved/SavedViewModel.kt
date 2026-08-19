package com.example.reposcout.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reposcout.domain.model.Repository
import com.example.reposcout.domain.repository.GitHubRepository
import com.example.reposcout.util.ConnectivityObserver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedViewModel(
    private val gitHubRepository: GitHubRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

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

    val uiState: StateFlow<SavedUiState> = gitHubRepository.getSavedRepositories()
        .map { repos ->
            if (repos.isEmpty()) {
                SavedUiState.Empty
            } else {
                SavedUiState.Success(repos)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SavedUiState.Loading
        )

    fun toggleBookmark(repository: Repository) {
        viewModelScope.launch {
            gitHubRepository.toggleBookmark(repository)
            _eventFlow.emit("Removed '${repository.name}' from bookmarks")
        }
    }

    class Factory(
        private val gitHubRepository: GitHubRepository,
        private val connectivityObserver: ConnectivityObserver
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SavedViewModel(gitHubRepository, connectivityObserver) as T
        }
    }
}
