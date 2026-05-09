package com.subscriptiontracker.domain.model

enum class SortOption(val displayName: String) {
    BY_STATUS("按状态"),
    BY_DEADLINE("按到期日"),
    BY_DATE_ADDED("按加入时间"),
    CUSTOM("自定义");
}
