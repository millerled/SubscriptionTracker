package com.subscriptiontracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.subscriptiontracker.domain.model.SubscriptionStatus
import com.subscriptiontracker.ui.components.ConfirmDeleteDialog
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
    onEditClick: (Long) -> Unit,
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
    val pendingConfirmations by viewModel.pendingConfirmations.collectAsState()
    val dialogIndex by viewModel.dialogCurrentIndex.collectAsState()
    val subscriptionToDelete by viewModel.subscriptionToDelete.collectAsState()

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            TopAppBar(
                title = { Text("SubscriptionTracker") }
            )
        },
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
                StatsCard(
                    monthlyTotal = monthlyTotal,
                    dailyAverage = dailyAverage,
                    paidAmount = paidAmount,
                    pendingAmount = pendingAmount
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "即将续订 ($renewingCount)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (activeFilter == SubscriptionStatus.RENEWING) Primary else TextSecondary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = "即将到期 ($expiringCount)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (activeFilter == SubscriptionStatus.EXPIRING) Primary else TextSecondary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            item {
                FilterChips(
                    chips = listOf(
                        FilterChipData(null, "全部", activeCount + pausedCount + renewingCount + expiringCount, activeFilter == null),
                        FilterChipData(SubscriptionStatus.ACTIVE, "生效中", activeCount, activeFilter == SubscriptionStatus.ACTIVE),
                        FilterChipData(SubscriptionStatus.PAUSED, "已失效", pausedCount, activeFilter == SubscriptionStatus.PAUSED)
                    ),
                    onChipSelected = { status -> viewModel.setFilter(status) }
                )
            }

            if (subscriptions.isEmpty()) {
                item {
                    Text(
                        text = "还没有订阅\n点击 + 开始添加",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                            .padding(32.dp)
                    )
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
                        onLongClick = { viewModel.showDeleteConfirmation(subscription) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Payment confirmation dialog
    if (pendingConfirmations.isNotEmpty() && dialogIndex < pendingConfirmations.size) {
        PaymentConfirmationDialog(
            pendingItems = pendingConfirmations,
            currentIndex = dialogIndex,
            onChoice = { viewModel.onPaymentChoice(it) },
            onDismiss = { viewModel.dismissConfirmations() }
        )
    }

    // Delete confirmation dialog
    subscriptionToDelete?.let { subscription ->
        ConfirmDeleteDialog(
            subscription = subscription,
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.dismissDeleteConfirmation() }
        )
    }
}
