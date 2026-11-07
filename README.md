# Gestor Financeiro (mobile)

App Android (Kotlin + Jetpack Compose) que transforma notificações do **Nubank** e **Santander** em sugestões de despesa. Você confirma, descreve e categoriza; o mês fica consolidado no aparelho.

MVP local-only — sem backend, sem Play Store neste repositório.

## Stack

- Kotlin, minSdk **26** (Android 8+), target/compileSdk **35**
- Jetpack Compose + Navigation
- Room (Expense, Category, PendingSuggestion)
- `NotificationListenerService` + parsers por `packageName`

## Como gerar o APK (debug)

Pré-requisitos: JDK 17+, Android SDK (platform 35 + build-tools).

```bash
# opcional: aponte o SDK
echo "sdk.dir=$HOME/Android/Sdk" > local.properties

./gradlew assembleDebug
```

APK gerado em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Instale no aparelho:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Release sem minify (ainda sem keystore de distribuição):

```bash
./gradlew assembleRelease
```

## Como ativar o acesso a notificações

1. Abra o app → tela de onboarding (ou ícone de engrenagem na home).
2. Toque em **Abrir configuração de notificações**.
3. Ative **Gestor Financeiro** na lista de apps com acesso a notificações.
4. Volte ao app e continue.

Sem essa permissão o listener não recebe pushes dos bancos; o cadastro manual de despesas continua disponível.

Pacotes filtrados no MVP:

| Banco     | packageName           |
|-----------|-----------------------|
| Nubank    | `com.nu.production`   |
| Santander | `com.santander.app`   |

## Telas

1. **Onboarding** — explica e abre settings de notification access  
2. **Mês** — total + lista de despesas  
3. **Pendentes** — sugestões vindas de notificações  
4. **Confirmar** — valor, origem, descrição, categoria  
5. **Categorias** — lista editável (seed: alimentação, transporte, moradia, lazer, outros)

## Testes unitários (parsers)

```bash
./gradlew test
```

Parsers Nubank/Santander ainda são stubs com fixtures de exemplo — há `TODO`s para endurecer com textos reais de aparelho.

## Estrutura relevante

```text
app/src/main/java/.../
  data/           Room entities, DAOs, repository
  notification/   Listener, pipeline, dedupe, parsers
  ui/             Compose screens (PT-BR)
```

## Fora do escopo deste scaffold

- Publicação na Play Store / assinatura de release para loja
- Outros bancos, sync em nuvem, open banking
