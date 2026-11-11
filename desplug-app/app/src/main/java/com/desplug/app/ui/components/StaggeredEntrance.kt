package com.desplug.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.desplug.app.util.rememberIsReducedMotion

@Composable
fun StaggeredFadeUp(
    delayMillis: Int,
    modifier: Modifier = Modifier,
    slideDistance: Dp = 18.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isReducedMotion = rememberIsReducedMotion()
    val density = LocalDensity.current
    val slidePx = with(density) { slideDistance.toPx() }

    val alphaAnim = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val translateYAnim = remember { Animatable(if (isReducedMotion) 0f else slidePx) }

    LaunchedEffect(Unit, isReducedMotion) {
        if (!isReducedMotion) {
            val spec = tween<Float>(
                durationMillis = 800,
                delayMillis = delayMillis,
                easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1.0f)
            )
            alphaAnim.animateTo(1f, animationSpec = spec)
        }
    }

    LaunchedEffect(Unit, isReducedMotion) {
        if (!isReducedMotion) {
            val spec = tween<Float>(
                durationMillis = 800,
                delayMillis = delayMillis,
                easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1.0f)
            )
            translateYAnim.animateTo(0f, animationSpec = spec)
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            alpha = alphaAnim.value
            translationY = translateYAnim.value
        },
        content = content
    )
}
