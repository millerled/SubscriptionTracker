package com.subscriptiontracker.domain.model

enum class SubscriptionStatus(val displayName: String) {
    ACTIVE("生效中"),
    RENEWING("即将续订"),
    EXPIRING("即将到期"),
    PAUSED("暂停/已失效");

    companion object {
        fun fromString(value: String): SubscriptionStatus =
            entries.firstOrNull { it.name == value } ?: ACTIVE
    }
}
