package com.kahavanu.ui.income.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.kahavanu.ui.common.MatchItemState
import com.kahavanu.ui.common.MatchingSection

@Composable
fun MatchAndCatchSection() {
    val items = listOf(
        MatchItemState(
            id = "1",
            title = "Commercial Bank",
            subtitle = "Today, 10:42",
            amount = "LKR 50,000",
            matchPercent = 86,
            likelyFor = "Likely for SME WordPress build"
        ),
        MatchItemState(
            id = "2",
            title = "Sampath Bank",
            subtitle = "Yesterday",
            amount = "LKR 18,500",
            matchPercent = 64,
            likelyFor = "Likely for Logo retainer · Aprco"
        )
    )

    MatchingSection(
        title = "Match & Catch",
        subtitle = "Unmatched deposits from SMS",
        items = items,
        onPrimaryAction = { },
        onSecondaryAction = { },
        onDismiss = { }
    )
}
