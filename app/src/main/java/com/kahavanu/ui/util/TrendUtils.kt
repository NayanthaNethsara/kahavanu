package com.kahavanu.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

/**
 * Buckets a list of (epochMillis, amount) entries into one daily total per day for the
 * last [days] days, oldest first. Used to drive the insight trend charts from real data.
 */
fun dailyTotals(entries: List<Pair<Long, Double>>, days: Int = 7): List<Float> {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    return (days - 1 downTo 0).map { ago ->
        val day = today.minusDays(ago.toLong())
        entries
            .filter { Instant.ofEpochMilli(it.first).atZone(zone).toLocalDate() == day }
            .sumOf { it.second }
            .toFloat()
    }
}

/**
 * Short weekday labels (e.g. "Mon"…"Sun") for the last [days] days, oldest first, ending today.
 * Aligns 1:1 with the buckets from [dailyTotals] so the trend chart can label each point.
 */
fun lastNDayLabels(days: Int = 7): List<String> {
    val today = LocalDate.now(ZoneId.systemDefault())
    return (days - 1 downTo 0).map { ago ->
        today.minusDays(ago.toLong())
            .dayOfWeek
            .getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }
}
