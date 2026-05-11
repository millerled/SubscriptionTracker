package com.subscriptiontracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.subscriptiontracker.ui.theme.LogoShape
import com.subscriptiontracker.util.initialLetter
import java.io.File

data class PresetLogo(
    val id: String,
    val label: String,
    val icon: String,
    val bgColor: Color,
    val category: String = ""
)

val presetLogos = listOf(
    PresetLogo("music", "音乐", "♪", Color(0xFFE74C3C), "娱乐"),
    PresetLogo("video", "视频", "▶", Color(0xFF3498DB), "娱乐"),
    PresetLogo("game", "游戏", "◆", Color(0xFF9B59B6), "娱乐"),
    PresetLogo("ai", "AI", "◇", Color(0xFF1ABC9C), "工具"),
    PresetLogo("cloud", "云盘", "☁", Color(0xFFF39C12), "工具"),
    PresetLogo("tool", "工具", "⚙", Color(0xFF5D6D7E), "工具"),
    PresetLogo("learn", "学习", "▲", Color(0xFF2ECC71), "学习"),
    PresetLogo("learn2", "学习", "📖", Color(0xFF27AE60), "学习"),
    PresetLogo("health", "健康", "♥", Color(0xFFE67E22), "健康"),
    PresetLogo("health2", "健康", "💪", Color(0xFFD35400), "健康"),
    PresetLogo("life", "生活", "●", Color(0xFF2C3E50), "生活"),
    PresetLogo("life2", "生活", "🏠", Color(0xFF34495E), "生活"),
    PresetLogo("other", "其他", "✧", Color(0xFF95A5A6), ""),
)

// Google 4-color palette
private val GoogleBlue = Color(0xFF4285F4)
private val GoogleRed = Color(0xFFEA4335)
private val GoogleYellow = Color(0xFFFBBC05)
private val GoogleGreen = Color(0xFF34A853)

private val googleColorSets = listOf(
    listOf(GoogleBlue, GoogleRed, GoogleYellow, GoogleGreen),
    listOf(GoogleRed, GoogleBlue, GoogleGreen, GoogleYellow),
    listOf(GoogleGreen, GoogleYellow, GoogleRed, GoogleBlue),
    listOf(GoogleYellow, GoogleGreen, GoogleBlue, GoogleRed),
    listOf(GoogleBlue, GoogleGreen, GoogleYellow, GoogleRed),
    listOf(GoogleRed, GoogleYellow, GoogleBlue, GoogleGreen),
    listOf(GoogleGreen, GoogleBlue, GoogleRed, GoogleYellow),
    listOf(GoogleYellow, GoogleRed, GoogleGreen, GoogleBlue),
)

private fun logoGoogleColors(name: String): List<Color> {
    val index = name.hashCode().mod(googleColorSets.size).let { if (it < 0) it + googleColorSets.size else it }
    return googleColorSets[index]
}

@Composable
fun AppLogoIcon(
    name: String,
    logoUri: String? = null,
    modifier: Modifier = Modifier
) {
    val initial = name.initialLetter()
    val colors = logoGoogleColors(name)

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(LogoShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        when {
            logoUri.isNullOrBlank() -> {
                GoogleStyleLetter(
                    letter = initial,
                    colors = colors,
                    modifier = Modifier.size(40.dp)
                )
            }
            logoUri.startsWith("preset:") -> {
                val presetId = logoUri.removePrefix("preset:")
                val preset = presetLogos.find { it.id == presetId }
                GoogleStyleLetter(
                    letter = preset?.icon ?: initial,
                    colors = if (preset != null) listOf(preset.bgColor, preset.bgColor, preset.bgColor, preset.bgColor)
                        else colors,
                    modifier = Modifier.size(40.dp)
                )
            }
            else -> {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(if (logoUri.startsWith("content://")) logoUri else File(logoUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = "$name logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
private fun GoogleStyleLetter(
    letter: String,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val halfW = w / 2
            val halfH = h / 2

            // Top-left quadrant
            drawRect(
                color = colors[0],
                topLeft = Offset.Zero,
                size = Size(halfW, halfH)
            )
            // Top-right quadrant
            drawRect(
                color = colors[1],
                topLeft = Offset(halfW, 0f),
                size = Size(w - halfW, halfH)
            )
            // Bottom-left quadrant
            drawRect(
                color = colors[2],
                topLeft = Offset(0f, halfH),
                size = Size(halfW, h - halfH)
            )
            // Bottom-right quadrant
            drawRect(
                color = colors[3],
                topLeft = Offset(halfW, halfH),
                size = Size(w - halfW, h - halfH)
            )
        }
        Text(
            text = letter,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
