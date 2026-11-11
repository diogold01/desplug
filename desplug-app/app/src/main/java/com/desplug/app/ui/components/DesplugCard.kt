package com.desplug.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.desplug.app.ui.theme.DesplugCardBg
import com.desplug.app.ui.theme.DesplugCardBorder
import com.desplug.app.ui.theme.DesplugHeroGradient
import com.desplug.app.ui.theme.DesplugRedCardBg
import com.desplug.app.ui.theme.DesplugRedCardBorder

enum class DesplugCardType {
    DEFAULT,
    HERO_GRADIENT,
    ALERT
}

@Composable
fun DesplugCard(
    modifier: Modifier = Modifier,
    type: DesplugCardType = DesplugCardType.DEFAULT,
    cornerRadius: Dp = 28.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    val boxModifier = when (type) {
        DesplugCardType.DEFAULT -> {
            modifier
                .clip(shape)
                .background(DesplugCardBg)
                .border(1.dp, DesplugCardBorder, shape)
        }
        DesplugCardType.HERO_GRADIENT -> {
            modifier
                .clip(shape)
                .background(DesplugHeroGradient)
        }
        DesplugCardType.ALERT -> {
            modifier
                .clip(shape)
                .background(DesplugRedCardBg)
                .border(1.dp, DesplugRedCardBorder, shape)
        }
    }

    Box(
        modifier = boxModifier,
        content = content
    )
}
