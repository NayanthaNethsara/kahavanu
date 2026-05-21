package com.kahavanu.ui.expenses.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.common.categoryColor
import com.kahavanu.ui.common.categoryIcon
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize
import java.util.Locale

private fun Long.toDateLabel(): String = formatDate(this)

/**
 * Shared expense row used in both the main screen "Recent Expenses" list
 * and the "View All" history screen.
 *
 * Layout:
 *   [Icon]  Title            Amount
 *           Category
 */
@Composable
fun ExpenseListItem(
    title: String,
    category: String,
    amount: Double,
    currencyCode: String,
    spentAtEpochMillis: Long? = null,
    modifier: Modifier = Modifier,
) {
    val tint = categoryColor(category)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        Icon(
            imageVector = categoryIcon(category),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = TextSize.sm,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = TextSecondary,
                )
                if (spentAtEpochMillis != null) {
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = TextSecondary,
                    )
                    Text(
                        text = spentAtEpochMillis.toDateLabel(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = TextSecondary,
                    )
                }
            }
        }

        Text(
            text = "-${currencyCode} ${String.format(Locale.getDefault(), "%,.0f", amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontSize = TextSize.sm,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )
    }
}
