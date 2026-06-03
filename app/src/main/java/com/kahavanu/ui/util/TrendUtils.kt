package com.kahavanu.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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
