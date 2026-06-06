package com.kahavanu.ui.util

object CurrencyConverter {

    private val ratesInBase: Map<String, Double> = mapOf(
        "LKR" to 1.0,
        "USD" to 300.0,
        "EUR" to 330.0,
        "GBP" to 385.0,
        "AUD" to 200.0,
        "JPY" to 2.0,
        "INR" to 3.6,
        "CAD" to 220.0,
    )

    fun convert(amount: Double, fromCode: String, toCode: String): Double {
        if (amount == 0.0 || fromCode.equals(toCode, ignoreCase = true)) return amount
        val from = ratesInBase[fromCode.uppercase()] ?: return amount
        val to = ratesInBase[toCode.uppercase()] ?: return amount
        return amount * (from / to)
    }
}
