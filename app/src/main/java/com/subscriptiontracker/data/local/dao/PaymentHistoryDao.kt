package com.subscriptiontracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.subscriptiontracker.data.local.entity.PaymentHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentHistoryDao {

    @Query("SELECT * FROM payment_history WHERE subscriptionId = :subscriptionId ORDER BY paymentDate DESC")
    fun getBySubscription(subscriptionId: Long): Flow<List<PaymentHistoryEntity>>

    @Query("SELECT * FROM payment_history WHERE paymentDate >= :startDate AND paymentDate <= :endDate AND action = 'RENEWED'")
    fun getRenewedInRange(startDate: Long, endDate: Long): Flow<List<PaymentHistoryEntity>>

    @Query("SELECT * FROM payment_history WHERE paymentDate >= :startDate AND paymentDate <= :endDate")
    fun getAllInRange(startDate: Long, endDate: Long): Flow<List<PaymentHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PaymentHistoryEntity): Long
}
