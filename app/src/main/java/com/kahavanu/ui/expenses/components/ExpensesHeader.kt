package com.kahavanu.ui.expenses.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize

@Composable
fun ExpensesHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Expenses",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = TextSize.base,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(Spacing.extraSmall))
        Text(
            text = "Spend with clarity",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = TextSize.xxl,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
        )
    }
}
