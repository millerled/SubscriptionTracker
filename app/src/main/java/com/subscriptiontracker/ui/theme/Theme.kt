package com.subscriptiontracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    background = PageBackground,
    surface = CardWhite,
    onBackground = TextMain,
    onSurface = TextMain,
    onSurfaceVariant = TextSecondary,
    outline = TextMuted,
    error = StatusBorderExpiring
)

@Composable
fun SubscriptionTrackerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
