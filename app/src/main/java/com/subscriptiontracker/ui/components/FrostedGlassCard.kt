package com.subscriptiontracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.subscriptiontracker.ui.theme.CardWhite

@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Transparent,
    backgroundColor: Color = CardWhite,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .drawBehind {
                if (borderColor != Color.Transparent) {
                    val gradient = Brush.horizontalGradient(
                        colors = listOf(borderColor, borderColor.copy(alpha = 0.2f)),
                        startX = 0f,
                        endX = size.width
                    )
                    drawRoundRect(
                        brush = gradient,
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
            .border(
                width = if (borderColor == Color.Transparent) 0.dp else 0.5.dp,
                color = borderColor.copy(alpha = 0.15f),
                shape = shape
            )
            .background(backgroundColor, shape)
    ) {
        content()
    }
}
