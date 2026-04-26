package com.example.xinggui.data.repository

import com.example.xinggui.data.model.GrowthDimension
import com.example.xinggui.data.model.ReportSummary
import com.example.xinggui.data.model.WeeklyCheckIn
import kotlin.math.roundToInt

object ReportRuleEngine {
    fun generateUpdatedReport(
        baseReport: ReportSummary,
        checkIn: WeeklyCheckIn,
        note: String
    ): ReportSummary {
        val normalizedScores = buildNormalizedScores(baseReport)
        val scoreDelta = calculateScoreDelta(checkIn.completed, note)
        val updatedScores = normalizedScores.toMutableMap().apply {
            val original = this[checkIn.dimensionId] ?: 0
            this[checkIn.dimensionId] = (original + scoreDelta).coerceIn(0, 100)
        }

        val ordered = updatedScores.entries.sortedByDescending { it.value }
        val average = updatedScores.values.average().roundToInt()
        val top = ordered.take(2).mapNotNull { entry ->
            GrowthDimension.entries.firstOrNull { it.id == entry.key }?.displayName
        }
        val lowest = ordered.lastOrNull()?.key?.let { key ->
            GrowthDimension.entries.firstOrNull { it.id == key }?.displayName
        }.orEmpty()
        val currentDimensionName = GrowthDimension.entries
            .firstOrNull { it.id == checkIn.dimensionId }
            ?.displayName
            ?: checkIn.dimensionId

        val trimmedNote = note.trim()
        val noteSummary = if (trimmedNote.isBlank()) {
            "本次打卡未填写备注。"
        } else {
            "家长记录：$trimmedNote"
        }

        val overview = when {
            average >= 80 -> "整体状态优秀，训练节奏稳定。"
            average >= 65 -> "整体状态稳步提升，建议继续维持当前节奏。"
            else -> "整体仍处于爬坡阶段，建议增加结构化重复训练。"
        }

        val aiAnalysis = buildString {
            append("本次在“")
            append(currentDimensionName)
            append("”维度产生")
            append(if (scoreDelta >= 0) "正向" else "负向")
            append("波动（")
            append(if (scoreDelta >= 0) "+" else "")
            append(scoreDelta)
            append("）。")
            append(noteSummary)
        }

        val overallEvaluation = buildString {
            append("当前平均得分 ")
            append(average)
            append(" 分。")
            if (top.isNotEmpty()) {
                append("优势维度：")
                append(top.joinToString("、"))
                append("。")
            }
            if (lowest.isNotBlank()) {
                append("重点提升维度：")
                append(lowest)
                append("。")
            }
        }

        val nextSuggestions = buildString {
            append("建议围绕“")
            append(currentDimensionName)
            append("”继续执行微目标训练，并在家庭场景保持同口径反馈。")
            if (lowest.isNotBlank()) {
                append("下周期优先为“")
                append(lowest)
                append("”增加 1~2 个可量化任务。")
            }
        }

        val highlights = buildList {
            if (top.isNotEmpty()) add("优势维度：${top.joinToString("、")}")
            if (lowest.isNotBlank()) add("需重点支持：$lowest")
            add("本次打卡维度：$currentDimensionName，分值变化 ${if (scoreDelta >= 0) "+" else ""}$scoreDelta")
        }

        return baseReport.copy(
            overview = overview,
            aiAnalysis = aiAnalysis,
            overallEvaluation = overallEvaluation,
            nextSuggestions = nextSuggestions,
            dimensionScores = updatedScores,
            dimensionHighlights = highlights
        )
    }

    private fun buildNormalizedScores(baseReport: ReportSummary): Map<String, Int> {
        val merged = GrowthDimension.entries.associate { dimension ->
            dimension.id to (baseReport.dimensionScores[dimension.id] ?: 60).coerceIn(0, 100)
        }.toMutableMap()
        baseReport.dimensionScores.forEach { (key, value) ->
            if (key !in merged) {
                merged[key] = value.coerceIn(0, 100)
            }
        }
        return merged
    }

    private fun calculateScoreDelta(completed: Boolean, note: String): Int {
        if (!completed) return -2
        val len = note.trim().length
        return when {
            len >= 24 -> 6
            len >= 10 -> 5
            len > 0 -> 4
            else -> 3
        }
    }
}
