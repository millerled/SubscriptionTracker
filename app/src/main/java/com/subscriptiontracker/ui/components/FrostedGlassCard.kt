package com.subscriptiontracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.subscriptiontracker.ui.theme.CardShape

@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    blurRadius: Float = 20f,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape),
        shape = CardShape,
        color = backgroundColor.copy(alpha = 0.85f),
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .background(backgroundColor.copy(alpha = 0.15f))
        ) {
            content()
        }
    }
}
