package com.desplug.app

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.desplug.app.data.BlockPreferences
import com.desplug.app.data.model.AppBlockRule
import com.desplug.app.data.model.QuickBlockState
import com.desplug.app.data.model.ScheduleRule
import com.desplug.app.receiver.DesplugDeviceAdminReceiver
import com.desplug.app.service.MonitorForegroundService
import com.desplug.app.ui.theme.DesplugTheme
import com.desplug.app.util.PermissionHelper
import com.desplug.app.util.UsageStatsCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Calendar

data class InstalledAppItem(
    val packageName: String,
    val label: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MonitorForegroundService.startServiceIfNeeded(this)

        setContent {
            DesplugTheme(darkTheme = true) {
                DesplugMainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesplugMainApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Quick Block, 1 = Horários, 2 = Limites Diários
    var isBlockingEnabled by remember { mutableStateOf(BlockPreferences.isBlockingEnabled(context)) }
    var isAntiTamperEnabled by remember { mutableStateOf(BlockPreferences.isAntiTamperEnabled(context)) }
    var isDeviceAdminActive by remember { mutableStateOf(PermissionHelper.isDeviceAdminActive(context)) }
    var hasAccessibility by remember { mutableStateOf(PermissionHelper.isAccessibilityServiceEnabled(context)) }
    var hasUsageStats by remember { mutableStateOf(PermissionHelper.hasUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf(PermissionHelper.hasOverlayPermission(context)) }
    var hasBatteryExemption by remember { mutableStateOf(PermissionHelper.isIgnoringBatteryOptimizations(context)) }
    var resetHour by remember { mutableIntStateOf(BlockPreferences.getDayResetHour(context)) }

    var quickBlockState by remember { mutableStateOf(BlockPreferences.getQuickBlockState(context)) }
    var scheduleRules by remember { mutableStateOf(BlockPreferences.getAllScheduleRules(context)) }
    var appRules by remember { mutableStateOf(BlockPreferences.getAllRules(context)) }
    var usageMap by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var installedApps by remember { mutableStateOf<List<InstalledAppItem>>(emptyList()) }

    var scheduleToEdit by remember { mutableStateOf<ScheduleRule?>(null) }
    var isCreatingSchedule by remember { mutableStateOf(false) }
    var appToConfigure by remember { mutableStateOf<InstalledAppItem?>(null) }
    var showResetHourDialog by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showUnlockPinDialog by remember { mutableStateOf(false) }
    var showStartQuickBlockDialog by remember { mutableStateOf(false) }

    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var totalAttemptsToday by remember { mutableIntStateOf(BlockPreferences.getTotalAttemptsToday(context)) }

    val deviceAdminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isDeviceAdminActive = PermissionHelper.isDeviceAdminActive(context)
    }

    fun refreshState() {
        currentTimeMillis = System.currentTimeMillis()
        isBlockingEnabled = BlockPreferences.isBlockingEnabled(context)
        isAntiTamperEnabled = BlockPreferences.isAntiTamperEnabled(context)
        isDeviceAdminActive = PermissionHelper.isDeviceAdminActive(context)
        hasAccessibility = PermissionHelper.isAccessibilityServiceEnabled(context)
        hasUsageStats = PermissionHelper.hasUsageStatsPermission(context)
        hasOverlay = PermissionHelper.hasOverlayPermission(context)
        hasBatteryExemption = PermissionHelper.isIgnoringBatteryOptimizations(context)
        resetHour = BlockPreferences.getDayResetHour(context)
        quickBlockState = BlockPreferences.getQuickBlockState(context)
        scheduleRules = BlockPreferences.getAllScheduleRules(context)
        appRules = BlockPreferences.getAllRules(context)
        totalAttemptsToday = BlockPreferences.getTotalAttemptsToday(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Timer contínuo para contagem regressiva viva na tela a cada segundo
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTimeMillis = System.currentTimeMillis()
            quickBlockState = BlockPreferences.getQuickBlockState(context)
            isDeviceAdminActive = PermissionHelper.isDeviceAdminActive(context)
            totalAttemptsToday = BlockPreferences.getTotalAttemptsToday(context)
        }
    }

    LaunchedEffect(Unit) {
        installedApps = withContext(Dispatchers.IO) {
            loadLaunchableApps(context)
        }
        usageMap = withContext(Dispatchers.IO) {
            UsageStatsCalculator.getAllUsageTodayInMinutes(context, resetHour)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0D1117),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Desplug",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                        Text(
                            text = when {
                                quickBlockState.isActive && quickBlockState.isCoolingDown(currentTimeMillis) -> {
                                    val rem = quickBlockState.remainingCooldownSeconds(currentTimeMillis)
                                    "Espera de desbloqueio: %02d:%02d".format(rem / 60, rem % 60)
                                }
                                quickBlockState.isActive -> "Quick Block Ativo • Foco Imediato"
                                else -> "Proteção Ativa • $totalAttemptsToday tentativa(s) bloqueada(s) hoje"
                            },
                            color = if (quickBlockState.isActive) Color(0xFFEF4444) else Color(0xFF10B981),
                            fontSize = 12.sp
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showPinSetupDialog = true }) {
                        Text(
                            text = if (BlockPreferences.hasPinConfigured(context)) "PIN Ativo" else "Definir PIN",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF161B22))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Seletor de Abas
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF161B22),
                contentColor = Color(0xFF10B981),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF10B981)
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Quick Block",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) Color.White else Color(0xFF9CA3AF)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Horários (${scheduleRules.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) Color.White else Color(0xFF9CA3AF)
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "Limites (${appRules.size})",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 2) Color.White else Color(0xFF9CA3AF)
                        )
                    }
                )
            }

            when (selectedTab) {
                0 -> {
                    // ABA 0: QUICK BLOCK & PROTEÇÃO ANTI-BURLA
                    val hasAnyActiveBlock = BlockPreferences.hasAnyActiveProtectionNow(context)
                    QuickBlockTabContent(
                        state = quickBlockState,
                        currentTimeMillis = currentTimeMillis,
                        hasPin = BlockPreferences.hasPinConfigured(context),
                        isDeviceAdminActive = isDeviceAdminActive,
                        isAntiTamperEnabled = isAntiTamperEnabled,
                        hasAnyActiveBlock = hasAnyActiveBlock,
                        hasAccessibility = hasAccessibility,
                        hasUsageStats = hasUsageStats,
                        hasOverlay = hasOverlay,
                        hasBatteryExemption = hasBatteryExemption,
                        onActivateDeviceAdmin = {
                            BlockPreferences.grantTemporarySettingsPass(45_000L)
                            val adminComponent = ComponentName(context, DesplugDeviceAdminReceiver::class.java)
                            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                                putExtra(
                                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                    "Ative o Administrador do Dispositivo para impedir que o Desplug seja desinstalado enquanto um bloqueio estiver ativo."
                                )
                            }
                            try {
                                deviceAdminLauncher.launch(intent)
                            } catch (_: Exception) {
                                PermissionHelper.openDeviceAdminActivation(context)
                            }
                        },
                        onToggleAntiTamper = { enabled ->
                            if (!enabled && hasAnyActiveBlock) {
                                // Impede desligar o Anti-Burla enquanto houver bloqueio em execução
                                return@QuickBlockTabContent
                            }
                            isAntiTamperEnabled = enabled
                            BlockPreferences.setAntiTamperEnabled(context, enabled)
                        },
                        onStartQuickBlockClick = {
                            if (!BlockPreferences.hasPinConfigured(context)) {
                                showPinSetupDialog = true
                            } else {
                                showStartQuickBlockDialog = true
                            }
                        },
                        onRequestUnlockClick = { showUnlockPinDialog = true },
                        onCancelUnlockClick = {
                            BlockPreferences.cancelQuickBlockUnlock(context)
                            quickBlockState = BlockPreferences.getQuickBlockState(context)
                        }
                    )
                }
                1 -> {
                    // ABA 1: REGRAS DE HORÁRIO
                    ScheduleRulesTabContent(
                        rules = scheduleRules,
                        onToggleRule = { rule ->
                            val updated = rule.copy(isEnabled = !rule.isEnabled)
                            BlockPreferences.saveScheduleRule(context, updated)
                            scheduleRules = BlockPreferences.getAllScheduleRules(context)
                        },
                        onEditRule = { rule -> scheduleToEdit = rule },
                        onAddNewRule = { isCreatingSchedule = true }
                    )
                }
                2 -> {
                    // ABA 2: LIMITES DIÁRIOS E USO
                    DailyLimitsTabContent(
                        installedApps = installedApps,
                        appRules = appRules,
                        usageMap = usageMap,
                        resetHour = resetHour,
                        onOpenResetHourDialog = { showResetHourDialog = true },
                        onAppClick = { app -> appToConfigure = app }
                    )
                }
            }
        }
    }

    // Diálogo de Iniciar Quick Block
    if (showStartQuickBlockDialog) {
        StartQuickBlockDialog(
            installedApps = installedApps,
            onDismiss = { showStartQuickBlockDialog = false },
            onStart = { durationMinutes, selectedPackages ->
                BlockPreferences.startQuickBlock(context, durationMinutes, selectedPackages)
                quickBlockState = BlockPreferences.getQuickBlockState(context)
                showStartQuickBlockDialog = false
            }
        )
    }

    // Diálogo de PIN para solicitar desbloqueio do Quick Block
    if (showUnlockPinDialog) {
        UnlockPinDialog(
            onDismiss = { showUnlockPinDialog = false },
            onVerifyPin = { pin ->
                if (BlockPreferences.verifySecurityPin(context, pin)) {
                    BlockPreferences.requestQuickBlockUnlock(context)
                    quickBlockState = BlockPreferences.getQuickBlockState(context)
                    showUnlockPinDialog = false
                    true
                } else {
                    false
                }
            }
        )
    }

    // Diálogo de Cadastro/Alteração de PIN
    if (showPinSetupDialog) {
        PinSetupDialog(
            hasExistingPin = BlockPreferences.hasPinConfigured(context),
            onDismiss = { showPinSetupDialog = false },
            onSavePin = { pin ->
                BlockPreferences.setSecurityPin(context, pin)
                showPinSetupDialog = false
            }
        )
    }

    // Modal de Criar / Editar Regra de Horário
    if (isCreatingSchedule || scheduleToEdit != null) {
        val targetRule = scheduleToEdit ?: ScheduleRule(
            name = "Foco Noturno",
            startHour = 22,
            startMinute = 0,
            endHour = 6,
            endMinute = 0,
            daysOfWeek = setOf(
                Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
            ),
            targetPackages = emptySet()
        )

        ScheduleRuleEditDialog(
            initialRule = targetRule,
            isNew = isCreatingSchedule,
            installedApps = installedApps,
            onDismiss = {
                isCreatingSchedule = false
                scheduleToEdit = null
            },
            onSave = { rule ->
                BlockPreferences.saveScheduleRule(context, rule)
                scheduleRules = BlockPreferences.getAllScheduleRules(context)
                isCreatingSchedule = false
                scheduleToEdit = null
            },
            onDelete = { ruleId ->
                BlockPreferences.removeScheduleRule(context, ruleId)
                scheduleRules = BlockPreferences.getAllScheduleRules(context)
                scheduleToEdit = null
            }
        )
    }

    // Modal de Configurar Limite de Tempo do App
    appToConfigure?.let { appItem ->
        val existingRule = appRules.firstOrNull { it.packageName == appItem.packageName }
        AppRuleConfigDialog(
            appItem = appItem,
            existingRule = existingRule,
            usedMinutesToday = usageMap.getOrDefault(appItem.packageName, 0),
            onDismiss = { appToConfigure = null },
            onSave = { newRule ->
                if (newRule == null) {
                    BlockPreferences.removeRule(context, appItem.packageName)
                } else {
                    BlockPreferences.saveRule(context, newRule)
                }
                appRules = BlockPreferences.getAllRules(context)
                appToConfigure = null
            }
        )
    }

    // Modal de Horário de Reinício do Dia
    if (showResetHourDialog) {
        ResetHourDialog(
            currentHour = resetHour,
            onDismiss = { showResetHourDialog = false },
            onConfirm = { selectedHour ->
                resetHour = selectedHour
                BlockPreferences.setDayResetHour(context, selectedHour)
                showResetHourDialog = false
            }
        )
    }
}

