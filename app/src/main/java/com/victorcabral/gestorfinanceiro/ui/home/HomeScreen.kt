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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.victorcabral.gestorfinanceiro.data.entity.Expense
import com.victorcabral.gestorfinanceiro.util.MoneyFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Aba Relatórios: registros e total do mês com navegação prev/next.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onOpenPending: () -> Unit,
    onAddManual: () -> Unit,
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val totalCents by viewModel.totalCents.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle()
    val monthLabel by viewModel.monthLabel.collectAsStateWithLifecycle()
    val paymentNames by viewModel.paymentSourceNames.collectAsStateWithLifecycle()
    val categoryNames by viewModel.categoryNames.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Relatórios") },
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
                        ExpenseRow(
                            expense = expense,
                            categoryLabel = expense.categoryId?.let { categoryNames[it] },
                            subcategoryLabel = expense.subcategoryId?.let { categoryNames[it] },
                            paymentLabel = expense.paymentSourceId?.let { paymentNames[it] },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(
    expense: Expense,
    categoryLabel: String?,
    subcategoryLabel: String?,
    paymentLabel: String?,
) {
    val dateLabel = formatDayMonth(expense.occurredAtEpochMs)
    val meta = listOfNotNull(
        paymentLabel,
        listOfNotNull(categoryLabel, subcategoryLabel).joinToString(" › ").ifBlank { null },
        expense.origin.takeIf { it.isNotBlank() && it != expense.description },
        dateLabel,
    ).joinToString(" · ")

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
            if (meta.isNotBlank()) {
                Text(meta, style = MaterialTheme.typography.bodySmall)
            }
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
