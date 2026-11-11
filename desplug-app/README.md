# Desplug

Aplicativo Android local-first (sem backend, sem login, sem conexao externa) para gerenciamento de tempo de tela e bloqueio de aplicativos.

## Funcionalidades

- Limite diario de uso por aplicativo (via UsageStatsManager).
- Regras de bloqueio por horario e dias da semana.
- Quick Block temporario com protecao por PIN e espera de 5 minutos para cancelamento.
- Tela de bloqueio instantanea sobreposta com contador de tentativas.
- Servico em primeiro plano persistente e reinicializacao automatica no boot.
- Protecao contra desinstalacao e fechamento forcado via AccessibilityService e DeviceAdmin.

## Stack

- Kotlin
- Jetpack Compose (Material 3)
- Android SDK (minSdk 26, targetSdk 34)
- Room Database / DataStore Preferences

## Permissoes Requeridas

- PACKAGE_USAGE_STATS: medicao de tempo de tela.
- SYSTEM_ALERT_WINDOW: sobreposicao da tela de bloqueio.
- ACCESSIBILITY_SERVICE: deteccao imediata de abertura de apps e protecao anti-burla.
- FOREGROUND_SERVICE / POST_NOTIFICATIONS: persistencia do servico de monitoramento.
- RECEIVE_BOOT_COMPLETED: inicializacao com o sistema.

## Build

Compilacao via Gradle ou Android Studio:

```bash
./gradlew assembleDebug
```

O APK gerado fica em `app/build/outputs/apk/debug/app-debug.apk`.
