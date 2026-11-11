package com.desplug.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desplug.app.ui.theme.DesplugNavBg
import com.desplug.app.ui.theme.DesplugNavBorder
import com.desplug.app.ui.theme.DesplugTextSecondary
import com.desplug.app.ui.theme.InterTightFamily

enum class DesplugNavTab(val label: String) {
    HOME("Início"),
    FOCUS("Foco"),
    SCHEDULES("Horários"),
    LIMITS("Limites")
}

// Ícones minimalistas desenhados exatamente como no SVG do mockup
private val HomeNavIcon = ImageVector.Builder(
    name = "Home",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(3f, 11f)
        lineTo(12f, 3f)
        lineTo(21f, 11f)
        verticalLineTo(20f)
        curveTo(21f, 20.55f, 20.55f, 21f, 20f, 21f)
        horizontalLineTo(15f)
        verticalLineTo(15f)
        horizontalLineTo(9f)
        verticalLineTo(21f)
        horizontalLineTo(4f)
        curveTo(3.45f, 21f, 3f, 20.55f, 3f, 20f)
        close()
    }
}.build()

private val FocusNavIcon = ImageVector.Builder(
    name = "Focus",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(12f, 3f)
        lineTo(20f, 6f)
        verticalLineTo(12f)
        curveTo(20f, 17f, 16.5f, 20f, 12f, 21f)
        curveTo(7.5f, 20f, 4f, 17f, 4f, 12f)
        verticalLineTo(6f)
        close()
    }
}.build()

private val SchedulesNavIcon = ImageVector.Builder(
    name = "Schedules",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(12f, 21f)
        curveTo(16.97f, 21f, 21f, 16.97f, 21f, 12f)
        curveTo(21f, 7.03f, 16.97f, 3f, 12f, 3f)
        curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
        curveTo(3f, 16.97f, 7.03f, 21f, 12f, 21f)
        close()
        moveTo(12f, 7f)
        verticalLineTo(12f)
        lineTo(15f, 14f)
    }
}.build()

private val LimitsNavIcon = ImageVector.Builder(
    name = "Limits",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(6f, 3f)
        horizontalLineTo(18f)
        moveTo(6f, 21f)
        horizontalLineTo(18f)
        moveTo(7f, 3f)
        verticalLineTo(7f)
        lineTo(12f, 12f)
        lineTo(7f, 17f)
        verticalLineTo(21f)
        moveTo(17f, 3f)
        verticalLineTo(7f)
        lineTo(12f, 12f)
        lineTo(17f, 17f)
        verticalLineTo(21f)
    }
}.build()

@Composable
fun DesplugFloatingNav(
    selectedTab: DesplugNavTab,
    onTabSelected: (DesplugNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(DesplugNavBg)
            .border(1.dp, DesplugNavBorder, RoundedCornerShape(32.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DesplugNavTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                val icon = when (tab) {
                    DesplugNavTab.HOME -> HomeNavIcon
                    DesplugNavTab.FOCUS -> FocusNavIcon
                    DesplugNavTab.SCHEDULES -> SchedulesNavIcon
                    DesplugNavTab.LIMITS -> LimitsNavIcon
                }

                val itemBg by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color.Transparent,
                    animationSpec = tween(durationMillis = 200),
                    label = "navItemBg"
                )
                val itemColor by animateColorAsState(
                    targetValue = if (isSelected) Color.Black else DesplugTextSecondary,
                    animationSpec = tween(durationMillis = 200),
                    label = "navItemColor"
                )

                Row(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(itemBg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(tab) }
                        .padding(horizontal = if (isSelected) 18.dp else 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.label,
                        tint = itemColor,
                        modifier = Modifier.size(22.dp)
                    )
                    AnimatedVisibility(
                        visible = isSelected,
                        enter = fadeIn(tween(150)),
                        exit = fadeOut(tween(100))
                    ) {
                        Text(
                            text = tab.label,
                            fontFamily = InterTightFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = itemColor,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
