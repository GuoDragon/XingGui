package com.example.xinggui.presentation.resources

import com.example.xinggui.data.model.ResourceRuntimeState
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.AppRepository
import com.example.xinggui.presentation.common.ScreenRenderState

class ResourcesPresenter(
    private val repository: AppRepository
) : ResourcesContract.Presenter {
    private var view: ResourcesContract.View? = null

    override fun attachView(view: ResourcesContract.View) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

    override suspend fun loadData(role: UserRole) {
        view?.render(ScreenRenderState.Loading)
        runCatching {
            val items = repository.getResources()
            if (items.isEmpty()) {
                null
            } else {
                val cards = items.map {
                    ResourceCardUiModel(
                        resourceId = it.resourceId,
                        title = it.title,
                        summary = it.summary,
                        category = it.category,
                        isPaid = it.isPaid,
                        assetPath = it.assetPath,
                        sourceUrl = it.sourceUrl
                    )
                }
                val categories = items.groupBy { it.category }
                    .map { (category, grouped) ->
                        ResourceCategoryUiModel(
                            title = category,
                            count = grouped.size,
                            isPaid = grouped.any { it.isPaid }
                        )
                    }
                    .sortedBy {
                        when (it.title) {
                            ResourceCategoryNames.NEWS_POLICY -> 0
                            ResourceCategoryNames.CASE_STUDY -> 1
                            ResourceCategoryNames.POLICY_INTERPRETATION -> 2
                            ResourceCategoryNames.TEACHING_GUIDE -> 3
                            else -> 99
                        }
                    }
                val runtimeState = repository.getResourceRuntimeState()
                ResourcesUiState(
                    role = role,
                    recommended = cards.take(4),
                    categories = categories,
                    allItems = cards,
                    unlockedResourceIds = runtimeState.unlockedResourceIds,
                    searchHistory = runtimeState.searchHistory
                )
            }
        }.onSuccess { state ->
            if (state == null) {
                view?.render(ScreenRenderState.Empty("暂无资源展示数据"))
            } else {
                view?.render(ScreenRenderState.Content(state))
            }
        }.onFailure { error ->
            view?.render(ScreenRenderState.Error(error.message ?: "加载失败"))
        }
    }

    override fun search(items: List<ResourceCardUiModel>, keyword: String): List<ResourceCardUiModel> {
        val query = keyword.trim()
        if (query.isBlank()) return emptyList()
        return items.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.summary.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
        }
    }

    override suspend fun saveRuntimeState(unlockedResourceIds: Set<String>, searchHistory: List<String>) {
        repository.saveResourceRuntimeState(
            ResourceRuntimeState(
                unlockedResourceIds = unlockedResourceIds,
                searchHistory = searchHistory
            )
        )
    }
}
