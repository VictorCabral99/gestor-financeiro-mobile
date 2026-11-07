package com.victorcabral.gestorfinanceiro.ui.pending

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.victorcabral.gestorfinanceiro.data.entity.PendingSuggestion
import com.victorcabral.gestorfinanceiro.util.MoneyFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingScreen(
    viewModel: PendingViewModel,
    onBack: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    val items by viewModel.pending.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sugestões pendentes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        if (items.isEmpty()) {
            Text(
                text = "Nenhuma sugestão no momento.\nQuando o Nubank ou Santander notificar uma compra, ela aparece aqui.",
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items, key = { it.id }) { suggestion ->
                    PendingRow(
                        suggestion = suggestion,
                        onConfirm = { onConfirm(suggestion.id) },
                        onDismiss = { viewModel.dismiss(suggestion.id) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun PendingRow(
    suggestion: PendingSuggestion,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = suggestion.bankLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = MoneyFormat.formatCents(suggestion.amountCents),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = suggestion.origin ?: "Origem não identificada",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = suggestion.rawText,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 3,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onConfirm) { Text("Confirmar") }
            OutlinedButton(onClick = onDismiss) { Text("Descartar") }
        }
    }
}
