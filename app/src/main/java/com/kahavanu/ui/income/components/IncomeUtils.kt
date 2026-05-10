package com.kahavanu.ui.income.components

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
