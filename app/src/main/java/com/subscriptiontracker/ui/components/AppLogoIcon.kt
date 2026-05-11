package com.subscriptiontracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
    val category: String = "",
    val brandColors: List<Color> = listOf(bgColor, bgColor.copy(alpha = 0.82f)),
    val aliases: List<String> = emptyList()
)

data class LogoStyle(
    val label: String,
    val glyph: String,
    val colors: List<Color>,
    val category: String = ""
)

val GoogleBlue = Color(0xFF4285F4)
val GoogleRed = Color(0xFFEA4335)
val GoogleYellow = Color(0xFFFBBC05)
val GoogleGreen = Color(0xFF34A853)

private val AppleMusicPink = Color(0xFFFA2D65)
private val AppleOrange = Color(0xFFFF9500)
private val ICloudBlue = Color(0xFF4DB7FF)
private val BilibiliBlue = Color(0xFF00A1D6)
private val TencentBlue = Color(0xFF12B7F5)

val presetLogos = listOf(
    PresetLogo("spotify", "Spotify", "S", Color(0xFF1DB954), "娱乐", aliases = listOf("声田")),
    PresetLogo("netflix", "Netflix", "N", Color(0xFFE50914), "娱乐"),
    PresetLogo("youtube", "YouTube", "Y", Color(0xFFFF0033), "娱乐", aliases = listOf("油管")),
    PresetLogo("bilibili", "Bilibili", "B", BilibiliBlue, "娱乐", aliases = listOf("哔哩哔哩", "b站")),
    PresetLogo("iqiyi", "爱奇艺", "i", Color(0xFF00D15F), "娱乐", aliases = listOf("iqiyi")),
    PresetLogo("tencentvideo", "腾讯视频", "▶", Color(0xFF00C896), "娱乐", aliases = listOf("腾讯")),
    PresetLogo("youku", "优酷", "Y", Color(0xFF2B7FFF), "娱乐", aliases = listOf("youku")),
    PresetLogo("disney", "Disney+", "D", Color(0xFF113CCF), "娱乐", aliases = listOf("迪士尼")),
    PresetLogo("applemusic", "Apple Music", "♪", AppleMusicPink, "娱乐", aliases = listOf("音乐")),
    PresetLogo("applebooks", "Apple Books", "B", AppleOrange, "学习", aliases = listOf("books", "图书")),
    PresetLogo("ximalaya", "喜马拉雅", "X", Color(0xFFFF5C35), "娱乐"),
    PresetLogo("qqmusic", "QQ音乐", "Q", Color(0xFF31C27C), "娱乐", aliases = listOf("qq音乐")),
    PresetLogo("neteasemusic", "网易云音乐", "N", Color(0xFFE60026), "娱乐", aliases = listOf("网易云")),
    PresetLogo("chatgpt", "ChatGPT", "AI", Color(0xFF10A37F), "工具", aliases = listOf("openai")),
    PresetLogo("claude", "Claude", "C", Color(0xFFD97757), "工具", aliases = listOf("anthropic")),
    PresetLogo("cursor", "Cursor", ">", Color(0xFF111827), "工具"),
    PresetLogo("notion", "Notion", "N", Color(0xFF111111), "工具"),
    PresetLogo("figma", "Figma", "F", Color(0xFFA259FF), "工具",
        brandColors = listOf(Color(0xFFF24E1E), Color(0xFFFF7262), Color(0xFFA259FF), Color(0xFF1ABCFE))),
    PresetLogo("github", "GitHub", "G", Color(0xFF24292F), "工具"),
    PresetLogo("icloud", "iCloud", "☁", ICloudBlue, "工具",
        brandColors = listOf(Color(0xFF8EDCFF), Color(0xFF2F80ED)), aliases = listOf("icloud+", "苹果云")),
    PresetLogo("googleone", "Google One", "G", GoogleBlue, "工具",
        brandColors = listOf(GoogleBlue, GoogleRed, GoogleYellow, GoogleGreen), aliases = listOf("google")),
    PresetLogo("dropbox", "Dropbox", "D", Color(0xFF0061FF), "工具"),
    PresetLogo("microsoft", "Microsoft", "M", Color(0xFF7FBA00), "工具",
        brandColors = listOf(Color(0xFFF25022), Color(0xFF7FBA00), Color(0xFF00A4EF), Color(0xFFFFB900)), aliases = listOf("office", "microsoft 365")),
    PresetLogo("onedrive", "OneDrive", "☁", Color(0xFF0078D4), "工具"),
    PresetLogo("xbox", "Xbox", "X", Color(0xFF107C10), "娱乐"),
    PresetLogo("playstation", "PS", "P", Color(0xFF003791), "娱乐"),
    PresetLogo("steam", "Steam", "S", Color(0xFF171A21), "娱乐"),
    PresetLogo("nintendoswitch", "Nintendo", "N", Color(0xFFE60012), "娱乐", aliases = listOf("switch", "任天堂")),
    PresetLogo("duolingo", "Duolingo", "D", Color(0xFF58CC02), "学习", aliases = listOf("多邻国")),
    PresetLogo("coursera", "Coursera", "C", Color(0xFF0056D2), "学习"),
    PresetLogo("udemy", "Udemy", "U", Color(0xFFA435F0), "学习"),
    PresetLogo("appledeveloper", "Apple开发者", "A", Color(0xFF3B9DFF), "工具", aliases = listOf("developer", "开发者")),
    PresetLogo("strava", "Strava", "S", Color(0xFFFC4C02), "健康"),
    PresetLogo("keepapp", "Keep", "K", Color(0xFF16A085), "健康", aliases = listOf("keep")),
    PresetLogo("calm", "Calm", "C", Color(0xFF3A80F7), "健康"),
    PresetLogo("didi", "滴滴出行", "D", Color(0xFFFF7A00), "生活", aliases = listOf("滴滴")),
    PresetLogo("jdplus", "京东 PLUS", "J", Color(0xFFE1251B), "生活", aliases = listOf("京东", "jd")),
    PresetLogo("taobao", "淘宝", "淘", Color(0xFFFF5000), "生活"),
    PresetLogo("eleme", "饿了么", "E", Color(0xFF0097FF), "生活"),
    PresetLogo("meituan", "美团", "M", Color(0xFFFFD100), "生活"),
    PresetLogo("wechatread", "微信读书", "微", Color(0xFF20B9FF), "学习", aliases = listOf("微信读书")),
    PresetLogo("qq", "QQ", "Q", TencentBlue, "工具"),
    PresetLogo("music", "音乐", "♪", Color(0xFFE74C3C), "娱乐"),
    PresetLogo("video", "视频", "▶", Color(0xFF3498DB), "娱乐"),
    PresetLogo("game", "游戏", "◆", Color(0xFF9B59B6), "娱乐"),
    PresetLogo("ai", "AI", "AI", Color(0xFF1ABC9C), "工具"),
    PresetLogo("cloud", "云盘", "☁", Color(0xFFF39C12), "工具"),
    PresetLogo("tool", "工具", "⚙", Color(0xFF5D6D7E), "工具"),
    PresetLogo("learn", "学习", "书", Color(0xFF2ECC71), "学习"),
    PresetLogo("health", "健康", "♥", Color(0xFFE67E22), "健康"),
    PresetLogo("life", "生活", "●", Color(0xFF2C3E50), "生活"),
    PresetLogo("other", "其他", "✧", Color(0xFF95A5A6), ""),
)

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

