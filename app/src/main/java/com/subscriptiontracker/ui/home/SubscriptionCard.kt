package com.subscriptiontracker.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.subscriptiontracker.domain.model.Subscription
import com.subscriptiontracker.domain.model.SubscriptionStatus
import com.subscriptiontracker.ui.components.FrostedGlassCard
import com.subscriptiontracker.ui.components.StatusBadge
import com.subscriptiontracker.ui.theme.CardConsideringBackground
import com.subscriptiontracker.ui.theme.CardExpiringBackground
import com.subscriptiontracker.ui.theme.CardPausedBackground
import com.subscriptiontracker.ui.theme.CardPlannedBackground
import com.subscriptiontracker.ui.theme.CardUndecidedBackground
import com.subscriptiontracker.ui.theme.TextPrimary
import com.subscriptiontracker.ui.theme.TextSecondary
import com.subscriptiontracker.util.daysUntil
import com.subscriptiontracker.util.remainingDaysText

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubscriptionCard(
    subscription: Subscription,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpiring = subscription.status == SubscriptionStatus.ACTIVE &&
            subscription.deadlineDate.daysUntil() in 1..7

    val cardColor = cardBackgroundColor(subscription.status, isExpiring)
    val isDimmed = subscription.status != SubscriptionStatus.ACTIVE || isExpiring
    val textAlpha = if (subscription.status == SubscriptionStatus.PAUSED) 0.6f else 1f

    FrostedGlassCard(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        backgroundColor = cardColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subscription.name,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary.copy(alpha = textAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                StatusBadge(
                    status = subscription.status,
                    isExpiring = isExpiring
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subscription.amountPerCycleText(),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = TextSecondary.copy(alpha = textAlpha)
                )

                Text(
                    text = subscription.deadlineDate.remainingDaysText(),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = if (isExpiring) com.subscriptiontracker.ui.theme.StatusExpiring
                    else TextSecondary.copy(alpha = textAlpha),
                    fontWeight = if (isExpiring) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

private fun cardBackgroundColor(status: SubscriptionStatus, isExpiring: Boolean) = when {
    isExpiring -> CardExpiringBackground
    status == SubscriptionStatus.CONSIDERING -> CardConsideringBackground
    status == SubscriptionStatus.PLANNED -> CardPlannedBackground
    status == SubscriptionStatus.PAUSED -> CardPausedBackground
    status == SubscriptionStatus.UNDECIDED -> CardUndecidedBackground
    else -> androidx.compose.ui.graphics.Color.White
}

private fun Subscription.amountPerCycleText(): String {
    val cycleText = when (billingCycle) {
        com.subscriptiontracker.domain.model.BillingCycle.MONTHLY -> "月"
        com.subscriptiontracker.domain.model.BillingCycle.QUARTERLY -> "季"
        com.subscriptiontracker.domain.model.BillingCycle.YEARLY -> "年"
        com.subscriptiontracker.domain.model.BillingCycle.ONE_TIME -> "次"
    }
    return "¥%.2f / %s".format(amount, cycleText)
}

