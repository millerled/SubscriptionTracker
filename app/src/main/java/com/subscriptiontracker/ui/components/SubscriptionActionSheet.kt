package com.subscriptiontracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscriptiontracker.domain.model.Subscription
import com.subscriptiontracker.ui.theme.TextMain
import com.subscriptiontracker.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionActionSheet(
    subscription: Subscription,
    onDismiss: () -> Unit,
    onPin: () -> Unit,
    onUnpin: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = subscription.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextMain,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pin / Unpin
            if (subscription.sortOrder < 0) {
                ActionSheetRow(
                    icon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(22.dp)) },
                    label = "取消置顶",
                    onClick = onUnpin
                )
            } else {
                ActionSheetRow(
                    icon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(22.dp)) },
                    label = "置顶",
                    onClick = onPin
                )
            }

            // Delete
            ActionSheetRow(
                icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(22.dp)) },
                label = "删除",
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun ActionSheetRow(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, fontSize = 16.sp, color = TextMain)
    }
}
