
package com.example.marketpulseai.data.remote.model

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MarketPulseApi {

    @GET("api/kpis")
    suspend fun getKpis(
        @Query("orgId") orgId: String,
        @Query("timeframe") timeframe: String = "weekly"
    ): KpiResponse

    @POST("api/insights")
    suspend fun getInsights(
        @Body request: InsightsRequest
    ): InsightsResponse
}