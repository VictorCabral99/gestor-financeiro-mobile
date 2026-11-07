package com.victorcabral.gestorfinanceiro.ui.prompt

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.victorcabral.gestorfinanceiro.GestorFinanceiroApp
import com.victorcabral.gestorfinanceiro.MainActivity
import com.victorcabral.gestorfinanceiro.data.entity.PendingSuggestion
import com.victorcabral.gestorfinanceiro.notification.SuggestionNotifier
import com.victorcabral.gestorfinanceiro.ui.theme.GestorFinanceiroTheme
import com.victorcabral.gestorfinanceiro.util.MoneyFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Janela flutuante sobre a tela atual (mesmo com o app fechado) quando
 * chega uma sugestão de compra bancária. Oferece registrar ou adiar.
 */
class SuggestionPromptActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnScreenOnAndShowOverLock()
        enableEdgeToEdge()

        val pendingId = intent?.getLongExtra(SuggestionNotifier.EXTRA_PENDING_ID, -1L) ?: -1L
        if (pendingId <= 0L) {
            finish()
            return
        }

        // Clear the status-bar notification that opened us.
        NotificationManagerCompat.from(this).cancel(
            SuggestionNotifier.notificationIdFor(pendingId),
        )

        setContent {
            GestorFinanceiroTheme {
                var suggestion by remember { mutableStateOf<PendingSuggestion?>(null) }
                var missing by remember { mutableStateOf(false) }

                LaunchedEffect(pendingId) {
                    val repo = (application as GestorFinanceiroApp).repository
                    suggestion = withContext(Dispatchers.IO) { repo.getPending(pendingId) }
                    if (suggestion == null) missing = true
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        missing -> {
                            PromptCard(
                                title = "Sugestão indisponível",
                                amountLabel = null,
                                subtitle = "Ela pode ter sido confirmada ou descartada.",
                                onRegister = { finish() },
                                onDismiss = { finish() },
                                registerLabel = "Fechar",
                                showDismiss = false,
                            )
                        }
                        suggestion == null -> {
                            PromptCard(
                                title = "Nova compra",
                                amountLabel = null,
                                subtitle = "Carregando…",
                                onRegister = {},
                                onDismiss = { finish() },
                                registerLabel = "Registrar",
                                registerEnabled = false,
                            )
                        }
                        else -> {
                            val s = suggestion!!
                            val amount = MoneyFormat.formatCents(s.amountCents)
                            val origin = s.origin?.takeIf { it.isNotBlank() } ?: "revisar manualmente"
                            PromptCard(
                                title = "Registrar despesa?",
                                amountLabel = amount,
                                subtitle = "${s.bankLabel} · $origin",
                                onRegister = {
                                    startActivity(
                                        MainActivity.confirmIntent(this@SuggestionPromptActivity, s.id),
                                    )
                                    finish()
                                },
                                onDismiss = { finish() },
                            )
                        }
                    }
                }
            }
        }
    }

    private fun turnScreenOnAndShowOverLock() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguard = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguard.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    companion object {
        fun intent(context: Context, pendingId: Long): Intent =
            Intent(context, SuggestionPromptActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
                putExtra(SuggestionNotifier.EXTRA_PENDING_ID, pendingId)
            }
    }
}

@Composable
private fun PromptCard(
    title: String,
    amountLabel: String?,
    subtitle: String,
    onRegister: () -> Unit,
    onDismiss: () -> Unit,
    registerLabel: String = "Registrar",
    showDismiss: Boolean = true,
    registerEnabled: Boolean = true,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            if (amountLabel != null) {
                Text(
                    text = amountLabel,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (showDismiss) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Agora não")
                    }
                }
                Button(
                    onClick = onRegister,
                    enabled = registerEnabled,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(registerLabel)
                }
            }
        }
    }
}
