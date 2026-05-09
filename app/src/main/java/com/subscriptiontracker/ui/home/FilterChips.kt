package com.subscriptiontracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscriptiontracker.domain.model.SubscriptionStatus
import com.subscriptiontracker.ui.theme.ChipShape
import com.subscriptiontracker.ui.theme.Primary
import com.subscriptiontracker.ui.theme.TextMain
import com.subscriptiontracker.ui.theme.TextSecondary

data class FilterChipData(
    val status: SubscriptionStatus?,
    val label: String,
    val count: Int,
    val isSelected: Boolean
)

@Composable
fun FilterChips(
    chips: List<FilterChipData>,
    onChipSelected: (SubscriptionStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            val bgColor = if (chip.isSelected) Primary else Color(0xFFF1F5F9)
            val textColor = if (chip.isSelected) Color.White else TextSecondary

            Row(
                modifier = Modifier
                    .clip(ChipShape)
                    .background(bgColor)
                    .clickable { onChipSelected(chip.status) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = chip.label,
                    fontSize = 13.sp,
                    color = textColor
                )
                Text(
                    text = " ${chip.count}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }
}
