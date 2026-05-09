package com.subscriptiontracker.domain.model

import java.time.LocalDate

data class Subscription(
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val billingCycle: BillingCycle,
    val deadlineDate: LocalDate,
    val category: String,
    val status: SubscriptionStatus,
    val logoUri: String? = null,
    val notes: String? = null,
    val intention: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)
