package com.example.reposcout.ui.explore

import com.example.reposcout.domain.model.Repository

sealed interface ExploreUiState {
    data object Loading : ExploreUiState
    data class Success(
        val repositories: List<Repository>,
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val paginationError: String? = null,
        val canPaginate: Boolean = true,
        val currentPage: Int = 1
    ) : ExploreUiState
    data class Error(
        val message: String,
        val isRateLimited: Boolean = false
    ) : ExploreUiState
    data object Empty : ExploreUiState
}
