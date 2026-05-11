package com.subscriptiontracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscriptiontracker.ui.theme.CardWhite
import com.subscriptiontracker.ui.theme.TextMuted
import com.subscriptiontracker.ui.theme.TextSecondary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val HeatmapEmpty = Color(0xFFEBEDF0)
private val HeatmapLow = Color(0xFF9BE9A8)
private val HeatmapMedium = Color(0xFF40C463)
private val HeatmapHigh = Color(0xFF216E39)

private val dayLabels = listOf("日", "一", "二", "三", "四", "五", "六")

@Composable
fun ActivityHeatmap(
    data: Map<LocalDate, Int>,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val startDate = today.minusWeeks(12).with(DayOfWeek.SUNDAY)
    val rows = 7
    val cols = ChronoUnit.WEEKS.between(startDate, today).toInt() + 1

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardWhite)
            .padding(16.dp)
    ) {
        Text(
            text = "订阅活跃度",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Grid: day labels on left + heatmap cells
        Row {
            // Day labels column
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                dayLabels.forEachIndexed { _, label ->
                    Box(
                        modifier = Modifier.size(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Heatmap grid
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                for (row in 0 until rows) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (col in 0 until cols) {
                            val date = startDate.plusDays((col * 7L + row))
                            val isFuture = date.isAfter(today)
                            val count = data[date] ?: 0

                            val color = when {
                                isFuture -> Color.Transparent
                                count >= 4 -> HeatmapHigh
                                count >= 2 -> HeatmapMedium
                                count >= 1 -> HeatmapLow
                                else -> HeatmapEmpty
                            }

                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isFuture) Color.Transparent else color)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color legend
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("少", fontSize = 9.sp, color = TextMuted)
                Spacer(modifier = Modifier.width(4.dp))
                listOf(HeatmapEmpty, HeatmapLow, HeatmapMedium, HeatmapHigh).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
                Text("多", fontSize = 9.sp, color = TextMuted)
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "过去 12 周",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}
