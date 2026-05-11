package com.subscriptiontracker.util

import java.text.NumberFormat
import java.util.Locale

private val currencyFormat by lazy { NumberFormat.getCurrencyInstance(Locale.CHINA) }

fun formatCurrency(amount: Double): String = currencyFormat.format(amount)

fun formatAmountPerCycle(amount: Double, cycleDisplayName: String): String =
    "${formatCurrency(amount)} / ${cycleDisplayName}"
