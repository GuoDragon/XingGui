package com.example.xinggui.presentation.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.TrackChanges
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
fun GoalsScreen(
    selectedChildId: String,
    currentRole: UserRole,
    modifier: Modifier = Modifier
) {
    val presenter = remember { GoalsPresenter(DataRepository) }
    var uiState by remember { mutableStateOf<GoalsUiState?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showEmpty by remember { mutableStateOf(false) }
    val view = remember {
        object : GoalsContract.View {
            override fun showLoading() {
                isLoading = true
                showEmpty = false
            }

            override fun showContent(state: GoalsUiState) {
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
        showEmpty || uiState == null -> EmptyView(message = "暂无目标演示数据", modifier = modifier)
        else -> {
            val state = uiState!!
            Column(
                modifier = modifier
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionCard(title = "个别化教育计划长期目标", subtitle = state.semesterGoal) {
                    Text(text = state.monthlyGoal, style = MaterialTheme.typography.bodyMedium)
                }
                ModuleEntryCard(
                    title = "个别化教育计划上传与数字化录入",
                    description = state.uploadHint,
                    icon = Icons.Default.Description
                )
                SectionCard(title = "个别化教育计划微目标", subtitle = "将长期目标拆分为可追踪的小任务") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.weeklyTasks.forEach { task ->
                            ModuleEntryCard(
                                title = task.title,
                                description = if (task.completed) {
                                    "已完成，奖励 ${task.rewardStars} 颗星"
                                } else {
                                    "待完成，奖励 ${task.rewardStars} 颗星"
                                },
                                icon = Icons.Default.TrackChanges
                            )
                        }
                    }
                }
                ModuleEntryCard(
                    title = "智能辅助分析",
                    description = state.aiHint,
                    icon = Icons.Default.AutoAwesome,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}