fun findPresetLogo(id: String?): PresetLogo? =
    presetLogos.firstOrNull { it.id == id }

fun inferPresetLogo(name: String): PresetLogo? {
    val normalized = name.lowercase().replace(" ", "")
    return presetLogos.firstOrNull { preset ->
        normalized.contains(preset.label.lowercase().replace(" ", "")) ||
            preset.aliases.any { alias -> normalized.contains(alias.lowercase().replace(" ", "")) }
    }
}

fun logoStyleFor(name: String, logoUri: String?): LogoStyle {
    val preset = when {
        logoUri?.startsWith("preset:") == true -> findPresetLogo(logoUri.removePrefix("preset:"))
        else -> inferPresetLogo(name)
    }
    val colors = preset?.brandColors?.takeIf { it.isNotEmpty() } ?: logoGoogleColors(name)
    return LogoStyle(
        label = preset?.label ?: name,
        glyph = preset?.icon ?: name.initialLetter(),
        colors = colors,
        category = preset?.category.orEmpty()
    )
}

fun logoAccentColor(name: String, logoUri: String?): Color =
    logoStyleFor(name, logoUri).colors.first()

fun logoBackgroundColor(name: String, logoUri: String?): Color =
    logoAccentColor(name, logoUri).copy(alpha = 0.12f)

@Composable
fun AppLogoIcon(
    name: String,
    logoUri: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    showBorder: Boolean = true
) {
    val style = logoStyleFor(name, logoUri)

    Box(
        modifier = modifier
            .size(size)
            .clip(LogoShape)
            .background(Color.White)
            .then(
                if (showBorder) Modifier.border(1.dp, Color.Black.copy(alpha = 0.06f), LogoShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            logoUri.isNullOrBlank() || logoUri.startsWith("preset:") -> {
                BrandLetterLogo(
                    letter = style.glyph,
                    colors = style.colors,
                    modifier = Modifier.size(size)
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
                    modifier = Modifier.size(size)
                )
            }
        }
    }
}

@Composable
fun BrandLetterLogo(
    letter: String,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val safeColors = when (colors.size) {
        0 -> logoGoogleColors(letter)
        1 -> listOf(colors[0], colors[0].copy(alpha = 0.78f))
        else -> colors
    }
    val foreground = if (safeColors.first().luminance() > 0.55f) Color(0xFF1F2937) else Color.White

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            if (safeColors.size >= 4) {
                drawGoogleTiles(safeColors)
            } else {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(safeColors[0], safeColors[1]),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    )
                )
            }
            drawCircle(
                color = Color.White.copy(alpha = 0.18f),
                radius = size.minDimension * 0.46f,
                center = Offset(size.width * 0.82f, size.height * 0.12f)
            )
        }
        Text(
            text = letter.take(2),
            fontSize = if (letter.length > 1) 15.sp else 19.sp,
            fontWeight = FontWeight.Bold,
            color = foreground,
            textAlign = TextAlign.Center
        )
    }
}

private fun DrawScope.drawGoogleTiles(colors: List<Color>) {
    val w = size.width
    val h = size.height
    val halfW = w / 2
    val halfH = h / 2
    drawRect(colors[0], topLeft = Offset.Zero, size = Size(halfW, halfH))
    drawRect(colors[1], topLeft = Offset(halfW, 0f), size = Size(w - halfW, halfH))
    drawRect(colors[2], topLeft = Offset(0f, halfH), size = Size(halfW, h - halfH))
    drawRect(colors[3], topLeft = Offset(halfW, halfH), size = Size(w - halfW, h - halfH))

    val sweep = Path().apply {
        moveTo(w * 0.18f, h)
        cubicTo(w * 0.42f, h * 0.58f, w * 0.62f, h * 0.42f, w, h * 0.2f)
        lineTo(w, h)
        close()
    }
    drawPath(sweep, Color.White.copy(alpha = 0.16f))
}
