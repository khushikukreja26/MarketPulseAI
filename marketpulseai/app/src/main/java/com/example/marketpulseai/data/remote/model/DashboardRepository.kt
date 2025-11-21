package com.example.marketpulseai.data.remote.model

import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val api: MarketPulseApi
) {
    suspend fun fetchKpis(orgId: String): KpiResponse {
        return api.getKpis(orgId = orgId, timeframe = "weekly")
    }

    suspend fun fetchInsights(orgId: String): InsightsResponse {
        val request = InsightsRequest(
            orgId = orgId,
            timeframe = "weekly"
        )
        return api.getInsights(request)
    }
}