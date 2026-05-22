package com.kahavanu.domain.model

data class Subscription(
    val id: String,
    val name: String,
    val cost: Double,
    val currency: String = "USD",
    val frequency: String = "monthly", // "monthly" or "yearly"
    val nextBillingDate: String,
    val isPaused: Boolean = false,
    val category: String = "subscriptions"
)
