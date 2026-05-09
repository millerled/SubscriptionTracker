package com.subscriptiontracker.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.subscriptiontracker.data.local.AppDatabase
import com.subscriptiontracker.data.repository.SubscriptionRepository
import com.subscriptiontracker.domain.model.SortOption
import com.subscriptiontracker.domain.model.Subscription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = SubscriptionRepository(database.subscriptionDao())

    private val _sortOption = MutableStateFlow(SortOption.BY_STATUS)
    val sortOption: StateFlow<SortOption> = _sortOption

    val subscriptions: StateFlow<List<Subscription>> = _sortOption
        .flatMapLatest { repository.getSubscriptions(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _subscriptionToDelete = MutableStateFlow<Subscription?>(null)
    val subscriptionToDelete: StateFlow<Subscription?> = _subscriptionToDelete

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
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
