package com.victorcabral.gestorfinanceiro.ui.categories

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.FilterChip
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
import com.victorcabral.gestorfinanceiro.data.entity.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel,
    onBack: () -> Unit,
) {
    val roots by viewModel.roots.collectAsStateWithLifecycle()
    val children by viewModel.children.collectAsStateWithLifecycle()
    val selectedRootId by viewModel.selectedRootId.collectAsStateWithLifecycle()
    val newRootName by viewModel.newRootName.collectAsStateWithLifecycle()
    val newSubName by viewModel.newSubName.collectAsStateWithLifecycle()
    val editingId by viewModel.editingId.collectAsStateWithLifecycle()
    val editName by viewModel.editName.collectAsStateWithLifecycle()

    val selectedRoot = roots.firstOrNull { it.id == selectedRootId }

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
                "Categorias raiz e uma camada de subcategorias.",
                style = MaterialTheme.typography.bodyMedium,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = newRootName,
                    onValueChange = viewModel::onNewRootNameChange,
                    label = { Text("Nova categoria") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                Button(onClick = viewModel::addRoot) { Text("Add") }
            }

            Text("Categorias", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Wrap chips in a simple flow via Column of rows would be heavy; use LazyColumn of chips below.
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(roots, key = { "root-${it.id}" }) { category ->
                    CategoryLine(
                        category = category,
                        selected = category.id == selectedRootId,
                        editing = editingId == category.id,
                        editName = editName,
                        onSelect = { viewModel.selectRoot(category.id) },
                        onStartRename = { viewModel.startRename(category) },
                        onEditNameChange = viewModel::onEditNameChange,
                        onCommitRename = { viewModel.commitRename(category) },
                        onCancelRename = viewModel::cancelRename,
                        onToggleHidden = { viewModel.toggleHidden(category) },
                    )
                    HorizontalDivider()
                }

                item {
                    Text(
                        text = if (selectedRoot == null) {
                            "Toque numa categoria para ver/adicionar subcategorias."
                        } else {
                            "Subcategorias de “${selectedRoot.name}”"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    )
                }

                if (selectedRoot != null) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp),
                        ) {
                            OutlinedTextField(
                                value = newSubName,
                                onValueChange = viewModel::onNewSubNameChange,
                                label = { Text("Nova subcategoria") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                            Button(onClick = viewModel::addSubcategory) { Text("Add") }
                        }
                    }
                    items(children, key = { "sub-${it.id}" }) { category ->
                        CategoryLine(
                            category = category,
                            selected = false,
                            editing = editingId == category.id,
                            editName = editName,
                            onSelect = {},
                            onStartRename = { viewModel.startRename(category) },
                            onEditNameChange = viewModel::onEditNameChange,
                            onCommitRename = { viewModel.commitRename(category) },
                            onCancelRename = viewModel::cancelRename,
                            onToggleHidden = { viewModel.toggleHidden(category) },
                            indent = true,
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryLine(
    category: Category,
    selected: Boolean,
    editing: Boolean,
    editName: String,
    onSelect: () -> Unit,
    onStartRename: () -> Unit,
    onEditNameChange: (String) -> Unit,
    onCommitRename: () -> Unit,
    onCancelRename: () -> Unit,
    onToggleHidden: () -> Unit,
    indent: Boolean = false,
) {
    if (editing) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .padding(start = if (indent) 16.dp else 0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = editName,
                onValueChange = onEditNameChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            Button(onClick = onCommitRename) { Text("Salvar") }
            OutlinedButton(onClick = onCancelRename) { Text("Cancelar") }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSelect)
                .padding(vertical = 8.dp)
                .padding(start = if (indent) 16.dp else 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(category.name, style = MaterialTheme.typography.titleMedium)
                    if (selected) {
                        FilterChip(
                            selected = true,
                            onClick = onSelect,
                            label = { Text("selecionada") },
                        )
                    }
                }
                val tags = buildList {
                    if (category.isDefault) add("padrão")
                    if (category.isHidden) add("oculta")
                    if (indent) add("sub")
                }
                if (tags.isNotEmpty()) {
                    Text(
                        tags.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            IconButton(onClick = onStartRename) {
                Icon(Icons.Default.Edit, contentDescription = "Renomear")
            }
            IconButton(onClick = onToggleHidden) {
                Icon(
                    if (category.isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (category.isHidden) "Mostrar" else "Ocultar",
                )
            }
        }
    }
}
