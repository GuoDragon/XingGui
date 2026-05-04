package com.example.xinggui.presentation.report.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xinggui.R
import com.example.xinggui.data.model.ReportDataSource
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.presentation.common.ChildInfoProfileCard
import com.example.xinggui.presentation.report.DimensionScoreUiModel
import com.example.xinggui.presentation.report.ReportHistoryUiModel
import com.example.xinggui.presentation.report.ReportUiState
import com.example.xinggui.ui.theme.IosCard
import com.example.xinggui.ui.theme.IosGroupedBackground
import com.example.xinggui.ui.theme.StarBlue
import com.example.xinggui.ui.theme.StarTextPrimary
import com.example.xinggui.ui.theme.StarTextSecondary

@Composable
fun ReportReferencePage(
    state: ReportUiState,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHistory by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IosGroupedBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ReportTopBar(onShare = onShare)
        }
        item {
            ChildInfoProfileCard(
                childName = state.childName,
                age = state.age,
                interventionDuration = state.interventionDuration,
                birthDate = state.childBirthDate,
                avatarKey = state.childAvatarKey
            )
        }
        item {
            ReportRadarCard(
                dimensions = state.dimensions,
                sourceLabel = dataSourceLabel(state),
                showHistory = showHistory,
                onToggleHistory = { showHistory = !showHistory }
            )
        }
        item {
            ReportAiSummaryCard(
                state = state,
                showHistory = showHistory,
                onShare = onShare,
                onToggleHistory = { showHistory = !showHistory }
            )
        }
        if (showHistory) {
            item {
                ReportHistoryCard(history = state.history)
            }
        }
    }
}

@Composable
private fun ReportTopBar(onShare: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = ReportReferenceAssets.headerFolderIcon),
                    contentDescription = null,
                    modifier = Modifier.size(width = 48.dp, height = 44.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box {
                    Text(
                        text = "星报告",
                        color = Color(0xFF111111),
                        fontSize = 32.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        lineHeight = 32.sp
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC857),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-4).dp)
                            .size(14.dp)
                    )
                }
            }

            IconButton(
                onClick = onShare,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F5FA))
            ) {
                Image(
                    painter = painterResource(id = ReportReferenceAssets.shareEntryIcon),
                    contentDescription = "分享报告",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ReportRadarCard(
    dimensions: List<DimensionScoreUiModel>,
    sourceLabel: String,
    showHistory: Boolean,
    onToggleHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFC3D8F9))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "发展维度雷达图",
                        color = Color(0xFF1D3557),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Image(
                        painter = painterResource(id = ReportReferenceAssets.chartTitleStar),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }

                OutlinedButton(
                    onClick = onToggleHistory,
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (showHistory) "收起历史" else "查看历史")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.04f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = ReportReferenceAssets.chartBackgroundStars),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentScale = ContentScale.FillBounds,
                    alpha = 0.65f
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = ReportReferenceAssets.chartCenterStar),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    RadarChart(
                        dimensions = dimensions,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                    )
                }
            }

            Text(
                text = sourceLabel,
                style = MaterialTheme.typography.labelMedium,
                color = StarTextSecondary
            )
        }
    }
}

@Composable
private fun ReportAiSummaryCard(
    state: ReportUiState,
    showHistory: Boolean,
    onShare: () -> Unit,
    onToggleHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "AI",
                    color = Color(0xFF101317),
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    lineHeight = 44.sp
                )
                Text(
                    text = state.aiAnalysis,
                    modifier = Modifier.weight(1f),
                    color = Color(0xCC000000),
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 23.sp
                )
            }

            Text(
                text = "报告概览：${state.overview}",
                style = MaterialTheme.typography.bodyMedium,
                color = StarTextPrimary
            )
            Text(
                text = "综合评估：${state.overallEvaluation}",
                style = MaterialTheme.typography.bodyMedium,
                color = StarTextPrimary
            )
            Text(
                text = "后续建议：${state.nextSuggestions}",
                style = MaterialTheme.typography.bodyMedium,
                color = StarTextPrimary
            )

            if (state.highlights.isNotEmpty()) {
                Text(
                    text = "重点观察：${state.highlights.joinToString("；")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarTextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StarBlue)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (state.role == UserRole.PARENT) "分享给教师" else "分享给家长")
                }
                OutlinedButton(
                    onClick = onToggleHistory,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (showHistory) "收起历史" else "查看历史")
                }
            }
        }
    }
}

@Composable
private fun ReportHistoryCard(history: List<ReportHistoryUiModel>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "历史报告",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = StarTextPrimary
            )
            if (history.isEmpty()) {
                Text(
                    text = "暂无历史记录。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StarTextSecondary
                )
            } else {
                history.take(5).forEach { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F8FD))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = item.generatedAtLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = StarTextSecondary
                            )
                            Text(
                                text = "备注：${item.note}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StarTextPrimary
                            )
                            Text(
                                text = item.overview,
                                style = MaterialTheme.typography.bodySmall,
                                color = StarTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun dataSourceLabel(state: ReportUiState): String {
    val sourceText = when (state.dataSource) {
        ReportDataSource.REMOTE_API -> "在线接口"
    }
    return "当前数据来源：$sourceText"
}

private object ReportReferenceAssets {
    val headerFolderIcon = R.drawable.report_ref_989dded1c7eb6b35cfe52141929984ec
    val titleStarIcon = R.drawable.report_ref_2edfaab6685ff323b516eab190bc9064
    val shareEntryIcon = R.drawable.report_ref_46b85f0ee5b4af4b7f9e4dc4f3251dae
    val chartBackgroundStars = R.drawable.report_ref_627fbc53da0bdbaef5d7f34fdb6c2306
    val chartCenterStar = R.drawable.report_ref_fa1c28fc3cd1b5cb9e69c151db162473
    val chartTitleStar = R.drawable.report_ref_bdda76f4f9cc64478b98b84e891c3eac
}
