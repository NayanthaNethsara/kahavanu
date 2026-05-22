package com.kahavanu.domain.model

enum class CurrencyOption(
    val code: String,
    val symbol: String,
    val fullName: String
) {
    LKR("LKR", "Rs", "Sri Lankan Rupee"),
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "€", "Euro"),
    GBP("GBP", "£", "British Pound"),
    AUD("AUD", "A$", "Australian Dollar"),
    JPY("JPY", "¥", "Japanese Yen"),
    INR("INR", "₹", "Indian Rupee"),
    CAD("CAD", "C$", "Canadian Dollar"),
}
