package com.example.xinggui.data.repository

import com.example.xinggui.data.model.ReportSummary
import com.example.xinggui.data.model.WeeklyCheckIn
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportRuleEngineTest {
    @Test
    fun completedCheckInShouldIncreaseTargetDimensionScore() {
        val base = ReportSummary(
            childId = "child001",
            overview = "old",
            overallEvaluation = "old",
            nextSuggestions = "old",
            aiAnalysis = "old",
            dimensionScores = mapOf("communication" to 70)
        )
        val checkIn = WeeklyCheckIn(
            itemId = "w1",
            dimensionId = "communication",
            title = "短语接话",
            completed = true,
            rewardStars = 2
        )

        val updated = ReportRuleEngine.generateUpdatedReport(base, checkIn, "今天可主动完整表达需求")

        val oldScore = base.dimensionScores["communication"] ?: 0
        val newScore = updated.dimensionScores["communication"] ?: 0
        assertTrue(newScore > oldScore)
    }

    @Test
    fun incompleteCheckInShouldDecreaseTargetDimensionScore() {
        val base = ReportSummary(
            childId = "child001",
            overview = "old",
            overallEvaluation = "old",
            nextSuggestions = "old",
            aiAnalysis = "old",
            dimensionScores = mapOf("action" to 66)
        )
        val checkIn = WeeklyCheckIn(
            itemId = "w2",
            dimensionId = "action",
            title = "动作训练",
            completed = false,
            rewardStars = 1
        )

        val updated = ReportRuleEngine.generateUpdatedReport(base, checkIn, "")

        val oldScore = base.dimensionScores["action"] ?: 0
        val newScore = updated.dimensionScores["action"] ?: 0
        assertTrue(newScore < oldScore)
    }
}
