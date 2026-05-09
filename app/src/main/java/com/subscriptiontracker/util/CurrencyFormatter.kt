package com.subscriptiontracker.util

import java.text.NumberFormat
import java.util.Locale

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.CHINA)
    return format.format(amount)
}

fun formatAmountPerCycle(amount: Double, cycleDisplayName: String): String =
    "${formatCurrency(amount)} / ${cycleDisplayName}"
