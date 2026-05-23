package com.kahavanu.ui.income.components

import com.kahavanu.ui.util.dueLabel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CurrencyBitcoin
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector
import android.text.format.DateUtils
import java.util.Locale

// formatAmount / formatDate / currentMonthLabel moved to ui/common
// (MoneyFormat.kt and DateFormat.kt). Re-import from there.

/** Resolves an icon for a known income source by name. */
fun sourceIconFor(name: String): ImageVector =
    when (name.trim().lowercase(Locale.getDefault())) {
        "salary" -> Icons.Outlined.AccountBalanceWallet
        "freelance" -> Icons.Outlined.WorkOutline
        "adsense" -> Icons.Outlined.Public
        "crypto" -> Icons.Outlined.CurrencyBitcoin
        else -> Icons.Outlined.AccountBalanceWallet
    }

fun isPending(sourceType: String?): Boolean = sourceType == "pending"

fun isRecurrent(sourceType: String?): Boolean = sourceType == "recurrent"

fun isPersistent(sourceType: String?): Boolean = isPending(sourceType) || isRecurrent(sourceType)

fun isOverdue(epochMillis: Long): Boolean {
    val now = System.currentTimeMillis()
    return epochMillis < (now - DateUtils.DAY_IN_MILLIS)
}

/** @deprecated Use `com.kahavanu.ui.util.dueLabel` instead. Kept as a thin alias for now. */
fun getDueText(epochMillis: Long): String = com.kahavanu.ui.util.dueLabel(epochMillis)
