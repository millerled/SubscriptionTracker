package com.subscriptiontracker.ui.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.subscriptiontracker.domain.model.SortOption
import com.subscriptiontracker.domain.model.SubscriptionStatus
import com.subscriptiontracker.ui.components.ActivityHeatmap
import com.subscriptiontracker.ui.components.ConfirmDeleteDialog
import com.subscriptiontracker.ui.components.SubscriptionActionSheet
import com.subscriptiontracker.ui.theme.CardWhite
import com.subscriptiontracker.ui.theme.PageBackground
import com.subscriptiontracker.ui.theme.Primary
import com.subscriptiontracker.ui.theme.TextMuted
import com.subscriptiontracker.ui.theme.TextSecondary
import com.subscriptiontracker.ui.theme.OnPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddClick: () -> Unit,
    onDetailClick: (Long) -> Unit
) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    val monthlyTotal by viewModel.monthlyTotal.collectAsState()
    val dailyAverage by viewModel.dailyAverage.collectAsState()
    val paidAmount by viewModel.paidAmount.collectAsState()
    val pendingAmount by viewModel.pendingAmount.collectAsState()
    val activeCount by viewModel.activeCount.collectAsState()
    val pausedCount by viewModel.pausedCount.collectAsState()
    val renewingCount by viewModel.renewingCount.collectAsState()
    val expiringCount by viewModel.expiringCount.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val pendingConfirmations by viewModel.pendingConfirmations.collectAsState()
    val dialogIndex by viewModel.dialogCurrentIndex.collectAsState()
    val subscriptionToDelete by viewModel.subscriptionToDelete.collectAsState()
    val actionSheetSubscription by viewModel.actionSheetSubscription.collectAsState()
    val heatmapData by viewModel.heatmapData.collectAsState()

    Scaffold(
        containerColor = PageBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = Primary,
                contentColor = OnPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "新增订阅")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                HomeHeader(
                    totalCount = activeCount + pausedCount + renewingCount + expiringCount,
                    subscriptionCount = activeCount + renewingCount + expiringCount,
                    activeCount = activeCount,
                    pausedCount = pausedCount,
                    activeFilter = activeFilter,
                    sortOption = sortOption,
                    onFilterSelected = { viewModel.setFilter(it) },
                    onSortCycle = { viewModel.setSortOption(sortOption.next()) }
                )
            }

            item {
                StatsCard(
                    monthlyTotal = monthlyTotal,
                    dailyAverage = dailyAverage,
                    paidAmount = paidAmount,
                    pendingAmount = pendingAmount
                )
            }

            if (heatmapData.isNotEmpty()) {
                item {
                    ActivityHeatmap(
                        data = heatmapData,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            item {
                AlertFilterRow(
                    renewingCount = renewingCount,
                    expiringCount = expiringCount,
                    activeFilter = activeFilter,
                    onFilterSelected = { viewModel.setFilter(it) }
                )
            }

            item {
                SortSelector(
                    currentSort = sortOption,
                    onSortSelected = { viewModel.setSortOption(it) }
                )
            }

            if (subscriptions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Primary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Primary.copy(alpha = 0.4f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "还没有订阅",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextMuted
                        )
                        Text(
                            text = "点击右下角 + 开始添加",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(
                    items = subscriptions,
                    key = { it.id }
                ) { subscription ->
                    SubscriptionCard(
                        subscription = subscription,
                        onClick = { onDetailClick(subscription.id) },
                        onLongClick = { viewModel.showActionSheet(subscription) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (pendingConfirmations.isNotEmpty() && dialogIndex < pendingConfirmations.size) {
        PaymentConfirmationDialog(
            pendingItems = pendingConfirmations,
            currentIndex = dialogIndex,
            onChoice = { viewModel.onPaymentChoice(it) },
            onDismiss = { viewModel.dismissConfirmations() }
        )
    }

    actionSheetSubscription?.let { subscription ->
        SubscriptionActionSheet(
            subscription = subscription,
            onDismiss = { viewModel.dismissActionSheet() },
            onPin = { viewModel.pinSubscription() },
            onUnpin = { viewModel.unpinSubscription() },
            onDelete = { viewModel.deleteFromActionSheet() }
        )
    }

    subscriptionToDelete?.let { subscription ->
        ConfirmDeleteDialog(
            subscription = subscription,
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.dismissDeleteConfirmation() }
        )
    }
}

@Composable
private fun HomeHeader(
    totalCount: Int,
    subscriptionCount: Int,
    activeCount: Int,
    pausedCount: Int,
    activeFilter: SubscriptionStatus?,
    sortOption: SortOption,
    onFilterSelected: (SubscriptionStatus?) -> Unit,
    onSortCycle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "订阅",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "管理你的所有订阅服务",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFFEFF3FA))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HeaderSegment(
                    text = "订阅",
                    count = subscriptionCount,
                    selected = activeFilter != SubscriptionStatus.PAUSED,
                    onClick = { onFilterSelected(null) }
                )
                HeaderSegment(
                    text = "暂停",
                    count = pausedCount,
                    selected = activeFilter == SubscriptionStatus.PAUSED,
                    onClick = { onFilterSelected(SubscriptionStatus.PAUSED) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HeaderTool(
                icon = { Icon(Icons.Default.FilterList, contentDescription = null, tint = TextMuted) },
                label = "全部 $totalCount",
                modifier = Modifier.weight(1f),
                onClick = { onFilterSelected(null) }
            )
            HeaderTool(
                icon = { Icon(Icons.Default.FilterList, contentDescription = null, tint = TextMuted) },
                label = "生效 $activeCount",
                modifier = Modifier.weight(1f),
                onClick = { onFilterSelected(SubscriptionStatus.ACTIVE) }
            )
            HeaderTool(
                icon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, tint = Primary) },
                label = sortOption.displayName,
                active = true,
                onClick = onSortCycle
            )
        }
    }
}

@Composable
private fun HeaderSegment(
    text: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) Primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else TextSecondary,
            style = MaterialTheme.typography.labelLarge
        )
        Box(
            modifier = Modifier
                .padding(start = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) Color.White else Primary.copy(alpha = 0.12f))
                .padding(horizontal = 7.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = if (selected) Primary else Primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HeaderTool(
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (active) Primary.copy(alpha = 0.08f) else CardWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        icon()
        Text(
            text = label,
            color = if (active) Primary else TextSecondary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AlertFilterRow(
    renewingCount: Int,
    expiringCount: Int,
    activeFilter: SubscriptionStatus?,
    onFilterSelected: (SubscriptionStatus?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickFilterPill(
            text = "即将续订",
            count = renewingCount,
            selected = activeFilter == SubscriptionStatus.RENEWING,
            color = Color(0xFFF59E0B),
            onClick = { onFilterSelected(SubscriptionStatus.RENEWING) }
        )
        QuickFilterPill(
            text = "即将到期",
            count = expiringCount,
            selected = activeFilter == SubscriptionStatus.EXPIRING,
            color = Color(0xFFEF4444),
            onClick = { onFilterSelected(SubscriptionStatus.EXPIRING) }
        )
        QuickFilterPill(
            text = "全部",
            count = renewingCount + expiringCount,
            selected = activeFilter == null,
            color = Primary,
            onClick = { onFilterSelected(null) }
        )
    }
}

private fun SortOption.next(): SortOption {
    val entries = SortOption.entries
    return entries[(entries.indexOf(this) + 1) % entries.size]
}

@Composable
private fun QuickFilterPill(
    text: String,
    count: Int,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Text(
        text = "$text $count",
        style = MaterialTheme.typography.labelMedium,
        color = if (selected) Color.White else color,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) color else color.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}
