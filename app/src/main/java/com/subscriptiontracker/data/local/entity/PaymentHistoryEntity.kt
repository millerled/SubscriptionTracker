package com.subscriptiontracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment_history",
    foreignKeys = [
        ForeignKey(
            entity = SubscriptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["subscriptionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subscriptionId")]
)
data class PaymentHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subscriptionId: Long,
    val paymentDate: Long,
    val amount: Double,
    val action: String,
    val createdAt: Long = System.currentTimeMillis()
)
