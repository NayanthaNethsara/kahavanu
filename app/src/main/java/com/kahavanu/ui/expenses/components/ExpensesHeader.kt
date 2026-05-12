package com.kahavanu.ui.expenses.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.circularIconButton
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextPrimaryEmerald
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextSize

@Composable
fun ExpensesHeader(
    onQuickActionClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.size(40.dp))
            IconButton(
                onClick = onQuickActionClick,
                modifier = Modifier.circularIconButton(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = "Notifications",
                    tint = TextPrimaryEmerald,
                )
            }
        }

        Text(
            text = "EXPENSES",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            letterSpacing = 0.7.sp,
            color = TextSecondary,
        )
        Spacer(modifier = Modifier.height(Spacing.extraSmall))
        Text(
            text = "Where Money Goes",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 29.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            letterSpacing = (-0.7).sp,
        )
    }
}
