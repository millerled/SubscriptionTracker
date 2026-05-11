package com.subscriptiontracker.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.subscriptiontracker.data.local.AppDatabase
import com.subscriptiontracker.data.local.entity.PaymentHistoryEntity
import com.subscriptiontracker.data.repository.SubscriptionRepository
import com.subscriptiontracker.domain.model.Subscription
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = SubscriptionRepository(
        database.subscriptionDao(),
        database.paymentHistoryDao()
    )

    private val _subscription = MutableStateFlow<Subscription?>(null)
    val subscription: StateFlow<Subscription?> = _subscription

    private val _paymentHistory = MutableStateFlow<List<PaymentHistoryEntity>>(emptyList())
    val paymentHistory: StateFlow<List<PaymentHistoryEntity>> = _paymentHistory

    private val _showEditor = MutableStateFlow(false)
    val showEditor: StateFlow<Boolean> = _showEditor

    private var loadJob: Job? = null

    fun loadSubscription(id: Long) {
        loadJob?.cancel()
        _paymentHistory.value = emptyList()
        loadJob = viewModelScope.launch {
            val sub = repository.getById(id)
            _subscription.value = sub
            if (sub != null) {
                repository.getPaymentHistory(sub.id).collect { history ->
                    _paymentHistory.value = history
                }
            }
        }
    }

    fun toggleEditor() {
        _showEditor.value = !_showEditor.value
    }
}
