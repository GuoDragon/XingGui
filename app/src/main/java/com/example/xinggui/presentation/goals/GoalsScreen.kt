package com.example.xinggui.presentation.goals

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.xinggui.data.model.IepDocument
import com.example.xinggui.data.model.IepWeeklyGoalInput
import com.example.xinggui.data.model.UserRole
import com.example.xinggui.data.repository.AppRepository
import com.example.xinggui.data.repository.DataRepository
import com.example.xinggui.presentation.common.ScreenRenderState
import com.example.xinggui.ui.components.DecorativeStarsOverlay
import com.example.xinggui.ui.components.IosStateView
import com.example.xinggui.ui.components.ModuleEntryCard
import com.example.xinggui.ui.components.SectionCard
import com.example.xinggui.ui.theme.IosBlue
import com.example.xinggui.ui.theme.IosCard
import com.example.xinggui.ui.theme.IosCardMuted
import com.example.xinggui.ui.theme.IosGroupedBackground
import com.example.xinggui.ui.theme.IosRed
import com.example.xinggui.ui.theme.IosSeparator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val IepMimeTypes = arrayOf(
    "application/pdf",
    "text/plain",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
)

@Composable
fun GoalsScreen(
    selectedChildId: String,
    currentRole: UserRole,
    childProfileRefreshKey: String? = null,
    modifier: Modifier = Modifier,
    repository: AppRepository = DataRepository,
    presenterFactory: (AppRepository) -> GoalsContract.Presenter = { GoalsPresenter(it) }
) {
    val context = LocalContext.current
    val presenter = remember(repository, presenterFactory) { presenterFactory(repository) }
    val scope = rememberCoroutineScope()
    var renderState by remember {
        mutableStateOf<ScreenRenderState<GoalsUiState>>(ScreenRenderState.Loading)
    }
    var showIepDialog by remember { mutableStateOf(false) }
    var isIepUploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }
    var selectedIepFile by remember { mutableStateOf<SelectedIepFile?>(null) }
    var semesterGoalInput by remember { mutableStateOf("") }
    var monthlyGoalInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    val weeklyGoalForms = remember { mutableStateListOf<IepWeeklyGoalFormState>() }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        selectedIepFile = uri?.let { context.describeIepFile(it) }
        if (uri != null) {
            uploadMessage = null
        }
    }
    val view = remember {
        object : GoalsContract.View {
            override fun render(state: ScreenRenderState<GoalsUiState>) {
                renderState = state
            }

            override fun showIepUploading(isUploading: Boolean) {
                isIepUploading = isUploading
                if (isUploading) {
                    uploadMessage = null
                }
            }

            override fun showIepUploadSuccess(document: IepDocument) {
                selectedIepFile = null
                showIepDialog = false
                uploadMessage = "上传成功：${document.originalFileName}"
            }

            override fun showIepUploadError(message: String) {
                uploadMessage = message
            }
        }
    }

    LaunchedEffect(Unit) { presenter.attachView(view) }
    LaunchedEffect(selectedChildId, currentRole, childProfileRefreshKey) {
        presenter.loadData(selectedChildId, currentRole)
    }
    DisposableEffect(Unit) { onDispose { presenter.detachView() } }

    IosStateView(
        state = renderState,
        onRetry = { scope.launch { presenter.loadData(selectedChildId, currentRole) } },
        modifier = modifier.fillMaxSize()
    ) { state ->
            LaunchedEffect(state.semesterGoal, state.monthlyGoal, state.weeklyTasks, state.latestIepDocument) {
                semesterGoalInput = state.semesterGoal
                monthlyGoalInput = state.monthlyGoal
                notesInput = state.latestIepDocument?.notes.orEmpty()
                weeklyGoalForms.clear()
                val seededGoals = state.weeklyTasks.map {
                    IepWeeklyGoalFormState(
                        dimensionId = it.dimensionId,
                        title = it.title,
                        rewardStars = it.rewardStars.toString()
                    )
                }.ifEmpty {
                    listOf(IepWeeklyGoalFormState())
                }
                weeklyGoalForms.addAll(seededGoals)
            }

            Box(modifier = modifier
                .fillMaxSize()
                .background(IosGroupedBackground)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    GoalsTitleHeader()
                    SectionCard(title = "个别化教育计划长期目标", subtitle = state.semesterGoal) {
                        Text(text = state.monthlyGoal, style = MaterialTheme.typography.bodyMedium)
                    }
                    SectionCard(
                        title = "个别化教育计划上传与数字化录入",
                        subtitle = state.uploadHint
                    ) {
                        IepUploadEntry(
                            latestIepDocument = state.latestIepDocument,
                            isUploading = isIepUploading,
                            uploadMessage = uploadMessage,
                            onOpen = {
                                uploadMessage = null
                                showIepDialog = true
                            }
                        )
                    }
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
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (showIepDialog) {
                    IepUploadDialog(
                        selectedFile = selectedIepFile,
                        semesterGoal = semesterGoalInput,
                        monthlyGoal = monthlyGoalInput,
                        notes = notesInput,
                        weeklyGoals = weeklyGoalForms,
                        latestIepDocument = state.latestIepDocument,
                        isUploading = isIepUploading,
                        uploadMessage = uploadMessage,
                        onDismiss = { if (!isIepUploading) showIepDialog = false },
                        onPickFile = { filePicker.launch(IepMimeTypes) },
                        onSemesterGoalChange = { semesterGoalInput = it },
                        onMonthlyGoalChange = { monthlyGoalInput = it },
                        onNotesChange = { notesInput = it },
                        onWeeklyGoalChange = { index, item -> weeklyGoalForms[index] = item },
                        onAddWeeklyGoal = { weeklyGoalForms.add(IepWeeklyGoalFormState()) },
                        onRemoveWeeklyGoal = { index ->
                            if (weeklyGoalForms.size > 1) {
                                weeklyGoalForms.removeAt(index)
                            }
                        },
                        onSubmit = submit@{
                            val file = selectedIepFile
                            if (file == null) {
                                uploadMessage = "请先选择 PDF、DOC、DOCX 或 TXT 文件"
                                return@submit
                            }
                            scope.launch {
                                val fileBytes = runCatching {
                                    withContext(Dispatchers.IO) {
                                        context.contentResolver.openInputStream(file.uri)?.use { it.readBytes() }
                                            ?: error("无法读取文件")
                                    }
                                }.getOrElse { error ->
                                    uploadMessage = error.message ?: "无法读取文件"
                                    return@launch
                                }
                                presenter.submitIepDocument(
                                    childId = selectedChildId,
                                    role = currentRole,
                                    fileName = file.name,
                                    mimeType = file.mimeType,
                                    fileBytes = fileBytes,
                                    semesterGoal = semesterGoalInput,
                                    monthlyGoal = monthlyGoalInput,
                                    weeklyGoals = weeklyGoalForms.map {
                                        IepWeeklyGoalInput(
                                            dimensionId = it.dimensionId,
                                            title = it.title,
                                            rewardStars = it.rewardStars.toIntOrNull() ?: 0
                                        )
                                    },
                                    notes = notesInput.ifBlank { null }
                                )
                            }
                        }
                    )
                }

                DecorativeStarsOverlay(
                    modifier = Modifier.fillMaxSize(),
                    pageKey = "goals_screen"
                )
            }
    }
}

