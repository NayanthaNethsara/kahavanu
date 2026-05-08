package com.kahavanu.ui.income.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing

@Composable
fun IncomeHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Income",
            style = MaterialTheme.typography.bodyLarge,
            color = RawColors.Emerald.Emerald700
        )
        Spacer(modifier = Modifier.height(Spacing.extraSmall))
        Text(
            text = "Wealth in the Air",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
            color = RawColors.Emerald.Emerald900
        )
    }
}
