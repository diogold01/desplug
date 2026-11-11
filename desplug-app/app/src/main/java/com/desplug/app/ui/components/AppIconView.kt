package com.desplug.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desplug.app.ui.theme.DesplugBlueMuted
import com.desplug.app.ui.theme.DesplugRingTrackBg
import com.desplug.app.ui.theme.InterTightFamily
import com.desplug.app.util.AppIconCache

@Composable
fun AppIconView(
    packageName: String,
    appName: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val context = LocalContext.current
    val appIcon = remember(packageName) { AppIconCache.getIcon(context, packageName) }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(DesplugRingTrackBg),
        contentAlignment = Alignment.Center
    ) {
        if (appIcon != null) {
            Image(
                bitmap = appIcon,
                contentDescription = appName,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback com a inicial como no mockup HTML
            val initial = appName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Text(
                text = initial,
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = (size.value * 0.42f).sp,
                color = DesplugBlueMuted
            )
        }
    }
}
