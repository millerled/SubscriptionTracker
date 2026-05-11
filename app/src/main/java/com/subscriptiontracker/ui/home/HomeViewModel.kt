package com.subscriptiontracker.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.subscriptiontracker.data.local.AppDatabase
import com.subscriptiontracker.data.repository.SubscriptionRepository
import com.subscriptiontracker.domain.model.Subscription
import com.subscriptiontracker.domain.model.SubscriptionStatus
import kotlinx.coroutines.Job
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

    // Confirmation dialog
    private val _pendingConfirmations = MutableStateFlow<List<PendingSubscription>>(emptyList())
    val pendingConfirmations: StateFlow<List<PendingSubscription>> = _pendingConfirmations

    private val _dialogCurrentIndex = MutableStateFlow(0)
    val dialogCurrentIndex: StateFlow<Int> = _dialogCurrentIndex

    private val _actionSheetSubscription = MutableStateFlow<Subscription?>(null)
    val actionSheetSubscription: StateFlow<Subscription?> = _actionSheetSubscription

    private val _heatmapData = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val heatmapData: StateFlow<Map<LocalDate, Int>> = _heatmapData

    // Delete
    private val _subscriptionToDelete = MutableStateFlow<Subscription?>(null)
    val subscriptionToDelete: StateFlow<Subscription?> = _subscriptionToDelete

    fun showActionSheet(subscription: Subscription) {
        _actionSheetSubscription.value = subscription
    }

    fun dismissActionSheet() {
        _actionSheetSubscription.value = null
    }

    fun pinSubscription() {
        val sub = _actionSheetSubscription.value ?: return
        viewModelScope.launch {
            repository.pinSubscription(sub.id)
            _actionSheetSubscription.value = null
        }
    }

    fun unpinSubscription() {
        val sub = _actionSheetSubscription.value ?: return
        viewModelScope.launch {
            repository.unpinSubscription(sub.id)
            _actionSheetSubscription.value = null
        }
    }

    fun deleteFromActionSheet() {
        val sub = _actionSheetSubscription.value ?: return
        _actionSheetSubscription.value = null
        _subscriptionToDelete.value = sub
    }

    init {
        loadStats()
        checkDueSubscriptions()
        loadHeatmap()
    }

    private fun loadHeatmap() {
        viewModelScope.launch {
            _heatmapData.value = repository.getActivityHeatmapData()
        }
    }

    private fun loadStats() {
        val now = LocalDate.now()
        val monthStart = now.withDayOfMonth(1)
        val monthEnd = now.withDayOfMonth(now.lengthOfMonth())
        val monthStartEpoch = monthStart.toEpochDay()
        val monthEndEpoch = monthEnd.toEpochDay()
        val epochRange = monthStartEpoch..monthEndEpoch

        viewModelScope.launch {
            combine(
                allSubscriptions,
                repository.getRenewedPaymentsInRange(monthStartEpoch, monthEndEpoch)
            ) { subs, payments ->
                val total = subs.sumOf { it.amount }
                _monthlyTotal.value = total
                val totalDurationDays = subs.sumOf {
                    val duration = it.deadlineDate.toEpochDay() - it.startDate.toEpochDay()
                    if (duration > 0) duration else 0L
                }
                _dailyAverage.value = if (totalDurationDays > 0) total / totalDurationDays else 0.0
                _paidAmount.value = payments.sumOf { it.amount }
                _pendingAmount.value = subs
                    .filter { it.deadlineDate.toEpochDay() in epochRange }
                    .filter { it.status == SubscriptionStatus.ACTIVE }
                    .sumOf { it.amount }
                _activeCount.value = subs.count { it.status == SubscriptionStatus.ACTIVE }
                _pausedCount.value = subs.count { it.status == SubscriptionStatus.PAUSED }
                _renewingCount.value = subs.count { it.status == SubscriptionStatus.RENEWING }
                _expiringCount.value = subs.count { it.status == SubscriptionStatus.EXPIRING }
            }.collect {}
        }
    }

    private fun checkDueSubscriptions() {
        viewModelScope.launch {
            val due = repository.getDueWithinDays(7)
                .filter { it.status == SubscriptionStatus.ACTIVE }
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

            when (choice) {
                PaymentChoice.RENEW -> {
                    val cycleDays = sub.billingCycle.cycleDays
                    val newDeadline = if (cycleDays > 0)
                        sub.deadlineDate.plusDays(cycleDays)
                    else sub.deadlineDate
                    repository.update(sub.copy(
                        status = SubscriptionStatus.RENEWING,
                        deadlineDate = newDeadline
                    ))
                }
                PaymentChoice.CANCEL -> {
                    repository.update(sub.copy(status = SubscriptionStatus.EXPIRING))
                }
                PaymentChoice.UNDECIDED -> { /* status stays ACTIVE */ }
            }

            if (index + 1 >= pending.size) {
                _pendingConfirmations.value = emptyList()
                _dialogCurrentIndex.value = 0
            } else {
                _dialogCurrentIndex.value = index + 1
            }
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
