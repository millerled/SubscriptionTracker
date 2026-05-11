package com.subscriptiontracker.domain.model

enum class BillingCycle(val displayName: String, val shortName: String, val cycleDays: Long, private val renewLabel: String, private val nonRenewLabel: String) {
    MONTHLY("月付", "月", 30L, "每月", "单月"),
    QUARTERLY("季付", "季", 91L, "每季", "单季"),
    YEARLY("年付", "年", 365L, "每年", "单年"),
    ONE_TIME("一次性", "次", 0L, "单次", "单次");

    fun cycleLabel(autoRenew: Boolean) = if (autoRenew) renewLabel else nonRenewLabel

    companion object {
        fun fromString(value: String): BillingCycle =
            entries.firstOrNull { it.name == value } ?: MONTHLY
    }
}
