package com.desplug.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.desplug.app.ui.theme.DesplugBlue
import com.desplug.app.util.rememberIsReducedMotion
import kotlin.math.PI
import kotlin.math.sin

data class ParticleData(
    val x: Float,
    val y: Float,
    val rotation: Float,
    val delaySec: Float
)

private val particles = listOf(
    ParticleData(131f, 300f, 206f, -2.9f),
    ParticleData(327f, 253f, 116f, -1.6f),
    ParticleData(331f, 240f, 110f, -0.2f),
    ParticleData(76f, 235f, 246f, -0.4f),
    ParticleData(34f, 265f, 242f, -0.6f),
    ParticleData(215f, 344f, 170f, -4.3f),
    ParticleData(60f, 118f, 297f, -4.4f),
    ParticleData(357f, 236f, 106f, -1.3f),
    ParticleData(266f, 285f, 141f, -1.4f),
    ParticleData(241f, 66f, 383f, -2.6f),
    ParticleData(96f, 76f, 320f, -2.5f),
    ParticleData(301f, 232f, 112f, -0.9f),
    ParticleData(126f, 52f, 334f, -1.4f),
    ParticleData(61f, 109f, 300f, -1.3f),
    ParticleData(234f, 26f, 375f, -1.1f),
    ParticleData(51f, 116f, 296f, -3.9f),
    ParticleData(171f, 49f, 352f, -4.4f),
    ParticleData(296f, 284f, 132f, -3.4f),
    ParticleData(276f, 309f, 144f, -0.2f),
    ParticleData(105f, 37f, 330f, -2.6f),
    ParticleData(287f, 87f, 405f, -3.1f),
    ParticleData(58f, 98f, 303f, -2.1f),
    ParticleData(286f, 32f, 392f, -2.1f),
    ParticleData(126f, 81f, 329f, -3.2f),
    ParticleData(77f, 37f, 322f, -3.7f),
    ParticleData(157f, 326f, 192f, -3.0f),
    ParticleData(336f, 207f, 98f, -0.8f),
    ParticleData(279f, 267f, 132f, -3.5f),
    ParticleData(281f, 283f, 136f, -1.8f),
    ParticleData(274f, 96f, 403f, -2.0f),
    ParticleData(19f, 131f, 287f, -3.7f),
    ParticleData(278f, 82f, 401f, -1.9f),
    ParticleData(76f, 324f, 219f, -4.3f),
    ParticleData(264f, 291f, 144f, -1.0f),
    ParticleData(204f, 336f, 173f, -2.7f),
    ParticleData(179f, 303f, 184f, -1.9f),
    ParticleData(82f, 300f, 222f, -4.3f),
    ParticleData(133f, 43f, 338f, -2.8f),
    ParticleData(134f, 77f, 333f, -4.0f),
    ParticleData(222f, 11f, 370f, -3.6f),
    ParticleData(75f, 276f, 231f, -0.5f),
    ParticleData(107f, 94f, 318f, -0.3f),
    ParticleData(222f, 310f, 165f, -1.5f),
    ParticleData(300f, 224f, 108f, -0.7f),
    ParticleData(303f, 270f, 126f, -0.1f),
    ParticleData(301f, 72f, 404f, -0.7f)
)

@Composable
fun ParticleCloudCanvas(
    modifier: Modifier = Modifier
) {
    val isReducedMotion = rememberIsReducedMotion()
    val transition = rememberInfiniteTransition(label = "particleCloud")

    val rotationAngle by if (!isReducedMotion) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 140_000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "spin140s"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    val twinklePhase by if (!isReducedMotion) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "twinkle4.5s"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    Canvas(modifier = modifier.size(380.dp)) {
        val centerOffset = Offset(size.width / 2f, size.height / 2f)
        val scaleX = size.width / 380f
        val scaleY = size.height / 380f
        val particleW = 3.dp.toPx()
        val particleH = 8.dp.toPx()
        val cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())

        rotate(degrees = rotationAngle, pivot = centerOffset) {
            for (p in particles) {
                val posX = p.x * scaleX
                val posY = p.y * scaleY

                val alpha = if (isReducedMotion) {
                    0.5f
                } else {
                    val phase = twinklePhase - (p.delaySec * (2 * PI / 4.5f).toFloat())
                    val normalizedSine = (sin(phase.toDouble()).toFloat() + 1f) / 2f
                    0.08f + (0.87f * normalizedSine)
                }

                rotate(degrees = p.rotation, pivot = Offset(posX + particleW / 2f, posY + particleH / 2f)) {
                    drawRoundRect(
                        color = DesplugBlue.copy(alpha = alpha.coerceIn(0.08f, 0.95f)),
                        topLeft = Offset(posX, posY),
                        size = Size(particleW, particleH),
                        cornerRadius = cornerRadius
                    )
                }
            }
        }
    }
}
