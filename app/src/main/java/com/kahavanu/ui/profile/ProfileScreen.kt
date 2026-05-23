package com.kahavanu.ui.profile

import com.kahavanu.ui.theme.extendedColors
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kahavanu.domain.model.UserSession
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.KahavanuScreen
import com.kahavanu.ui.common.SectionLabel
import com.kahavanu.ui.common.screenSection
import com.kahavanu.ui.theme.AccentExpense
import com.kahavanu.ui.theme.AccentExpenseSoft
import com.kahavanu.ui.theme.AccentIncome
import com.kahavanu.ui.theme.AccentIncomeSoft
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextTertiary

@Composable
fun ProfileScreen(
    onNavigateToSmsSenders: () -> Unit,
    onNavigateToIncomeSources: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToHelpSupport: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userSession by viewModel.userSession.collectAsState()
    val isAutoMatchDepositsEnabled by viewModel.isAutoMatchDepositsEnabled.collectAsState()
    val isPushAlertsEnabled by viewModel.isPushAlertsEnabled.collectAsState()
    val isDarkModeEnabled by viewModel.isDarkModeEnabled.collectAsState()

    KahavanuScreen(
        headerLabel = "Profile",
        headerTitle = "Account & Preferences",
    ) {
        screenSection {
            ProfileCard(userSession = userSession)
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.extraLarge)
            ) {
                SectionLabel(text = "Preferences")
            }
        }

        screenSection {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.9f),
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            ) {
                SettingToggleRow(
                    icon = Icons.Default.Autorenew,
                    iconColor = AccentIncome,
                    iconBackgroundColor = AccentIncomeSoft,
                    title = "Auto-match deposits",
                    subtitle = "Link bank SMS to pending invoices",
                    checked = isAutoMatchDepositsEnabled,
                    onCheckedChange = { viewModel.toggleAutoMatchDeposits() }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                SettingToggleRow(
                    icon = Icons.Default.NotificationsActive,
                    iconColor = AccentExpense,
                    iconBackgroundColor = AccentExpenseSoft,
                    title = "Push alerts",
                    subtitle = "Overspend & arrival updates",
                    checked = isPushAlertsEnabled,
                    onCheckedChange = { viewModel.togglePushAlerts() }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                SettingToggleRow(
                    icon = Icons.Default.DarkMode,
                    iconColor = AccentIncome,
                    iconBackgroundColor = AccentIncomeSoft,
                    title = "Dark mode",
                    subtitle = "Easier on the eyes at night",
                    checked = isDarkModeEnabled,
                    onCheckedChange = { viewModel.toggleDarkMode() }
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.extraLarge)
            ) {
                SectionLabel(text = "Finance Settings")
            }
        }

        screenSection {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.9f),
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            ) {
                SettingNavigationRow(
                    icon = Icons.Default.Language,
                    iconColor = AccentIncome,
                    iconBackgroundColor = AccentIncomeSoft,
                    title = "Currencies & Income Sources",
                    subtitle = "LKR (USD) · 4 sources",
                    onClick = onNavigateToIncomeSources
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                SettingNavigationRow(
                    icon = Icons.Default.CreditCard,
                    iconColor = MaterialTheme.extendedColors.dangerAccent,
                    iconBackgroundColor = MaterialTheme.extendedColors.dangerAccent.copy(alpha = 0.1f),
                    title = "Manage Subscriptions",
                    subtitle = "Track recurring leaks",
                    onClick = onNavigateToSubscriptions
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.extraLarge)
            ) {
                SectionLabel(text = "SMS Scanning")
            }
        }

        screenSection {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.9f),
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            ) {
                SettingNavigationRow(
                    icon = Icons.Default.Sms,
                    iconColor = AccentIncome,
                    iconBackgroundColor = AccentIncomeSoft,
                    title = "SMS Sender IDs",
                    subtitle = "Authorized SMS senders for tracking",
                    onClick = onNavigateToSmsSenders
                )
            }
        }

        screenSection {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.9f),
                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            ) {
                SettingNavigationRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    iconColor = TextSecondary,
                    iconBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    title = "Help & support",
                    subtitle = "FAQ, contact us, send feedback",
                    onClick = onNavigateToHelpSupport
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                SettingNavigationRow(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    iconColor = MaterialTheme.colorScheme.error,
                    iconBackgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    title = "Log out",
                    subtitle = "Sign out on this device",
                    onClick = { viewModel.logout() },
                    isDanger = true
                )
            }
        }

        item {
            Text(
                text = "Kahavanu · v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                fontSize = 10.sp,
            textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.medium, bottom = Spacing.large)
            )
        }
    }
}

@Composable
private fun ProfileCard(
    userSession: UserSession?,
    modifier: Modifier = Modifier
) {
    val displayName = userSession?.displayName ?: "Tharindu Perera"
    val email = userSession?.email ?: "tharindu@kahavanu.lk"
    val initials = getInitials(displayName)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.9f),
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF00BC7D),
                                Color(0xFF009361)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
        )
            }
            Spacer(modifier = Modifier.width(Spacing.large))
            Column {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
                    fontSize = 17.sp,
                    letterSpacing = (-0.6).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    iconColor: Color,
    iconBackgroundColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(Spacing.medium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            fontSize = 13.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.extendedColors.brandAccent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor = Color.Transparent,
            )
        )
    }
}

@Composable
private fun SettingNavigationRow(
    icon: ImageVector,
    iconColor: Color,
    iconBackgroundColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDanger: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(Spacing.medium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            fontSize = 13.sp,
                color = if (isDanger) MaterialTheme.colorScheme.error else TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 11.sp,
                color = if (isDanger) MaterialTheme.extendedColors.dangerSoft else TextSecondary
            )
        }
        if (!isDanger) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun getInitials(name: String?): String {
    if (name.isNullOrBlank()) return "U"
    val parts = name.trim().split("\\s+".toRegex())
    if (parts.size >= 2) {
        return "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
    }
    return parts[0].take(2).uppercase()
}
