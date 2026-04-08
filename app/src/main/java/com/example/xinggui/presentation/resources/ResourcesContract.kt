package com.example.xinggui.presentation.resources

import com.example.xinggui.data.model.UserRole

data class ResourceCategoryUiModel(
    val title: String,
    val count: Int,
    val isPaid: Boolean
)

data class ResourceCardUiModel(
    val title: String,
    val summary: String,
    val category: String,
    val isPaid: Boolean
)

data class ResourcesUiState(
    val role: UserRole,
    val recommended: List<ResourceCardUiModel>,
    val categories: List<ResourceCategoryUiModel>,
    val allItems: List<ResourceCardUiModel>
)

interface ResourcesContract {
    interface View {
        fun showLoading()
        fun showContent(state: ResourcesUiState)
        fun showEmpty()
        fun showError(message: String)
    }

    interface Presenter {
        fun attachView(view: View)
        fun detachView()
        fun loadData(role: UserRole)
    }
}
