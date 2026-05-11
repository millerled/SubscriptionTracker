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
    val autoRenew: Boolean = false,
    val intention: Intention = Intention.UNDECIDED,
    val logoUri: String? = null,
    val wallpaperUri: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
    val startDate: LocalDate = LocalDate.now(),
    val modifiedAt: Long = 0L
)
