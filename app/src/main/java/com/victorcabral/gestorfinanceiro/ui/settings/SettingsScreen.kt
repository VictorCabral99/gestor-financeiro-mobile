package com.victorcabral.gestorfinanceiro.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.victorcabral.gestorfinanceiro.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onOpenCategories: () -> Unit,
    onOpenPaymentSources: () -> Unit,
    onOpenIncomes: () -> Unit,
    onOpenFixedExpenses: () -> Unit,
    onOpenInvoices: () -> Unit,
    onOpenOnboarding: () -> Unit,
    onOpenPending: () -> Unit,
    onOpenDebugSimulator: (() -> Unit)?,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Configuração") })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsRow(
                icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                title = "Receitas / salário",
                subtitle = "Alimenta o Caixa da Home",
                onClick = onOpenIncomes,
            )
            SettingsRow(
                icon = { Icon(Icons.Default.EventRepeat, contentDescription = null) },
                title = "Despesas fixas",
                subtitle = "Pendentes até o fim do mês",
                onClick = onOpenFixedExpenses,
            )
            SettingsRow(
                icon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                title = "Faturas do cartão",
                subtitle = "Valor em aberto no mês",
                onClick = onOpenInvoices,
            )
            SettingsRow(
                icon = { Icon(Icons.Default.Category, contentDescription = null) },
                title = "Categorias e subcategorias",
                subtitle = "Um nível de subcategoria",
                onClick = onOpenCategories,
            )
            SettingsRow(
                icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                title = "Contas e cartões",
                subtitle = "Conta = débito · Cartão = crédito",
                onClick = onOpenPaymentSources,
            )
            SettingsRow(
                icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) },
                title = "Acesso a notificações",
                subtitle = "Listener Nubank / Santander",
                onClick = onOpenOnboarding,
            )
            SettingsRow(
                icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                title = "Sugestões pendentes",
                subtitle = "Fila de confirmação",
                onClick = onOpenPending,
            )
            if (BuildConfig.DEBUG && onOpenDebugSimulator != null) {
                SettingsRow(
                    icon = { Icon(Icons.Default.BugReport, contentDescription = null) },
                    title = "Simulador de notificações",
                    subtitle = "DEBUG — fixtures no pipeline",
                    onClick = onOpenDebugSimulator,
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    ListItem(
        leadingContent = icon,
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
    HorizontalDivider()
}
