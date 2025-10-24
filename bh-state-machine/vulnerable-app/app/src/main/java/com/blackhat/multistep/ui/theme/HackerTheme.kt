package com.blackhat.multistep.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.blackhat.multistep.R

// 1. Colors
private val HackerGreen = Color(0xFF00FF7A)
private val HackerBgTop = Color(0xFF0A0F10)   // near-black teal
private val HackerBgBottom = Color(0xFF1A2A2F) // deep desaturated teal
private val BubbleBg = Color(0xFF1F2E34)      // speech bubble fill
private val BubbleStroke = Color(0xFF00D16B)  // outline glow-y green
private val TextWhite = Color(0xFFFFFFFF)

val HackerColors = darkColorScheme(
    primary = HackerGreen,
    onPrimary = Color.Black,
    background = HackerBgTop,
    surface = BubbleBg,
    onSurface = TextWhite
)

// 2. FontFamily
val HackerFontFamily = FontFamily(
    Font(
        resId = R.font.pixeltype,
        weight = FontWeight.SemiBold
    )
)

// 3. Typography using that font
private val HackerTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = HackerFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        color = TextWhite
    ),
    headlineMedium = TextStyle(
        fontFamily = HackerFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        color = TextWhite
    )
)

@Composable
fun HackerTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // we're always dark, but we keep the param for flexibility
    MaterialTheme(
        colorScheme = HackerColors,
        typography = HackerTypography,
        content = content
    )
}