package com.victorcabral.gestorfinanceiro.ui.categories

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    onBack: () -> Unit,
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val newName by viewModel.newName.collectAsStateWithLifecycle()
    val editingId by viewModel.editingId.collectAsStateWithLifecycle()
    val editName by viewModel.editName.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorias") },
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
                text = "Crie, renomeie ou oculte categorias. Ocultas somem do seletor de confirmação.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = viewModel::onNewNameChange,
                    label = { Text("Nova categoria") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                Button(onClick = viewModel::addCategory) {
                    Text("Adicionar")
                }
            }
            HorizontalDivider()
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(categories, key = { it.id }) { category ->
                    if (editingId == category.id) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = viewModel::onEditNameChange,
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                            Button(onClick = { viewModel.commitRename(category) }) {
                                Text("Salvar")
                            }
                            OutlinedButton(onClick = viewModel::cancelRename) {
                                Text("Cancelar")
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(category.name, style = MaterialTheme.typography.titleMedium)
                                val tags = buildList {
                                    if (category.isDefault) add("padrão")
                                    if (category.isHidden) add("oculta")
                                }
                                if (tags.isNotEmpty()) {
                                    Text(
                                        tags.joinToString(" · "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.startRename(category) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Renomear")
                            }
                            IconButton(onClick = { viewModel.toggleHidden(category) }) {
                                Icon(
                                    if (category.isHidden) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (category.isHidden) "Mostrar" else "Ocultar",
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
