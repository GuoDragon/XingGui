package com.example.xinggui.data.model

data class ReportHistoryEntry(
    val entryId: String,
    val childId: String,
    val sourceItemId: String,
    val sourceDimensionId: String,
    val note: String,
    val generatedAt: Long,
    val dimensionScores: Map<String, Int>,
    val overview: String,
    val aiAnalysis: String,
    val overallEvaluation: String,
    val nextSuggestions: String
)
