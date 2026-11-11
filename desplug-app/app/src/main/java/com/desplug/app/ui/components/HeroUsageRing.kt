package com.desplug.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.desplug.app.ui.theme.DesplugBlue
import com.desplug.app.ui.theme.DesplugRingTrackBg
import com.desplug.app.ui.theme.DesplugTextPrimary
import com.desplug.app.ui.theme.DesplugTextSecondary
import com.desplug.app.ui.theme.InterTightFamily
import com.desplug.app.util.rememberIsReducedMotion

@Composable
fun HeroUsageRing(
    usedTimeText: String,
    subtitleText: String,
    progressFraction: Float, // 0.0f a 1.0f (ex: 0.58f)
    modifier: Modifier = Modifier
) {
    val isReducedMotion = rememberIsReducedMotion()
    val animatedProgress = remember { Animatable(if (isReducedMotion) progressFraction else 0f) }

    LaunchedEffect(progressFraction, isReducedMotion) {
        if (!isReducedMotion) {
            animatedProgress.animateTo(
                targetValue = progressFraction.coerceIn(0f, 1f),
                animationSpec = tween(
                    durationMillis = 1800,
                    delayMillis = 300,
                    easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1.0f)
                )
            )
        } else {
            animatedProgress.snapTo(progressFraction.coerceIn(0f, 1f))
        }
    }

    val transition = rememberInfiniteTransition(label = "ringSpin")
    val dottedRotation by if (!isReducedMotion) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 60_000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "spin60s"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    Box(
        modifier = modifier.size(232.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(232.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = 92f * (size.width / 200f)
            val trackRadius = 78f * (size.width / 200f)
            val strokeWidthPx = 9f * (size.width / 200f)
            val dottedWidthPx = 1.5f * (size.width / 200f)

            // 1. Anel externo pontilhado giratório (60s)
            rotate(degrees = dottedRotation, pivot = center) {
                drawCircle(
                    color = DesplugBlue.copy(alpha = 0.45f),
                    radius = outerRadius,
                    center = center,
                    style = Stroke(
                        width = dottedWidthPx,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(2.dp.toPx(), 9.dp.toPx()),
                            phase = 0f
                        ),
                        cap = StrokeCap.Round
                    )
                )
            }

            // 2. Trilha interna (#10162A)
            drawCircle(
                color = DesplugRingTrackBg,
                radius = trackRadius,
                center = center,
                style = Stroke(width = strokeWidthPx)
            )

            // 3. Arco de progresso ativo (#4C7DFF com glow)
            val sweepAngle = 360f * animatedProgress.value
            if (sweepAngle > 0f) {
                // Desenho com glow nativo
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#4C7DFF")
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = strokeWidthPx
                        strokeCap = android.graphics.Paint.Cap.ROUND
                        isAntiAlias = true
                        setShadowLayer(8.dp.toPx(), 0f, 0f, android.graphics.Color.parseColor("#4C7DFF"))
                    }
                    val arcRect = android.graphics.RectF(
                        center.x - trackRadius,
                        center.y - trackRadius,
                        center.x + trackRadius,
                        center.y + trackRadius
                    )
                    drawArc(arcRect, -90f, sweepAngle, false, paint)
                }
            }
        }

        // 4. Texto Central
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = usedTimeText,
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 58.sp,
                lineHeight = 58.sp,
                letterSpacing = (-0.05).em,
                color = DesplugTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitleText,
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                color = DesplugTextSecondary
            )
        }
    }
}
