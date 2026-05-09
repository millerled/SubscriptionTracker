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
    val logoUri: String? = null,
    val notes: String? = null,
    val intention: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)
