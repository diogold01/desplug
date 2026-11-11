# Contexto Geral do Projeto Desplug

Este documento resume a visão de produto, arquitetura, decisões técnicas já consolidadas e o status atual do desenvolvimento.

---

## 📱 Visão de Produto
- **Nome:** Desplug
- **Objetivo:** Aplicativo Android pessoal de foco, autocontrole e bloqueio de aplicativos, com arquitetura 100% **Local-First** (sem internet, sem login, sem dependência de nuvem).
- **Inspiração:** AppBlock (mantendo a eficácia técnica com design minimalista em Jetpack Compose, em português do Brasil).
- **Distribuição:** Instalação direta via APK (sideloading pessoal).

---

## 🏛️ Arquitetura e Decisões Técnicas
- **Linguagem & UI:** Kotlin nativo + Jetpack Compose (Material 3).
- **Core de Bloqueio:**
  - `AccessibilityService` para detecção em milissegundos e anti-burla nas Configurações.
  - `UsageStatsManager` para cálculo de tempo de tela e limites diários.
  - `Foreground Service` para imunidade contra encerramento pelo sistema.
  - `LockActivity` para tela de bloqueio instantânea com contador de tentativas.
  - `DeviceAdminReceiver` para evitar desinstalação não autorizada.
- **Banco de Dados Local:** Room Database (SQLite) + Jetpack DataStore (Preferences).
- **Regras de Negócio Consolidadas:**
  - Desbloqueio temporário: PIN + espera obrigatória de 5 minutos com contagem regressiva e opção de cancelar.
  - Horário: Respeita estritamente o fuso local do aparelho (GMT-4 Cuiabá ou qualquer outro).

---

## 📈 Status das Etapas de Desenvolvimento

- [x] **Etapa 1:** Setup Base + Onboarding de Permissões + Serviço de Detecção e Tela de Bloqueio
- [x] **Etapa 2:** Seleção de Apps + Limite Diário de Minutos (Room DB / Local Cache)
- [x] **Etapa 3:** Bloqueio por Horário (Regras recorrentes, fuso local e regra mais restritiva)
- [x] **Etapa 4:** Quick Block (Bloqueio Rápido) + PIN + Espera de 5 min com contador
- [ ] **Etapa 5:** Contador de Tentativas na Tela de Bloqueio ("Tentou abrir X vezes hoje")
- [ ] **Etapa 6:** Proteção Anti-Burla (Device Admin contra desinstalação + proteção das Configurações)
- [ ] **Etapa 7:** Polimento final, otimização de bateria e geração do APK release

---

## 📝 Histórico de Execuções
- **Etapa 0 (Setup de Ambiente):**
  - Repositório REA clonado em `rea/`.
  - Engenharia reversa realizada no APK do AppBlock para validar permissões oficiais de sistema.
  - Projeto base criado no Android Studio em `desplug-app/`.
  - Arquivos de governança [`GEMINI.md`](file:///c:/Users/DIOGO/Documents/📁%20Projetos/appblock/GEMINI.md), [`rules.md`](file:///c:/Users/DIOGO/Documents/📁%20Projetos/appblock/rules.md) e [`context.md`](file:///c:/Users/DIOGO/Documents/📁%20Projetos/appblock/context.md) inicializados.
- **Etapa 1 (Core de Permissões, Detecção e Bloqueio Básico):**
  - Configuradas permissões no `AndroidManifest.xml` (`PACKAGE_USAGE_STATS`, `SYSTEM_ALERT_WINDOW`, `QUERY_ALL_PACKAGES`, `FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, `BIND_ACCESSIBILITY_SERVICE`).
  - Implementados `PermissionHelper.kt`, `BlockPreferences.kt`, `DesplugAccessibilityService.kt`, `MonitorForegroundService.kt`, `BootReceiver.kt`, `LockActivity.kt` e `MainActivity.kt`.
  - Validado com sucesso no aparelho do usuário (bloqueio funcional do app 99).
- **Etapa 2 (Seleção de Apps e Limite de Tempo Diário):**
  - Criado modelo de regra de bloqueio `AppBlockRule.kt` (suporte a minutos por dia ou bloqueio total instantâneo).
  - Implementado `UsageStatsCalculator.kt` utilizando o `UsageStatsManager` do Android, respeitando o fuso local do aparelho (`Calendar.getInstance()`) e o horário de reinício diário configurado.
  - Adicionado seletor de Horário de Reinício do Dia (padrão: 00:00, com suporte a 04:00 ou 06:00).
  - Implementado modal intuitivo no Jetpack Compose para definir minutos por app (slider interativo de 5 a 240 min com botões rápidos de 15m, 30m, 60m, 120m).
  - A tela de bloqueio `LockActivity.kt` agora informa com precisão a razão do bloqueio (ex: "Você atingiu o seu limite diário de X min (usou Y min hoje)").
  - Gerado e validado o build limpo: `Desplug-Etapa2.apk`.
- **Etapa 3 (Bloqueio por Horário - Regras Recorrentes):**
  - Criado modelo `ScheduleRule.kt` com suporte a dias da semana, intervalo de horas (incluindo travessia da meia-noite) e lista de apps alvo.
  - Respeita o fuso horário local do aparelho (GMT-4 Cuiabá ou qualquer fuso do celular).
  - Algoritmo "regra mais restritiva vence": se houver uma regra de horário em vigor, ela tem precedência e bloqueia de imediato.
  - Interface com Abas no Compose (`Bloqueio por Horário` vs `Limites Diários`), diálogo completo para criação/edição de regras com seletor de dias da semana e busca de apps.
  - Gerado build limpo: `Desplug-Etapa3.apk`.
- **Etapa 4 (Quick Block + PIN de Segurança + Espera de 5 Minutos Cancelável):**
  - Criado modelo `QuickBlockState.kt` com suporte a durações pré-definidas (15m, 25m Pomodoro, 1h, 2h ou até desligar) e seleção de apps alvo.
  - Sistema de segurança com PIN criptografado em SHA-256 local (`BlockPreferences.kt`).
  - Fluxo de desativação com fricção psicológica anti-impulso: digitar o PIN correto dispara contagem regressiva visível de 5 minutos (300 segundos).
  - Durante os 5 minutos o bloqueio continua rigorosamente ativo, com barra de progresso em tempo real e botão para **Cancelar e Manter o Bloqueio**.
  - Gerado build limpo: `Desplug-Etapa4.apk`.
