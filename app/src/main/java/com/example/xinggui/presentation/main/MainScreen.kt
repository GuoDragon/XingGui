package com.example.xinggui.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.xinggui.R
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.DataRepository
import com.example.xinggui.navigation.AppRoute
import com.example.xinggui.presentation.archive.ArchiveScreen
import com.example.xinggui.presentation.goals.GoalsScreen
import com.example.xinggui.presentation.report.ReportScreen
import com.example.xinggui.presentation.resources.ResourcesScreen
import com.example.xinggui.ui.components.ChildSelectorBar
import com.example.xinggui.ui.components.ErrorView
import com.example.xinggui.ui.components.LoadingIndicator
import com.example.xinggui.ui.components.RoleBadge
import com.example.xinggui.ui.theme.StarBackground

private data class MainTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val presenter = remember { MainPresenter(DataRepository) }
    val tabNavController = rememberNavController()
    val tabs = listOf(
        MainTab(AppRoute.MainReport.route, stringResource(R.string.tab_report), Icons.Default.Assessment),
        MainTab(AppRoute.MainArchive.route, stringResource(R.string.tab_archive), Icons.Default.AutoStories),
        MainTab(AppRoute.MainGoals.route, stringResource(R.string.tab_goals), Icons.Default.Flag),
        MainTab(AppRoute.MainResources.route, stringResource(R.string.tab_resources), Icons.AutoMirrored.Filled.LibraryBooks)
    )
    var uiState by remember { mutableStateOf<MainShellUiState?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val currentRoute = tabNavController.currentBackStackEntryAsState().value?.destination?.route
    val view = remember {
        object : MainContract.View {
            override fun showShell(state: MainShellUiState) {
                uiState = state
                errorMessage = null
            }

            override fun showError(message: String) {
                errorMessage = message
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

    when {
        errorMessage != null -> ErrorView(message = errorMessage!!, onRetry = presenter::loadShell, modifier = modifier.fillMaxSize())
        uiState == null -> LoadingIndicator(modifier = modifier.fillMaxSize())
        else -> {
            val state = uiState!!
            Scaffold(
                modifier = modifier.fillMaxSize(),
                topBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StarBackground)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = stringResource(R.string.shell_title), style = MaterialTheme.typography.headlineSmall)
                                Text(
                                    text = state.currentUserName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            RoleBadge(role = state.currentRole)
                        }
                        ChildSelectorBar(
                            role = state.currentRole,
                            currentChild = state.currentChild,
                            children = state.availableChildren,
                            onChildSelected = presenter::onChildSelected
                        )
                    }
                },
                bottomBar = {
                    NavigationBar {
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
                                label = { Text(text = tab.label) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = tabNavController,
                    startDestination = AppRoute.MainReport.route,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    composable(AppRoute.MainReport.route) {
                        ReportScreen(selectedChildId = state.currentChild?.childId.orEmpty(), currentRole = state.currentRole)
                    }
                    composable(AppRoute.MainArchive.route) {
                        ArchiveScreen(selectedChildId = state.currentChild?.childId.orEmpty(), currentRole = state.currentRole)
                    }
                    composable(AppRoute.MainGoals.route) {
                        GoalsScreen(selectedChildId = state.currentChild?.childId.orEmpty(), currentRole = state.currentRole)
                    }
                    composable(AppRoute.MainResources.route) {
                        ResourcesScreen(selectedChildId = state.currentChild?.childId.orEmpty(), currentRole = state.currentRole)
                    }
                }
            }
        }
    }
}

