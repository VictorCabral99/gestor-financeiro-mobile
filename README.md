# Gestor Financeiro (mobile)

App Android (Kotlin + Jetpack Compose) que transforma notificações do **Nubank** e **Santander** em sugestões de despesa. Você confirma, descreve e categoriza; o mês fica consolidado no aparelho.

MVP local-only — sem backend. Sem publicação na Play Store neste repositório.

## Stack

- Kotlin, minSdk **26** (Android 8+), target/compileSdk **35**
- Jetpack Compose + Navigation
- Room (Expense, Category, PendingSuggestion)
- `NotificationListenerService` + parsers por `packageName`
- Notificação local opcional ao criar sugestão (`POST_NOTIFICATIONS` no Android 13+)

## Como gerar o APK (debug)

Pré-requisitos: JDK 17+, Android SDK (platform 35 + build-tools).

```bash
# aponte o SDK (não commitar local.properties)
echo "sdk.dir=$HOME/Android/Sdk" > local.properties

./gradlew assembleDebug
```

APK gerado em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Instale no aparelho (sideload):

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Release local (sem secrets no git)

O `assembleRelease` gera APK assinado com a keystore **debug** do Android por padrão neste MVP (`isMinifyEnabled = false`). Para um release próprio:

1. Crie uma keystore **fora do repositório** (nunca commite `.jks` / `.keystore` / senhas):

```bash
keytool -genkeypair -v -keystore "$HOME/keys/gestor-financeiro.jks" -keyalg RSA -keysize 2048 -validity 10000 -alias gestor
```

2. Crie `keystore.properties` na raiz do projeto (já está no `.gitignore` se você adicionar o nome) **ou** passe via `~/.gradle/gradle.properties` / variáveis de ambiente — não versionar.

3. Exemplo de bloco opcional em `app/build.gradle.kts` (ajuste paths localmente; não precisa estar commitado):

```kotlin
// signingConfigs { create("release") { ... } }
// buildTypes.getByName("release") { signingConfig = signingConfigs.getByName("release") }
```

```bash
./gradlew assembleRelease
# app/build/outputs/apk/release/app-release.apk  (ou app-release-unsigned.apk)
```

## Como ativar o acesso a notificações

1. Abra o app → onboarding (ou engrenagem na home).
2. Toque em **Abrir configuração de notificações**.
3. Ative **Gestor Financeiro** na lista de apps com acesso a notificações (Notification Listener).
4. Volte ao app. A home mostra um aviso se o listener estiver **off**.
5. (Opcional, Android 13+) aceite `POST_NOTIFICATIONS` para alertas locais de “Nova sugestão de despesa”.

Sem o listener, o app **não** recebe pushes dos bancos; o cadastro manual continua disponível.

Pacotes filtrados no MVP:

| Banco     | packageName                 |
|-----------|-----------------------------|
| Nubank    | `com.nu.production`         |
| Santander | `com.santander.app`         |

## Fluxo

1. Banco notifica compra/Pix → listener → parser → dedupe → `PendingSuggestion`
2. **Pendentes** → **Confirmar** (valor, estabelecimento, conta/cartão, categoria/subcategoria)
3. Tab **Home** — dashboards; tab **Relatórios** — registros do mês; tab **Config** — cadastros
4. Conta = débito · Cartão = crédito (implícito ao escolher a fonte)
5. Categorias com **um** nível de subcategoria

Se o parse falhar parcialmente, a sugestão fica marcada para **revisar manualmente** com o texto cru.

## Simulador de notificações (debug only)

No APK **debug**: tab **Config** → **Simulador de notificações**.

1. Escolha um fixture Nubank/Santander → **Simular esta notificação**.
2. Abra **Pendentes** (sino) para ver a sugestão gerada pelo mesmo pipeline do listener.

**Abordagem:** injeção direta no `NotificationPipeline` com o `packageName` do banco (fixtures).  
Não usamos `NotificationManager` para fingir ser outro app — o Android sempre atribui a notificação ao nosso package, então o listener filtrado por `com.nu.production` / `com.santander.app` não veria o spoof.

## Testes

```bash
./gradlew assembleDebug testDebugUnitTest
```

## Estrutura

```text
app/src/main/java/.../
  data/           Room entities, DAOs, repository
  notification/   Listener, pipeline, dedupe, parsers, notifier
  ui/             Compose screens (PT-BR)
```

## Fora do escopo

- Play Store listing / distribuição na loja
- Outros bancos (estrutura plugável), sync em nuvem, open banking
