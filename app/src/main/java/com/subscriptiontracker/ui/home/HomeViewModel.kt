package com.subscriptiontracker.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.subscriptiontracker.data.local.AppDatabase
import com.subscriptiontracker.data.repository.SubscriptionRepository
import com.subscriptiontracker.domain.model.Subscription
import com.subscriptiontracker.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = SubscriptionRepository(
        database.subscriptionDao(),
        database.paymentHistoryDao()
    )

    private val _activeFilter = MutableStateFlow<SubscriptionStatus?>(null)
    val activeFilter: StateFlow<SubscriptionStatus?> = _activeFilter

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val subscriptions: StateFlow<List<Subscription>> = _activeFilter
        .flatMapLatest { filter ->
            if (filter != null) repository.getByStatus(filter)
            else repository.getSubscriptions(com.subscriptiontracker.domain.model.SortOption.BY_DEADLINE)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubscriptions: StateFlow<List<Subscription>> = repository
        .getSubscriptions(com.subscriptiontracker.domain.model.SortOption.BY_DEADLINE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stats
    private val _monthlyTotal = MutableStateFlow(0.0)
    val monthlyTotal: StateFlow<Double> = _monthlyTotal

    private val _dailyAverage = MutableStateFlow(0.0)
    val dailyAverage: StateFlow<Double> = _dailyAverage

    private val _paidAmount = MutableStateFlow(0.0)
    val paidAmount: StateFlow<Double> = _paidAmount

    private val _pendingAmount = MutableStateFlow(0.0)
    val pendingAmount: StateFlow<Double> = _pendingAmount

    // Counts for chips
    private val _activeCount = MutableStateFlow(0)
    val activeCount: StateFlow<Int> = _activeCount

    private val _pausedCount = MutableStateFlow(0)
    val pausedCount: StateFlow<Int> = _pausedCount

    private val _renewingCount = MutableStateFlow(0)
    val renewingCount: StateFlow<Int> = _renewingCount

    private val _expiringCount = MutableStateFlow(0)
    val expiringCount: StateFlow<Int> = _expiringCount

    // Due subscriptions
    private val _dueSubscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val dueSubscriptions: StateFlow<List<Subscription>> = _dueSubscriptions

    // Confirmation dialog
    private val _pendingConfirmations = MutableStateFlow<List<PendingSubscription>>(emptyList())
    val pendingConfirmations: StateFlow<List<PendingSubscription>> = _pendingConfirmations

    private val _dialogCurrentIndex = MutableStateFlow(0)
    val dialogCurrentIndex: StateFlow<Int> = _dialogCurrentIndex

    // Delete
    private val _subscriptionToDelete = MutableStateFlow<Subscription?>(null)
    val subscriptionToDelete: StateFlow<Subscription?> = _subscriptionToDelete

    init {
        loadStats()
        checkDueSubscriptions()
    }

    private fun loadStats() {
        viewModelScope.launch {
            allSubscriptions.collect { subs ->
                val now = LocalDate.now()
                val monthStart = now.withDayOfMonth(1)
                val monthEnd = now.withDayOfMonth(now.lengthOfMonth())
                val todayEpoch = now.toEpochDay()

                // Monthly total: all subscriptions with amount
                val total = subs.sumOf { it.amount }
                _monthlyTotal.value = total
                _dailyAverage.value = total / now.lengthOfMonth()

                // Paid: confirmed renewed in this month
                repository.getRenewedPaymentsInRange(
                    monthStart.toEpochDay(),
                    monthEnd.toEpochDay()
                ).collect { payments ->
                    _paidAmount.value = payments.sumOf { it.amount }
                }

                // Pending: subs with deadline this month not yet confirmed
                _pendingAmount.value = subs
                    .filter { it.deadlineDate.toEpochDay() in monthStart.toEpochDay()..monthEnd.toEpochDay() }
                    .filter { it.status == SubscriptionStatus.ACTIVE }
                    .sumOf { it.amount }

                // Counts
                _activeCount.value = subs.count { it.status == SubscriptionStatus.ACTIVE }
                _pausedCount.value = subs.count { it.status == SubscriptionStatus.PAUSED }
                _renewingCount.value = subs.count { it.status == SubscriptionStatus.RENEWING }
                _expiringCount.value = subs.count { it.status == SubscriptionStatus.EXPIRING }
            }
        }
    }

    private fun checkDueSubscriptions() {
        viewModelScope.launch {
            val due = repository.getDueWithinDays(7)
                .filter { it.status == SubscriptionStatus.ACTIVE }
            _dueSubscriptions.value = due

            if (due.size in 1..7) {
                _pendingConfirmations.value = due.map { PendingSubscription(it) }
                _dialogCurrentIndex.value = 0
            }
        }
    }

    fun setFilter(status: SubscriptionStatus?) {
        _activeFilter.value = status
    }

    fun onPaymentChoice(choice: PaymentChoice) {
        val pending = _pendingConfirmations.value
        val index = _dialogCurrentIndex.value
        if (index >= pending.size) return

        val sub = pending[index].subscription
        viewModelScope.launch {
            val action = when (choice) {
                PaymentChoice.RENEW -> "RENEWED"
                PaymentChoice.CANCEL -> "CANCELLED"
                PaymentChoice.UNDECIDED -> "UNCERTAIN"
            }
            repository.recordPayment(sub.id, sub.amount, action)

            val newStatus = when (choice) {
                PaymentChoice.RENEW -> {
                    val cycleDays = when (sub.billingCycle) {
                        com.subscriptiontracker.domain.model.BillingCycle.MONTHLY -> 30L
                        com.subscriptiontracker.domain.model.BillingCycle.QUARTERLY -> 91L
                        com.subscriptiontracker.domain.model.BillingCycle.YEARLY -> 365L
                        com.subscriptiontracker.domain.model.BillingCycle.ONE_TIME -> 0L
                    }
                    val newDeadline = if (cycleDays > 0)
                        sub.deadlineDate.plusDays(cycleDays)
                    else sub.deadlineDate
                    repository.update(sub.copy(
                        status = SubscriptionStatus.RENEWING,
                        deadlineDate = newDeadline
                    ))
                    SubscriptionStatus.RENEWING
                }
                PaymentChoice.CANCEL -> {
                    repository.update(sub.copy(status = SubscriptionStatus.EXPIRING))
                    SubscriptionStatus.EXPIRING
                }
                PaymentChoice.UNDECIDED -> SubscriptionStatus.ACTIVE
            }
        }

        if (index + 1 >= pending.size) {
            _pendingConfirmations.value = emptyList()
            _dialogCurrentIndex.value = 0
        } else {
            _dialogCurrentIndex.value = index + 1
        }
    }

    fun dismissConfirmations() {
        _pendingConfirmations.value = emptyList()
    }

    fun showDeleteConfirmation(subscription: Subscription) {
        _subscriptionToDelete.value = subscription
    }

    fun dismissDeleteConfirmation() {
        _subscriptionToDelete.value = null
    }

    fun confirmDelete() {
        val subscription = _subscriptionToDelete.value ?: return
        viewModelScope.launch {
            repository.delete(subscription)
            _subscriptionToDelete.value = null
        }
    }
}
