package com.subscriptiontracker.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
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
import com.subscriptiontracker.ui.components.ConfirmDeleteDialog
import com.subscriptiontracker.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddClick: () -> Unit,
    onEditClick: (Long) -> Unit
) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val subscriptionToDelete by viewModel.subscriptionToDelete.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SubscriptionTracker") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = com.subscriptiontracker.ui.theme.Primary,
                contentColor = com.subscriptiontracker.ui.theme.OnPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "新增订阅")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SortSelector(
                currentSort = sortOption,
                onSortSelected = { viewModel.setSortOption(it) }
            )

            if (subscriptions.isEmpty()) {
                Text(
                    text = "还没有订阅\n点击 + 开始添加",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                        .padding(32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(
                        items = subscriptions,
                        key = { it.id }
                    ) { subscription ->
                        SubscriptionCard(
                            subscription = subscription,
                            onClick = { onEditClick(subscription.id) },
                            onLongClick = { viewModel.showDeleteConfirmation(subscription) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    subscriptionToDelete?.let { subscription ->
        ConfirmDeleteDialog(
            subscription = subscription,
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.dismissDeleteConfirmation() }
        )
    }
}
