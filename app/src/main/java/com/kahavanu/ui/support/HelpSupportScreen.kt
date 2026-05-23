package com.kahavanu.ui.support

import com.kahavanu.ui.theme.extendedColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.kahavanu.R
import com.kahavanu.ui.common.GlassCard
import com.kahavanu.ui.common.KahavanuSubScreen
import com.kahavanu.ui.common.SectionLabel
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import com.kahavanu.ui.theme.TextTertiary

@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // FAQ Accordion States
    var expandedFaqIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    KahavanuSubScreen(
        label = "Help Center",
        title = "Help & Support",
        onBack = onBack,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    start = Spacing.extraLarge,
                    end = Spacing.extraLarge,
                    bottom = Spacing.huge
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.large)
        ) {
            // Introductory Card
            CozyIntroCard()

            // FAQ Accordions Section
            FaqSection(
                expandedIndex = expandedFaqIndex,
                onToggleIndex = { index ->
                    expandedFaqIndex = if (expandedFaqIndex == index) null else index
                }
            )

            // Contact Information Section (Reading from AppConfig.kt)
            ContactInfoSection()

            Spacer(modifier = Modifier.height(Spacing.large))
        }
    }
}

@Composable
private fun CozyIntroCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.9f),
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large)
        ) {
            Text(
                text = "Welcome to Support",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
        )
            Spacer(modifier = Modifier.height(Spacing.small))
            Text(
                text = "We are here to help you manage your personal finance seamlessly. Browse answers below, get in touch with our team, or leave feedback to help us build a better experience.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun FaqSection(
    expandedIndex: Int?,
    onToggleIndex: (Int) -> Unit
) {
    val faqs = listOf(
        FaqItem(
            question = "How does SMS tracking work?",
            answer = "Kahavanu scans incoming SMS messages from registered sender IDs (like your bank) to automatically match transactional notifications to pending invoices or records, saving you manual entry time."
        ),
        FaqItem(
            question = "Where is my transaction data stored?",
            answer = "All transactional entries, budgets, goals, and income logs are securely stored locally on your device to protect your personal privacy. We do not upload your financial data to external servers."
        ),
        FaqItem(
            question = "Can I manage multiple currencies?",
            answer = "Yes! Under the 'Currencies & Income Sources' preferences screen, you can define a primary base currency (e.g. LKR) and a secondary currency (e.g. USD) to track recurrent items and logs."
        ),
        FaqItem(
            question = "How are subscription costs calculated?",
            answer = "Subscription renewals are aggregated in real-time. For yearly billing schedules, the app automatically normalizes the cost to its monthly equivalent (yearly fee / 12.0) to give you a true monthly leak analysis."
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        SectionLabel(text = "Frequently Asked Questions")

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White.copy(alpha = 0.9f),
            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                faqs.forEachIndexed { index, faq ->
                    val isExpanded = expandedIndex == index
                    FaqRow(
                        faq = faq,
                        isExpanded = isExpanded,
                        onClick = { onToggleIndex(index) }
                    )
                    if (index < faqs.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqRow(
    faq: FaqItem,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(Spacing.medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = faq.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(Spacing.small))
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun ContactInfoSection() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        SectionLabel(text = "Contact Support")

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White.copy(alpha = 0.9f),
            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ContactRow(
                    icon = Icons.Default.Email,
                    iconColor = MaterialTheme.colorScheme.primary,
                    iconBgColor = MaterialTheme.extendedColors.brandWashed,
                    label = "Support Email",
                    value = stringResource(R.string.support_email)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
                ContactRow(
                    icon = Icons.Default.Phone,
                    iconColor = MaterialTheme.extendedColors.infoAccent,
                    iconBgColor = MaterialTheme.extendedColors.infoWashed,
                    label = "Hotline Phone",
                    value = stringResource(R.string.support_phone)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
                ContactRow(
                    icon = Icons.Default.Place,
                    iconColor = MaterialTheme.extendedColors.dangerAccent,
                    iconBgColor = MaterialTheme.extendedColors.dangerWashed.copy(alpha = 0.8f),
                    label = "Main Office",
                    value = stringResource(R.string.office_address)
                )
            }
        }
    }
}

@Composable
private fun ContactRow(
    icon: ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
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
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
        )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}



private data class FaqItem(
    val question: String,
    val answer: String
)
