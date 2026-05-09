package com.subscriptiontracker.data.repository

import com.subscriptiontracker.data.local.dao.SubscriptionDao
import com.subscriptiontracker.data.local.entity.SubscriptionEntity
import com.subscriptiontracker.domain.model.BillingCycle
import com.subscriptiontracker.domain.model.SortOption
import com.subscriptiontracker.domain.model.Subscription
import com.subscriptiontracker.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class SubscriptionRepository(private val dao: SubscriptionDao) {

    fun getSubscriptions(sortOption: SortOption): Flow<List<Subscription>> {
        val flow = when (sortOption) {
            SortOption.BY_STATUS -> dao.getAllByStatus()
            SortOption.BY_DEADLINE -> dao.getAllByDeadline()
            SortOption.BY_DATE_ADDED -> dao.getAllByDateAdded()
            SortOption.CUSTOM -> dao.getAllByCustomOrder()
        }
        return flow.map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun getById(id: Long): Subscription? =
        dao.getById(id)?.toDomain()

    suspend fun insert(subscription: Subscription): Long =
        dao.insert(subscription.toEntity())

    suspend fun update(subscription: Subscription) =
        dao.update(subscription.toEntity())

    suspend fun delete(subscription: Subscription) =
        dao.delete(subscription.toEntity())

    private fun SubscriptionEntity.toDomain() = Subscription(
        id = id,
        name = name,
        amount = amount,
        billingCycle = BillingCycle.fromString(billingCycle),
        deadlineDate = LocalDate.ofEpochDay(deadlineDate),
        category = category,
        status = SubscriptionStatus.fromString(status),
        logoUri = logoUri,
        notes = notes,
        intention = intention,
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
        logoUri = logoUri,
        notes = notes,
        intention = intention,
        createdAt = createdAt,
        sortOrder = sortOrder
    )
}
