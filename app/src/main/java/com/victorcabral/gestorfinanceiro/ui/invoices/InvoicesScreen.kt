package com.victorcabral.gestorfinanceiro.ui.invoices

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
fun InvoicesScreen(
    viewModel: InvoicesViewModel,
    onBack: () -> Unit,
) {
    val rows by viewModel.rows.collectAsStateWithLifecycle()
    val selectedCardId by viewModel.selectedCardId.collectAsStateWithLifecycle()
    val amountText by viewModel.amountText.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Faturas do mês") },
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
                viewModel.monthLabel,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                "Informe o valor a pagar de cada cartão neste mês. Aberto entra no card Faturas; pago some do Livre.",
                style = MaterialTheme.typography.bodyMedium,
            )

            if (rows.isEmpty()) {
                Text(
                    "Cadastre um cartão em Contas e cartões primeiro.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rows.forEach { row ->
                        FilterChip(
                            selected = selectedCardId == row.card.id,
                            onClick = { viewModel.onSelectCard(row.card.id) },
                            label = { Text(row.card.name) },
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = viewModel::onAmountChange,
                    label = { Text("Valor da fatura (R$)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = selectedCardId != null,
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                )
                Button(
                    onClick = viewModel::save,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedCardId != null,
                ) {
                    Text("Salvar fatura")
                }

                HorizontalDivider()

                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(rows, key = { it.card.id }) { row ->
                        val invoice = row.invoice
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(row.card.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    if (invoice == null) {
                                        "Sem fatura neste mês"
                                    } else {
                                        MoneyFormat.formatCents(invoice.amountCents) +
                                            if (invoice.isPaid) " · paga" else " · em aberto"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when {
                                        invoice == null -> MaterialTheme.colorScheme.onSurfaceVariant
                                        invoice.isPaid -> Color(0xFF1B5E20)
                                        else -> Color(0xFF8B1E1E)
                                    },
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            if (invoice != null) {
                                FilterChip(
                                    selected = invoice.isPaid,
                                    onClick = { viewModel.togglePaid(invoice) },
                                    label = { Text(if (invoice.isPaid) "Paga" else "Aberta") },
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
