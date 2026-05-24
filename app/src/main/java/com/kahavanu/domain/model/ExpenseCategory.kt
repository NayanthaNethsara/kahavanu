package com.kahavanu.domain.model

object ExpenseCategory {
    const val FOOD = "Food"
    const val TRANSPORT = "Transport"
    const val UTILITIES = "Utilities"
    const val SHOPPING = "Shopping"
    const val HEALTH = "Health"
    const val FUN = "Fun"
    const val RENT = "Rent"
    const val OTHER = "Other"

    val ALL = listOf(
        FOOD,
        TRANSPORT,
        UTILITIES,
        SHOPPING,
        HEALTH,
        FUN,
        RENT,
        OTHER
    )
}
