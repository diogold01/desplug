# Diretrizes e Regras de Desenvolvimento do Desplug

Este arquivo centraliza o contexto técnico do projeto, as regras permanentes de governança e a metodologia de trabalho operacional. Ele é lido automaticamente pelo assistente em todas as sessões.

---

## 🛠️ Stack Tecnológica Oficial

O **Desplug** adota uma arquitetura **Local-First**, sem backend, sem login e sem internet:

1. **Linguagem & Framework:**
   - **Kotlin Nativo**
   - **Jetpack Compose** para toda a interface do usuário (UI reativa).
   - **Android SDK:** MinSdk 26 (Android 8.0) / TargetSdk 34+ (Android 14+).

2. **Serviços de Sistema Android (Core de Bloqueio):**
   - **AccessibilityService:** Detecção imediata de abertura de apps bloqueados e proteção contra abertura das Configurações do Android.
   - **UsageStatsManager:** Monitoramento e cálculo de tempo de uso diário em primeiro plano.
   - **Foreground Service:** Serviço persistente com notificação contínua, imune ao encerramento pelo sistema.
   - **Overlay Window / Lock Activity:** Tela de bloqueio instantânea sobreposta ao app restrito com contador de tentativas.
   - **Device Admin (`DeviceAdminReceiver`):** Proteção contra desinstalação não autorizada do aplicativo.
   - **Boot Receiver (`RECEIVE_BOOT_COMPLETED`):** Reinicialização automática e imediata de todos os serviços ao ligar o celular.

3. **Banco de Dados & Persistência Local:**
   - **Room Database (SQLite):** Armazenamento de apps bloqueados, regras de horários, contadores de tentativas diárias e histórico.
   - **Jetpack DataStore (Preferences):** Configurações gerais (PIN de emergência, horário de reinício diário, estado do Quick Block).

4. **Design & UX:**
   - Interface limpa, minimalista e moderna em Jetpack Compose (Material 3).
   - Totalmente em **Português do Brasil (pt-BR)**.

---

## 🚫 Regra Estrita de Modelos no Antigravity

**NUNCA** sugerir ou citar nomes de modelos inexistentes ou genéricos.
No ambiente Antigravity deste workspace, os únicos modelos válidos e a estratégia de cotas são:

- **Gemini 4 Argon** (Low / Medium / High) — **Carro-chefe para tarefas complexas, arquitetura e core** (cota alta ~88%).
- **Gemini 3.8 Flash** (Low / Medium / High) — **Ideal para UI Compose, CRUDs, ajustes rápidos e tarefas frequentes** (cota alta ~76% semanal / 96% ciclo 5h).
- **Claude Sonnet 5.5 / Opus 5.5** — **USO RESTRITO DE EMERGÊNCIA** (cota semanal crítica em 5%, restando ~4 dias para reset).

---

## 🎯 Metodologia de Trabalho: PCCD & "Done When"

1. **P — Problema:** Compreender exatamente o que precisa ser feito ou corrigido antes de qualquer edição.
2. **C — Causa / Contexto:** Mapear a camada exata do Android (Service, Accessibility, Repository, ViewModel ou UI Compose).
3. **C — Correção Cirúrgica:** Aplicar o código completo, sem trechos omitidos ou comentários como "o resto continua igual".
4. **D — Done When (Critério de Aceite):**
   - Cada etapa tem critérios claros de conclusão e validação no celular.
   - Ao concluir uma etapa, **parar**, apresentar um resumo curto e aguardar o comando **"continue"** do usuário antes de iniciar a próxima.
   - **Sempre lembrar o usuário do Modelo e Nível de Esforço recomendados no início de cada etapa.**
