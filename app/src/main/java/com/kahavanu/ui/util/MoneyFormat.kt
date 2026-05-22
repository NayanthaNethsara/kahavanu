package com.kahavanu.ui.util

import java.util.Locale

/**
 * Formats a monetary amount with thousands separators.
 * Example: `formatAmount(1234.5, "LKR")` -> `"LKR 1,234.50"`.
 * Pass `decimals = 0` for whole-number display.
 */
fun formatAmount(amount: Double, currency: String, decimals: Int = 2): String {
    val pattern = "%,.${decimals}f"
    val formatted = String.format(Locale.getDefault(), pattern, amount)
    return "$currency $formatted"
}
