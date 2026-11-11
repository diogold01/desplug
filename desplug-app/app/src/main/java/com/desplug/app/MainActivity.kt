package com.desplug.app

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.desplug.app.data.BlockPreferences
import com.desplug.app.data.model.AppBlockRule
import com.desplug.app.data.model.QuickBlockState
import com.desplug.app.data.model.ScheduleRule
import com.desplug.app.receiver.DesplugDeviceAdminReceiver
import com.desplug.app.service.MonitorForegroundService
import com.desplug.app.ui.components.DesplugFloatingNav
import com.desplug.app.ui.components.DesplugNavTab
import com.desplug.app.ui.screens.AppLimitConfigSheet
import com.desplug.app.ui.screens.DayResetHourSheet
import com.desplug.app.ui.screens.FocusScreen
import com.desplug.app.ui.screens.HomeScreen
import com.desplug.app.ui.screens.LimitsScreen
import com.desplug.app.ui.screens.PinSetupSheet
import com.desplug.app.ui.screens.QuickBlockSheet
import com.desplug.app.ui.screens.ScheduleRuleSheet
import com.desplug.app.ui.screens.SchedulesScreen
import com.desplug.app.ui.screens.UnlockPinSheet
import com.desplug.app.ui.theme.DesplugBackground
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

@Composable
fun DesplugMainApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedTab by remember { mutableStateOf(DesplugNavTab.HOME) }
    var selectedQuickDurationMinutes by remember { mutableIntStateOf(25) }

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
    var showResetHourSheet by remember { mutableStateOf(false) }
    var showPinSetupSheet by remember { mutableStateOf(false) }
    var showUnlockPinSheet by remember { mutableStateOf(false) }
    var showQuickBlockSheet by remember { mutableStateOf(false) }

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

    // Carrega a lista de aplicativos instalados em segundo plano
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val apps = loadLaunchableApps(context)
            withContext(Dispatchers.Main) {
                installedApps = apps
            }
        }
    }

    // Cronômetro de tempo real: atualiza a cada segundo
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTimeMillis = System.currentTimeMillis()
            val updatedQuickBlock = BlockPreferences.getQuickBlockState(context)
            if (updatedQuickBlock != quickBlockState) {
                quickBlockState = updatedQuickBlock
            }
        }
    }

    // Carrega e atualiza estatísticas de uso em primeiro plano
    LaunchedEffect(hasUsageStats, resetHour) {
        if (hasUsageStats) {
            withContext(Dispatchers.IO) {
                val stats = UsageStatsCalculator.getAllUsageTodayInMinutes(context, resetHour)
                withContext(Dispatchers.Main) {
                    usageMap = stats
                }
            }
        }
    }

    // Atualiza estado ao voltar para o app (ON_RESUME)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshState()
                if (hasUsageStats) {
                    usageMap = UsageStatsCalculator.getAllUsageTodayInMinutes(context, resetHour)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val allPermissionsGranted = hasAccessibility && hasUsageStats && hasOverlay && hasBatteryExemption
    val hasAnyActiveBlock = BlockPreferences.hasAnyActiveProtectionNow(context)

    // Botão Voltar do Android: se estiver em outra aba, volta para Início antes de sair
    BackHandler(enabled = selectedTab != DesplugNavTab.HOME) {
        selectedTab = DesplugNavTab.HOME
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DesplugBackground
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Conteúdo da Aba Selecionada
            when (selectedTab) {
                DesplugNavTab.HOME -> {
                    HomeScreen(
                        hasPinConfigured = BlockPreferences.hasPinConfigured(context),
                        allPermissionsGranted = allPermissionsGranted,
                        isBlockingEnabled = isBlockingEnabled,
                        totalAttemptsToday = totalAttemptsToday,
                        appRules = appRules,
                        usageMap = usageMap,
                        scheduleRules = scheduleRules,
                        quickBlockState = quickBlockState,
                        selectedQuickDurationMinutes = selectedQuickDurationMinutes,
                        onSelectQuickDuration = { selectedQuickDurationMinutes = it },
                        onOpenQuickBlockSheet = {
                            if (!BlockPreferences.hasPinConfigured(context)) {
                                showPinSetupSheet = true
                            } else {
                                showQuickBlockSheet = true
                            }
                        },
                        onOpenPinSetup = { showPinSetupSheet = true },
                        onTogglePrimarySchedule = { rule ->
                            if (rule != null) {
                                val updated = rule.copy(isEnabled = !rule.isEnabled)
                                BlockPreferences.saveScheduleRule(context, updated)
                                scheduleRules = BlockPreferences.getAllScheduleRules(context)
                            } else {
                                isCreatingSchedule = true
                            }
                        },
                        onNavigateToSchedules = { selectedTab = DesplugNavTab.SCHEDULES },
                        onNavigateToFocus = { selectedTab = DesplugNavTab.FOCUS }
                    )
                }
                DesplugNavTab.FOCUS -> {
                    FocusScreen(
                        state = quickBlockState,
                        currentTimeMillis = currentTimeMillis,
                        installedApps = installedApps,
                        hasPin = BlockPreferences.hasPinConfigured(context),
                        isAntiTamperEnabled = isAntiTamperEnabled,
                        hasAnyActiveBlock = hasAnyActiveBlock,
                        hasAccessibility = hasAccessibility,
                        hasUsageStats = hasUsageStats,
                        hasOverlay = hasOverlay,
                        hasBatteryExemption = hasBatteryExemption,
                        onToggleAntiTamper = { enabled ->
                            if (!enabled && hasAnyActiveBlock) return@FocusScreen
                            isAntiTamperEnabled = enabled
                            BlockPreferences.setAntiTamperEnabled(context, enabled)
                        },
                        onStartQuickBlockClick = {
                            if (!BlockPreferences.hasPinConfigured(context)) {
                                showPinSetupSheet = true
                            } else {
                                showQuickBlockSheet = true
                            }
                        },
                        onRequestUnlockClick = { showUnlockPinSheet = true },
                        onCancelUnlockClick = {
                            BlockPreferences.cancelQuickBlockUnlock(context)
                            quickBlockState = BlockPreferences.getQuickBlockState(context)
                        },
                        onOpenPinSetup = { showPinSetupSheet = true }
                    )
                }
                DesplugNavTab.SCHEDULES -> {
                    SchedulesScreen(
                        rules = scheduleRules,
                        installedApps = installedApps,
                        onToggleRule = { rule ->
                            val updated = rule.copy(isEnabled = !rule.isEnabled)
                            BlockPreferences.saveScheduleRule(context, updated)
                            scheduleRules = BlockPreferences.getAllScheduleRules(context)
                        },
                        onEditRule = { rule -> scheduleToEdit = rule },
                        onAddNewRule = { isCreatingSchedule = true }
                    )
                }
                DesplugNavTab.LIMITS -> {
                    LimitsScreen(
                        installedApps = installedApps,
                        appRules = appRules,
                        usageMap = usageMap,
                        resetHour = resetHour,
                        onOpenResetHourDialog = { showResetHourSheet = true },
                        onAppClick = { app -> appToConfigure = app }
                    )
                }
            }

            // --- RODAPÉ COM GRADIENTE FADE (OPÇÃO 1 APROVADA) ---

            // 1. Camada de Gradiente Fade: desvanece suavemente o conteúdo rolável antes da barra
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                DesplugBackground.copy(alpha = 0.75f),
                                DesplugBackground,
                                DesplugBackground
                            )
                        )
                    )
            )

            // 2. Proteção de fundo preto puro sob a barra dos 3 botões do Android
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
                    .background(DesplugBackground)
            )

            // 3. Barra Flutuante de Navegação Inferior (100% Opaca)
            DesplugFloatingNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            )

            // --- SHEETS MODAIS ---

            // 1. Sheet do Quick Block
            if (showQuickBlockSheet) {
                QuickBlockSheet(
                    initialDurationMinutes = selectedQuickDurationMinutes,
                    initialSelectedPackages = appRules.map { it.packageName }.toSet(),
                    installedApps = installedApps,
                    onStartQuickBlock = { duration, targetPackages ->
                        BlockPreferences.startQuickBlock(context, duration, targetPackages)
                        quickBlockState = BlockPreferences.getQuickBlockState(context)
                        showQuickBlockSheet = false
                        selectedTab = DesplugNavTab.FOCUS
                    },
                    onDismiss = { showQuickBlockSheet = false }
                )
            }

            // 2. Sheet de Desbloqueio com PIN
            if (showUnlockPinSheet) {
                UnlockPinSheet(
                    onDismiss = { showUnlockPinSheet = false },
                    onVerifyPin = { pin ->
                        if (BlockPreferences.verifySecurityPin(context, pin)) {
                            BlockPreferences.requestQuickBlockUnlock(context)
                            quickBlockState = BlockPreferences.getQuickBlockState(context)
                            showUnlockPinSheet = false
                            true
                        } else {
                            false
                        }
                    }
                )
            }

            // 3. Sheet de Criação / Edição de PIN
            if (showPinSetupSheet) {
                PinSetupSheet(
                    hasExistingPin = BlockPreferences.hasPinConfigured(context),
                    onDismiss = { showPinSetupSheet = false },
                    onSavePin = { pin ->
                        BlockPreferences.setSecurityPin(context, pin)
                        showPinSetupSheet = false
                        refreshState()
                    }
                )
            }

            // 4. Sheet de Configuração de Limite por App
            appToConfigure?.let { appItem ->
                val existingRule = appRules.firstOrNull { it.packageName == appItem.packageName }
                AppLimitConfigSheet(
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

            // 5. Sheet de Horário de Reinício do Dia
            if (showResetHourSheet) {
                DayResetHourSheet(
                    currentHour = resetHour,
                    onDismiss = { showResetHourSheet = false },
                    onConfirm = { selectedHour ->
                        resetHour = selectedHour
                        BlockPreferences.setDayResetHour(context, selectedHour)
                        showResetHourSheet = false
                        refreshState()
                    }
                )
            }

            // 6. Sheet de Regra de Horário (Criar / Editar)
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

                ScheduleRuleSheet(
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
        }
    }
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
