package com.subscriptiontracker.ui.theme

import androidx.compose.ui.graphics.Color
import com.subscriptiontracker.domain.model.SubscriptionStatus

// Backgrounds
val PageBackground = Color(0xFFF5F7FA)
val CardWhite = Color(0xFFFFFFFF)
val DividerColor = Color(0xFFEDF2F7)

// Text
val TextMain = Color(0xFF1A2C3E)
val TextSecondary = Color(0xFF6C7A89)
val TextMuted = Color(0xFF94A3B8)

// Primary
val Primary = Color(0xFF1C4E80)
val OnPrimary = Color.White

// Status border colors (thin gradient on card)
val StatusBorderActive = Color(0xFF2ECC71)
val StatusBorderRenewing = Color(0xFFE67E22)
val StatusBorderExpiring = Color(0xFFFF6B6B)
val StatusBorderPaused = Color(0xFF94A3B8)

// Intention dot colors (deeper than border colors)
val IntentionConsidering = Color(0xFFD97706)
val IntentionQuitting = Color(0xFFB91C1C)
val IntentionUndecided = Color(0xFF64748B)
val IntentionUndecidedStale = Color(0xFF374151)

// Stats
val PaidGreen = Color(0xFF2ECC71)
val PendingOrange = Color(0xFFE67E22)

fun statusBorderColor(status: SubscriptionStatus): Color = when (status) {
    SubscriptionStatus.ACTIVE -> StatusBorderActive
    SubscriptionStatus.RENEWING -> StatusBorderRenewing
    SubscriptionStatus.EXPIRING -> StatusBorderExpiring
    SubscriptionStatus.PAUSED -> StatusBorderPaused
}
