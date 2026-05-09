package com.subscriptiontracker.data.repository

import com.subscriptiontracker.data.local.dao.PaymentHistoryDao
import com.subscriptiontracker.data.local.dao.SubscriptionDao
import com.subscriptiontracker.data.local.entity.PaymentHistoryEntity
import com.subscriptiontracker.data.local.entity.SubscriptionEntity
import com.subscriptiontracker.domain.model.BillingCycle
import com.subscriptiontracker.domain.model.Intention
import com.subscriptiontracker.domain.model.SortOption
import com.subscriptiontracker.domain.model.Subscription
import com.subscriptiontracker.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class SubscriptionRepository(
    private val dao: SubscriptionDao,
    private val historyDao: PaymentHistoryDao
) {

    fun getSubscriptions(sortOption: SortOption): Flow<List<Subscription>> {
        val flow = when (sortOption) {
            SortOption.BY_DEADLINE -> dao.getAllByDeadline()
            SortOption.BY_DATE_ADDED -> dao.getAllByDateAdded()
            SortOption.CUSTOM -> dao.getAllByCustomOrder()
            SortOption.BY_STATUS -> dao.getAllByDeadline()
        }
        return flow.map { entities -> entities.map { it.toDomain() } }
    }

    fun getByStatus(status: SubscriptionStatus): Flow<List<Subscription>> =
        dao.getByStatus(status.name).map { entities -> entities.map { it.toDomain() } }

    suspend fun getById(id: Long): Subscription? =
        dao.getById(id)?.toDomain()

    suspend fun getDueWithinDays(days: Int): List<Subscription> {
        val today = LocalDate.now()
        val expiring = today.plusDays(days.toLong())
        return dao.getDueWithinDays(today.toEpochDay(), expiring.toEpochDay())
            .map { it.toDomain() }
    }

    suspend fun insert(subscription: Subscription): Long =
        dao.insert(subscription.toEntity())

    suspend fun update(subscription: Subscription) =
        dao.update(subscription.toEntity())

    suspend fun delete(subscription: Subscription) =
        dao.delete(subscription.toEntity())

    suspend fun recordPayment(
        subscriptionId: Long,
        amount: Double,
        action: String
    ) {
        historyDao.insert(
            PaymentHistoryEntity(
                subscriptionId = subscriptionId,
                paymentDate = LocalDate.now().toEpochDay(),
                amount = amount,
                action = action
            )
        )
    }

    fun getPaymentHistory(subscriptionId: Long): Flow<List<PaymentHistoryEntity>> =
        historyDao.getBySubscription(subscriptionId)

    fun getRenewedPaymentsInRange(startDate: Long, endDate: Long): Flow<List<PaymentHistoryEntity>> =
        historyDao.getRenewedInRange(startDate, endDate)

    fun getAllPaymentsInRange(startDate: Long, endDate: Long): Flow<List<PaymentHistoryEntity>> =
        historyDao.getAllInRange(startDate, endDate)

    private fun SubscriptionEntity.toDomain() = Subscription(
        id = id,
        name = name,
        amount = amount,
        billingCycle = BillingCycle.fromString(billingCycle),
        deadlineDate = LocalDate.ofEpochDay(deadlineDate),
        category = category,
        status = SubscriptionStatus.fromString(status),
        autoRenew = autoRenew,
        intention = Intention.fromString(intention),
        logoUri = logoUri,
        wallpaperUri = wallpaperUri,
        notes = notes,
        createdAt = createdAt,
        sortOrder = sortOrder
    )

    private fun Subscription.toEntity() = SubscriptionEntity(
        id = id,
        name = name,
        amount = amount,
        billingCycle = billingCycle.name,
        deadlineDate = deadlineDate.toEpochDay(),
        category = category,
        status = status.name,
        autoRenew = autoRenew,
        intention = intention.name,
        logoUri = logoUri,
        wallpaperUri = wallpaperUri,
        notes = notes,
        createdAt = createdAt,
        sortOrder = sortOrder
    )
}
