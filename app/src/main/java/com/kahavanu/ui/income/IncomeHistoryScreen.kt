package com.kahavanu.ui.income

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kahavanu.ui.income.components.*
import com.kahavanu.ui.theme.RawColors
import com.kahavanu.ui.theme.Spacing
import com.kahavanu.ui.theme.TextPrimary
import com.kahavanu.ui.theme.TextSecondary
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

@Composable
fun IncomeHistoryScreen(
    onBack: () -> Unit,
    initialFilter: HistoryFilter = HistoryFilter.ALL,
    viewModel: IncomeOverviewViewModel = hiltViewModel()
) {
    val allLogs by viewModel.incomeLogs.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(initialFilter) }

    val filteredLogs = allLogs.filter { log ->
        val matchesSearch = log.title.contains(searchQuery, ignoreCase = true) || 
                          log.amount.toString().contains(searchQuery)
        
        val isLogPersistent = isPersistent(log.sourceType)
        val isLogOverdue = isOverdue(log.receivedAtEpochMillis) && isPending(log.sourceType)
        
        val matchesFilter = when (selectedFilter) {
            HistoryFilter.ALL -> true
            HistoryFilter.PENDING -> isLogPersistent
            HistoryFilter.OVERDUE -> isLogOverdue
            HistoryFilter.PAID -> !isPending(log.sourceType) && !isRecurrent(log.sourceType)
        }
        matchesSearch && matchesFilter
    }.sortedByDescending { it.receivedAtEpochMillis }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Background Blobs
        GradientBlob(
            modifier = Modifier.offset(x = (-96).dp, y = (-128).dp),
            size = 360.dp,
            colors = listOf(
                RawColors.Slate.Slate200.copy(alpha = 0.16f),
                RawColors.Slate.Slate400.copy(alpha = 0.08f),
                Color.Transparent,
            ),
        )
        
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
                        text = "INCOME HISTORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${filteredLogs.size} logs",
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
                            "Search logs...",
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

            // Filter Tabs
            HistoryFilterToggle(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(Spacing.large))

            // List
            GlassCard(modifier = Modifier.weight(1f)) {
                if (filteredLogs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No income history found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                } else {
                    LazyColumn {
                        itemsIndexed(filteredLogs) { index, log ->
                            val isLogOverdue = isOverdue(log.receivedAtEpochMillis) && isPending(log.sourceType)
                            PersistenceListItem(
                                title = log.title,
                                dueText = formatDate(log.receivedAtEpochMillis),
                                isOverdue = isLogOverdue,
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
                    text = "Show 20",
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
                        text = "1 / ${maxOf(1, (filteredLogs.size + 19) / 20)}",
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
