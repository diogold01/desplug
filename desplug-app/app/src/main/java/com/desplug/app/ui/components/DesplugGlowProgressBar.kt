package com.desplug.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.desplug.app.ui.theme.DesplugBlue
import com.desplug.app.ui.theme.DesplugRed
import com.desplug.app.ui.theme.DesplugTrackBg
import com.desplug.app.util.rememberIsReducedMotion

@Composable
fun DesplugGlowProgressBar(
    progressFraction: Float, // 0.0f a 1.0f
    modifier: Modifier = Modifier,
    barColor: Color = DesplugBlue,
    isAlert: Boolean = false,
    height: Dp = 6.dp
) {
    val isReducedMotion = rememberIsReducedMotion()
    val animatedProgress = remember { Animatable(if (isReducedMotion) progressFraction else 0f) }
    val effectiveColor = if (isAlert) DesplugRed else barColor

    LaunchedEffect(progressFraction, isReducedMotion) {
        if (!isReducedMotion) {
            animatedProgress.animateTo(
                targetValue = progressFraction.coerceIn(0f, 1f),
                animationSpec = tween(
                    durationMillis = 1200,
                    delayMillis = 400,
                    easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1.0f)
                )
            )
        } else {
            animatedProgress.snapTo(progressFraction.coerceIn(0f, 1f))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(DesplugTrackBg)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            val progressWidth = size.width * animatedProgress.value
            if (progressWidth > 0f) {
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = effectiveColor.toArgb()
                        isAntiAlias = true
                        setShadowLayer(
                            14.dp.toPx(),
                            0f,
                            0f,
                            effectiveColor.toArgb()
                        )
                    }
                    val rect = android.graphics.RectF(
                        0f,
                        0f,
                        progressWidth,
                        size.height
                    )
                    drawRoundRect(rect, (height / 2).toPx(), (height / 2).toPx(), paint)
                }
            }
        }
    }
}

@Composable
fun DesplugStatusPulse(
    modifier: Modifier = Modifier,
    color: Color = DesplugBlue
) {
    val isReducedMotion = rememberIsReducedMotion()
    val transition = rememberInfiniteTransition(label = "statusPulse")

    val pulseScale by if (!isReducedMotion) {
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 2.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulseRingScale"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    val pulseAlpha by if (!isReducedMotion) {
        transition.animateFloat(
            initialValue = 0.8f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulseRingAlpha"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    Box(
        modifier = modifier.size(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!isReducedMotion) {
            Box(
                modifier = Modifier
                    .size(8.dp * pulseScale)
                    .clip(CircleShape)
                    .background(color.copy(alpha = pulseAlpha))
            )
        }
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}
