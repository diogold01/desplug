# Regras Operacionais e Aprendizados Contínuos (Desplug)

Este arquivo registra regras de governança, lições aprendidas e histórico de correções de comportamento para auto-treinamento e prevenção de erros.

---

## 🚫 Erros Críticos Registrados & Prevenção

### 1. Nomenclatura de Modelos no Seletor Antigravity
- **Erro Ocorrido:** Sugestão de modelos com nomes externos/genéricos (como Gemini 1.5, 2.0, 2.5 Pro/Flash).
- **Causa:** Uso de nomenclatura externa sem consultar os identificadores reais suportados no Antigravity.
- **Regra Permanente:** Usar **única e exclusivamente** os modelos reais suportados no menu Antigravity:
  - `Gemini 4 Argon` (Low / Medium / High)
  - `Gemini 3.8 Flash` (Low / Medium / High)
  - `Claude Sonnet 5.5` (Low / Medium / High) — *Reservar para emergências devido à cota semanal baixa*
  - `Claude Opus 5.5` (Low / Medium / High) — *Reservar para emergências devido à cota semanal baixa*

### 2. Compilação Android em Pasta com Emoji (`📁 Projetos`) no Windows
- **Erro Ocorrido:** O `gradlew.bat` falha no `cmd.exe` por causa do emoji `📁` no caminho (`Unable to access jarfile ... ?? Projetos`), e o Android Gradle Plugin bloqueia o build acusando caracteres não-ASCII no caminho do projeto.
- **Solução Aplicada e Regra Permanente:**
  1. Manter `android.overridePathCheck=true` no arquivo `desplug-app/gradle.properties`.
  2. Para compilar via terminal, usar o binário do Gradle em `C:\Users\DIOGO\.gradle\wrapper\dists\...` passando `-p "C:\Users\DIOGO\Documents\desplug-workspace\desplug-app"` (Junction ASCII criada apontando para a pasta do projeto) com `JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"`.

---

## 📊 Estratégia de Alocação de Cotas (Baseada em Cotas Reais)
- **Claude & GPT:** Cota semanal em apenas 5% (restam quase 4 dias para resetar). **PROIBIDO usar Claude de rotina** para não bloquear o acesso.
- **Gemini 4 Argon:** Cota semanal com 88% disponível. É o modelo de raciocínio profundo e máxima qualidade para arquitetura, lógica complexa e serviços de sistema.
- **Gemini 3.8 Flash:** Cota semanal com 76% e ciclo de 5h com 96% disponível. É o modelo rápido e ultra-eficiente para UI Compose, CRUDs e pequenos ajustes.

---

## 🔁 Ciclo Operacional por Etapa
1. **Lembrete de Modelo:** Iniciar cada etapa informando o modelo ideal e nível de esforço.
2. **Execução Cirúrgica:** Criar e editar arquivos completos e funcionais.
3. **Registro de Contexto:** Ao concluir a etapa, registrar o resumo no `context.md` e os aprendizados aqui no `rules.md`.
4. **Pausa para Teste:** Apresentar resumo curto em 1 parágrafo e aguardar o comando **"continue"** do usuário após validação no aparelho.

readme quando tiver. básico simples, daqueles que você bate o olho e entende. sem emojis. linguagem humana direta
