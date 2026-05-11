package com.subscriptiontracker.ui.theme

import androidx.compose.ui.graphics.Color
import com.subscriptiontracker.domain.model.SubscriptionStatus

// Backgrounds
val PageBackground = Color(0xFFF7F9FC)
val CardWhite = Color(0xFFFFFFFF)
val DividerColor = Color(0xFFEDF2F7)

// Text
val TextMain = Color(0xFF111827)
val TextSecondary = Color(0xFF6B7280)
val TextMuted = Color(0xFF94A3B8)

// Primary
val Primary = Color(0xFF4285F4)
val OnPrimary = Color.White

// Status border colors (thin gradient on card)
val StatusBorderActive = Color(0xFF34A853)
val StatusBorderRenewing = Color(0xFFFBBC05)
val StatusBorderExpiring = Color(0xFFEA4335)
val StatusBorderPaused = Color(0xFF94A3B8)

// Intention dot colors (deeper than border colors)
val IntentionConsidering = Color(0xFFD97706)
val IntentionQuitting = Color(0xFFB91C1C)
val IntentionUndecided = Color(0xFF64748B)
val IntentionUndecidedStale = Color(0xFF374151)

// Accent
val AmberWarning = Color(0xFFF59E0B)
val DangerRed = Color(0xFFEF4444)

// Stats
val PaidGreen = Color(0xFF34A853)
val PendingOrange = Color(0xFFFBBC05)

fun statusBorderColor(status: SubscriptionStatus): Color = when (status) {
    SubscriptionStatus.ACTIVE -> StatusBorderActive
    SubscriptionStatus.RENEWING -> StatusBorderRenewing
    SubscriptionStatus.EXPIRING -> StatusBorderExpiring
    SubscriptionStatus.PAUSED -> StatusBorderPaused
}
