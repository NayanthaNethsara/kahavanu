package com.kahavanu.ui.common

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

enum class HistoryDateRange(val label: String) {
    ALL("All time"),
    THIS_WEEK("This week"),
    THIS_MONTH("This month"),
    LAST_3_MONTHS("Last 3 months"),
    THIS_YEAR("This year"),
}

/** Returns true if [epochMillis] falls within the [range] relative to today. */
fun HistoryDateRange.contains(epochMillis: Long, today: LocalDate = LocalDate.now()): Boolean {
    if (this == HistoryDateRange.ALL) return true
    val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    return when (this) {
        HistoryDateRange.ALL -> true
        HistoryDateRange.THIS_WEEK -> {
            val start = today.minusDays((today.dayOfWeek.value - 1).toLong())
            !date.isBefore(start) && !date.isAfter(today)
        }
        HistoryDateRange.THIS_MONTH -> {
            val month = YearMonth.from(today)
            !date.isBefore(month.atDay(1)) && !date.isAfter(today)
        }
        HistoryDateRange.LAST_3_MONTHS -> {
            val start = today.minusMonths(3)
            !date.isBefore(start) && !date.isAfter(today)
        }
        HistoryDateRange.THIS_YEAR -> {
            !date.isBefore(today.withDayOfYear(1)) && !date.isAfter(today)
        }
    }
}
