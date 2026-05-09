package com.subscriptiontracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.subscriptiontracker.domain.model.SortOption

@Composable
fun SortSelector(
    currentSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SortOption.entries.forEach { option ->
            FilterChip(
                selected = currentSort == option,
                onClick = { onSortSelected(option) },
                label = { Text(option.displayName) }
            )
        }
    }
}
