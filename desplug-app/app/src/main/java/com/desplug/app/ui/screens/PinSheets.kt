package com.desplug.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.desplug.app.ui.components.DesplugPrimaryButton
import com.desplug.app.ui.theme.DesplugBackground
import com.desplug.app.ui.theme.DesplugBlue
import com.desplug.app.ui.theme.DesplugChipBorder
import com.desplug.app.ui.theme.DesplugRed
import com.desplug.app.ui.theme.DesplugSheetBg
import com.desplug.app.ui.theme.DesplugSheetBorder
import com.desplug.app.ui.theme.DesplugSheetHandle
import com.desplug.app.ui.theme.DesplugTextPrimary
import com.desplug.app.ui.theme.DesplugTextSecondary
import com.desplug.app.ui.theme.InterTightFamily
import com.desplug.app.util.rememberIsReducedMotion

@Composable
fun PinSetupSheet(
    hasExistingPin: Boolean,
    onDismiss: () -> Unit,
    onSavePin: (String) -> Unit
) {
    BackHandler(onBack = onDismiss)

    val isReducedMotion = rememberIsReducedMotion()
    val density = LocalDensity.current
    val slideStartPx = with(density) { 500.dp.toPx() }
    val sheetOffsetY = remember { Animatable(if (isReducedMotion) 0f else slideStartPx) }

    LaunchedEffect(Unit, isReducedMotion) {
        if (!isReducedMotion) {
            sheetOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = CubicBezierEasing(0.2f, 0.9f, 0.2f, 1.0f)
                )
            )
        }
    }

    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesplugBackground.copy(alpha = 0.88f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        val sheetShape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationY = sheetOffsetY.value }
                .clip(sheetShape)
                .background(DesplugSheetBg)
                .border(1.dp, DesplugSheetBorder, sheetShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consome clique */ }
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 20.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DesplugSheetHandle)
            )

            Text(
                text = if (hasExistingPin) "Alterar PIN" else "Criar PIN",
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 34.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.04).em,
                color = DesplugTextPrimary
            )

            Text(
                text = "Defina um PIN numérico de 4 a 6 dígitos para proteger suas sessões de foco.",
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = DesplugTextSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )

            PinInputField(
                value = pin,
                onValueChange = {
                    pin = it.filter { ch -> ch.isDigit() }.take(6)
                    errorMessage = null
                },
                placeholder = "Novo PIN (4 a 6 dígitos)",
                isError = errorMessage != null
            )

            Spacer(modifier = Modifier.height(12.dp))

            PinInputField(
                value = confirmPin,
                onValueChange = {
                    confirmPin = it.filter { ch -> ch.isDigit() }.take(6)
                    errorMessage = null
                },
                placeholder = "Confirmar novo PIN",
                isError = errorMessage != null
            )

            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    fontFamily = InterTightFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = DesplugRed,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            DesplugPrimaryButton(
                text = "Salvar PIN",
                onClick = {
                    when {
                        pin.length < 4 -> errorMessage = "O PIN deve ter pelo menos 4 dígitos."
                        pin != confirmPin -> errorMessage = "Os PINs informados não coincidem."
                        else -> onSavePin(pin)
                    }
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(44.dp)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancelar",
                    fontFamily = InterTightFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = DesplugTextSecondary
                )
            }
        }
    }
}

@Composable
fun UnlockPinSheet(
    onDismiss: () -> Unit,
    onVerifyPin: (String) -> Boolean
) {
    BackHandler(onBack = onDismiss)

    val isReducedMotion = rememberIsReducedMotion()
    val density = LocalDensity.current
    val slideStartPx = with(density) { 500.dp.toPx() }
    val sheetOffsetY = remember { Animatable(if (isReducedMotion) 0f else slideStartPx) }

    LaunchedEffect(Unit, isReducedMotion) {
        if (!isReducedMotion) {
            sheetOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = CubicBezierEasing(0.2f, 0.9f, 0.2f, 1.0f)
                )
            )
        }
    }

    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesplugBackground.copy(alpha = 0.88f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        val sheetShape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationY = sheetOffsetY.value }
                .clip(sheetShape)
                .background(DesplugSheetBg)
                .border(1.dp, DesplugSheetBorder, sheetShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consome clique */ }
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 20.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DesplugSheetHandle)
            )

            Text(
                text = "Confirmar com PIN",
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 34.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.04).em,
                color = DesplugTextPrimary
            )

            Text(
                text = "Digite seu PIN de segurança. Após confirmar, a espera obrigatória de 5 minutos será iniciada.",
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = DesplugTextSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )

            PinInputField(
                value = pin,
                onValueChange = {
                    pin = it.filter { ch -> ch.isDigit() }.take(6)
                    isError = false
                },
                placeholder = "Digite seu PIN",
                isError = isError
            )

            if (isError) {
                Text(
                    text = "PIN incorreto. Tente novamente.",
                    fontFamily = InterTightFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = DesplugRed,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            DesplugPrimaryButton(
                text = "Iniciar Espera de 5 min",
                onClick = {
                    val ok = onVerifyPin(pin)
                    if (!ok) isError = true
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(44.dp)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancelar",
                    fontFamily = InterTightFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = DesplugTextSecondary
                )
            }
        }
    }
}

@Composable
private fun PinInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean
) {
    val borderColor = if (isError) DesplugRed else DesplugChipBorder
    val shape = RoundedCornerShape(26.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                color = DesplugTextSecondary
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            textStyle = TextStyle(
                fontFamily = InterTightFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                letterSpacing = 0.25.em,
                color = DesplugTextPrimary,
                textAlign = TextAlign.Start
            ),
            cursorBrush = SolidColor(DesplugBlue),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
