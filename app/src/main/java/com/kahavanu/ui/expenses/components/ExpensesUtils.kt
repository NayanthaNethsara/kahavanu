package com.kahavanu.ui.expenses.components

import android.text.format.DateUtils
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

fun formatAmount(amount: Double, currency: String): String {
    val formatted = String.format(Locale.getDefault(), "%,.2f", amount)
    return "$currency $formatted"
}

fun formatDate(epochMillis: Long): String {
    val date = Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    return "$month ${date.dayOfMonth}, ${date.year}"
}

fun currentMonthLabel(): String {
    val month = YearMonth.now()
    return month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
}

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
