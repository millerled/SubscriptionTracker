package com.subscriptiontracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscriptiontracker.ui.theme.CardWhite
import com.subscriptiontracker.ui.theme.PaidGreen
import com.subscriptiontracker.ui.theme.PendingOrange
import com.subscriptiontracker.ui.theme.TextMain
import com.subscriptiontracker.ui.theme.TextMuted
import com.subscriptiontracker.ui.theme.TextSecondary
import com.subscriptiontracker.util.formatCurrency

@Composable
fun StatsCard(
    monthlyTotal: Double,
    dailyAverage: Double,
    paidAmount: Double,
    pendingAmount: Double,
    modifier: Modifier = Modifier
) {
    val totalForRatio = paidAmount + pendingAmount
    val paidRatio = if (totalForRatio > 0.0) (paidAmount / totalForRatio).toFloat() else 0.05f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardWhite)
            .padding(20.dp)
    ) {
        Text(
            text = "本月订阅支出",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )

        Text(
            text = formatCurrency(monthlyTotal),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = TextMain,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = "日均支出 ${formatCurrency(dailyAverage)}",
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 2.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(paidRatio)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PaidGreen)
            )
            Box(
                modifier = Modifier
                    .weight(1f - paidRatio)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PendingOrange)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "已扣费 ${formatCurrency(paidAmount)}",
                fontSize = 12.sp,
                color = PaidGreen
            )
            Text(
                text = "待扣费 ${formatCurrency(pendingAmount)}",
                fontSize = 12.sp,
                color = PendingOrange
            )
        }
    }
}
