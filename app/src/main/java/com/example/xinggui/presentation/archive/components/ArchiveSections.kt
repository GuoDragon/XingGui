package com.example.xinggui.presentation.archive.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.xinggui.R
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.presentation.common.ChildInfoProfileCard
import com.example.xinggui.presentation.archive.ArchiveDimensionUiModel
import com.example.xinggui.presentation.archive.ArchiveUiState
import com.example.xinggui.ui.theme.IosBlue
import com.example.xinggui.ui.theme.IosCard
import com.example.xinggui.ui.theme.IosCardMuted
import com.example.xinggui.ui.theme.IosGroupedBackground

@Composable
// AI辅助生成：Doubao-Seed-2.0-Code, 2026-05-02
fun ArchiveReferencePage(
    state: ArchiveUiState,
    onDimensionClick: (ArchiveDimensionUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val orderedDimensions = remember(state.dimensions) { orderedDimensionModels(state.dimensions) }

    LazyColumn(
        modifier = modifier.background(IosGroupedBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ArchiveTitleHeader()
        }
        item {
            ChildInfoProfileCard(
                childName = state.childName,
                age = state.childAge,
                interventionDuration = state.childInterventionDuration,
                birthDate = state.childBirthDate,
                avatarKey = state.childAvatarKey
            )
        }
        item {
            ArchiveDimensionsSection(
                dimensions = orderedDimensions,
                onDimensionClick = onDimensionClick
            )
        }
    }
}

@Composable
fun ArchiveDetailDialog(
    dimension: ArchiveDimensionUiModel,
    state: ArchiveUiState,
    onDismiss: () -> Unit,
    onCheckIn: (String, String, Int) -> Unit
) {
    val dimensionWeeklyItems = remember(state.weeklyItems, dimension.id) {
        state.weeklyItems.filter { it.dimensionId == dimension.id }
    }
    val uncompletedItem = remember(dimensionWeeklyItems) {
        dimensionWeeklyItems.firstOrNull { !it.completed }
    }
    val targetItem = uncompletedItem ?: dimensionWeeklyItems.firstOrNull()
    var checkInNote by remember(dimension.id) { mutableStateOf("") }
    var selectedStars by remember(dimension.id, targetItem?.id) {
        mutableStateOf((targetItem?.rewardStars ?: 1).coerceIn(1, 5))
    }

    val isParent = state.role == UserRole.PARENT
    val canSelectStars = isParent && targetItem != null
    val canSubmit = isParent && targetItem != null && checkInNote.isNotBlank()
    val dimensionLabel = displayLabelForDimension(dimension.id, dimension.title)
    val currentWeekCheckInCount = state.weeklyCheckInCountsByDimension[dimension.id] ?: 0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = IosCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ArchiveGoalSection(title = "本学期目标", content = state.semesterGoal)
                ArchiveGoalSection(title = "本月目标", content = state.monthlyGoal)

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = IosCardMuted)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "$dimensionLabel 本周目标打卡",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF121212)
                        )

                        if (targetItem == null) {
                            Text(
                                text = "暂无打卡任务",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF606B78)
                            )
                        } else {
                            Text(
                                text = targetItem.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                text = if (isParent) "家长用：${state.roleHint}" else "教师用：${state.roleHint}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5A6677)
                            )
                            Text(
                                text = "本周已打卡 $currentWeekCheckInCount 次",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5A6677)
                            )
                            if (isParent) {
                                OutlinedTextField(
                                    value = checkInNote,
                                    onValueChange = { checkInNote = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2,
                                    label = { Text("本次表现记录") },
                                    placeholder = { Text("例如：在提示下能完成 1-2 个复杂指令") }
                                )
                            }
                            StarRatingBar(
                                rating = if (canSelectStars) selectedStars else targetItem.rewardStars,
                                maxStars = 5,
                                enabled = canSelectStars,
                                onRatingChange = { selectedStars = it }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE6EBF2),
                            contentColor = Color(0xFF4A5568)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "取消")
                    }
                    Button(
                        onClick = {
                            if (targetItem != null) {
                                onCheckIn(targetItem.id, checkInNote, selectedStars.coerceIn(1, 5))
                            }
                        },
                        enabled = canSubmit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IosBlue,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFB9C5D4),
                            disabledContentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = if (isParent) "打卡" else "仅家长可打卡")
                    }
                }
            }
        }
    }
}

@Composable
fun ArchiveSuccessDialog(
    stars: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = IosCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "已为宝贝记录成长瞬间",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF111111),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                StarRatingBar(rating = stars, maxStars = 5)
                TextButton(onClick = onDismiss) {
                    Text(text = "我知道了", color = Color(0xFF6B7280))
                }
            }
        }
    }
}

@Composable
private fun ArchiveTitleHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = ArchiveReferenceAssets.folderIcon),
                contentDescription = null,
                modifier = Modifier.size(width = 48.dp, height = 44.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "星档案",
                color = Color(0xFF111111),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                lineHeight = 32.sp
            )
        }
    }
}

@Composable
private fun ArchiveDimensionsSection(
    dimensions: List<ArchiveDimensionUiModel>,
    onDimensionClick: (ArchiveDimensionUiModel) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        dimensions.forEach { dimension ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDimensionClick(dimension) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = IosCard)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(66.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayLabelForDimension(dimension.id, dimension.title),
                        color = Color(0xFF101010),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveGoalSection(title: String, content: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = IosCardMuted)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111111)
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF303A4A),
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun StarRatingBar(
    rating: Int,
    maxStars: Int,
    enabled: Boolean = false,
    onRatingChange: ((Int) -> Unit)? = null
) {
    val safeRating = rating.coerceIn(0, maxStars)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(maxStars) { index ->
            val starValue = index + 1
            Icon(
                imageVector = if (index < safeRating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = if (index < safeRating) Color(0xFF3B82F6) else Color(0xFFD5DCE6),
                modifier = Modifier
                    .size(26.dp)
                    .then(
                        if (enabled && onRatingChange != null) {
                            Modifier.clickable { onRatingChange(starValue) }
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

private fun orderedDimensionModels(dimensions: List<ArchiveDimensionUiModel>): List<ArchiveDimensionUiModel> {
    val source = dimensions.associateBy { it.id }
    return OrderedDimensionIds.map { id ->
        source[id] ?: ArchiveDimensionUiModel(
            id = id,
            title = displayLabelForDimension(id, id),
            score = 0
        )
    }
}

private fun displayLabelForDimension(id: String, fallback: String): String {
    return when (id) {
        "cognition" -> "认知能力"
        "communication" -> "沟通能力"
        "action" -> "行动能力"
        "relationship" -> "人际关系"
        "emotion" -> "情绪"
        "sensory" -> "感官功能"
        "health" -> "健康状态"
        "self_care" -> "生活自理"
        "academic" -> "学业能力"
        else -> fallback
    }
}

private val OrderedDimensionIds = listOf(
    "cognition",
    "communication",
    "action",
    "relationship",
    "emotion",
    "sensory",
    "health",
    "self_care",
    "academic"
)

private object ArchiveReferenceAssets {
    val folderIcon = R.drawable.report_ref_989dded1c7eb6b35cfe52141929984ec
}
