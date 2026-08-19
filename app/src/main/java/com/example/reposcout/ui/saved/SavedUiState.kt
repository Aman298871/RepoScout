package com.example.reposcout.ui.saved

import com.example.reposcout.domain.model.Repository

sealed interface SavedUiState {
    data object Loading : SavedUiState
    data class Success(val repositories: List<Repository>) : SavedUiState
    data object Empty : SavedUiState
}
