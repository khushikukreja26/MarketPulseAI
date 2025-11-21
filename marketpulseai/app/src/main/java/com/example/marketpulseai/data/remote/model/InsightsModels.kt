package com.example.marketpulseai.data.remote.model


data class InsightsRequest(
    val orgId: String,
    val timeframe: String = "weekly"
)

data class InsightsData(
    val summary: String,
    val recommendations: List<String>,
    val risk_score: Int
)

data class InsightsResponse(
    val orgId: String,
    val timeframe: String,
    val kpis: List<KpiMetricDto>,
    val insights: InsightsData
)