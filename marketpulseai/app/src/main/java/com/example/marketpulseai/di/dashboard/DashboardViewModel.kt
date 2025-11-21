package com.example.marketpulseai.di.dashboard


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marketpulseai.data.remote.model.DashboardRepository
import com.example.marketpulseai.data.remote.model.InsightsData
import com.example.marketpulseai.data.remote.model.KpiMetricDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val metrics: List<KpiMetricDto> = emptyList(),
    val insights: InsightsData? = null,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState

    // For now, hard-coded orgId that matches your backend tests
    private val orgId = "demo-org"

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val kpiResponse = repository.fetchKpis(orgId)
                val insightsResponse = repository.fetchInsights(orgId)

                _uiState.value = DashboardUiState(
                    isLoading = false,
                    metrics = kpiResponse.metrics,
                    insights = insightsResponse.insights,
                    error = null
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    metrics = emptyList(),
                    insights = null,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
}