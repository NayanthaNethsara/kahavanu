package com.kahavanu.ui.income.components

import android.text.format.DateUtils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector
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

fun sourceIconFor(name: String): ImageVector {
    return when (name.trim().lowercase(Locale.getDefault())) {
        "salary" -> Icons.Outlined.AccountBalanceWallet
        "freelance" -> Icons.Outlined.WorkOutline
        "adsense" -> Icons.Outlined.Public
        "crypto" -> Icons.Outlined.CurrencyBitcoin
        else -> Icons.Outlined.AccountBalanceWallet
    }
}

fun isPending(note: String?): Boolean {
    return note?.startsWith("Pending", ignoreCase = true) == true
}

fun isOverdue(epochMillis: Long): Boolean {
    val now = System.currentTimeMillis()
    // A payment is overdue if it's pending and the expected date is in the past (more than a day)
    return epochMillis < (now - DateUtils.DAY_IN_MILLIS)
}

fun getDueText(epochMillis: Long): String {
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
