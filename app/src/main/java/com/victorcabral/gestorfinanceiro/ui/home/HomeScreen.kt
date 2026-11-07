package com.victorcabral.gestorfinanceiro.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.victorcabral.gestorfinanceiro.data.entity.Expense
import com.victorcabral.gestorfinanceiro.util.MoneyFormat
import com.victorcabral.gestorfinanceiro.util.NotificationAccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenPending: () -> Unit,
    onOpenCategories: () -> Unit,
    onAddManual: () -> Unit,
    onOpenOnboarding: () -> Unit,
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val totalCents by viewModel.totalCents.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle()
    val monthLabel by viewModel.monthLabel.collectAsStateWithLifecycle()

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
        topBar = {
            TopAppBar(
                title = { Text("Despesas do mês") },
                actions = {
                    IconButton(onClick = onOpenPending) {
                        BadgedBox(
                            badge = {
                                if (pendingCount > 0) {
                                    Badge { Text(pendingCount.toString()) }
                                }
                            },
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Pendentes")
                        }
                    }
                    IconButton(onClick = onOpenCategories) {
                        Icon(Icons.Default.Category, contentDescription = "Categorias")
                    }
                    IconButton(onClick = onOpenOnboarding) {
                        Icon(Icons.Default.Settings, contentDescription = "Permissões")
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
                .padding(horizontal = 16.dp),
        ) {
            if (!listenerEnabled) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
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
                                text = "Acesso a notificações desativado",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Text(
                                text = "Sem isso o app não captura Nubank/Santander.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                        TextButton(onClick = onOpenOnboarding) {
                            Text("Ativar")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = viewModel::previousMonth) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Mês anterior",
                    )
                }
                Text(
                    text = monthLabel,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.clickable(onClick = viewModel::goToCurrentMonth),
                )
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Próximo mês",
                    )
                }
            }

            Text(
                text = MoneyFormat.formatCents(totalCents),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Total gasto no mês",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            if (pendingCount > 0) {
                Text(
                    text = "$pendingCount sugestão(ões) pendente(s) — toque no sino",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clickable(onClick = onOpenPending)
                        .padding(bottom = 8.dp),
                )
            }

            HorizontalDivider()

            if (expenses.isEmpty()) {
                Text(
                    text = "Nenhuma despesa neste mês ainda.\nConfirme uma sugestão ou adicione manualmente.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 24.dp),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(expenses, key = { it.id }) { expense ->
                        ExpenseRow(expense)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense) {
    val dateLabel = formatDayMonth(expense.occurredAtEpochMs)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.description.ifBlank { expense.origin },
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = listOfNotNull(
                    expense.origin.takeIf { it.isNotBlank() && it != expense.description },
                    dateLabel,
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            text = MoneyFormat.formatCents(expense.amountCents),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

private fun formatDayMonth(epochMs: Long): String {
    val formatter = SimpleDateFormat("dd/MM", Locale("pt", "BR"))
    return formatter.format(Date(epochMs))
}
