package com.example.reposcout.ui.search

import com.example.reposcout.domain.model.Repository

data class SearchUiState(
    val query: String = "",
    val repositories: List<Repository> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isSearching: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canPaginate: Boolean = false,
    val currentPage: Int = 1,
    val errorMessage: String? = null,
    val searchFailedQuery: String? = null,
    val isRateLimited: Boolean = false,
    val hasSearched: Boolean = false
)
