package com.victorcabral.gestorfinanceiro.ui.onboarding

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.victorcabral.gestorfinanceiro.util.NotificationAccess

@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(NotificationAccess.isEnabled(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                enabled = NotificationAccess.isEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Gestor Financeiro",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Transforme notificações do Nubank e Santander em despesas do mês.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "1. Ative o acesso a notificações (listener) para este app.\n" +
                    "2. Quando o banco notificar uma compra/Pix, nasce uma sugestão pendente.\n" +
                    "3. Confirme valor, origem, descrição e categoria.\n\n" +
                    "Nenhuma informação sai do aparelho.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (enabled) {
                    "Listener de notificações: ativo"
                } else {
                    "Listener de notificações: desativado"
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
            Button(
                onClick = {
                    (context as? Activity)?.startActivity(NotificationAccess.openSettingsIntent())
                        ?: context.startActivity(NotificationAccess.openSettingsIntent())
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Abrir configuração de notificações")
            }
            OutlinedButton(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
            ) {
                Text(if (enabled) "Continuar" else "Ative o listener para continuar")
            }
            OutlinedButton(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Pular por agora (cadastro manual)")
            }
        }
    }
}
