package com.example.xinggui.presentation.report

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.example.xinggui.presentation.report.components.ReportReferencePage
import com.example.xinggui.presentation.report.components.shareReport
import com.example.xinggui.ui.components.DecorativeStarsOverlay
import com.example.xinggui.ui.components.IosStateView
import kotlinx.coroutines.launch

@Composable
fun ReportScreen(
    selectedChildId: String,
    currentRole: UserRole,
    modifier: Modifier = Modifier,
    repository: AppRepository = DataRepository,
    presenterFactory: (AppRepository) -> ReportContract.Presenter = { ReportPresenter(it) }
) {
    val presenter = remember(repository, presenterFactory) { presenterFactory(repository) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val shareScope = rememberCoroutineScope()
    val scope = rememberCoroutineScope()
    var renderState by remember {
        mutableStateOf<ScreenRenderState<ReportUiState>>(ScreenRenderState.Loading)
    }

    val view = remember {
        object : ReportContract.View {
            override fun render(state: ScreenRenderState<ReportUiState>) {
                renderState = state
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
            val onShare = {
                shareReport(
                    context = context,
                    state = state,
                    snackbarHostState = snackbarHostState,
                    scope = shareScope
                )
            }
            Box(modifier = modifier.fillMaxSize()) {
                ReportReferencePage(
                    state = state,
                    onShare = onShare,
                    modifier = Modifier.fillMaxSize()
                )
                DecorativeStarsOverlay(
                    modifier = Modifier.fillMaxSize(),
                    pageKey = "report_screen"
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
                )
            }
    }
}
