package com.example.xinggui.data.model

data class ReportSummary(
    val childId: String,
    val overview: String,
    val overallEvaluation: String,
    val nextSuggestions: String,
    val aiAnalysis: String,
    val dimensionScores: Map<String, Int> = emptyMap(),
    val dimensionHighlights: List<String> = emptyList()
)
