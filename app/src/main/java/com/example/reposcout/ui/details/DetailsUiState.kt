package com.example.reposcout.ui.details

import com.example.reposcout.domain.model.Repository

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Success(val repository: Repository) : DetailsUiState
    data class Error(val message: String, val isRateLimited: Boolean = false) : DetailsUiState
}
