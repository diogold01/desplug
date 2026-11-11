package com.desplug.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desplug.app.ui.theme.DesplugChipBorder
import com.desplug.app.ui.theme.InterTightFamily

@Composable
fun DesplugChip(
    text: String,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(17.dp)
    val bgColor = if (isSelected) Color.White else Color.Transparent
    val textColor = if (isSelected) Color.Black else Color.White
    val borderColor = if (isSelected) Color.White else DesplugChipBorder

    Row(
        modifier = modifier
            .height(34.dp)
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (leadingIcon != null) {
            leadingIcon()
        }
        Text(
            text = text,
            fontFamily = InterTightFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = textColor
        )
    }
}
