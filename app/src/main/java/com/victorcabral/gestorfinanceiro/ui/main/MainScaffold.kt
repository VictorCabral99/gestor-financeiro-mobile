package com.victorcabral.gestorfinanceiro.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.victorcabral.gestorfinanceiro.ui.dashboard.DashboardScreen
import com.victorcabral.gestorfinanceiro.ui.dashboard.DashboardViewModel
import com.victorcabral.gestorfinanceiro.ui.home.HomeScreen
import com.victorcabral.gestorfinanceiro.ui.home.HomeViewModel
import com.victorcabral.gestorfinanceiro.ui.navigation.MainTab
import com.victorcabral.gestorfinanceiro.ui.settings.SettingsScreen

@Composable
fun MainScaffold(
    dashboardViewModel: DashboardViewModel,
    reportsViewModel: HomeViewModel,
    onOpenPending: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenPaymentSources: () -> Unit,
    onOpenIncomes: () -> Unit,
    onOpenFixedExpenses: () -> Unit,
    onOpenInvoices: () -> Unit,
    onAddManual: () -> Unit,
    onOpenOnboarding: () -> Unit,
    onOpenDebugSimulator: (() -> Unit)?,
) {
    var tab by rememberSaveable { mutableStateOf(MainTab.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == MainTab.HOME,
                    onClick = { tab = MainTab.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") },
                )
                NavigationBarItem(
                    selected = tab == MainTab.REPORTS,
                    onClick = { tab = MainTab.REPORTS },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                    label = { Text("Relatórios") },
                )
                NavigationBarItem(
                    selected = tab == MainTab.SETTINGS,
                    onClick = { tab = MainTab.SETTINGS },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Config") },
                )
            }
        },
    ) { padding ->
        when (tab) {
            MainTab.HOME -> DashboardScreen(
                viewModel = dashboardViewModel,
                modifier = Modifier.padding(padding),
                onOpenPending = onOpenPending,
                onAddManual = onAddManual,
                onOpenReports = { tab = MainTab.REPORTS },
                onOpenOnboarding = onOpenOnboarding,
                onOpenIncomes = onOpenIncomes,
                onOpenFixedExpenses = onOpenFixedExpenses,
                onOpenInvoices = onOpenInvoices,
            )
            MainTab.REPORTS -> HomeScreen(
                viewModel = reportsViewModel,
                modifier = Modifier.padding(padding),
                onOpenPending = onOpenPending,
                onAddManual = onAddManual,
            )
            MainTab.SETTINGS -> SettingsScreen(
                modifier = Modifier.padding(padding),
                onOpenCategories = onOpenCategories,
                onOpenPaymentSources = onOpenPaymentSources,
                onOpenIncomes = onOpenIncomes,
                onOpenFixedExpenses = onOpenFixedExpenses,
                onOpenInvoices = onOpenInvoices,
                onOpenOnboarding = onOpenOnboarding,
                onOpenDebugSimulator = onOpenDebugSimulator,
                onOpenPending = onOpenPending,
            )
        }
    }
}
