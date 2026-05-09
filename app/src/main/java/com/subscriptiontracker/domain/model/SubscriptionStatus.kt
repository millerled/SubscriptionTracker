package com.subscriptiontracker.domain.model

enum class SubscriptionStatus(val displayName: String) {
    ACTIVE("活跃"),
    CONSIDERING("考虑续订"),
    PLANNED("新增预订"),
    PAUSED("已暂停"),
    UNDECIDED("不确定");

    companion object {
        fun fromString(value: String): SubscriptionStatus =
            entries.firstOrNull { it.name == value } ?: ACTIVE
    }
}
