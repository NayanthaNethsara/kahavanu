package com.kahavanu.ui.expenses.components

// All previously local helpers have moved to ui/common:
//   formatAmount / formatDate / currentMonthLabel / dueLabel -> common/MoneyFormat.kt + common/DateFormat.kt
//   categoryColor / categoryIcon                              -> common/CategoryPalette.kt
// This file is kept as an explicit re-export point so the existing
// `com.kahavanu.ui.expenses.components.*` imports still resolve.

@Suppress("unused")
private object ExpensesUtilsMarker
