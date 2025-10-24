package com.blackhat.multistep.ui.components

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blackhat.multistep.R
import com.blackhat.multistep.ui.theme.HackerFontFamily
import com.blackhat.multistep.ui.theme.HackerTheme
import kotlinx.coroutines.delay
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Speech bubble shape:
 * - Rounded rect
 * - Little triangular "tail"
 */
private object BubbleShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        // Use dp-friendly values
        val cornerRadiusPx = with(density) { 24.dp.toPx() }
        val tailWidthPx = with(density) { 24.dp.toPx() }
        val tailHeightPx = with(density) { 18.dp.toPx() }
        val tailOffsetXPx = size.width * 0.3f // where tail sits along bottom edge

        val path = Path().apply {
            // Base rounded rect (shorter in height so we have room for tail)
            addRoundRect(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height - tailHeightPx,
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                )
            )
            // Tail triangle
            moveTo(tailOffsetXPx, size.height - tailHeightPx)
            lineTo(tailOffsetXPx + tailWidthPx / 2f, size.height)
            lineTo(tailOffsetXPx + tailWidthPx, size.height - tailHeightPx)
            close()
        }

        return Outline.Generic(path)
    }
}

/**
 * Animated typewriter-style text for the message.
 */
@Composable
private fun AnimatedMessageText(
    fullText: String,
    textColor: Color,
) {
    var visibleChars by remember(fullText) { mutableStateOf(0) }

    // Reveal characters over time
    LaunchedEffect(fullText) {
        visibleChars = 0
        while (visibleChars < fullText.length) {
            delay(40) // ms per char - tweak for faster/slower "typing"
            visibleChars++
        }
    }

    Text(
        text = fullText.take(visibleChars),
        style = TextStyle(
            fontFamily = HackerFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            color = textColor
        )
    )
}

/**
 * The speech bubble itself with animated pop-in (scale + fade).
 */
@Composable
private fun AnimatedSpeechBubble(
    message: String,
    bubbleColor: Color,
    outlineColor: Color
) {
    var startAnim by remember { mutableStateOf(false) }

    // Kick off animation when composable enters
    LaunchedEffect(Unit) { startAnim = true }

    val bubbleScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bubbleScale"
    )
    val bubbleAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "bubbleAlpha"
    )

    Box(
        modifier = Modifier
            .wrapContentSize(align = Alignment.Center)
            .graphicsLayer {
                scaleX = bubbleScale
                scaleY = bubbleScale
                alpha = bubbleAlpha
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f) // anchor bottom center
            },
        contentAlignment = Alignment.Center
    ) {
        // Bubble background + stroke "glow"
        Box(
            modifier = Modifier
                .background(color = outlineColor, shape = BubbleShape) // thin outer glow
                .padding(2.dp) // stroke thickness
        ) {
            Box(
                modifier = Modifier
                    .background(color = bubbleColor, shape = BubbleShape)
                    .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 34.dp)
            ) {
                AnimatedMessageText(
                    fullText = message,
                    textColor = Color.White
                )
            }
        }
    }
}

/**
 * Full screen scene:
 * - gradient dark background
 * - fedora-android logo
 * - animated speech bubble with message
 *
 * onScreenTap() lets us hop Activity if needed.
 */
@Composable
fun SpeechScene(
    message: String,
    onScreenTap: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.background,   // top dark
                        colorScheme.surface.copy(alpha = 0.2f),
                        Color(0xFF1A2A2F)        // bottom deep teal-ish
                    )
                )
            )
            .clickable { onScreenTap() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedSpeechBubble(
                message = message,
                bubbleColor = MaterialTheme.colorScheme.surface, // BubbleBg
                outlineColor = MaterialTheme.colorScheme.primary // neon outline
            )
            // Gap between bubble and droid
            Spacer(modifier = Modifier.height(32.dp))
            // Fedora Android logo
            Image(
                painter = painterResource(id = R.drawable.ic_hacker_droid),
                contentDescription = "Hacker Android",
                modifier = Modifier
                    .size(180.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun HackerScreen(
    message: String,
    onScreenTap: () -> Unit
) {
    HackerTheme {
        SpeechScene(
            message = message,
            onScreenTap = onScreenTap
        )
    }
}