@Composable
private fun IepUploadEntry(
    latestIepDocument: IepDocument?,
    isUploading: Boolean,
    uploadMessage: String?,
    onOpen: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (latestIepDocument != null) {
            LatestIepSummary(latestIepDocument)
        }
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = IosCardMuted),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = IosBlue,
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (latestIepDocument == null) "尚未录入 IEP" else "IEP 已同步到星目标",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = if (latestIepDocument == null) {
                            "点击入口选择文件，并录入长期目标、月目标和周微目标。"
                        } else {
                            "最近文件：${latestIepDocument.originalFileName}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF667085),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        Button(
            onClick = onOpen,
            enabled = !isUploading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(if (isUploading) "正在上传..." else "上传 / 编辑 IEP")
        }
        uploadMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = if (it.startsWith("上传成功")) Color(0xFF15803D) else IosRed
            )
        }
    }
}

@Composable
private fun IepUploadDialog(
    selectedFile: SelectedIepFile?,
    semesterGoal: String,
    monthlyGoal: String,
    notes: String,
    weeklyGoals: List<IepWeeklyGoalFormState>,
    latestIepDocument: IepDocument?,
    isUploading: Boolean,
    uploadMessage: String?,
    onDismiss: () -> Unit,
    onPickFile: () -> Unit,
    onSemesterGoalChange: (String) -> Unit,
    onMonthlyGoalChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onWeeklyGoalChange: (Int, IepWeeklyGoalFormState) -> Unit,
    onAddWeeklyGoal: () -> Unit,
    onRemoveWeeklyGoal: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = IosCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "上传 / 编辑 IEP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "选择原始文件，并把目标结构化录入到星目标。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF667085)
                    )
                }
                HorizontalDivider(color = IosSeparator)
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    IepUploadFields(
                        selectedFile = selectedFile,
                        semesterGoal = semesterGoal,
                        monthlyGoal = monthlyGoal,
                        notes = notes,
                        weeklyGoals = weeklyGoals,
                        latestIepDocument = latestIepDocument,
                        isUploading = isUploading,
                        uploadMessage = uploadMessage,
                        onPickFile = onPickFile,
                        onSemesterGoalChange = onSemesterGoalChange,
                        onMonthlyGoalChange = onMonthlyGoalChange,
                        onNotesChange = onNotesChange,
                        onWeeklyGoalChange = onWeeklyGoalChange,
                        onAddWeeklyGoal = onAddWeeklyGoal,
                        onRemoveWeeklyGoal = onRemoveWeeklyGoal
                    )
                }
                HorizontalDivider(color = IosSeparator)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        enabled = !isUploading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE6EBF2),
                            contentColor = Color(0xFF4A5568),
                            disabledContainerColor = Color(0xFFE6EBF2),
                            disabledContentColor = Color(0xFF94A3B8)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("取消")
                    }
                    Button(
                        onClick = onSubmit,
                        enabled = !isUploading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IosBlue,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFB9C5D4),
                            disabledContentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(if (isUploading) "上传中" else "提交同步")
                    }
                }
            }
        }
    }
}

