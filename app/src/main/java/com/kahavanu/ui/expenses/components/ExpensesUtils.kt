package com.kahavanu.ui.expenses.components

import android.text.format.DateUtils
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.kahavanu.ui.theme.RawColors
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

fun categoryColor(category: String): Color {
    return when (category.trim().lowercase()) {
        "food", "essentials" -> Color(0xFFF97316)
        "transport" -> RawColors.Blue.Blue500
        "utilities" -> RawColors.Violet.Violet500
        "shopping", "lifestyle" -> RawColors.Rose.Rose500
        "health" -> RawColors.Red.Red500
        "fun", "subscriptions" -> RawColors.Emerald.Emerald500
        else -> RawColors.Slate.Slate400
    }
}

fun categoryIcon(category: String): ImageVector {
    return when (category.trim().lowercase()) {
        "food", "essentials" -> Icons.Outlined.LocalPizza
        "transport" -> Icons.Outlined.LocalTaxi
        "utilities" -> Icons.Outlined.Bolt
        "shopping", "lifestyle" -> Icons.Outlined.ShoppingBag
        "health" -> Icons.Outlined.LocalHospital
        "fun", "subscriptions" -> Icons.Outlined.SportsEsports
        else -> Icons.Outlined.TipsAndUpdates
    }
}

fun formatAmountNoDecimals(amount: Double, currencyCode: String): String {
    return "$currencyCode ${String.format(java.util.Locale.getDefault(), "%,.0f", amount)}"
}
