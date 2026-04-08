package com.example.xinggui.presentation.archive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.DataRepository
import com.example.xinggui.ui.components.EmptyView
import com.example.xinggui.ui.components.ErrorView
import com.example.xinggui.ui.components.LoadingIndicator
import com.example.xinggui.ui.components.SectionCard

@Composable
fun ArchiveScreen(
    selectedChildId: String,
    currentRole: UserRole,
    modifier: Modifier = Modifier
) {
    val presenter = remember { ArchivePresenter(DataRepository) }
    var uiState by remember { mutableStateOf<ArchiveUiState?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showEmpty by remember { mutableStateOf(false) }
    val view = remember {
        object : ArchiveContract.View {
            override fun showLoading() {
                isLoading = true
                showEmpty = false
            }

            override fun showContent(state: ArchiveUiState) {
                uiState = state
                isLoading = false
                showEmpty = false
                errorMessage = null
            }

            override fun showEmpty() {
                uiState = null
                isLoading = false
                showEmpty = true
            }

            override fun showError(message: String) {
                errorMessage = message
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { presenter.attachView(view) }
    LaunchedEffect(selectedChildId, currentRole) { presenter.loadData(selectedChildId, currentRole) }
    DisposableEffect(Unit) { onDispose { presenter.detachView() } }

    when {
        errorMessage != null -> ErrorView(message = errorMessage!!, onRetry = { presenter.loadData(selectedChildId, currentRole) }, modifier = modifier)
        isLoading -> LoadingIndicator(modifier = modifier)
        showEmpty || uiState == null -> EmptyView(message = "暂无档案演示数据", modifier = modifier)
        else -> {
            val state = uiState!!
            Column(
                modifier = modifier
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionCard(title = "成长瞬间", subtitle = state.roleHint) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.dimensions.chunked(2).forEach { rowItems ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                rowItems.forEach { item ->
                                    SectionCard(
                                        title = item.title,
                                        modifier = Modifier.weight(1f),
                                        subtitle = "通用详情模板条目"
                                    ) {
                                        Text(text = "当前进度 ${item.score}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Column(modifier = Modifier.weight(1f)) {}
                                }
                            }
                        }
                    }
                }
                SectionCard(title = "学期目标", subtitle = state.semesterGoal) {
                    Text(text = state.monthlyGoal, style = MaterialTheme.typography.bodyMedium)
                }
                SectionCard(title = "每周打卡", subtitle = "展示后续打卡与星星奖励区域") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.weeklyItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (item.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null
                                    )
                                    Text(text = item.title, style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(text = "+${item.rewardStars} 颗星", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
                SectionCard(title = "星星进度", subtitle = "${state.earnedStars}/${state.totalStars} 颗星") {
                    StarProgressIndicator(filled = state.earnedStars, total = state.totalStars.coerceAtLeast(1))
                }
            }
        }
    }
}

@Composable
private fun StarProgressIndicator(filled: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { index ->
            Icon(
                imageVector = if (index < filled) Icons.Default.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = if (index < filled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
            )
        }
    }
}

