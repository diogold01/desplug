package com.desplug.app.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desplug.app.ui.theme.InterTightFamily
import kotlin.math.roundToInt

// Cores do degradê contínuo extraídas de slider-limite-diario.html
val LimitSliderStops = listOf(
    Color(0xFF22D3EE), // Ciano (0%)
    Color(0xFF3B82F6), // Azul (25%)
    Color(0xFF8B5CF6), // Roxo (50%)
    Color(0xFFD946EF), // Magenta (70%)
    Color(0xFFFB7185), // Rosa (85%)
    Color(0xFFFB923C)  // Laranja (100%)
)

/**
 * Interpola suavemente a cor com base no progresso 0f..1f
 */
fun interpolateLimitColor(fraction: Float): Color {
    val clamped = fraction.coerceIn(0f, 1f)
    val scaled = clamped * (LimitSliderStops.size - 1)
    val index = scaled.toInt().coerceAtMost(LimitSliderStops.size - 2)
    val localFraction = scaled - index
    return lerp(LimitSliderStops[index], LimitSliderStops[index + 1], localFraction)
}

/**
 * Formata minutos em formato amigável para o balão (ex: "15m", "1h", "2h30m", "4h")
 */
fun formatLimitDuration(minutes: Int): String {
    return if (minutes < 60) {
        "${minutes}m"
    } else {
        val h = minutes / 60
        val m = minutes % 60
        if (m == 0) "${h}h" else "${h}h${m}m"
    }
}

@Composable
fun ModernLimitSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    min: Float = 5f,
    max: Float = 240f,
    step: Float = 5f,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    var isDragging by remember { mutableStateOf(false) }
    var trackWidthPx by remember { mutableFloatStateOf(1f) }

    val currentProgress = ((value - min) / (max - min)).coerceIn(0f, 1f)
    val currentColor = interpolateLimitColor(currentProgress)

    val thumbScaleY by animateFloatAsState(
        targetValue = if (isDragging) 1.25f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "thumbScale"
    )

    val bubbleAlpha by animateFloatAsState(
        targetValue = if (isDragging && enabled) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 180),
        label = "bubbleAlpha"
    )

    val bubbleScale by animateFloatAsState(
        targetValue = if (isDragging && enabled) 1.0f else 0.8f,
        animationSpec = tween(durationMillis = 180),
        label = "bubbleScale"
    )

    fun updateValueFromX(x: Float) {
        if (!enabled || trackWidthPx <= 0f) return
        val rawP = (x / trackWidthPx).coerceIn(0f, 1f)
        val rawVal = min + rawP * (max - min)
        val steppedVal = ((rawVal / step).roundToInt() * step).coerceIn(min, max)
        if (steppedVal != value) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onValueChange(steppedVal)
        }
    }

    val alphaMultiplier = if (enabled) 1.0f else 0.35f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = alphaMultiplier }
    ) {
        // Container do Slider + Balão Flutuante
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectTapGestures(
                        onPress = { offset ->
                            isDragging = true
                            updateValueFromX(offset.x)
                            tryAwaitRelease()
                            isDragging = false
                        }
                    )
                }
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            updateValueFromX(offset.x)
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            updateValueFromX(change.position.x)
                        }
                    )
                }
        ) {
            val trackHeightDp = 16.dp
            val trackHeightPx = with(density) { trackHeightDp.toPx() }
            val dotRadiusPx = with(density) { 1.3.dp.toPx() }
            val dotSpacingPx = with(density) { 9.dp.toPx() }
            val thumbWidthDp = 6.dp
            val thumbHeightDp = 32.dp

            // 1. Canvas de desenho do Trilho, Gradiente Preso, Pontilhados e Glow
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeightDp)
                    .align(Alignment.Center)
            ) {
                trackWidthPx = size.width
                val cornerRadius = CornerRadius(trackHeightPx / 2f, trackHeightPx / 2f)

                // Trilho Inativo (Fundo translúcido)
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.06f),
                    size = size,
                    cornerRadius = cornerRadius
                )

                // Pontinhos claros no trilho inativo
                var dotX = dotSpacingPx / 2f
                val cy = size.height / 2f
                while (dotX < size.width) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.20f),
                        radius = dotRadiusPx,
                        center = Offset(dotX, cy)
                    )
                    dotX += dotSpacingPx
                }

                // Preenchimento com Gradiente Fixo (não estica)
                val fillWidth = size.width * currentProgress
                if (fillWidth > 0f) {
                    // Glow no preenchimento ativo
                    drawIntoCanvas { canvas ->
                        val glowPaint = Paint().apply {
                            isAntiAlias = true
                            color = currentColor.copy(alpha = 0.6f).toArgb()
                            setShadowLayer(
                                with(density) { 18.dp.toPx() },
                                0f,
                                0f,
                                currentColor.copy(alpha = 0.65f).toArgb()
                            )
                        }
                        canvas.nativeCanvas.drawRoundRect(
                            0f,
                            0f,
                            fillWidth,
                            size.height,
                            trackHeightPx / 2f,
                            trackHeightPx / 2f,
                            glowPaint
                        )
                    }

                    // Gradiente que ocupa a largura total (startX = 0, endX = size.width)
                    val fixedGradient = Brush.horizontalGradient(
                        colors = LimitSliderStops,
                        startX = 0f,
                        endX = size.width
                    )

                    drawRoundRect(
                        brush = fixedGradient,
                        size = Size(fillWidth, size.height),
                        cornerRadius = cornerRadius
                    )

                    // Pontinhos escuros por cima da barra colorida ativa
                    var activeDotX = dotSpacingPx / 2f
                    while (activeDotX < fillWidth) {
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.32f),
                            radius = dotRadiusPx,
                            center = Offset(activeDotX, cy)
                        )
                        activeDotX += dotSpacingPx
                    }
                }
            }

            // 2. Marcador Thumb (Pílula branca vertical com halo)
            val thumbXPx = trackWidthPx * currentProgress
            val thumbXDp = with(density) { thumbXPx.toDp() }

            Box(
                modifier = Modifier
                    .offset(x = thumbXDp - (thumbWidthDp / 2))
                    .align(Alignment.CenterStart)
                    .graphicsLayer {
                        this.scaleY = thumbScaleY
                    }
                    .shadow(
                        elevation = if (isDragging) 16.dp else 10.dp,
                        shape = RoundedCornerShape(6.dp),
                        spotColor = currentColor,
                        ambientColor = currentColor
                    )
                    .height(thumbHeightDp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .padding(horizontal = thumbWidthDp / 2)
            )

            // 3. Balão Flutuante acima do Thumb (Bubble)
            if (bubbleAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (thumbXPx - with(density) { 24.dp.toPx() }).roundToInt(),
                                y = with(density) { -4.dp.toPx() }.roundToInt()
                            )
                        }
                        .graphicsLayer {
                            alpha = bubbleAlpha
                            scaleX = bubbleScale
                            scaleY = bubbleScale
                        }
                        .shadow(12.dp, RoundedCornerShape(999.dp), spotColor = Color.Black)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatLimitDuration(value.toInt()),
                        fontFamily = InterTightFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF0B0B10)
                    )
                }
            }
        }

        // 4. Escala Inferior (5m, 1h, 2h, 3h, 4h)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("5m", "1h", "2h", "3h", "4h").forEach { mark ->
                Text(
                    text = mark,
                    fontFamily = InterTightFamily,
                    fontSize = 11.sp,
                    color = Color(0xFF6A6A78)
                )
            }
        }
    }
}
