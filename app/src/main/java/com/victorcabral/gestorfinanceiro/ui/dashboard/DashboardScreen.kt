package com.victorcabral.gestorfinanceiro.ui.dashboard

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.victorcabral.gestorfinanceiro.util.MoneyFormat
import com.victorcabral.gestorfinanceiro.util.NotificationAccess

private val PositiveGreen = Color(0xFF1B5E20)
private val NegativeRed = Color(0xFF8B1E1E)
private val CardBorder = Color(0xFFBDBDBD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier,
    onOpenPending: () -> Unit,
    onAddManual: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenOnboarding: () -> Unit,
    onOpenIncomes: () -> Unit,
    onOpenFixedExpenses: () -> Unit,
    onOpenInvoices: () -> Unit,
) {
    val guide by viewModel.guide.collectAsStateWithLifecycle()
    val totalCents by viewModel.totalCents.collectAsStateWithLifecycle()
    val expenseCount by viewModel.expenseCount.collectAsStateWithLifecycle()
    val suggestionCount by viewModel.suggestionCount.collectAsStateWithLifecycle()
    val topCategories by viewModel.topCategories.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var listenerEnabled by remember { mutableStateOf(NotificationAccess.isEnabled(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                listenerEnabled = NotificationAccess.isEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Gestor Financeiro") },
                actions = {
                    IconButton(onClick = onOpenPending) {
                        BadgedBox(
                            badge = {
                                if (suggestionCount > 0) {
                                    Badge { Text(suggestionCount.toString()) }
                                }
                            },
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Sugestões")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddManual) {
                Icon(Icons.Default.Add, contentDescription = "Nova despesa")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!listenerEnabled) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Acesso a notificações desativado",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Text(
                                "Sem isso o app não captura Nubank/Santander.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                        TextButton(onClick = onOpenOnboarding) { Text("Ativar") }
                    }
                }
            }

            Text(viewModel.monthLabel, style = MaterialTheme.typography.titleMedium)

            Text(
                text = "Saldo atual – pendentes até o fim do mês – faturas ainda em aberto",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    GuideCard(
                        label = "Caixa",
                        amountCents = guide.caixaCents,
                        positiveIsGood = true,
                        onClick = onOpenIncomes,
                        modifier = Modifier.weight(1f),
                    )
                    GuideCard(
                        label = "Pendentes",
                        amountCents = guide.fixedPendingCents,
                        positiveIsGood = false,
                        onClick = onOpenFixedExpenses,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    GuideCard(
                        label = "Faturas",
                        amountCents = guide.openInvoicesCents,
                        positiveIsGood = false,
                        onClick = onOpenInvoices,
                        modifier = Modifier.weight(1f),
                    )
                    GuideCard(
                        label = "Livre",
                        amountCents = guide.livreCents,
                        positiveIsGood = true,
                        onClick = null,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Text(
                text = "Caixa sobe com salário/receitas e desce com gastos em conta. " +
                    "Pendentes = despesas fixas. Faturas = cartão do mês. Livre = Caixa − Pendentes − Faturas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text("Movimentação do mês", style = MaterialTheme.typography.titleMedium)
            Text(
                MoneyFormat.formatCents(totalCents),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "$expenseCount despesas registradas",
                style = MaterialTheme.typography.bodyMedium,
            )

            Text("Por categoria", style = MaterialTheme.typography.titleMedium)
            if (topCategories.isEmpty()) {
                Text(
                    "Nenhum gasto categorizado ainda.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                topCategories.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(row.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            MoneyFormat.formatCents(row.totalCents),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    HorizontalDivider()
                }
            }

            Button(
                onClick = onOpenReports,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                Text("Ver registros do mês")
            }
        }
    }
}

@Composable
private fun GuideCard(
    label: String,
    amountCents: Long,
    positiveIsGood: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val valueColor = when {
        amountCents > 0L && positiveIsGood -> PositiveGreen
        amountCents < 0L -> NegativeRed
        amountCents > 0L && !positiveIsGood -> NegativeRed
        else -> MaterialTheme.colorScheme.onSurface
    }
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .border(1.dp, CardBorder, shape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = MoneyFormat.formatCents(amountCents),
            color = valueColor,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
