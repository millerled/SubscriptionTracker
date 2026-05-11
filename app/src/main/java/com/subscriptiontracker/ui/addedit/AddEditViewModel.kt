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
import com.subscriptiontracker.ui.components.PresetLogo
import com.subscriptiontracker.util.formatFull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class AddEditFormState(
    val name: String = "",
    val amount: String = "",
    val billingCycle: BillingCycle = BillingCycle.MONTHLY,
    val startDate: LocalDate = LocalDate.now(),
    val startDateText: String = LocalDate.now().formatFull(),
    val startDateError: String? = null,
    val deadlineDate: LocalDate = LocalDate.now().plusMonths(1),
    val deadlineDateText: String = LocalDate.now().plusMonths(1).formatFull(),
    val deadlineDateError: String? = null,
    val deadlineOverridden: Boolean = false,
    val category: String = "",
    val customCategory: String = "",
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
        val today = LocalDate.now()
        _formState.value = AddEditFormState(
            startDateText = today.formatFull(),
            deadlineDateText = today.plusMonths(1).formatFull()
        )
    }

    fun loadSubscription(id: Long) {
        if (id == 0L) return
        editingId = id
        viewModelScope.launch {
            val subscription = repository.getById(id) ?: return@launch
            val computedDeadline = subscription.startDate.plusDays(subscription.billingCycle.cycleDays)
            val isOverridden = subscription.deadlineDate != computedDeadline ||
                subscription.billingCycle == BillingCycle.ONE_TIME
            _formState.value = AddEditFormState(
                name = subscription.name,
                amount = subscription.amount.toBigDecimal().stripTrailingZeros().toPlainString(),
                billingCycle = subscription.billingCycle,
                startDate = subscription.startDate,
                startDateText = subscription.startDate.formatFull(),
                deadlineDate = subscription.deadlineDate,
                deadlineDateText = subscription.deadlineDate.formatFull(),
                deadlineOverridden = isOverridden,
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
        val current = _formState.value
        val newDeadline = recomputeDeadline(current.startDate, cycle.cycleDays, current.deadlineDate, current.deadlineOverridden)
        _formState.value = current.copy(
            billingCycle = cycle,
            deadlineDate = newDeadline,
            deadlineDateText = newDeadline.formatFull(),
            deadlineDateError = null
        )
    }

    fun updateStartDate(date: LocalDate) {
        val current = _formState.value
        val newDeadline = recomputeDeadline(date, current.billingCycle.cycleDays, current.deadlineDate, current.deadlineOverridden)
        _formState.value = current.copy(
            startDate = date,
            startDateText = date.formatFull(),
            startDateError = null,
            deadlineDate = newDeadline,
            deadlineDateText = newDeadline.formatFull(),
            deadlineDateError = null
        )
    }

    fun updateStartDateText(text: String) {
        val current = _formState.value
        val trimmed = text.trim()
        val parsed = parseDateInput(trimmed)
        if (parsed != null) {
            updateStartDate(parsed)
        } else {
            _formState.value = current.copy(
                startDateText = trimmed,
                startDateError = if (trimmed.isNotEmpty()) "格式: 2026-05-11 或 5月11日" else null
            )
        }
    }

    fun updateDeadlineDateText(text: String) {
        val current = _formState.value
        if (current.billingCycle == BillingCycle.ONE_TIME) return
        val trimmed = text.removeSuffix(" (已手动修改)").trim()
        val parsed = parseDateInput(trimmed)
        if (parsed != null) {
            updateDeadlineDate(parsed)
        } else {
            _formState.value = current.copy(
                deadlineDateText = trimmed,
                deadlineDateError = if (trimmed.isNotEmpty()) "格式: 2026-05-11 或 5月11日" else null
            )
        }
    }

    private fun parseDateInput(input: String): LocalDate? {
        if (input.isBlank()) return null
        val trimmed = input.trim()
        // Try ISO format: 2026-05-11 or 2026/05/11
        try {
            return LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: DateTimeParseException) {}
        try {
            return LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        } catch (_: DateTimeParseException) {}
        try {
            return LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("yyyyMMdd"))
        } catch (_: DateTimeParseException) {}
        // Try Chinese format: 2026年5月11日
        try {
            return LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("yyyy年M月d日"))
        } catch (_: DateTimeParseException) {}
        // Try: 5月11日 (use current year)
        try {
            val formatter = DateTimeFormatter.ofPattern("M月d日")
            val parsed = LocalDate.parse(trimmed, formatter)
            return parsed.withYear(LocalDate.now().year)
        } catch (_: DateTimeParseException) {}
        return null
    }

    private fun recomputeDeadline(startDate: LocalDate, cycleDays: Long, currentDeadline: LocalDate, overridden: Boolean): LocalDate =
        if (!overridden) startDate.plusDays(cycleDays) else currentDeadline

    fun updateDeadlineDate(date: LocalDate) {
        _formState.value = _formState.value.copy(
            deadlineDate = date,
            deadlineDateText = date.formatFull(),
            deadlineDateError = null,
            deadlineOverridden = true
        )
    }

    fun onPresetSelected(preset: PresetLogo) {
        _formState.value = _formState.value.copy(
            logoUri = "preset:${preset.id}",
            category = preset.category,
            customCategory = if (preset.id == "other") _formState.value.customCategory else "",
            categoryError = null
        )
    }

    fun updateCustomCategory(custom: String) {
        _formState.value = _formState.value.copy(customCategory = custom, categoryError = null)
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

    fun updateLogoUri(uri: String?) {
        _formState.value = _formState.value.copy(logoUri = uri)
    }

    fun updateWallpaperUri(uri: String?) {
        _formState.value = _formState.value.copy(wallpaperUri = uri)
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

        val effectiveCategory = if (state.category.isBlank()) state.customCategory.trim() else state.category
        if (effectiveCategory.isBlank()) {
            newState = newState.copy(categoryError = "请选择或输入分类")
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
            category = effectiveCategory.trim(),
            status = state.status,
            autoRenew = state.autoRenew,
            intention = state.intention,
            logoUri = state.logoUri,
            wallpaperUri = state.wallpaperUri,
            notes = state.notes.ifBlank { null },
            startDate = state.startDate,
            modifiedAt = System.currentTimeMillis()
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
