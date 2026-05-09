package com.subscriptiontracker.domain.model

enum class BillingCycle(val displayName: String) {
    MONTHLY("月付"),
    QUARTERLY("季付"),
    YEARLY("年付"),
    ONE_TIME("一次性");

    companion object {
        fun fromString(value: String): BillingCycle =
            entries.firstOrNull { it.name == value } ?: MONTHLY
    }
}
