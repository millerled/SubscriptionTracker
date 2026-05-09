package com.subscriptiontracker.ui.components

import androidx.compose.foundation.background
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
import com.subscriptiontracker.domain.model.SubscriptionStatus
import com.subscriptiontracker.ui.theme.BadgeShape
import com.subscriptiontracker.ui.theme.StatusConsidering
import com.subscriptiontracker.ui.theme.StatusExpiring
import com.subscriptiontracker.ui.theme.StatusPaused
import com.subscriptiontracker.ui.theme.StatusPlanned
import com.subscriptiontracker.ui.theme.StatusUndecided

@Composable
fun StatusBadge(
    status: SubscriptionStatus,
    isExpiring: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, text) = when {
        isExpiring -> Triple(
            StatusExpiring.copy(alpha = 0.12f),
            StatusExpiring,
            "即将到期"
        )
        status == SubscriptionStatus.ACTIVE -> Triple(
            Color(0xFF34C759).copy(alpha = 0.12f),
            Color(0xFF34C759),
            status.displayName
        )
        else -> statusBadgeColors(status)
    }

    Text(
        text = text,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(BadgeShape)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

private fun statusBadgeColors(status: SubscriptionStatus): Triple<Color, Color, String> {
    return when (status) {
        SubscriptionStatus.CONSIDERING -> Triple(
            StatusConsidering.copy(alpha = 0.12f),
            StatusConsidering,
            status.displayName
        )
        SubscriptionStatus.PLANNED -> Triple(
            StatusPlanned.copy(alpha = 0.12f),
            StatusPlanned,
            status.displayName
        )
        SubscriptionStatus.PAUSED -> Triple(
            StatusPaused.copy(alpha = 0.12f),
            StatusPaused,
            status.displayName
        )
        SubscriptionStatus.UNDECIDED -> Triple(
            StatusUndecided.copy(alpha = 0.12f),
            StatusUndecided,
            status.displayName
        )
        else -> Triple(
            Color(0xFF34C759).copy(alpha = 0.12f),
            Color(0xFF34C759),
            status.displayName
        )
    }
}
