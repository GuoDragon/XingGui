package com.example.xinggui.data.model

data class ReportLoadResult(
    val report: ReportSummary?,
    val source: ReportDataSource,
    val fallbackUsed: Boolean
)
