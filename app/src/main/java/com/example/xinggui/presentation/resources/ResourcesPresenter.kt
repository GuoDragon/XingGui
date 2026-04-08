package com.example.xinggui.presentation.resources

import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.AppRepository

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

    override fun loadData(role: UserRole) {
        view?.showLoading()
        val items = repository.getResources()
        if (items.isEmpty()) {
            view?.showEmpty()
            return
        }
        val cards = items.map {
            ResourceCardUiModel(
                title = it.title,
                summary = it.summary,
                category = it.category,
                isPaid = it.isPaid
            )
        }
        val categories = items.groupBy { it.category }.map { (category, grouped) ->
            ResourceCategoryUiModel(
                title = category,
                count = grouped.size,
                isPaid = grouped.any { it.isPaid }
            )
        }
        view?.showContent(
            ResourcesUiState(
                role = role,
                recommended = cards.take(2),
                categories = categories,
                allItems = cards
            )
        )
    }
}
