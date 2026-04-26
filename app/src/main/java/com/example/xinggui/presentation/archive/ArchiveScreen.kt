package com.example.xinggui.presentation.archive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.AppRepository
import com.example.xinggui.data.repository.DataRepository
import com.example.xinggui.presentation.common.ScreenRenderState
import com.example.xinggui.presentation.archive.components.ArchiveDetailDialog
import com.example.xinggui.presentation.archive.components.ArchiveReferencePage
import com.example.xinggui.presentation.archive.components.ArchiveSuccessDialog
import com.example.xinggui.ui.components.DecorativeStarsOverlay
import com.example.xinggui.ui.components.IosStateView
import kotlinx.coroutines.launch

@Composable
fun ArchiveScreen(
    selectedChildId: String,
    currentRole: UserRole,
    modifier: Modifier = Modifier,
    repository: AppRepository = DataRepository,
    presenterFactory: (AppRepository) -> ArchiveContract.Presenter = { ArchivePresenter(it) }
) {
    val presenter = remember(repository, presenterFactory) { presenterFactory(repository) }
    val scope = rememberCoroutineScope()
    var renderState by remember {
        mutableStateOf<ScreenRenderState<ArchiveUiState>>(ScreenRenderState.Loading)
    }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedDimension by remember { mutableStateOf<ArchiveDimensionUiModel?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successEarnedStars by remember { mutableIntStateOf(0) }

    val view = remember {
        object : ArchiveContract.View {
            override fun render(state: ScreenRenderState<ArchiveUiState>) {
                renderState = state
            }

            override fun showCheckInFeedback(success: Boolean, stars: Int) {
                if (success) {
                    showDetailDialog = false
                    successEarnedStars = stars
                    showSuccessDialog = true
                }
            }
        }
    }

    LaunchedEffect(Unit) { presenter.attachView(view) }
    LaunchedEffect(selectedChildId, currentRole) { presenter.loadData(selectedChildId, currentRole) }
    DisposableEffect(Unit) { onDispose { presenter.detachView() } }

    IosStateView(
        state = renderState,
        onRetry = { scope.launch { presenter.loadData(selectedChildId, currentRole) } },
        modifier = modifier.fillMaxSize()
    ) { state ->
            Box(modifier = modifier.fillMaxSize()) {
                ArchiveReferencePage(
                    state = state,
                    onDimensionClick = {
                        selectedDimension = it
                        showDetailDialog = true
                    },
                    modifier = Modifier.fillMaxSize()
                )
                DecorativeStarsOverlay(
                    modifier = Modifier.fillMaxSize(),
                    pageKey = "archive_screen"
                )
            }

            if (showDetailDialog && selectedDimension != null) {
                ArchiveDetailDialog(
                    dimension = selectedDimension!!,
                    state = state,
                    onDismiss = { showDetailDialog = false },
                    onCheckIn = { itemId, note, stars ->
                        scope.launch { presenter.performCheckIn(itemId, note, stars, true) }
                    }
                )
            }

            if (showSuccessDialog) {
                ArchiveSuccessDialog(
                    stars = successEarnedStars,
                    onDismiss = { showSuccessDialog = false }
                )
            }
    }
}
