package com.example.reposcout.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.reposcout.RepoScoutApplication
import com.example.reposcout.ui.details.DetailsScreen
import com.example.reposcout.ui.details.DetailsViewModel
import com.example.reposcout.ui.explore.ExploreScreen
import com.example.reposcout.ui.explore.ExploreViewModel
import com.example.reposcout.ui.saved.SavedScreen
import com.example.reposcout.ui.saved.SavedViewModel
import com.example.reposcout.ui.search.SearchScreen
import com.example.reposcout.ui.search.SearchViewModel

@Composable
fun MainApp(
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current
    val app = context.applicationContext as RepoScoutApplication
    val gitHubRepository = app.gitHubRepository
    val connectivityObserver = app.connectivityObserver

    val isBottomBarVisible = currentRoute in listOf(
        Screen.Explore.route,
        Screen.Search.route,
        Screen.Saved.route
    )

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Explore.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Explore.route) {
                val exploreViewModel: ExploreViewModel = viewModel(
                    factory = ExploreViewModel.Factory(gitHubRepository, connectivityObserver)
                )
                ExploreScreen(
                    viewModel = exploreViewModel,
                    isDarkMode = isDarkMode,
                    onToggleTheme = onToggleTheme,
                    onRepoClick = { owner, repo ->
                        navController.navigate(Screen.Details.createRoute(owner, repo))
                    }
                )
            }

            composable(Screen.Search.route) {
                val searchViewModel: SearchViewModel = viewModel(
                    factory = SearchViewModel.Factory(gitHubRepository, connectivityObserver)
                )
                SearchScreen(
                    viewModel = searchViewModel,
                    onRepoClick = { owner, repo ->
                        navController.navigate(Screen.Details.createRoute(owner, repo))
                    }
                )
            }

            composable(Screen.Saved.route) {
                val savedViewModel: SavedViewModel = viewModel(
                    factory = SavedViewModel.Factory(gitHubRepository, connectivityObserver)
                )
                SavedScreen(
                    viewModel = savedViewModel,
                    onRepoClick = { owner, repo ->
                        navController.navigate(Screen.Details.createRoute(owner, repo))
                    }
                )
            }

            composable(
                route = Screen.Details.route,
                arguments = listOf(
                    navArgument("owner") { type = NavType.StringType },
                    navArgument("repo") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val owner = backStackEntry.arguments?.getString("owner").orEmpty()
                val repo = backStackEntry.arguments?.getString("repo").orEmpty()
                val detailsViewModel: DetailsViewModel = viewModel(
                    key = "$owner/$repo",
                    factory = DetailsViewModel.Factory(owner, repo, gitHubRepository, connectivityObserver)
                )
                DetailsScreen(
                    viewModel = detailsViewModel,
                    onNavigateUp = { navController.navigateUp() }
                )
            }
        }
    }
}
