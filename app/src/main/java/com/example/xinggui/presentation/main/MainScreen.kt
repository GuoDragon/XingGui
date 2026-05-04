package com.example.xinggui.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.xinggui.R
import com.example.xinggui.data.repository.AppRepository
import com.example.xinggui.data.repository.DataRepository
import com.example.xinggui.navigation.AppRoute
import com.example.xinggui.presentation.archive.ArchiveScreen
import com.example.xinggui.presentation.common.PrivacyNoticeDialog
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

private val FloatingBottomNavHeight = 64.dp
private val FloatingBottomNavVerticalMargin = 6.dp
private val FloatingBottomNavReservedHeight = 96.dp

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
    val snackbarHostState = remember { SnackbarHostState() }
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
    var editingAccount by remember { mutableStateOf(false) }
    var editingChild by remember { mutableStateOf(false) }
    var savingProfile by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
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

            override fun showMessage(message: String) {
                scope.launch { snackbarHostState.showSnackbar(message) }
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
        val selectedRoute = currentRoute ?: AppRoute.MainReport.route
        val childProfileRefreshKey = state.currentChild?.let { child ->
            listOf(
                child.childId,
                child.name,
                child.birthDate.orEmpty(),
                child.interventionStartDate.orEmpty(),
                child.avatarKey.orEmpty()
            ).joinToString("|")
        }
        val density = LocalDensity.current
        val bottomInset = with(density) {
            WindowInsets.navigationBars.getBottom(density).toDp()
        }
        val contentBottomPadding = bottomInset + FloatingBottomNavReservedHeight

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(IosGroupedBackground)
        ) {
            NavHost(
                navController = tabNavController,
                startDestination = AppRoute.MainReport.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = contentBottomPadding)
            ) {
                composable(AppRoute.MainReport.route) {
                    ReportScreen(
                        selectedChildId = state.currentChild?.childId.orEmpty(),
                        currentRole = state.currentRole,
                        childProfileRefreshKey = childProfileRefreshKey,
                        repository = repository
                    )
                }
                composable(AppRoute.MainArchive.route) {
                    ArchiveScreen(
                        selectedChildId = state.currentChild?.childId.orEmpty(),
                        currentRole = state.currentRole,
                        childProfileRefreshKey = childProfileRefreshKey,
                        repository = repository
                    )
                }
                composable(AppRoute.MainGoals.route) {
                    GoalsScreen(
                        selectedChildId = state.currentChild?.childId.orEmpty(),
                        currentRole = state.currentRole,
                        childProfileRefreshKey = childProfileRefreshKey,
                        repository = repository
                    )
                }
                composable(AppRoute.MainResources.route) {
                    // AI辅助生成：Doubao-Seed-2.0-Code, 2026-05-02
                    ResourcesScreen(
                        currentRole = state.currentRole,
                        repository = repository
                    )
                }
                composable(AppRoute.MainProfile.route) {
                    ProfileScreen(
                        state = state,
                        availableChildren = state.availableChildren,
                        onChildSelected = { childId ->
                            scope.launch { presenter.onChildSelected(childId) }
                        },
                        onEditAccountClick = { editingAccount = true },
                        onEditChildClick = {
                            if (state.currentChild != null) {
                                editingChild = true
                            }
                        },
                        onPrivacyClick = { showPrivacy = true },
                        onLogoutAllDevicesClick = { scope.launch { presenter.onLogoutAllDevicesClicked() } },
                        onLogoutClick = { scope.launch { presenter.onLogoutClicked() } },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = contentBottomPadding)
            )
            IosFloatingBottomNavigation(
                tabs = tabs,
                selectedRoute = selectedRoute,
                onTabClick = { tab ->
                    tabNavController.navigate(tab.route) {
                        popUpTo(AppRoute.MainReport.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        if (editingAccount) {
            AccountProfileEditDialog(
                state = state,
                saving = savingProfile,
                onDismiss = { if (!savingProfile) editingAccount = false },
                onSave = { displayName, email, avatarKey ->
                    scope.launch {
                        savingProfile = true
                        val saved = presenter.onAccountProfileSaved(displayName, email, avatarKey)
                        savingProfile = false
                        if (saved) {
                            editingAccount = false
                        }
                    }
                }
            )
        }
        if (showPrivacy) {
            PrivacyNoticeDialog(onDismiss = { showPrivacy = false })
        }
        val childForEdit = state.currentChild
        if (editingChild && childForEdit != null) {
            ChildProfileEditDialog(
                child = childForEdit,
                saving = savingProfile,
                onDismiss = { if (!savingProfile) editingChild = false },
                onSave = { name, birthDate, interventionStartDate, avatarKey ->
                    scope.launch {
                        savingProfile = true
                        val saved = presenter.onChildProfileSaved(
                            childId = childForEdit.childId,
                            name = name,
                            birthDate = birthDate,
                            interventionStartDate = interventionStartDate,
                            avatarKey = avatarKey
                        )
                        savingProfile = false
                        if (saved) {
                            editingChild = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun IosFloatingBottomNavigation(
    tabs: List<MainTab>,
    selectedRoute: String,
    onTabClick: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = FloatingBottomNavVerticalMargin)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(0.6.dp, IosSeparator)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FloatingBottomNavHeight)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    IosBottomTab(
                        tab = tab,
                        selected = selectedRoute == tab.route,
                        onClick = { onTabClick(tab) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun IosBottomTab(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = Color(0xFF8E8E93)
    val contentColor = if (selected) activeColor else inactiveColor

    Column(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(top = 5.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(25.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(if (selected) activeColor.copy(alpha = 0.1f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = contentColor,
                modifier = Modifier.size(21.dp)
            )
        }
        Text(
            text = tab.label,
            color = contentColor,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
