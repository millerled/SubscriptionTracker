package com.subscriptiontracker.ui.detail

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.subscriptiontracker.ui.components.AppLogoIcon
import com.subscriptiontracker.ui.components.BrandLetterLogo
import com.subscriptiontracker.ui.components.logoStyleFor
import com.subscriptiontracker.ui.theme.CardWhite
import com.subscriptiontracker.ui.theme.PageBackground
import com.subscriptiontracker.ui.theme.StatusBorderActive
import com.subscriptiontracker.ui.theme.StatusBorderExpiring
import com.subscriptiontracker.ui.theme.Primary
import com.subscriptiontracker.ui.theme.TextMain
import com.subscriptiontracker.ui.theme.TextMuted
import com.subscriptiontracker.ui.theme.TextSecondary
import com.subscriptiontracker.ui.theme.OnPrimary
import com.subscriptiontracker.util.formatCurrency
import com.subscriptiontracker.util.formatFull
import com.subscriptiontracker.util.formatShort
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
                    DetailHero(sub = sub)
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

@Composable
private fun DetailHero(sub: com.subscriptiontracker.domain.model.Subscription) {
    val logoStyle = remember(sub.name, sub.logoUri) {
        logoStyleFor(sub.name, sub.logoUri)
    }
    val accent = logoStyle.colors.first()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(188.dp)
            .padding(16.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(accent.copy(alpha = 0.78f), accent.copy(alpha = 0.48f)),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            .padding(22.dp)
    ) {
        if (!sub.wallpaperUri.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(if (sub.wallpaperUri.startsWith("content://")) sub.wallpaperUri else File(sub.wallpaperUri))
                    .crossfade(true)
                    .build(),
                contentDescription = "${sub.name} 壁纸",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.22f))
            )
        } else {
            BrandLetterLogo(
                letter = logoStyle.glyph,
                colors = logoStyle.colors,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(120.dp)
                    .clip(RoundedCornerShape(30.dp))
            )
        }

        Column(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalArrangement = Arrangement.Center
        ) {
            AppLogoIcon(
                name = sub.name,
                logoUri = sub.logoUri,
                size = 58.dp,
                showBorder = false
            )
            Text(
                text = sub.name,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = "${formatCurrency(sub.amount)} / ${sub.billingCycle.displayName}",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "${sub.status.displayName} · ${sub.deadlineDate.formatFull()}",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.82f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
