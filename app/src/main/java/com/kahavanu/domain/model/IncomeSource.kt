package com.kahavanu.domain.model

data class IncomeSource(
    val id: Long,
    val name: String,
    val types: Set<IncomeSourceType>,
)
