package com.victorcabral.gestorfinanceiro.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.victorcabral.gestorfinanceiro.data.entity.Category
import com.victorcabral.gestorfinanceiro.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val repository: FinanceRepository,
) : ViewModel() {
    val categories: StateFlow<List<Category>> = repository
        .observeAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _newName = MutableStateFlow("")
    val newName: StateFlow<String> = _newName.asStateFlow()

    private val _editingId = MutableStateFlow<Long?>(null)
    val editingId: StateFlow<Long?> = _editingId.asStateFlow()

    private val _editName = MutableStateFlow("")
    val editName: StateFlow<String> = _editName.asStateFlow()

    fun onNewNameChange(value: String) {
        _newName.value = value
    }

    fun addCategory() {
        val name = _newName.value.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            repository.addCategory(name)
            _newName.value = ""
        }
    }

    fun startRename(category: Category) {
        _editingId.value = category.id
        _editName.value = category.name
    }

    fun onEditNameChange(value: String) {
        _editName.value = value
    }

    fun cancelRename() {
        _editingId.value = null
        _editName.value = ""
    }

    fun commitRename(category: Category) {
        val name = _editName.value.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            repository.renameCategory(category, name)
            cancelRename()
        }
    }

    fun toggleHidden(category: Category) {
        viewModelScope.launch {
            repository.setCategoryHidden(category, !category.isHidden)
        }
    }

    companion object {
        fun factory(repository: FinanceRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CategoriesViewModel(repository) as T
                }
            }
    }
}
