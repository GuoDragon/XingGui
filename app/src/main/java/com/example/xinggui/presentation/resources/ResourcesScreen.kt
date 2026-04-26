package com.example.xinggui.presentation.resources

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.AppRepository
import com.example.xinggui.data.repository.DataRepository
import com.example.xinggui.presentation.common.ScreenRenderState
import com.example.xinggui.presentation.resources.components.ResourcePayDialog
import com.example.xinggui.presentation.resources.components.ResourcePdfReaderPage
import com.example.xinggui.presentation.resources.components.ResourcesCategoryPage
import com.example.xinggui.presentation.resources.components.ResourcesDetailPage
import com.example.xinggui.presentation.resources.components.ResourcesHomePage
import com.example.xinggui.presentation.resources.components.ResourcesSearchPage
import com.example.xinggui.ui.components.DecorativeStarsOverlay
import com.example.xinggui.ui.components.IosStateView
import kotlinx.coroutines.launch

private enum class ResourcePage {
    HOME,
    SEARCH,
    CATEGORY,
    DETAIL,
    READER
}

private enum class UnlockAction {
    STAY_ON_DETAIL,
    OPEN_READER
}

@Composable
fun ResourcesScreen(
    selectedChildId: String,
    currentRole: UserRole,
    modifier: Modifier = Modifier,
    repository: AppRepository = DataRepository,
    presenterFactory: (AppRepository) -> ResourcesContract.Presenter = { ResourcesPresenter(it) }
) {
    val presenter = remember(repository, presenterFactory) { presenterFactory(repository) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var renderState by remember {
        mutableStateOf<ScreenRenderState<ResourcesUiState>>(ScreenRenderState.Loading)
    }

    var page by remember { mutableStateOf(ResourcePage.HOME) }
    var searchKeyword by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<ResourceCardUiModel>>(emptyList()) }
    var currentCategory by remember { mutableStateOf("资源分类") }
    var categoryItems by remember { mutableStateOf<List<ResourceCardUiModel>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<ResourceCardUiModel?>(null) }
    var readerSession by remember { mutableStateOf<ResourceReaderSession?>(null) }
    var showPayDialog by remember { mutableStateOf(false) }
    var unlockAction by remember { mutableStateOf(UnlockAction.STAY_ON_DETAIL) }
    var unlockedResourceIds by remember { mutableStateOf(setOf<String>()) }
    var searchHistory by remember { mutableStateOf(emptyList<String>()) }

    val view = remember {
        object : ResourcesContract.View {
            override fun render(state: ScreenRenderState<ResourcesUiState>) {
                renderState = state
                if (state is ScreenRenderState.Content) {
                    unlockedResourceIds = state.data.unlockedResourceIds
                    searchHistory = state.data.searchHistory
                }
            }
        }
    }

    fun persistRuntimeState(updatedUnlockedIds: Set<String> = unlockedResourceIds, updatedHistory: List<String> = searchHistory) {
        scope.launch {
            presenter.saveRuntimeState(updatedUnlockedIds, updatedHistory)
        }
    }

    fun recordSearch(keyword: String) {
        val trimmed = keyword.trim()
        if (trimmed.isBlank()) return
        if (trimmed !in searchHistory) {
            searchHistory = (listOf(trimmed) + searchHistory).take(8)
            persistRuntimeState(updatedHistory = searchHistory)
        }
    }

    LaunchedEffect(Unit) { presenter.attachView(view) }
    LaunchedEffect(selectedChildId, currentRole) { presenter.loadData(currentRole) }
    DisposableEffect(Unit) { onDispose { presenter.detachView() } }

    IosStateView(
        state = renderState,
        onRetry = { scope.launch { presenter.loadData(currentRole) } },
        modifier = modifier.fillMaxSize()
    ) { state ->
            val openReader: (ResourceCardUiModel) -> Unit = { item ->
                val assetPath = item.assetPath
                if (assetPath.isNullOrBlank()) {
                    Toast.makeText(context, "当前资源暂无可阅读文件", Toast.LENGTH_SHORT).show()
                } else {
                    readerSession = ResourceReaderSession(
                        resourceId = item.resourceId,
                        title = item.title,
                        assetPath = assetPath
                    )
                    page = ResourcePage.READER
                }
            }

            Box(modifier = modifier) {
                when (page) {
                    ResourcePage.HOME -> ResourcesHomePage(
                        state = state,
                        searchKeyword = searchKeyword,
                        onSearchKeywordChange = { searchKeyword = it },
                        onSearchClick = {
                            searchResults = presenter.search(state.allItems, searchKeyword)
                            recordSearch(searchKeyword)
                            page = ResourcePage.SEARCH
                        },
                        onCategoryClick = { category ->
                            currentCategory = category
                            categoryItems = state.allItems.filter { it.category == category }
                            readerSession = null
                            page = ResourcePage.CATEGORY
                        },
                        onRecommendedClick = { item ->
                            // 直接打开PDF，不再进入详情页
                            if (!item.assetPath.isNullOrBlank()) {
                                if (item.isPaid && item.resourceId !in unlockedResourceIds) {
                                    selectedItem = item
                                    unlockAction = UnlockAction.OPEN_READER
                                    showPayDialog = true
                                } else {
                                    openReader(item)
                                }
                            } else {
                                Toast.makeText(context, "该资源暂无可阅读文件", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    ResourcePage.SEARCH -> ResourcesSearchPage(
                        searchKeyword = searchKeyword,
                        onSearchKeywordChange = { searchKeyword = it },
                        onBack = { page = ResourcePage.HOME },
                        onSearchClick = {
                            searchResults = presenter.search(state.allItems, searchKeyword)
                            recordSearch(searchKeyword)
                        },
                        searchHistory = searchHistory,
                        onHistoryClick = { history ->
                            searchKeyword = history
                            searchResults = presenter.search(state.allItems, history)
                        },
                        searchResults = searchResults,
                        unlockedResourceIds = unlockedResourceIds,
                        onItemClick = { item ->
                            // 直接打开PDF，不再进入详情页
                            if (!item.assetPath.isNullOrBlank()) {
                                if (item.isPaid && item.resourceId !in unlockedResourceIds) {
                                    selectedItem = item
                                    unlockAction = UnlockAction.OPEN_READER
                                    showPayDialog = true
                                } else {
                                    openReader(item)
                                }
                            } else {
                                Toast.makeText(context, "该资源暂无可阅读文件", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    ResourcePage.CATEGORY -> ResourcesCategoryPage(
                        currentCategory = currentCategory,
                        categoryItems = categoryItems,
                        unlockedResourceIds = unlockedResourceIds,
                        onBack = { page = ResourcePage.HOME },
                        onItemClick = { item ->
                            // 所有资源都直接打开PDF，不再进入详情页
                            if (!item.assetPath.isNullOrBlank()) {
                                if (item.isPaid && item.resourceId !in unlockedResourceIds) {
                                    selectedItem = item
                                    unlockAction = UnlockAction.OPEN_READER
                                    showPayDialog = true
                                } else {
                                    openReader(item)
                                }
                            } else {
                                Toast.makeText(context, "该资源暂无可阅读文件", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    ResourcePage.DETAIL -> ResourcesDetailPage(
                        item = selectedItem,
                        currentRole = currentRole,
                        unlockedResourceIds = unlockedResourceIds,
                        onBack = { page = if (categoryItems.isNotEmpty()) ResourcePage.CATEGORY else ResourcePage.HOME },
                        onUnlockClick = {
                            if (selectedItem != null) {
                                unlockAction = UnlockAction.STAY_ON_DETAIL
                                showPayDialog = true
                            }
                        },
                        onOpenReaderClick = {
                            val item = selectedItem ?: return@ResourcesDetailPage
                            openReader(item)
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    ResourcePage.READER -> ResourcePdfReaderPage(
                        session = readerSession,
                        onBack = { page = ResourcePage.HOME },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                DecorativeStarsOverlay(
                    modifier = Modifier.fillMaxSize(),
                    pageKey = "resources_screen"
                )
            }

            if (showPayDialog && selectedItem != null) {
                ResourcePayDialog(
                    onConfirm = {
                        val pendingItem = selectedItem ?: return@ResourcePayDialog
                        unlockedResourceIds = unlockedResourceIds + pendingItem.resourceId
                        persistRuntimeState(updatedUnlockedIds = unlockedResourceIds)
                        showPayDialog = false
                        if (unlockAction == UnlockAction.OPEN_READER) {
                            openReader(pendingItem)
                        }
                        unlockAction = UnlockAction.STAY_ON_DETAIL
                    },
                    onDismiss = {
                        showPayDialog = false
                        unlockAction = UnlockAction.STAY_ON_DETAIL
                    }
                )
            }
    }
}