@Composable
private fun QuickBlockTabContent(
    state: QuickBlockState,
    currentTimeMillis: Long,
    hasPin: Boolean,
    isDeviceAdminActive: Boolean,
    isAntiTamperEnabled: Boolean,
    hasAnyActiveBlock: Boolean,
    hasAccessibility: Boolean,
    hasUsageStats: Boolean,
    hasOverlay: Boolean,
    hasBatteryExemption: Boolean,
    onActivateDeviceAdmin: () -> Unit,
    onToggleAntiTamper: (Boolean) -> Unit,
    onStartQuickBlockClick: () -> Unit,
    onRequestUnlockClick: () -> Unit,
    onCancelUnlockClick: () -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isActive) Color(0xFF1E1318) else Color(0xFF161B22)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(if (state.isActive) Color(0xFFEF4444) else Color(0xFF10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.isActive) "⚡" else "🛡️",
                            fontSize = 32.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (state.isActive) "Quick Block Ativo" else "Quick Block Desativado",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (state.isActive) {
                            if (state.durationMinutes > 0) {
                                val remDur = state.remainingDurationSeconds(currentTimeMillis)
                                "Bloqueando ${state.targetPackages.size} app(s) • Restam %02d:%02d".format(
                                    remDur / 60,
                                    remDur % 60
                                )
                            } else {
                                "Bloqueando ${state.targetPackages.size} aplicativo(s) até ser desativado."
                            }
                        } else {
                            "Ative o bloqueio temporário com um toque para entrar em foco imediato."
                        },
                        color = if (state.isActive && state.durationMinutes > 0) Color(0xFF10B981) else Color(0xFF9CA3AF),
                        fontWeight = if (state.isActive && state.durationMinutes > 0) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!state.isActive) {
                        Button(
                            onClick = onStartQuickBlockClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color(0xFF052E16)
                            )
                        ) {
                            Text("Ativar Quick Block Agora", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    } else {
                        // Quando está ativo, exibe se está em espera de 5 minutos ou botão de solicitar desbloqueio
                        if (state.isCoolingDown(currentTimeMillis)) {
                            val remaining = state.remainingCooldownSeconds(currentTimeMillis)
                            val progress = 1f - (remaining.toFloat() / state.unlockCooldownSeconds.toFloat())

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF2D161F), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⏳ Espera Obrigatória em Andamento",
                                    color = Color(0xFFF87171),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "%02d:%02d".format(remaining / 60, remaining % 60),
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "O bloqueio continuará por mais 5 minutos. Você pode cancelar a qualquer momento.",
                                    color = Color(0xFFD1D5DB),
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth().height(6.dp),
                                    color = Color(0xFFEF4444),
                                    trackColor = Color(0xFF4C1D24)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onCancelUnlockClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Cancelar e Manter Bloqueio", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Button(
                                onClick = onRequestUnlockClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEF4444),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Desativar Bloqueio (PIN + 5 min)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }

        // Seção Proteção Anti-Burla (Unificada: Contra Desinstalação e Forçar Parada)
        item {
            Text(
                text = "Proteção Anti-Burla",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        item {
            val isProtectionReady = isAntiTamperEnabled && hasAccessibility
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isProtectionReady) Color(0xFF11221C) else Color(0xFF161B22)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isProtectionReady) Color(0xFF10B981) else Color(0xFFF59E0B))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Impedir Desinstalação e Forçar Parada",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isProtectionReady && hasAnyActiveBlock) {
                                "Blindagem ativa agora! Fecha automaticamente tentativas de desinstalar ou forçar parada enquanto houver bloqueio em execução."
                            } else if (isProtectionReady) {
                                "Proteção pronta: impedirá a desinstalação e o acesso às configurações do Desplug assim que um bloqueio estiver ativo."
                            } else {
                                "Proteção desativada. Ative a chave ao lado para blindar o Desplug durante os bloqueios."
                            },
                            color = if (isProtectionReady && hasAnyActiveBlock) Color(0xFF10B981) else Color(0xFF9CA3AF),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = isAntiTamperEnabled,
                        enabled = !(isAntiTamperEnabled && hasAnyActiveBlock),
                        onCheckedChange = onToggleAntiTamper,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981)
                        )
                    )
                }
            }
        }

        // Card 3: Alerta se alguma permissão essencial foi revogada
        if (!hasAccessibility || !hasUsageStats || !hasOverlay || !hasBatteryExemption) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF22151B))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "⚠️ Permissões do Sistema Pendentes",
                            color = Color(0xFFF87171),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (!hasAccessibility) {
                            Button(
                                onClick = { PermissionHelper.openAccessibilitySettings(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color(0xFF052E16))
                            ) {
                                Text("Ativar Serviço de Acessibilidade", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        if (!hasUsageStats) {
                            Button(
                                onClick = { PermissionHelper.openUsageStatsSettings(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color(0xFF052E16))
                            ) {
                                Text("Ativar Acesso ao Uso", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        if (!hasOverlay) {
                            Button(
                                onClick = { PermissionHelper.openOverlaySettings(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color(0xFF052E16))
                            ) {
                                Text("Ativar Exibir Sobre Outros Apps", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        if (!hasBatteryExemption) {
                            Button(
                                onClick = { PermissionHelper.requestIgnoreBatteryOptimizations(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151), contentColor = Color.White)
                            ) {
                                Text("Ignorar Otimização de Bateria", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Como funciona o Desbloqueio Seguro?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Para combater impulsos, quando você pede para desativar o Quick Block, é necessário digitar o seu PIN. O app então inicia uma espera de 5 minutos antes de liberar. Se você se arrepender durante esse tempo, basta tocar em Cancelar.",
                        color = Color(0xFF9CA3AF),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StartQuickBlockDialog(
    installedApps: List<InstalledAppItem>,
    onDismiss: () -> Unit,
    onStart: (Int, Set<String>) -> Unit
) {
    var selectedDuration by remember { mutableIntStateOf(25) }
    var selectedPackages by remember { mutableStateOf<Set<String>>(emptySet()) }
    var appSearch by remember { mutableStateOf("") }

    val durationPresets = listOf(
        15 to "15 min",
        25 to "25 min (Pomodoro)",
        60 to "1 hora",
        120 to "2 horas",
        0 to "Até desligar"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161B22),
        title = {
            Text("Iniciar Quick Block", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text("Escolha a duração:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        durationPresets.forEach { (duration, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedDuration = duration }
                            ) {
                                RadioButton(
                                    selected = selectedDuration == duration,
                                    onClick = { selectedDuration = duration },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(label, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Selecione os apps a bloquear (${selectedPackages.size}):",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = appSearch,
                        onValueChange = { appSearch = it },
                        placeholder = { Text("Filtrar apps...", color = Color(0xFF6B7280), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF0D1117),
                            unfocusedContainerColor = Color(0xFF0D1117)
                        )
                    )
                }

                val appsToShow = installedApps.filter {
                    appSearch.isBlank() || it.label.contains(appSearch, ignoreCase = true)
                }.sortedByDescending { selectedPackages.contains(it.packageName) }

                items(appsToShow.take(40)) { app ->
                    val isChecked = selectedPackages.contains(app.packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPackages = if (isChecked) selectedPackages - app.packageName else selectedPackages + app.packageName
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                selectedPackages = if (it) selectedPackages + app.packageName else selectedPackages - app.packageName
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = app.label, color = Color.White, fontSize = 13.sp, maxLines = 1)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedPackages.isNotEmpty()) {
                        onStart(selectedDuration, selectedPackages)
                    }
                },
                enabled = selectedPackages.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color(0xFF052E16)
                )
            ) {
                Text("Iniciar Bloqueio", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9CA3AF))) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun UnlockPinDialog(
    onDismiss: () -> Unit,
    onVerifyPin: (String) -> Boolean
) {
    var pin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161B22),
        title = {
            Text("Confirmar com PIN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Digite o seu PIN de segurança. Após confirmar, a espera de 5 minutos começará a contar.",
                    color = Color(0xFF9CA3AF),
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        pin = it.filter { char -> char.isDigit() }.take(6)
                        isError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = isError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF10B981),
                        errorBorderColor = Color(0xFFEF4444)
                    )
                )
                if (isError) {
                    Text("PIN incorreto. Tente novamente.", color = Color(0xFFEF4444), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val success = onVerifyPin(pin)
                    if (!success) isError = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White)
            ) {
                Text("Iniciar Espera de 5 min", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9CA3AF))) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun PinSetupDialog(
    hasExistingPin: Boolean,
    onDismiss: () -> Unit,
    onSavePin: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161B22),
        title = {
            Text(
                if (hasExistingPin) "Alterar PIN de Segurança" else "Criar PIN de Segurança",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Defina um PIN de 4 a 6 dígitos para proteger desativações do bloqueio.",
                    color = Color(0xFF9CA3AF),
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        pin = it.filter { char -> char.isDigit() }.take(6)
                        errorMessage = null
                    },
                    label = { Text("Novo PIN (4-6 dígitos)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF10B981)
                    )
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        confirmPin = it.filter { char -> char.isDigit() }.take(6)
                        errorMessage = null
                    },
                    label = { Text("Confirmar Novo PIN") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF10B981)
                    )
                )
                errorMessage?.let {
                    Text(it, color = Color(0xFFEF4444), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pin.length < 4) {
                        errorMessage = "O PIN deve ter no mínimo 4 dígitos."
                    } else if (pin != confirmPin) {
                        errorMessage = "Os PINs digitados não coincidem."
                    } else {
                        onSavePin(pin)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color(0xFF052E16))
            ) {
                Text("Salvar PIN", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9CA3AF))) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ScheduleRulesTabContent(
    rules: List<ScheduleRule>,
    onToggleRule: (ScheduleRule) -> Unit,
    onEditRule: (ScheduleRule) -> Unit,
    onAddNewRule: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = onAddNewRule,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color(0xFF052E16)
                )
            ) {
                Text("+ Nova Regra de Horário", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        if (rules.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Nenhum horário agendado", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Crie regras automáticas como 'Trabalho' ou 'Sono' para bloquear apps nos dias e intervalos de hora escolhidos.",
                            color = Color(0xFF9CA3AF),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            items(items = rules, key = { it.id }) { rule ->
                val isActiveNow = rule.isEnabled && rule.isCurrentlyActive()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditRule(rule) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActiveNow) Color(0xFF22151B) else Color(0xFF161B22)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = rule.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    if (isActiveNow) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFEF4444), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("EM EXECUÇÃO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${rule.formattedTimeWindow()} • ${rule.formattedDaysOfWeek()}",
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${rule.targetPackages.size} aplicativo(s) bloqueado(s)",
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 12.sp
                                )
                            }
                            Switch(
                                checked = rule.isEnabled,
                                onCheckedChange = { onToggleRule(rule) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF10B981)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyLimitsTabContent(
    installedApps: List<InstalledAppItem>,
    appRules: List<AppBlockRule>,
    usageMap: Map<String, Int>,
    resetHour: Int,
    onOpenResetHourDialog: () -> Unit,
    onAppClick: (InstalledAppItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val rulesMap = remember(appRules) { appRules.associateBy { it.packageName } }

    val filteredApps = remember(installedApps, searchQuery, appRules, usageMap) {
        installedApps
            .filter {
                searchQuery.isBlank() ||
                    it.label.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
            }
            .sortedWith(
                compareByDescending<InstalledAppItem> { rulesMap.containsKey(it.packageName) }
                    .thenByDescending { usageMap.getOrDefault(it.packageName, 0) }
                    .thenBy { it.label.lowercase() }
            )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenResetHourDialog() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Horário de Reinício do Dia", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("Contagem diária zera às %02d:00 (Fuso do celular)".format(resetHour), color = Color(0xFF10B981), fontSize = 13.sp)
                    }
                    Text("Alterar", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar app para definir limite diário...", color = Color(0xFF6B7280)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF161B22),
                    unfocusedContainerColor = Color(0xFF161B22),
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFF30363D)
                )
            )
        }

        items(items = filteredApps, key = { it.packageName }) { appItem ->
            val rule = rulesMap[appItem.packageName]
            val usedMinutes = usageMap.getOrDefault(appItem.packageName, 0)
            val isLimitExceeded = rule != null && !rule.isAlwaysBlocked && rule.dailyLimitMinutes > 0 && usedMinutes >= rule.dailyLimitMinutes

            AppRuleCard(
                appItem = appItem,
                rule = rule,
                usedMinutes = usedMinutes,
                isLimitExceeded = isLimitExceeded,
                onClick = { onAppClick(appItem) }
            )
        }
    }
}

@Composable
private fun ScheduleRuleEditDialog(
    initialRule: ScheduleRule,
    isNew: Boolean,
    installedApps: List<InstalledAppItem>,
    onDismiss: () -> Unit,
    onSave: (ScheduleRule) -> Unit,
    onDelete: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialRule.name) }
    var startHour by remember { mutableIntStateOf(initialRule.startHour) }
    var startMinute by remember { mutableIntStateOf(initialRule.startMinute) }
    var endHour by remember { mutableIntStateOf(initialRule.endHour) }
    var endMinute by remember { mutableIntStateOf(initialRule.endMinute) }
    var selectedDays by remember { mutableStateOf(initialRule.daysOfWeek) }
    var selectedPackages by remember { mutableStateOf(initialRule.targetPackages) }
    var appSearch by remember { mutableStateOf("") }

    val daysOrder = listOf(
        Calendar.MONDAY to "Seg",
        Calendar.TUESDAY to "Ter",
        Calendar.WEDNESDAY to "Qua",
        Calendar.THURSDAY to "Qui",
        Calendar.FRIDAY to "Sex",
        Calendar.SATURDAY to "Sáb",
        Calendar.SUNDAY to "Dom"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161B22),
        title = {
            Text(
                if (isNew) "Nova Regra de Horário" else "Editar Regra de Horário",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome da Regra") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF30363D)
                        )
                    )
                }

                item {
                    Text("Intervalo de Horas (Fuso do Celular):", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Início", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                            Button(
                                onClick = { startHour = (startHour + 1) % 24 },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21262D))
                            ) {
                                Text("%02d:%02d".format(startHour, startMinute), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("até", color = Color(0xFF9CA3AF))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Término", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                            Button(
                                onClick = { endHour = (endHour + 1) % 24 },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21262D))
                            ) {
                                Text("%02d:%02d".format(endHour, endMinute), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Text("Dias da Semana:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysOrder.forEach { (calDay, label) ->
                            val isSelected = selectedDays.contains(calDay)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFF10B981) else Color(0xFF21262D))
                                    .clickable {
                                        selectedDays = if (isSelected) {
                                            if (selectedDays.size > 1) selectedDays - calDay else selectedDays
                                        } else {
                                            selectedDays + calDay
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color(0xFF052E16) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Apps Bloqueados nesta Regra (${selectedPackages.size}):",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = appSearch,
                        onValueChange = { appSearch = it },
                        placeholder = { Text("Filtrar apps...", color = Color(0xFF6B7280), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF0D1117),
                            unfocusedContainerColor = Color(0xFF0D1117)
                        )
                    )
                }

                val appsToShow = installedApps.filter {
                    appSearch.isBlank() || it.label.contains(appSearch, ignoreCase = true)
                }.sortedByDescending { selectedPackages.contains(it.packageName) }

                items(appsToShow.take(40)) { app ->
                    val isChecked = selectedPackages.contains(app.packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPackages = if (isChecked) selectedPackages - app.packageName else selectedPackages + app.packageName
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = {
                                selectedPackages = if (it) selectedPackages + app.packageName else selectedPackages - app.packageName
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = app.label, color = Color.White, fontSize = 13.sp, maxLines = 1)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = initialRule.copy(
                        name = if (name.isBlank()) "Regra de Horário" else name,
                        startHour = startHour,
                        startMinute = startMinute,
                        endHour = endHour,
                        endMinute = endMinute,
                        daysOfWeek = selectedDays,
                        targetPackages = selectedPackages
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color(0xFF052E16))
            ) {
                Text("Salvar Regra", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (!isNew) {
                TextButton(
                    onClick = { onDelete(initialRule.id) },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Excluir")
                }
            } else {
                TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9CA3AF))) {
                    Text("Cancelar")
                }
            }
        }
    )
}

@Composable
private fun AppRuleCard(
    appItem: InstalledAppItem,
    rule: AppBlockRule?,
    usedMinutes: Int,
    isLimitExceeded: Boolean,
    onClick: () -> Unit
) {
    val isBlockedNow = rule?.isAlwaysBlocked == true || isLimitExceeded
    val containerBg = when {
        isBlockedNow -> Color(0xFF1C1318)
        rule != null -> Color(0xFF11221C)
        else -> Color(0xFF161B22)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = appItem.label,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    if (isBlockedNow) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BLOQUEADO",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (rule != null) {
                        if (rule.isAlwaysBlocked) {
                            "Bloqueio Total • Usado hoje: $usedMinutes min"
                        } else {
                            "Limite: ${rule.dailyLimitMinutes} min/dia • Usado: $usedMinutes min"
                        }
                    } else {
                        "Sem limite • Usado hoje: $usedMinutes min"
                    },
                    color = if (rule != null) Color(0xFF10B981) else Color(0xFF6B7280),
                    fontSize = 12.sp
                )
            }

            Text(
                text = if (rule != null) "Editar" else "+ Limitar",
                color = if (rule != null) Color(0xFF38BDF8) else Color(0xFF9CA3AF),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun AppRuleConfigDialog(
    appItem: InstalledAppItem,
    existingRule: AppBlockRule?,
    usedMinutesToday: Int,
    onDismiss: () -> Unit,
    onSave: (AppBlockRule?) -> Unit
) {
    var mode by remember {
        mutableStateOf(
            when {
                existingRule == null -> "daily_limit"
                existingRule.isAlwaysBlocked -> "always"
                else -> "daily_limit"
            }
        )
    }

    var limitMinutes by remember {
        mutableStateOf(
            if (existingRule != null && existingRule.dailyLimitMinutes > 0) {
                existingRule.dailyLimitMinutes.toFloat()
            } else {
                30f
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161B22),
        title = {
            Text(
                text = "Configurar ${appItem.label}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Tempo de uso registrado hoje: $usedMinutesToday minuto(s)",
                    color = Color(0xFF9CA3AF),
                    fontSize = 13.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mode = "daily_limit" }
                ) {
                    RadioButton(
                        selected = mode == "daily_limit",
                        onClick = { mode = "daily_limit" },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Limite de Tempo Diário", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Bloqueia ao atingir os minutos configurados.", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                    }
                }

                if (mode == "daily_limit") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0D1117), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Limite: ${limitMinutes.toInt()} minutos por dia",
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Slider(
                            value = limitMinutes,
                            onValueChange = { limitMinutes = it },
                            valueRange = 5f..240f,
                            steps = 46,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF10B981),
                                activeTrackColor = Color(0xFF10B981)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(15f, 30f, 60f, 120f).forEach { preset ->
                                FilterChip(
                                    selected = limitMinutes.toInt() == preset.toInt(),
                                    onClick = { limitMinutes = preset },
                                    label = { Text("${preset.toInt()}m", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF10B981),
                                        selectedLabelColor = Color(0xFF052E16)
                                    )
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mode = "always" }
                ) {
                    RadioButton(
                        selected = mode == "always",
                        onClick = { mode = "always" },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Bloqueio Imediato Total", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Não permite abrir em nenhum momento.", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rule = AppBlockRule(
                        packageName = appItem.packageName,
                        appName = appItem.label,
                        dailyLimitMinutes = if (mode == "daily_limit") limitMinutes.toInt() else 0,
                        isAlwaysBlocked = mode == "always"
                    )
                    onSave(rule)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color(0xFF052E16)
                )
            ) {
                Text("Salvar Regra", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (existingRule != null) {
                TextButton(
                    onClick = { onSave(null) },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Remover Limite")
                }
            } else {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9CA3AF))
                ) {
                    Text("Cancelar")
                }
            }
        }
    )
}

@Composable
private fun ResetHourDialog(
    currentHour: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(currentHour) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161B22),
        title = {
            Text(
                text = "Horário de Reinício do Dia",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "A que horas o seu dia reinicia para zerar o contador de tempo de uso?",
                    color = Color(0xFF9CA3AF),
                    fontSize = 13.sp
                )
                listOf(
                    0 to "00:00 (Meia-noite - Padrão)",
                    4 to "04:00 (Madrugada)",
                    6 to "06:00 (Início da manhã)"
                ).forEach { (hour, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedHour = hour }
                    ) {
                        RadioButton(
                            selected = selectedHour == hour,
                            onClick = { selectedHour = hour },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = label, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedHour) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color(0xFF052E16)
                )
            ) {
                Text("Confirmar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9CA3AF))
            ) {
                Text("Cancelar")
            }
        }
    )
}

private fun loadLaunchableApps(context: Context): List<InstalledAppItem> {
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
    val myPackage = context.packageName

    return resolveInfos
        .mapNotNull { info ->
            val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
            if (pkg == myPackage || pkg == "com.android.settings") return@mapNotNull null
            val label = info.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: pkg
            InstalledAppItem(packageName = pkg, label = label)
        }
        .distinctBy { it.packageName }
}
