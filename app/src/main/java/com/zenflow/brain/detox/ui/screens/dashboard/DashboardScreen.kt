package com.zenflow.brain.detox.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenflow.brain.detox.di.appViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenflow.brain.detox.data.repository.DailyUsageSummary
import com.zenflow.brain.detox.ui.theme.PrimaryBlue
import com.zenflow.brain.detox.ui.theme.SuccessGreen
import com.zenflow.brain.detox.ui.theme.WarningPink
import com.zenflow.brain.detox.util.TimeFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = appViewModel { DashboardViewModel(it) }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                "Your Progress",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        item {
            RowStats(
                timeSavedMs = uiState.timeSavedMs,
                streakDays = uiState.streakDays,
            )
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Weekly Usage", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    WeeklyBarChart(summaries = uiState.weeklySummary)
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("7-Day Trend", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    TrendLineChart(summaries = uiState.weeklySummary)
                }
            }
        }
    }
}

@Composable
private fun RowStats(timeSavedMs: Long, streakDays: Int) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Time Saved", style = MaterialTheme.typography.labelMedium)
                Text(
                    TimeFormatter.formatDurationMs(timeSavedMs),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen,
                )
            }
        }
        Card(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Streak", style = MaterialTheme.typography.labelMedium)
                Text(
                    "$streakDays days",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = WarningPink,
                )
            }
        }
    }
}

@Composable
private fun WeeklyBarChart(summaries: List<DailyUsageSummary>) {
    val maxMs = summaries.maxOfOrNull { it.totalUsedMs }?.coerceAtLeast(1) ?: 1L
    val dayFormatter = DateTimeFormatter.ofPattern("EEE")

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
    ) {
        val barCount = summaries.size.coerceAtLeast(1)
        val barWidth = size.width / (barCount * 2f)
        summaries.forEachIndexed { index, summary ->
            val barHeight = (summary.totalUsedMs.toFloat() / maxMs) * size.height * 0.85f
            val x = index * (barWidth * 2) + barWidth / 2
            drawRoundRect(
                color = PrimaryBlue,
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8f, 8f),
            )
        }
    }
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        summaries.forEach { summary ->
            val day = runCatching {
                LocalDate.parse(summary.dateKey).format(dayFormatter)
            }.getOrDefault("--")
            Text(day, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun TrendLineChart(summaries: List<DailyUsageSummary>) {
    if (summaries.size < 2) {
        Text("Not enough data yet", style = MaterialTheme.typography.bodySmall)
        return
    }
    val maxMs = summaries.maxOf { it.totalUsedMs }.coerceAtLeast(1).toFloat()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
    ) {
        val stepX = size.width / (summaries.size - 1)
        for (i in 0 until summaries.size - 1) {
            val y1 = size.height - (summaries[i].totalUsedMs / maxMs) * size.height
            val y2 = size.height - (summaries[i + 1].totalUsedMs / maxMs) * size.height
            drawLine(
                color = WarningPink,
                start = Offset(i * stepX, y1),
                end = Offset((i + 1) * stepX, y2),
                strokeWidth = 4f,
            )
            drawCircle(Color.White, radius = 6f, center = Offset(i * stepX, y1))
        }
        val lastY = size.height - (summaries.last().totalUsedMs / maxMs) * size.height
        drawCircle(Color.White, radius = 6f, center = Offset((summaries.size - 1) * stepX, lastY))
    }
}
