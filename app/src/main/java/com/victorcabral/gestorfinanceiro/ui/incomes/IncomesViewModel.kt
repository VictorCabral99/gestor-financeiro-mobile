package com.victorcabral.gestorfinanceiro.ui.incomes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.victorcabral.gestorfinanceiro.data.entity.Income
import com.victorcabral.gestorfinanceiro.data.repository.FinanceRepository
import com.victorcabral.gestorfinanceiro.util.MoneyFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IncomesViewModel(
    private val repository: FinanceRepository,
) : ViewModel() {
    val incomes: StateFlow<List<Income>> = repository
        .observeIncomes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _description = MutableStateFlow("Salário")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _amountText = MutableStateFlow("")
    val amountText: StateFlow<String> = _amountText.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun onDescriptionChange(value: String) {
        _description.value = value
    }

    fun onAmountChange(value: String) {
        _amountText.value = value
    }

    fun add() {
        val cents = MoneyFormat.parseToCents(_amountText.value)
        if (cents == null || cents <= 0L) {
            _error.value = "Informe um valor válido"
            return
        }
        viewModelScope.launch {
            repository.addIncome(
                amountCents = cents,
                description = _description.value,
                paymentSourceId = null,
            )
            _amountText.value = ""
            _description.value = "Salário"
            _error.value = null
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.deleteIncome(id)
        }
    }

    companion object {
        fun factory(repository: FinanceRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return IncomesViewModel(repository) as T
                }
            }
    }
}
