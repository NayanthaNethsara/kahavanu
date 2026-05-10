package com.kahavanu.ui.income

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.ui.income.components.CircularIconButton
import com.kahavanu.ui.income.components.GlassCard
import com.kahavanu.ui.income.components.formatAmount
import com.kahavanu.ui.income.components.getDueText
import com.kahavanu.ui.income.components.isOverdue
import com.kahavanu.ui.income.components.isPending
import com.kahavanu.ui.income.components.isRecurrent
import com.kahavanu.ui.income.components.isPersistent
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary

@Composable
fun PersistenceListScreen(
    onBack: () -> Unit,
    viewModel: IncomeOverviewViewModel = hiltViewModel()
) {
    val allLogs by viewModel.incomeLogs.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(PersistenceFilter.ALL) }

    val filteredLogs = allLogs.filter { log ->
        val matchesSearch = log.title.contains(searchQuery, ignoreCase = true) || 
                          log.amount.toString().contains(searchQuery)
        val isLogPersistent = isPersistent(log.sourceType)
        
        val matchesFilter = when (selectedFilter) {
            PersistenceFilter.ALL -> isLogPersistent
            PersistenceFilter.OVERDUE -> isLogPersistent && isOverdue(log.receivedAtEpochMillis) && isPending(log.sourceType)
            PersistenceFilter.PENDING -> isLogPersistent && isPending(log.sourceType)
            PersistenceFilter.RECURRENT -> isRecurrent(log.sourceType)
            PersistenceFilter.COMPLETED -> !isLogPersistent
        }
        matchesSearch && matchesFilter
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        RawColors.Slate.Slate50,
                        RawColors.Emerald.Emerald50.copy(alpha = 0.3f),
                        RawColors.Slate.Slate100
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.large)
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack
                )
                Spacer(modifier = Modifier.width(Spacing.medium))
                Column {
                    Text(
                        text = "PERSISTENCE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${filteredLogs.size} payments",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            // Search Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        .border(0.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    placeholder = {
                        Text(
                            "Search client or amount...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary.copy(alpha = 0.5f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        .border(0.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { }
                        .padding(Spacing.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "Filter",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                FilterChip(
                    label = "All",
                    icon = Icons.Default.FilterList,
                    selected = selectedFilter == PersistenceFilter.ALL,
                    onClick = { selectedFilter = PersistenceFilter.ALL }
                )
                FilterChip(
                    label = "Overdue",
                    icon = Icons.Outlined.ErrorOutline,
                    selected = selectedFilter == PersistenceFilter.OVERDUE,
                    onClick = { selectedFilter = PersistenceFilter.OVERDUE }
                )
                FilterChip(
                    label = "Recurrent",
                    icon = Icons.Default.Autorenew,
                    selected = selectedFilter == PersistenceFilter.RECURRENT,
                    onClick = { selectedFilter = PersistenceFilter.RECURRENT }
                )
                FilterChip(
                    label = "Pending",
                    icon = Icons.Outlined.Schedule,
                    selected = selectedFilter == PersistenceFilter.PENDING,
                    onClick = { selectedFilter = PersistenceFilter.PENDING }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            // List
            GlassCard(modifier = Modifier.weight(1f)) {
                if (filteredLogs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No payments found matching criteria",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                } else {
                    LazyColumn {
                        itemsIndexed(filteredLogs) { index, log ->
                            val isOverdue = isOverdue(log.receivedAtEpochMillis) && isPending(log.sourceType)
                            PersistenceListItem(
                                title = log.title,
                                dueText = getDueText(log.receivedAtEpochMillis),
                                isOverdue = isOverdue,
                                isPending = isPending(log.sourceType),
                                isRecurrent = isRecurrent(log.sourceType),
                                amount = formatAmount(log.amount, log.currency),
                                isInvoiceSent = log.isInvoiceSent
                            )
                            if (index < filteredLogs.size - 1) {
                                HorizontalDivider(color = RawColors.Slate.Slate900.copy(alpha = 0.06f))
                            }
                        }
                    }
                }
            }
            
            // Pagination placeholder
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.large),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Show 10",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Prev",
                        modifier = Modifier.size(16.dp).clickable { },
                        tint = TextSecondary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(Spacing.medium))
                    Text(
                        text = "1 / 1",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(Spacing.medium))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        modifier = Modifier.size(16.dp).clickable { },
                        tint = TextSecondary.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) RawColors.Emerald.Emerald500 else Color.White.copy(alpha = 0.7f)
    val contentColor = if (selected) Color.White else TextPrimary
    
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = backgroundColor,
        modifier = Modifier
            .height(36.dp)
            .shadow(if (selected) 8.dp else 4.dp, CircleShape)
            .border(
                0.5.dp, 
                if (selected) Color.Transparent else RawColors.Slate.Slate200.copy(alpha = 0.9f), 
                CircleShape
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun PersistenceListItem(
    title: String,
    dueText: String,
    isOverdue: Boolean,
    isPending: Boolean,
    isRecurrent: Boolean,
    amount: String,
    isInvoiceSent: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = when {
                        isOverdue -> RawColors.Red.Red500.copy(alpha = 0.1f)
                        isRecurrent -> RawColors.Emerald.Emerald500.copy(alpha = 0.14f)
                        !isPending -> RawColors.Emerald.Emerald500.copy(alpha = 0.14f)
                        else -> RawColors.Amber.Amber500.copy(alpha = 0.12f)
                    },
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    isOverdue -> Icons.Outlined.ErrorOutline
                    isRecurrent -> Icons.Default.Autorenew
                    !isPending -> Icons.Default.CheckCircle
                    else -> Icons.Outlined.Schedule
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = when {
                    isOverdue -> RawColors.Red.Red600
                    isRecurrent -> RawColors.Emerald.Emerald600
                    !isPending -> RawColors.Emerald.Emerald600
                    else -> RawColors.Amber.Amber600
                }
            )
        }
        
        Spacer(modifier = Modifier.width(Spacing.medium))
        
        // Info Column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dueText,
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isOverdue -> RawColors.Red.Red600
                        !isPending -> RawColors.Emerald.Emerald600
                        else -> RawColors.Amber.Amber600
                    },
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = if (isInvoiceSent) TextSecondary else RawColors.Red.Red600
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isInvoiceSent) "Invoice sent" else "No invoice",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isInvoiceSent) TextSecondary else RawColors.Red.Red600,
                    fontSize = 10.sp
                )
            }
        }
        
        // Amount and Action Column
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                fontSize = 14.sp
            )
            
            if (isOverdue) {
                Spacer(modifier = Modifier.height(Spacing.small))
                Surface(
                    onClick = { },
                    modifier = Modifier
                        .height(24.dp)
                        .width(72.dp),
                    shape = CircleShape,
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        RawColors.Emerald.Emerald500.copy(alpha = 0.9f),
                                        RawColors.Emerald.Emerald500.copy(alpha = 0.75f),
                                        RawColors.Emerald.Emerald500.copy(alpha = 0.9f)
                                    )
                                )
                            )
                            .border(0.5.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Nudge",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class PersistenceFilter {
    ALL, OVERDUE, RECURRENT, PENDING, COMPLETED
}
