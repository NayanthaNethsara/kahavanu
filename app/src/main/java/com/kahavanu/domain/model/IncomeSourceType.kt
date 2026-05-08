package com.kahavanu.domain.model

enum class IncomeSourceType(
    val id: String,
    val label: String,
    val description: String,
) {
    ONE_TIME("one_time", "One-time", "Single payment received"),
    RECURRENT("recurrent", "Recurrent", "Monthly or regular income"),
    PENDING("pending", "Pending", "Expected future payment"),
    ;

    companion object {
        fun fromId(id: String): IncomeSourceType? = values().firstOrNull { it.id == id }
    }
}
