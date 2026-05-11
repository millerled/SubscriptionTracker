package com.subscriptiontracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.subscriptiontracker.domain.model.SortOption
import com.subscriptiontracker.ui.theme.CardWhite
import com.subscriptiontracker.ui.theme.Primary
import com.subscriptiontracker.ui.theme.TextMuted
import com.subscriptiontracker.ui.theme.TextSecondary

@Composable
fun SortSelector(
    currentSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardWhite)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = null,
                    tint = TextMuted
                )
                Text(
                    text = "排序",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
        items(SortOption.entries.size) { index ->
            val option = SortOption.entries[index]
            val selected = currentSort == option
            Text(
                text = option.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else TextSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) Primary else CardWhite)
                    .clickable { onSortSelected(option) }
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            )
        }
    }
}
