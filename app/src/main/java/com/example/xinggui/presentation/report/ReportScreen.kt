package com.example.xinggui.presentation.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.DataRepository
import com.example.xinggui.ui.components.EmptyView
import com.example.xinggui.ui.components.ErrorView
import com.example.xinggui.ui.components.LoadingIndicator
import com.example.xinggui.ui.components.ModuleEntryCard
import com.example.xinggui.ui.components.SectionCard

@Composable
fun ReportScreen(
    selectedChildId: String,
    currentRole: UserRole,
    modifier: Modifier = Modifier
) {
    val presenter = remember { ReportPresenter(DataRepository) }
    var uiState by remember { mutableStateOf<ReportUiState?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showEmpty by remember { mutableStateOf(false) }
    val view = remember {
        object : ReportContract.View {
            override fun showLoading() {
                isLoading = true
                showEmpty = false
            }

            override fun showContent(state: ReportUiState) {
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
        showEmpty || uiState == null -> EmptyView(message = "暂无报告演示数据", modifier = modifier)
        else -> {
            val state = uiState!!
            LazyColumn(
                modifier = modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SectionCard(
                        title = "儿童信息",
                        subtitle = "${state.childName} · ${state.age}岁 · 干预 ${state.interventionDuration}"
                    ) {
                        Text(
                            text = if (state.role == UserRole.PARENT) {
                                "家长模式默认展示家庭关联儿童档案。"
                            } else {
                                "教师模式可在已分配儿童间切换查看。"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                item {
                    SectionCard(title = "干预雷达", subtitle = "首版脚手架使用维度进度条作为图表占位。") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            state.dimensions.forEach { item ->
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = item.title, style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "${item.score}", style = MaterialTheme.typography.labelMedium)
                                    }
                                    LinearProgressIndicator(progress = { item.score / 100f }, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }
                item {
                    SectionCard(title = "报告复盘", subtitle = state.overview) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            ModuleEntryCard(
                                title = "智能发展摘要",
                                description = state.aiAnalysis,
                                icon = Icons.Default.AutoGraph
                            )
                            Text(text = "综合评估：${state.overallEvaluation}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "下一步建议：${state.nextSuggestions}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                item {
                    SectionCard(title = "重点提示") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.highlights.forEach { highlight ->
                                Text(text = "• $highlight", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                item {
                    ModuleEntryCard(
                        title = "一键反馈",
                        description = state.feedbackHint,
                        icon = Icons.Default.Share,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
        }
    }
}
