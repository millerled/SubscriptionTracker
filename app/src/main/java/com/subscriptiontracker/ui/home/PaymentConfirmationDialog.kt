package com.subscriptiontracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.subscriptiontracker.domain.model.Subscription
import com.subscriptiontracker.util.formatAmountPerCycle
import com.subscriptiontracker.util.remainingDaysText

data class PendingSubscription(
    val subscription: Subscription,
    val choice: PaymentChoice = PaymentChoice.UNDECIDED
)

enum class PaymentChoice(val action: String) {
    RENEW("RENEWED"),
    CANCEL("CANCELLED"),
    UNDECIDED("UNCERTAIN")
}

@Composable
fun PaymentConfirmationDialog(
    pendingItems: List<PendingSubscription>,
    currentIndex: Int,
    onChoice: (PaymentChoice) -> Unit,
    onDismiss: () -> Unit
) {
    if (currentIndex >= pendingItems.size) return

    val current = pendingItems[currentIndex]
    val sub = current.subscription
    val isLast = currentIndex == pendingItems.size - 1

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "扣费确认 (${currentIndex + 1}/${pendingItems.size})",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                Text(
                    text = sub.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatAmountPerCycle(sub.amount, sub.billingCycle.displayName),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = sub.deadlineDate.remainingDaysText(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onChoice(PaymentChoice.RENEW) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.subscriptiontracker.ui.theme.StatusBorderActive
                        )
                    ) {
                        Text("是，续了")
                    }
                    OutlinedButton(
                        onClick = { onChoice(PaymentChoice.CANCEL) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("否，没续")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { onChoice(PaymentChoice.UNDECIDED) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("不确定${if (isLast) "" else "，下一个"}")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("稍后再说")
            }
        }
    )
}
