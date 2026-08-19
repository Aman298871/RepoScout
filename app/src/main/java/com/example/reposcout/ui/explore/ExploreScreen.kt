package com.example.reposcout.ui.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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

private val exploreTopics = listOf("android", "kotlin", "compose", "architecture", "flutter", "react", "rust", "python")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onRepoClick: (owner: String, repo: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTopic by rememberSaveable { mutableStateOf("android") }
    var sortMenuExpanded by remember { mutableStateOf(false) }

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
                        text = "RepoScout",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { sortMenuExpanded = true }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Stars") },
                                onClick = {
                                    viewModel.loadInitialRepositories(selectedTopic, "stars")
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Forks") },
                                onClick = {
                                    viewModel.loadInitialRepositories(selectedTopic, "forks")
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Updated") },
                                onClick = {
                                    viewModel.loadInitialRepositories(selectedTopic, "updated")
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.testTag("explore_top_bar")
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

            // Topic filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(exploreTopics) { topic ->
                    val isSelected = selectedTopic == topic
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (selectedTopic != topic) {
                                selectedTopic = topic
                                viewModel.loadInitialRepositories(topic)
                            }
                        },
                        label = {
                            Text(
                                text = topic.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        border = null,
                        modifier = Modifier.testTag("topic_chip_$topic")
                    )
                }
            }

            // Main Content based on UI State
            when (val state = uiState) {
                is ExploreUiState.Loading -> {
                    LoadingView(message = "Discovering top $selectedTopic repositories...")
                }
                is ExploreUiState.Empty -> {
                    EmptyView(
                        title = "No repositories found",
                        description = "There are no matching repositories for '$selectedTopic'.",
                        icon = Icons.Default.Explore
                    )
                }
                is ExploreUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        isRateLimited = state.isRateLimited,
                        onRetry = { viewModel.loadInitialRepositories(selectedTopic) }
                    )
                }
                is ExploreUiState.Success -> {
                    ExploreContent(
                        state = state,
                        onRefresh = { viewModel.refresh(selectedTopic) },
                        onLoadMore = { viewModel.loadNextPage(selectedTopic) },
                        onRepoClick = onRepoClick,
                        onBookmarkToggle = { viewModel.toggleBookmark(it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExploreContent(
    state: ExploreUiState.Success,
    onRefresh: () -> Unit,
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
        if (shouldLoadMore.value && !state.isLoadingMore && state.canPaginate) {
            onLoadMore()
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .testTag("explore_pull_refresh")
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("explore_repo_list")
        ) {
            items(
                items = state.repositories,
                key = { it.id }
            ) { repo ->
                RepoCard(
                    repository = repo,
                    onClick = { onRepoClick(repo.owner.login, repo.name) },
                    onBookmarkToggle = { onBookmarkToggle(repo) }
                )
            }

            if (state.isLoadingMore || state.paginationError != null) {
                item {
                    PaginationFooter(
                        isLoading = state.isLoadingMore,
                        errorMessage = state.paginationError,
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
}
