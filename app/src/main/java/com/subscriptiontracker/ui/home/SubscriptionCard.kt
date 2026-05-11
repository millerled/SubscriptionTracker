package com.subscriptiontracker.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscriptiontracker.domain.model.Intention
import com.subscriptiontracker.domain.model.Subscription
import com.subscriptiontracker.domain.model.SubscriptionStatus
import com.subscriptiontracker.ui.components.AppLogoIcon
import com.subscriptiontracker.ui.components.FrostedGlassCard
import com.subscriptiontracker.ui.theme.CardWhite
import com.subscriptiontracker.ui.theme.IntentionConsidering
import com.subscriptiontracker.ui.theme.IntentionQuitting
import com.subscriptiontracker.ui.theme.IntentionUndecided
import com.subscriptiontracker.ui.theme.IntentionUndecidedStale
import com.subscriptiontracker.ui.theme.StatusBorderActive
import com.subscriptiontracker.ui.theme.StatusBorderExpiring
import com.subscriptiontracker.ui.theme.StatusBorderPaused
import com.subscriptiontracker.ui.theme.StatusBorderRenewing
import com.subscriptiontracker.ui.theme.TextMain
import com.subscriptiontracker.ui.theme.TextMuted
import com.subscriptiontracker.ui.theme.TextSecondary
import com.subscriptiontracker.ui.theme.statusBorderColor
import com.subscriptiontracker.util.daysUntil
import com.subscriptiontracker.util.formatCurrency
import com.subscriptiontracker.util.remainingDaysText

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubscriptionCard(
    subscription: Subscription,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = statusBorderColor(subscription.status)
    val intentionColor = intentionDotColor(subscription.intention, subscription.deadlineDate)
    val daysLeft = subscription.deadlineDate.daysUntil()

    val cycleText = subscription.billingCycle.cycleLabel(subscription.autoRenew)

    FrostedGlassCard(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        borderColor = borderColor,
        backgroundColor = CardWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppLogoIcon(
                name = subscription.name,
                logoUri = subscription.logoUri,
                modifier = Modifier
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subscription.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Box(
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(8.dp)
                        .drawBehind {
                            drawCircle(color = intentionColor)
                        }
                )
            }

            Text(
                text = formatCurrency(subscription.amount),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMain,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 68.dp, end = 16.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subscription.deadlineDate.remainingDaysText(),
                fontSize = 12.sp,
                color = if (daysLeft in 0..7 && subscription.status == SubscriptionStatus.ACTIVE)
                    StatusBorderExpiring else TextSecondary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(categoryColor(subscription.category))
                )
                Text(
                    text = subscription.category,
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Text(
                text = cycleText,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

private fun intentionDotColor(intention: Intention, deadlineDate: java.time.LocalDate): Color {
    val baseColor = when (intention) {
        Intention.CONSIDERING -> IntentionConsidering
        Intention.QUITTING -> IntentionQuitting
        Intention.UNDECIDED -> IntentionUndecided
    }
    if (intention == Intention.UNDECIDED && deadlineDate.daysUntil() < 0) {
        return IntentionUndecidedStale
    }
    return baseColor
}

private fun categoryColor(category: String): Color = when (category) {
    "娱乐" -> Color(0xFFE74C3C)
    "工具" -> Color(0xFF3498DB)
    "健康" -> Color(0xFF2ECC71)
    "学习" -> Color(0xFFF39C12)
    "生活" -> Color(0xFF9B59B6)
    else -> Color(0xFF95A5A6)
}
