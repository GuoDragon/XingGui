package com.example.xinggui.presentation.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun PrivacyNoticeDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "隐私与儿童数据说明") },
        text = {
            Text(
                text = "星轨仅在本机演示后端中保存账号、儿童档案、成长报告、IEP 文档与资源使用记录，用于家校协同展示。儿童姓名、评估记录和上传文件仅面向已授权家长/教师账号展示；演示数据不接入短信、支付或第三方广告服务。"
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "我知道了")
            }
        }
    )
}
