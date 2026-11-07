package com.victorcabral.gestorfinanceiro.ui.payments

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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.victorcabral.gestorfinanceiro.data.entity.PaymentSourceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSourcesScreen(
    viewModel: PaymentSourcesViewModel,
    onBack: () -> Unit,
) {
    val sources by viewModel.sources.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val bankLabel by viewModel.bankLabel.collectAsStateWithLifecycle()
    val type by viewModel.type.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contas e cartões") },
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
                "Conta = débito · Cartão = crédito. Ao registrar a despesa, escolher a fonte já define a modalidade.",
                style = MaterialTheme.typography.bodyMedium,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = type == PaymentSourceType.ACCOUNT,
                    onClick = { viewModel.onTypeChange(PaymentSourceType.ACCOUNT) },
                    label = { Text("Conta") },
                )
                FilterChip(
                    selected = type == PaymentSourceType.CREDIT_CARD,
                    onClick = { viewModel.onTypeChange(PaymentSourceType.CREDIT_CARD) },
                    label = { Text("Cartão") },
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = viewModel::onNameChange,
                label = { Text(if (type == PaymentSourceType.ACCOUNT) "Nome da conta" else "Nome do cartão") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = bankLabel,
                onValueChange = viewModel::onBankLabelChange,
                label = { Text("Banco (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Button(onClick = viewModel::add, modifier = Modifier.fillMaxWidth()) {
                Text("Adicionar")
            }

            HorizontalDivider()

            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(sources, key = { it.id }) { source ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(source.name, style = MaterialTheme.typography.titleMedium)
                            val kind = when (source.type) {
                                PaymentSourceType.ACCOUNT -> "conta · débito"
                                PaymentSourceType.CREDIT_CARD -> "cartão · crédito"
                            }
                            val tags = buildList {
                                add(kind)
                                source.bankLabel?.let { add(it) }
                                if (source.isHidden) add("oculta")
                            }
                            Text(
                                tags.joinToString(" · "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                        IconButton(onClick = { viewModel.toggleHidden(source) }) {
                            Icon(
                                if (source.isHidden) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = if (source.isHidden) "Mostrar" else "Ocultar",
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
