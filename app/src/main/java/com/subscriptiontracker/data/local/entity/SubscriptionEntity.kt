package com.subscriptiontracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val billingCycle: String,
    val deadlineDate: Long,
    val category: String,
    val status: String,
    val autoRenew: Boolean = false,
    val intention: String = "UNDECIDED",
    val logoUri: String? = null,
    val wallpaperUri: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)
