package com.example.marketpulseai.di.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.data.Entry
import com.example.marketpulseai.data.remote.model.KpiMetricDto
import com.example.marketpulseai.di.components.KpiLineChart
import com.example.marketpulseai.di.dashboard.DashboardUiState
import com.example.marketpulseai.di.dashboard.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MarketPulse AI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboard() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.error != null -> {
                    ErrorState(
                        message = state.error ?: "Something went wrong",
                        onRetry = { viewModel.loadDashboard() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    DashboardContent(
                        state = state,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    modifier: Modifier = Modifier
) {
    val metrics = state.metrics
    val insights = state.insights
    val riskScore = insights?.risk_score ?: 50

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // --- Hero header with gradient background ---
        item {
            HeroHeader(
                orgName = "demo-org",
                timeframe = "Weekly snapshot",
                riskScore = riskScore
            )
        }

        // --- KPI summary chips / cards ---
        if (metrics.isNotEmpty()) {
            item {
                SectionTitle("Key KPIs")
            }
            item {
                KPIGrid(metrics = metrics)
            }
        }

        // --- KPI trend chart section ---
        if (metrics.isNotEmpty()) {
            item {
                SectionTitle("Trend Overview")
            }
            item {
                TrendCard(metrics = metrics)
            }
        }

        // --- AI Insights section ---
        insights?.let { ai ->
            item {
                SectionTitle("AI Insights")
            }
            item {
                AIInsightsCard(
                    summary = ai.summary,
                    recommendations = ai.recommendations,
                    riskScore = ai.risk_score
                )
            }
        }
    }
}

@Composable
private fun HeroHeader(
    orgName: String,
    timeframe: String,
    riskScore: Int,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        )
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Competitor Pulse",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = orgName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = timeframe,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text("AI-powered insights") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = Color.White,
                            leadingIconContentColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.15f)
                        )
                    )

                    RiskBadge(riskScore = riskScore)
                }
            }
        }
    }
}

@Composable
private fun RiskBadge(riskScore: Int) {
    val (bg, text) = when {
        riskScore < 35 -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        riskScore < 70 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        else -> Color(0xFFB00020) to Color.White // high risk red
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (riskScore >= 70) Icons.Default.Warning else Icons.Default.Assessment,
            contentDescription = null,
            tint = text
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Risk: $riskScore",
            style = MaterialTheme.typography.labelMedium,
            color = text
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = modifier,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun KPIGrid(
    metrics: List<KpiMetricDto>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        metrics.forEach { metric ->
            MetricCard(metric = metric)
        }
    }
}

@Composable
private fun MetricCard(metric: KpiMetricDto) {
    val isPositive = metric.change > 0
    val isNeutral = metric.change == 0.0

    val changeColor = when {
        isPositive -> Color(0xFF2E7D32) // green
        isNeutral  -> MaterialTheme.colorScheme.onSurfaceVariant
        else       -> Color(0xFFC62828) // red
    }

    val changeIcon = when {
        isPositive -> Icons.Default.TrendingUp
        isNeutral  -> Icons.Default.Assessment
        else       -> Icons.Default.TrendingDown
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = metric.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Value: ${"%.2f".format(metric.value)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = changeIcon,
                    contentDescription = null,
                    tint = changeColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${if (metric.change > 0) "+" else ""}${"%.2f".format(metric.change)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = changeColor
                )
            }
        }
    }
}

@Composable
private fun TrendCard(
    metrics: List<KpiMetricDto>,
    modifier: Modifier = Modifier
) {
    val entries = metrics.mapIndexed { index, metric ->
        Entry(index.toFloat(), metric.value.toFloat())
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "KPI Value Trend",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                KpiLineChart(
                    entries = entries,
                    label = "KPI Value",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun AIInsightsCard(
    summary: String,
    recommendations: List<String>,
    riskScore: Int,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = "AI Assistant",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                RiskBadge(riskScore = riskScore)
            }

            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium
            )

            if (recommendations.isNotEmpty()) {
                Text(
                    text = "Recommended actions",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    recommendations.forEach { rec ->
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = rec,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Oops!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}