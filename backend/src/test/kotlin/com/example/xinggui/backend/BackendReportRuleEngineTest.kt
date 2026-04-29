package com.example.xinggui.backend

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackendReportRuleEngineTest {
    @Test
    fun completedCheckInRaisesTargetDimensionAndKeepsBounds() {
        val base = PublicReportSummary(
            childId = "child001",
            overview = "base",
            overallEvaluation = "base",
            nextSuggestions = "base",
            aiAnalysis = "base",
            dimensionScores = mapOf("communication" to 70, "emotion" to 99),
            dimensionHighlights = emptyList()
        )
        val checkIn = PublicWeeklyCheckIn(
            itemId = "w1",
            dimensionId = "communication",
            title = "完成短语接话游戏",
            completed = true,
            rewardStars = 2
        )

        val updated = BackendReportRuleEngine.generateUpdatedReport(base, checkIn, "能够主动回应教师提问")

        assertTrue(updated.dimensionScores.getValue("communication") > 70)
        assertEquals(99, updated.dimensionScores.getValue("emotion"))
        assertTrue(updated.dimensionScores.values.all { it in 0..100 })
        assertTrue(updated.dimensionHighlights.any { it.contains("沟通") })
    }

    @Test
    fun missedCheckInAppliesSmallNegativeDelta() {
        val base = PublicReportSummary(
            childId = "child001",
            overview = "base",
            overallEvaluation = "base",
            nextSuggestions = "base",
            aiAnalysis = "base",
            dimensionScores = mapOf("self_care" to 3),
            dimensionHighlights = emptyList()
        )
        val checkIn = PublicWeeklyCheckIn(
            itemId = "w2",
            dimensionId = "self_care",
            title = "独立洗手",
            completed = false,
            rewardStars = 2
        )

        val updated = BackendReportRuleEngine.generateUpdatedReport(base, checkIn, "")

        assertEquals(1, updated.dimensionScores.getValue("self_care"))
        assertTrue(updated.aiAnalysis.contains("负向"))
    }
}
