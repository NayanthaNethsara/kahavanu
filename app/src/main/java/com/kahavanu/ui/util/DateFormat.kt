package com.kahavanu.ui.util

import android.text.format.DateUtils
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

/** Short formatted date for log lists. Example: `"Jan 5, 2026"`. */
fun formatDate(epochMillis: Long): String {
    val date = Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    return "$month ${date.dayOfMonth}, ${date.year}"
}

/** Short month label for the current month. Example: `"May"`. */
fun currentMonthLabel(): String {
    val month = YearMonth.now()
    return month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
}

/**
 * Human-readable description of a due date relative to today.
 * Negative deltas read as past-due; positive read as upcoming.
 */
fun dueLabel(epochMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = epochMillis - now
    val days = (diff / DateUtils.DAY_IN_MILLIS).toInt()
    return when {
        days < -1 -> "Due ${-days} days ago"
        days == -1 -> "Due yesterday"
        days == 0 -> "Due today"
        days == 1 -> "Due tomorrow"
        days > 1 -> "Due in $days days"
        else -> "Due today"
    }
}
