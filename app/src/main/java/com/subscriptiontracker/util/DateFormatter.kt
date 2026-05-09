package com.subscriptiontracker.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val shortFormatter = DateTimeFormatter.ofPattern("MM/dd")
private val fullFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")

fun LocalDate.formatShort(): String =
    this.format(shortFormatter)

fun LocalDate.formatFull(): String =
    this.format(fullFormatter)

fun LocalDate.daysUntil(): Long =
    ChronoUnit.DAYS.between(LocalDate.now(), this)

fun LocalDate.daysSince(): Long =
    ChronoUnit.DAYS.between(this, LocalDate.now())

fun LocalDate.remainingDaysText(): String {
    val days = this.daysUntil()
    return when {
        days > 0 -> "还剩 ${days} 天"
        days == 0L -> "今天到期"
        else -> "已到期 ${-days} 天"
    }
}
