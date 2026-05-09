package com.subscriptiontracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subscriptiontracker.ui.theme.LogoShape
import com.subscriptiontracker.ui.theme.TextMuted

@Composable
fun AppLogoIcon(
    name: String,
    logoUri: String? = null,
    modifier: Modifier = Modifier
) {
    val initial = name.firstOrNull()?.uppercase() ?: "?"
    val bgColor = logoBackgroundColor(name)

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(LogoShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

private fun logoBackgroundColor(name: String): Color {
    val colors = listOf(
        Color(0xFFE74C3C),
        Color(0xFF8E44AD),
        Color(0xFF3498DB),
        Color(0xFF1ABC9C),
        Color(0xFF2ECC71),
        Color(0xFFF39C12),
        Color(0xFFE67E22),
        Color(0xFF2C3E50),
        Color(0xFF16A085),
        Color(0xFF2980B9),
    )
    val index = name.hashCode().mod(colors.size).let { if (it < 0) it + colors.size else it }
    return colors[index]
}
