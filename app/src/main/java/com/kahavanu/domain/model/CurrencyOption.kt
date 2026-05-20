package com.kahavanu.domain.model

enum class CurrencyOption(val code: String, val symbol: String) {
    LKR("LKR", "Rs."),
    USD("USD", "$"),
    EUR("EUR", "€"),
    GBP("GBP", "£"),
    AUD("AUD", "A$"),
    JPY("JPY", "¥"),
    INR("INR", "₹"),
    CAD("CAD", "C$"),
}
