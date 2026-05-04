package com.example.xinggui.presentation.resources

import com.example.xinggui.data.model.UserRole
import com.example.xinggui.presentation.common.ScreenRenderState

data class ResourceCategoryUiModel(
    val title: String,
    val count: Int,
    val isPaid: Boolean
)

data class ResourceCardUiModel(
    val resourceId: String,
    val title: String,
    val summary: String,
    val category: String,
    val isPaid: Boolean,
    val assetPath: String?,
    val sourceUrl: String?
)

data class ResourcesUiState(
    val role: UserRole,
    val recommended: List<ResourceCardUiModel>,
    val categories: List<ResourceCategoryUiModel>,
    val allItems: List<ResourceCardUiModel>,
    val unlockedResourceIds: Set<String>,
    val searchHistory: List<String>
)

interface ResourcesContract {
    interface View {
        fun render(state: ScreenRenderState<ResourcesUiState>) {
            when (state) {
                ScreenRenderState.Loading -> showLoading()
                is ScreenRenderState.Content -> showContent(state.data)
                is ScreenRenderState.Empty -> showEmpty()
                is ScreenRenderState.Error -> showError(state.message)
            }
        }

        fun showLoading() = Unit
        fun showContent(state: ResourcesUiState) = Unit
        fun showEmpty() = Unit
        fun showError(message: String) = Unit
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        // AI辅助生成：Doubao-Seed-2.0-Code, 2026-05-02
        suspend fun loadData(role: UserRole)
        fun search(items: List<ResourceCardUiModel>, keyword: String): List<ResourceCardUiModel>
        suspend fun saveRuntimeState(unlockedResourceIds: Set<String>, searchHistory: List<String>)
    }
}
