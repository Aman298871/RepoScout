package com.example.reposcout.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.reposcout.domain.model.Repository
import com.example.reposcout.ui.components.EmptyView
import com.example.reposcout.ui.components.ErrorView
import com.example.reposcout.ui.components.LoadingView
import com.example.reposcout.ui.components.NetworkBanner
import com.example.reposcout.ui.components.PaginationFooter
import com.example.reposcout.ui.components.RepoCard

private val suggestedQueries = listOf("compose", "coroutines", "ktor", "room", "retrofit", "architecture")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onRepoClick: (owner: String, repo: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Search Repositories",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.testTag("search_top_bar")
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Offline banner if disconnected
            NetworkBanner(isOffline = isOffline)

            // Search Input Box
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { viewModel.onQueryChange(it) },
                placeholder = { Text("Search GitHub (e.g. kotlin, compose, rust)...", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearQuery() },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    if (uiState.query.isNotBlank()) {
                        viewModel.performSearch(uiState.query.trim())
                    }
                }),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("search_text_field")
            )

            // Linear Progress Indicator during search if previous results exist
            if (uiState.isSearching && uiState.repositories.isNotEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .testTag("search_linear_loading"),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Suggestion chips when query is empty
            if (uiState.query.isEmpty() && !uiState.hasSearched) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Popular Searches",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(suggestedQueries) { query ->
                            SuggestionChip(
                                onClick = {
                                    viewModel.onQueryChange(query)
                                },
                                label = { Text(query, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp)) },
                                shape = RoundedCornerShape(10.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = null,
                                modifier = Modifier.testTag("suggestion_chip_$query")
                            )
                        }
                    }
                }
            }

            // Search failed error banner when previous results exist
            if (uiState.errorMessage != null && uiState.repositories.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("search_error_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.errorMessage ?: "Search failed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { viewModel.retrySearch() },
                            modifier = Modifier.testTag("search_retry_banner_button")
                        ) {
                            Text(
                                text = "Retry",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Main Content
            when {
                uiState.isInitialLoading -> {
                    LoadingView(message = "Searching GitHub repositories...")
                }
                uiState.errorMessage != null && uiState.repositories.isEmpty() -> {
                    ErrorView(
                        message = uiState.errorMessage ?: "Failed to execute search.",
                        isRateLimited = uiState.isRateLimited,
                        onRetry = { viewModel.retrySearch() }
                    )
                }
                uiState.hasSearched && uiState.repositories.isEmpty() && !uiState.isSearching -> {
                    EmptyView(
                        title = "No repositories found",
                        description = "No results matched your query '${uiState.query}'. Try different keywords.",
                        icon = Icons.Default.SearchOff
                    )
                }
                !uiState.hasSearched && uiState.repositories.isEmpty() -> {
                    EmptyView(
                        title = "Search GitHub",
                        description = "Type keywords above or select a popular tag to discover repositories.",
                        icon = Icons.Default.Search
                    )
                }
                else -> {
                    SearchResultsList(
                        repositories = uiState.repositories,
                        isLoadingMore = uiState.isLoadingMore,
                        errorMessage = if (uiState.errorMessage != null && !uiState.isSearching && uiState.repositories.isNotEmpty()) null else uiState.errorMessage,
                        onLoadMore = { viewModel.loadNextPage() },
                        onRepoClick = onRepoClick,
                        onBookmarkToggle = { viewModel.toggleBookmark(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultsList(
    repositories: List<Repository>,
    isLoadingMore: Boolean,
    errorMessage: String?,
    onLoadMore: () -> Unit,
    onRepoClick: (owner: String, repo: String) -> Unit,
    onBookmarkToggle: (Repository) -> Unit
) {
    val listState = rememberLazyListState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !isLoadingMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("search_results_list")
    ) {
        items(
            items = repositories,
            key = { it.id }
        ) { repo ->
            RepoCard(
                repository = repo,
                onClick = { onRepoClick(repo.owner.login, repo.name) },
                onBookmarkToggle = { onBookmarkToggle(repo) }
            )
        }

        if (isLoadingMore || errorMessage != null) {
            item {
                PaginationFooter(
                    isLoading = isLoadingMore,
                    errorMessage = errorMessage,
                    onRetry = onLoadMore
                )
            }
        } else {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
