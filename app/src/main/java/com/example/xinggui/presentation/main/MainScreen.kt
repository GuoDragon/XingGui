package com.example.xinggui.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.xinggui.R
import com.example.xinggui.data.repository.AppRepository
import com.example.xinggui.data.repository.DataRepository
import com.example.xinggui.navigation.AppRoute
import com.example.xinggui.presentation.archive.ArchiveScreen
import com.example.xinggui.presentation.common.ScreenRenderState
import com.example.xinggui.presentation.goals.GoalsScreen
import com.example.xinggui.presentation.report.ReportScreen
import com.example.xinggui.presentation.resources.ResourcesScreen
import com.example.xinggui.ui.components.IosStateView
import com.example.xinggui.ui.theme.IosGroupedBackground
import com.example.xinggui.ui.theme.IosSeparator
import kotlinx.coroutines.launch

private data class MainTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onLogoutSuccess: () -> Unit = {},
    repository: AppRepository = DataRepository,
    presenterFactory: (AppRepository) -> MainContract.Presenter = { MainPresenter(it) }
) {
    val presenter = remember(repository, presenterFactory) { presenterFactory(repository) }
    val scope = rememberCoroutineScope()
    val tabNavController = rememberNavController()
    val tabs = listOf(
        MainTab(AppRoute.MainReport.route, stringResource(R.string.tab_report), Icons.Default.Assessment),
        MainTab(AppRoute.MainArchive.route, stringResource(R.string.tab_archive), Icons.Default.AutoStories),
        MainTab(AppRoute.MainGoals.route, stringResource(R.string.tab_goals), Icons.Default.Flag),
        MainTab(AppRoute.MainResources.route, stringResource(R.string.tab_resources), Icons.AutoMirrored.Filled.LibraryBooks),
        MainTab(AppRoute.MainProfile.route, stringResource(R.string.tab_profile), Icons.Default.Person)
    )
    var renderState by remember {
        mutableStateOf<ScreenRenderState<MainShellUiState>>(ScreenRenderState.Loading)
    }
    val currentRoute = tabNavController.currentBackStackEntryAsState().value?.destination?.route
    val view = remember {
        object : MainContract.View {
            override fun render(state: ScreenRenderState<MainShellUiState>) {
                renderState = state
            }

            override fun showShell(state: MainShellUiState) = Unit

            override fun showError(message: String) {
                renderState = ScreenRenderState.Error(message)
            }

            override fun navigateToLogin() {
                onLogoutSuccess()
            }
        }
    }

    LaunchedEffect(Unit) {
        presenter.attachView(view)
        presenter.loadShell()
    }
    DisposableEffect(Unit) {
        onDispose { presenter.detachView() }
    }

    IosStateView(
        state = renderState,
        onRetry = { scope.launch { presenter.loadShell() } },
        modifier = modifier.fillMaxSize()
    ) { state ->
            Scaffold(
                modifier = modifier.fillMaxSize(),
                containerColor = IosGroupedBackground,
                bottomBar = {
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 0.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(0.6.dp, IosSeparator)
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp
                        ) {
                            tabs.forEach { tab ->
                                NavigationBarItem(
                                    selected = currentRoute == tab.route,
                                    onClick = {
                                        tabNavController.navigate(tab.route) {
                                            popUpTo(AppRoute.MainReport.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                                    label = { Text(text = tab.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = Color(0xFF8E8E93),
                                        unselectedTextColor = Color(0xFF8E8E93),
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(IosGroupedBackground)
                        .padding(innerPadding)
                ) {
                    NavHost(
                        navController = tabNavController,
                        startDestination = AppRoute.MainReport.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(AppRoute.MainReport.route) {
                            ReportScreen(
                                selectedChildId = state.currentChild?.childId.orEmpty(),
                                currentRole = state.currentRole,
                                repository = repository
                            )
                        }
                        composable(AppRoute.MainArchive.route) {
                            ArchiveScreen(
                                selectedChildId = state.currentChild?.childId.orEmpty(),
                                currentRole = state.currentRole,
                                repository = repository
                            )
                        }
                        composable(AppRoute.MainGoals.route) {
                            GoalsScreen(
                                selectedChildId = state.currentChild?.childId.orEmpty(),
                                currentRole = state.currentRole,
                                repository = repository
                            )
                        }
                        composable(AppRoute.MainResources.route) {
                            ResourcesScreen(
                                selectedChildId = state.currentChild?.childId.orEmpty(),
                                currentRole = state.currentRole,
                                repository = repository
                            )
                        }
                        composable(AppRoute.MainProfile.route) {
                            ProfileScreen(
                                state = state,
                                onLogoutClick = { scope.launch { presenter.onLogoutClicked() } },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
    }
}
