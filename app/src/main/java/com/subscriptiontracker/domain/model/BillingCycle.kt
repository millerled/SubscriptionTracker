package com.subscriptiontracker.domain.model

enum class BillingCycle(val displayName: String, val shortName: String) {
    MONTHLY("月付", "月"),
    QUARTERLY("季付", "季"),
    YEARLY("年付", "年"),
    ONE_TIME("一次性", "次");

    companion object {
        fun fromString(value: String): BillingCycle =
            entries.firstOrNull { it.name == value } ?: MONTHLY
    }
}
