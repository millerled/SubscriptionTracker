package com.subscriptiontracker.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscriptiontracker.domain.model.BillingCycle
import com.subscriptiontracker.domain.model.Intention
import com.subscriptiontracker.domain.model.Subscription
import com.subscriptiontracker.domain.model.SubscriptionStatus
import com.subscriptiontracker.ui.components.AppLogoIcon
import com.subscriptiontracker.ui.components.BrandLetterLogo
import com.subscriptiontracker.ui.components.logoAccentColor
import com.subscriptiontracker.ui.components.logoStyleFor
import com.subscriptiontracker.ui.theme.CardWhite
import com.subscriptiontracker.ui.theme.IntentionConsidering
import com.subscriptiontracker.ui.theme.IntentionQuitting
import com.subscriptiontracker.ui.theme.IntentionUndecided
import com.subscriptiontracker.ui.theme.IntentionUndecidedStale
import com.subscriptiontracker.ui.theme.StatusBorderExpiring
import com.subscriptiontracker.ui.theme.TextMain
import com.subscriptiontracker.ui.theme.TextMuted
import com.subscriptiontracker.ui.theme.TextSecondary
import com.subscriptiontracker.ui.theme.statusBorderColor
import com.subscriptiontracker.util.daysUntil
import com.subscriptiontracker.util.formatCurrency
import com.subscriptiontracker.util.formatShort
import com.subscriptiontracker.util.remainingDaysText

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubscriptionCard(
    subscription: Subscription,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = logoStyleFor(subscription.name, subscription.logoUri)
    val accentColor = logoAccentColor(subscription.name, subscription.logoUri)
    val statusColor = statusBorderColor(subscription.status)
    val intentionColor = intentionDotColor(subscription.intention, subscription.deadlineDate)
    val daysLeft = subscription.deadlineDate.daysUntil()
    val isUrgent = daysLeft in 0..7 && subscription.status == SubscriptionStatus.ACTIVE
    val cycleText = subscription.billingCycle.cycleLabel(subscription.autoRenew)
    val backgroundColors = cardGradientColors(accentColor, subscription.status, isUrgent)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 7.dp)
            .height(136.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = backgroundColors,
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            .drawBehind {
                drawRoundRect(
                    color = statusColor.copy(alpha = 0.28f),
                    size = size,
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                )
            }
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(18.dp)
    ) {
        BrandWatermark(
            styleColors = style.colors,
            glyph = style.glyph,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .graphicsLayer(alpha = 0.23f, rotationZ = -9f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppLogoIcon(
                    name = subscription.name,
                    logoUri = subscription.logoUri,
                    size = 58.dp,
                    showBorder = false
                )
                Spacer(modifier = Modifier.height(8.dp))
                RenewalProgress(daysLeft = daysLeft, cycle = subscription.billingCycle, accentColor = accentColor)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 18.dp, end = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = subscription.name,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(9.dp)
                            .drawBehind { drawCircle(color = intentionColor) }
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatCurrency(subscription.amount),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Text(
                        text = " / ${subscription.billingCycle.shortName}",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "下次付款: ${subscription.deadlineDate.formatShort()}",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 5.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusPill(
                        text = subscription.deadlineDate.remainingDaysText(),
                        color = if (isUrgent) StatusBorderExpiring else statusColor
                    )
                    if (subscription.autoRenew) {
                        StatusPill(text = "自动续费", color = accentColor)
                    } else {
                        StatusPill(text = cycleText, color = Color(0xFFF59E0B))
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandWatermark(
    styleColors: List<Color>,
    glyph: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        BrandLetterLogo(
            letter = glyph,
            colors = styleColors,
            modifier = Modifier.size(82.dp)
        )
    }
}

@Composable
private fun RenewalProgress(
    daysLeft: Long,
    cycle: BillingCycle,
    accentColor: Color
) {
    val progress = progressFraction(daysLeft, cycle.cycleDays)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.07f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(5.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(accentColor)
            )
        }
        Text(
            text = when {
                daysLeft < 0 -> "过期"
                daysLeft == 0L -> "今天"
                else -> "${daysLeft.coerceAtMost(99)}天"
            },
            fontSize = 11.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun StatusPill(
    text: String,
    color: Color
) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    )
}

private fun progressFraction(daysLeft: Long, cycleDays: Long): Float {
    if (cycleDays <= 0L) return 1f
    return (1f - daysLeft.coerceIn(0L, cycleDays).toFloat() / cycleDays.toFloat()).coerceIn(0.06f, 1f)
}

private fun cardGradientColors(accent: Color, status: SubscriptionStatus, isUrgent: Boolean): List<Color> {
    if (isUrgent) {
        return listOf(Color(0xFFFFF1F1), Color(0xFFFFF8F8), CardWhite)
    }
    if (status == SubscriptionStatus.PAUSED) {
        return listOf(Color(0xFFF4F4F5), Color(0xFFF8FAFC), CardWhite)
    }
    return listOf(accent.copy(alpha = 0.16f), accent.copy(alpha = 0.08f), CardWhite)
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
