package com.subscriptiontracker.ui.addedit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.subscriptiontracker.data.local.AppDatabase
import com.subscriptiontracker.data.repository.SubscriptionRepository
import com.subscriptiontracker.domain.model.BillingCycle
import com.subscriptiontracker.domain.model.Intention
import com.subscriptiontracker.domain.model.Subscription
import com.subscriptiontracker.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddEditFormState(
    val name: String = "",
    val amount: String = "",
    val billingCycle: BillingCycle = BillingCycle.MONTHLY,
    val deadlineDate: LocalDate = LocalDate.now().plusMonths(1),
    val category: String = "",
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val autoRenew: Boolean = false,
    val intention: Intention = Intention.UNDECIDED,
    val logoUri: String? = null,
    val wallpaperUri: String? = null,
    val notes: String = "",
    val nameError: String? = null,
    val amountError: String? = null,
    val categoryError: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

class AddEditViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = SubscriptionRepository(
        database.subscriptionDao(),
        database.paymentHistoryDao()
    )

    private val _formState = MutableStateFlow(AddEditFormState())
    val formState: StateFlow<AddEditFormState> = _formState.asStateFlow()

    private var editingId: Long = 0

    fun resetForNew() {
        editingId = 0
        _formState.value = AddEditFormState()
    }

    fun loadSubscription(id: Long) {
        if (id == 0L) return
        editingId = id
        viewModelScope.launch {
            val subscription = repository.getById(id) ?: return@launch
            _formState.value = AddEditFormState(
                name = subscription.name,
                amount = subscription.amount.toBigDecimal().stripTrailingZeros().toPlainString(),
                billingCycle = subscription.billingCycle,
                deadlineDate = subscription.deadlineDate,
                category = subscription.category,
                status = subscription.status,
                autoRenew = subscription.autoRenew,
                intention = subscription.intention,
                logoUri = subscription.logoUri,
                wallpaperUri = subscription.wallpaperUri,
                notes = subscription.notes ?: ""
            )
        }
    }

    fun updateName(name: String) {
        _formState.value = _formState.value.copy(name = name, nameError = null)
    }

    fun updateAmount(amount: String) {
        _formState.value = _formState.value.copy(amount = amount, amountError = null)
    }

    fun updateBillingCycle(cycle: BillingCycle) {
        _formState.value = _formState.value.copy(billingCycle = cycle)
    }

    fun updateDeadlineDate(date: LocalDate) {
        _formState.value = _formState.value.copy(deadlineDate = date)
    }

    fun updateCategory(category: String) {
        _formState.value = _formState.value.copy(category = category, categoryError = null)
    }

    fun updateStatus(status: SubscriptionStatus) {
        _formState.value = _formState.value.copy(status = status)
    }

    fun updateAutoRenew(autoRenew: Boolean) {
        _formState.value = _formState.value.copy(autoRenew = autoRenew)
    }

    fun updateIntention(intention: Intention) {
        _formState.value = _formState.value.copy(intention = intention)
    }

    fun updateNotes(notes: String) {
        _formState.value = _formState.value.copy(notes = notes)
    }

    fun save() {
        val state = _formState.value
        var hasError = false
        var newState = state

        if (state.name.isBlank()) {
            newState = newState.copy(nameError = "请输入订阅名称")
            hasError = true
        }

        val amount = state.amount.toDoubleOrNull()
        if (state.amount.isBlank() || amount == null || amount <= 0) {
            newState = newState.copy(amountError = "请输入有效金额")
            hasError = true
        }

        if (state.category.isBlank()) {
            newState = newState.copy(categoryError = "请选择分类")
            hasError = true
        }

        if (hasError) {
            _formState.value = newState
            return
        }

        _formState.value = newState.copy(isSaving = true)

        val subscription = Subscription(
            id = editingId,
            name = state.name.trim(),
            amount = amount!!,
            billingCycle = state.billingCycle,
            deadlineDate = state.deadlineDate,
            category = state.category.trim(),
            status = state.status,
            autoRenew = state.autoRenew,
            intention = state.intention,
            logoUri = state.logoUri,
            wallpaperUri = state.wallpaperUri,
            notes = state.notes.ifBlank { null }
        )

        viewModelScope.launch {
            if (editingId == 0L) {
                repository.insert(subscription)
            } else {
                repository.update(subscription)
            }
            _formState.value = _formState.value.copy(isSaving = false, saveSuccess = true)
        }
    }
}
