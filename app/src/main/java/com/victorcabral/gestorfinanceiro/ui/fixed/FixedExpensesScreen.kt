package com.victorcabral.gestorfinanceiro.ui.fixed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.victorcabral.gestorfinanceiro.util.MoneyFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedExpensesScreen(
    viewModel: FixedExpensesViewModel,
    onBack: () -> Unit,
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val amountText by viewModel.amountText.collectAsStateWithLifecycle()
    val dueDayText by viewModel.dueDayText.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Despesas fixas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Aluguel, internet, academia… A soma das ativas vira o card Pendentes na Home.",
                style = MaterialTheme.typography.bodyMedium,
            )

            OutlinedTextField(
                value = name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = amountText,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Valor mensal (R$)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = error != null,
                supportingText = error?.let { { Text(it) } },
            )
            OutlinedTextField(
                value = dueDayText,
                onValueChange = viewModel::onDueDayChange,
                label = { Text("Dia do vencimento (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Button(onClick = viewModel::add, modifier = Modifier.fillMaxWidth()) {
                Text("Adicionar despesa fixa")
            }

            HorizontalDivider()

            if (expenses.isEmpty()) {
                Text(
                    "Nenhuma despesa fixa cadastrada.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(expenses, key = { it.id }) { expense ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(expense.name, style = MaterialTheme.typography.titleMedium)
                                val due = expense.dueDayOfMonth?.let { "vence dia $it" }
                                Text(
                                    listOfNotNull(
                                        MoneyFormat.formatCents(expense.amountCents),
                                        due,
                                        if (expense.isActive) null else "inativa",
                                    ).joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (expense.isActive) {
                                        Color(0xFF8B1E1E)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            FilterChip(
                                selected = expense.isActive,
                                onClick = { viewModel.toggleActive(expense) },
                                label = { Text(if (expense.isActive) "Ativa" else "Off") },
                            )
                            IconButton(onClick = { viewModel.delete(expense.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir")
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
