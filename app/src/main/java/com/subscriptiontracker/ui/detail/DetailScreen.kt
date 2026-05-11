package com.subscriptiontracker.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.subscriptiontracker.domain.model.SubscriptionStatus
import com.subscriptiontracker.ui.theme.CardWhite
import com.subscriptiontracker.ui.theme.PageBackground
import com.subscriptiontracker.ui.theme.StatusBorderActive
import com.subscriptiontracker.ui.theme.StatusBorderExpiring
import com.subscriptiontracker.ui.theme.Primary
import com.subscriptiontracker.ui.theme.TextMain
import com.subscriptiontracker.ui.theme.TextMuted
import com.subscriptiontracker.ui.theme.TextSecondary
import com.subscriptiontracker.ui.theme.OnPrimary
import com.subscriptiontracker.ui.theme.statusBorderColor
import com.subscriptiontracker.util.formatCurrency
import com.subscriptiontracker.util.formatFull
import com.subscriptiontracker.util.formatShort
import com.subscriptiontracker.util.initialLetter
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    subscriptionId: Long,
    onNavigateBack: () -> Unit,
    onNavigateEdit: (Long) -> Unit,
    viewModel: DetailViewModel = viewModel()
) {
    LaunchedEffect(subscriptionId) {
        viewModel.loadSubscription(subscriptionId)
    }

    val subscription by viewModel.subscription.collectAsState()
    val paymentHistory by viewModel.paymentHistory.collectAsState()
    val showEditor by viewModel.showEditor.collectAsState()

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            TopAppBar(
                title = { Text(subscription?.name ?: "详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        subscription?.let { sub ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(statusBackgroundColor(sub.status)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!sub.wallpaperUri.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(if (sub.wallpaperUri.startsWith("content://")) sub.wallpaperUri
                                        else File(sub.wallpaperUri))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "${sub.name} 壁纸",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        } else {
                            Text(
                                text = sub.name.initialLetter(),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "${sub.name}    ${formatCurrency(sub.amount)} / ${sub.billingCycle.displayName}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextMain
                        )
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            Text(
                                text = "${sub.status.displayName} · ",
                                color = statusBorderColor(sub.status),
                                fontSize = 13.sp
                            )
                            Text(
                                text = sub.deadlineDate.formatFull(),
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleEditor() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showEditor) "收起编辑 ▲" else "展开编辑 ▼",
                            color = Primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                item {
                    AnimatedVisibility(visible = showEditor) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardWhite)
                                .padding(16.dp)
                        ) {
                            Button(
                                onClick = { onNavigateEdit(sub.id) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = Primary,
                                    contentColor = OnPrimary
                                )
                            ) {
                                Text("编辑订阅信息")
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "历史扣费记录",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                }

                if (paymentHistory.isEmpty()) {
                    item {
                        Text(
                            text = "暂无记录",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = TextMuted
                        )
                    }
                } else {
                    items(paymentHistory, key = { it.id }) { payment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = java.time.LocalDate.ofEpochDay(payment.paymentDate).formatShort(),
                                color = TextMuted,
                                fontSize = 14.sp,
                                modifier = Modifier.width(80.dp)
                            )
                            Text(
                                text = formatCurrency(payment.amount),
                                color = TextMain,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = when (payment.action) {
                                    "RENEWED" -> "已续"
                                    "CANCELLED" -> "已取消"
                                    "UNCERTAIN" -> "不确定"
                                    else -> payment.action
                                },
                                color = when (payment.action) {
                                    "RENEWED" -> StatusBorderActive
                                    "CANCELLED" -> StatusBorderExpiring
                                    else -> TextMuted
                                },
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

private fun statusBackgroundColor(status: SubscriptionStatus): Color = statusBorderColor(status).copy(alpha = 0.15f)
