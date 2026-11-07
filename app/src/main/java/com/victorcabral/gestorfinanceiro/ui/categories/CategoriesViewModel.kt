package com.victorcabral.gestorfinanceiro.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.victorcabral.gestorfinanceiro.data.entity.Category
import com.victorcabral.gestorfinanceiro.data.repository.FinanceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModel(
    private val repository: FinanceRepository,
) : ViewModel() {
    val roots: StateFlow<List<Category>> = repository
        .observeRootCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedRootId = MutableStateFlow<Long?>(null)
    val selectedRootId: StateFlow<Long?> = _selectedRootId.asStateFlow()

    val children: StateFlow<List<Category>> = _selectedRootId
        .flatMapLatest { id ->
            if (id == null) kotlinx.coroutines.flow.flowOf(emptyList())
            else repository.observeChildCategories(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _newRootName = MutableStateFlow("")
    val newRootName: StateFlow<String> = _newRootName.asStateFlow()

    private val _newSubName = MutableStateFlow("")
    val newSubName: StateFlow<String> = _newSubName.asStateFlow()

    private val _editingId = MutableStateFlow<Long?>(null)
    val editingId: StateFlow<Long?> = _editingId.asStateFlow()

    private val _editName = MutableStateFlow("")
    val editName: StateFlow<String> = _editName.asStateFlow()

    fun selectRoot(id: Long?) {
        _selectedRootId.value = id
        _newSubName.value = ""
    }

    fun onNewRootNameChange(value: String) {
        _newRootName.value = value
    }

    fun onNewSubNameChange(value: String) {
        _newSubName.value = value
    }

    fun addRoot() {
        val name = _newRootName.value.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            val id = repository.addRootCategory(name)
            _newRootName.value = ""
            if (id > 0) _selectedRootId.value = id
        }
    }

    fun addSubcategory() {
        val parentId = _selectedRootId.value ?: return
        val name = _newSubName.value.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            repository.addSubcategory(parentId, name)
            _newSubName.value = ""
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