@Composable
private fun IepUploadFields(
    selectedFile: SelectedIepFile?,
    semesterGoal: String,
    monthlyGoal: String,
    notes: String,
    weeklyGoals: List<IepWeeklyGoalFormState>,
    latestIepDocument: IepDocument?,
    isUploading: Boolean,
    uploadMessage: String?,
    onPickFile: () -> Unit,
    onSemesterGoalChange: (String) -> Unit,
    onMonthlyGoalChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onWeeklyGoalChange: (Int, IepWeeklyGoalFormState) -> Unit,
    onAddWeeklyGoal: () -> Unit,
    onRemoveWeeklyGoal: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (latestIepDocument != null) {
            LatestIepSummary(latestIepDocument)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(onClick = onPickFile, enabled = !isUploading) {
                Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("选择文件")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedFile?.name ?: "支持 PDF / DOC / DOCX / TXT，最大 10MB",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                selectedFile?.sizeBytes?.let {
                    Text(text = formatFileSize(it), style = MaterialTheme.typography.labelSmall, color = Color(0xFF667085))
                }
            }
        }
        OutlinedTextField(
            value = semesterGoal,
            onValueChange = onSemesterGoalChange,
            enabled = !isUploading,
            label = { Text("长期目标") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = monthlyGoal,
            onValueChange = onMonthlyGoalChange,
            enabled = !isUploading,
            label = { Text("月目标") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            enabled = !isUploading,
            label = { Text("备注（可选）") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(color = IosSeparator)
        Text(text = "周微目标", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        weeklyGoals.forEachIndexed { index, item ->
            WeeklyGoalEditor(
                index = index,
                item = item,
                canRemove = weeklyGoals.size > 1,
                enabled = !isUploading,
                onChange = { onWeeklyGoalChange(index, it) },
                onRemove = { onRemoveWeeklyGoal(index) }
            )
        }
        FilledTonalButton(onClick = onAddWeeklyGoal, enabled = !isUploading, modifier = Modifier.fillMaxWidth()) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("添加微目标")
        }
        uploadMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = if (it.startsWith("上传成功")) Color(0xFF15803D) else IosRed
            )
        }
    }
}

@Composable
private fun LatestIepSummary(document: IepDocument) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4FBF7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "最近录入", style = MaterialTheme.typography.labelLarge, color = Color(0xFF166534))
            Text(
                text = document.originalFileName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${formatFileSize(document.fileSizeBytes)} · ${formatTimestamp(document.uploadedAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF667085)
            )
        }
    }
}

@Composable
private fun WeeklyGoalEditor(
    index: Int,
    item: IepWeeklyGoalFormState,
    canRemove: Boolean,
    enabled: Boolean,
    onChange: (IepWeeklyGoalFormState) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "微目标 ${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemove, enabled = enabled && canRemove, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
            OutlinedTextField(
                value = item.dimensionId,
                onValueChange = { onChange(item.copy(dimensionId = it)) },
                enabled = enabled,
                label = { Text("维度 ID，例如 communication") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = item.title,
                onValueChange = { onChange(item.copy(title = it)) },
                enabled = enabled,
                label = { Text("微目标标题") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = item.rewardStars,
                onValueChange = { input -> onChange(item.copy(rewardStars = input.filter { it.isDigit() }.take(1))) },
                enabled = enabled,
                label = { Text("奖励星数 1-5") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GoalsTitleHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Flag,
                contentDescription = null,
                tint = Color(0xFF111111),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "星目标",
                color = Color(0xFF111111),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                lineHeight = 32.sp
            )
        }
    }
}

private data class SelectedIepFile(
    val uri: Uri,
    val name: String,
    val mimeType: String?,
    val sizeBytes: Long?
)

private data class IepWeeklyGoalFormState(
    val dimensionId: String = "communication",
    val title: String = "",
    val rewardStars: String = "3"
)

private fun Context.describeIepFile(uri: Uri): SelectedIepFile {
    var displayName: String? = null
    var size: Long? = null
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (nameIndex >= 0) {
                displayName = cursor.getString(nameIndex)
            }
            if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                size = cursor.getLong(sizeIndex)
            }
        }
    }
    return SelectedIepFile(
        uri = uri,
        name = displayName ?: uri.lastPathSegment ?: "iep-document",
        mimeType = contentResolver.getType(uri),
        sizeBytes = size
    )
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    return if (kb < 1024) {
        String.format(Locale.CHINA, "%.1f KB", kb)
    } else {
        String.format(Locale.CHINA, "%.1f MB", kb / 1024.0)
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date(timestamp))
}
