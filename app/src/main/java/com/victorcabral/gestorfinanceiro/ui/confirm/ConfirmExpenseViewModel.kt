package com.victorcabral.gestorfinanceiro.ui.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.victorcabral.gestorfinanceiro.data.entity.Category
import com.victorcabral.gestorfinanceiro.data.repository.FinanceRepository
import com.victorcabral.gestorfinanceiro.util.MoneyFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConfirmUiState(
    val amountText: String = "",
    val origin: String = "",
    val description: String = "",
    val selectedCategoryId: Long? = null,
    val bankPackage: String? = null,
    val bankLabel: String? = null,
    val occurredAtEpochMs: Long = System.currentTimeMillis(),
    val pendingId: Long? = null,
    val error: String? = null,
    val saved: Boolean = false,
)

class ConfirmExpenseViewModel(
    private val repository: FinanceRepository,
    private val pendingId: Long?,
) : ViewModel() {
    private val _state = MutableStateFlow(ConfirmUiState(pendingId = pendingId))
    val state: StateFlow<ConfirmUiState> = _state.asStateFlow()

    val categories: StateFlow<List<Category>> = repository
        .observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        if (pendingId != null) {
            viewModelScope.launch {
                val pending = repository.getPending(pendingId) ?: return@launch
                _state.update {
                    it.copy(
                        amountText = pending.amountCents?.let { cents ->
                            String.format(java.util.Locale("pt", "BR"), "%.2f", cents / 100.0)
                        }.orEmpty(),
                        origin = pending.origin.orEmpty(),
                        description = pending.origin.orEmpty(),
                        bankPackage = pending.bankPackage,
                        bankLabel = pending.bankLabel,
                        occurredAtEpochMs = pending.receivedAtEpochMs,
                        pendingId = pending.id,
                    )
                }
            }
        }
    }

    fun onAmountChange(value: String) = _state.update { it.copy(amountText = value, error = null) }
    fun onOriginChange(value: String) = _state.update { it.copy(origin = value, error = null) }
    fun onDescriptionChange(value: String) = _state.update { it.copy(description = value, error = null) }
    fun onCategorySelected(id: Long?) = _state.update { it.copy(selectedCategoryId = id) }

    fun save() {
        val current = _state.value
        val cents = MoneyFormat.parseToCents(current.amountText)
        if (cents == null || cents <= 0) {
            _state.update { it.copy(error = "Informe um valor válido (ex.: 12,50)") }
            return
        }
        if (current.origin.isBlank() && current.description.isBlank()) {
            _state.update { it.copy(error = "Informe origem ou descrição") }
            return
        }

        viewModelScope.launch {
            repository.confirmExpense(
                amountCents = cents,
                origin = current.origin.ifBlank { current.description },
                description = current.description.ifBlank { current.origin },
                categoryId = current.selectedCategoryId,
                bankPackage = current.bankPackage,
                occurredAtEpochMs = current.occurredAtEpochMs,
                pendingIdToDismiss = current.pendingId,
            )
            _state.update { it.copy(saved = true, error = null) }
        }
    }

    companion object {
        fun factory(
            repository: FinanceRepository,
            pendingId: Long?,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ConfirmExpenseViewModel(repository, pendingId) as T
                }
            }
    }
}
