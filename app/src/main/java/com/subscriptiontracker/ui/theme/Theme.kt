package com.subscriptiontracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = CardPlannedBackground,
    secondary = StatusConsidering,
    tertiary = StatusUndecided,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = TextTertiary,
    error = StatusExpiring
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
