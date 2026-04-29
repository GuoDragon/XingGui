package com.example.xinggui.backend

import kotlin.math.roundToInt

object BackendReportRuleEngine {
    private val dimensions = listOf(
        "cognition" to "认知",
        "communication" to "沟通",
        "action" to "动作",
        "relationship" to "关系",
        "emotion" to "情绪",
        "sensory" to "感知",
        "health" to "健康",
        "self_care" to "自理",
        "academic" to "学业"
    )
    private val dimensionNames = dimensions.toMap()

    fun generateUpdatedReport(
        baseReport: PublicReportSummary,
        checkIn: PublicWeeklyCheckIn,
        note: String
    ): PublicReportSummary {
        val normalizedScores = buildNormalizedScores(baseReport.dimensionScores)
        val scoreDelta = calculateScoreDelta(checkIn.completed, note)
        val updatedScores = normalizedScores.toMutableMap().apply {
            val original = this[checkIn.dimensionId] ?: 60
            this[checkIn.dimensionId] = (original + scoreDelta).coerceIn(0, 100)
        }

        val ordered = updatedScores.entries.sortedByDescending { it.value }
        val average = updatedScores.values.average().roundToInt()
        val top = ordered.take(2).map { dimensionNames[it.key] ?: it.key }
        val lowest = ordered.lastOrNull()?.let { dimensionNames[it.key] ?: it.key }.orEmpty()
        val currentDimensionName = dimensionNames[checkIn.dimensionId] ?: checkIn.dimensionId
        val noteSummary = note.trim().takeIf { it.isNotBlank() }?.let { "记录要点：$it" }
            ?: "本次打卡未填写备注。"

        val overview = when {
            average >= 80 -> "整体状态优秀，训练节奏稳定，建议保持当前家校协作频率。"
            average >= 65 -> "整体状态稳步提升，本次打卡已纳入成长报告动态评估。"
            else -> "整体仍处于爬坡阶段，建议增加结构化重复训练与及时反馈。"
        }

        val aiAnalysis = "本次在“$currentDimensionName”维度产生" +
            "${if (scoreDelta >= 0) "正向" else "负向"}波动（${if (scoreDelta >= 0) "+" else ""}$scoreDelta）。$noteSummary"

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

    private fun buildNormalizedScores(baseScores: Map<String, Int>): Map<String, Int> {
        val merged = dimensions.associate { (id, _) ->
            id to (baseScores[id] ?: 60).coerceIn(0, 100)
        }.toMutableMap()
        baseScores.forEach { (key, value) ->
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
