package com.kahavanu.ui.income.components

import androidx.compose.runtime.Composable
import com.kahavanu.ui.common.MatchItemState
import com.kahavanu.ui.common.MatchingSection

@Composable
fun MatchAndCatchSection(
    items: List<MatchItemState>,
    onConfirm: (String) -> Unit,
    onLogAsNew: (String) -> Unit,
    onDismiss: (String) -> Unit,
) {
    if (items.isEmpty()) return

    MatchingSection(
        title = "Match & Catch",
        subtitle = "Income detected from SMS",
        items = items,
        onPrimaryAction = { onConfirm(it.id) },
        onSecondaryAction = { onLogAsNew(it.id) },
        onDismiss = { onDismiss(it.id) },
    )
}
