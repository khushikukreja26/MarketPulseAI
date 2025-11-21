package com.example.marketpulseai.data.remote.model


data class KpiMetricDto(
    val name: String,
    val value: Double,
    val change: Double
)

data class KpiResponse(
    val orgId: String,
    val timeframe: String,
    val metrics: List<KpiMetricDto>
)