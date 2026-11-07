package com.victorcabral.gestorfinanceiro.ui.fixed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.victorcabral.gestorfinanceiro.data.entity.FixedExpense
import com.victorcabral.gestorfinanceiro.data.repository.FinanceRepository
import com.victorcabral.gestorfinanceiro.util.MoneyFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FixedExpensesViewModel(
    private val repository: FinanceRepository,
) : ViewModel() {
    val expenses: StateFlow<List<FixedExpense>> = repository
        .observeFixedExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _amountText = MutableStateFlow("")
    val amountText: StateFlow<String> = _amountText.asStateFlow()

    private val _dueDayText = MutableStateFlow("")
    val dueDayText: StateFlow<String> = _dueDayText.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun onNameChange(value: String) {
        _name.value = value
    }

    fun onAmountChange(value: String) {
        _amountText.value = value
    }

    fun onDueDayChange(value: String) {
        _dueDayText.value = value.filter { it.isDigit() }.take(2)
    }

    fun add() {
        val name = _name.value.trim()
        val cents = MoneyFormat.parseToCents(_amountText.value)
        if (name.isEmpty()) {
            _error.value = "Informe o nome"
            return
        }
        if (cents == null || cents <= 0L) {
            _error.value = "Informe um valor válido"
            return
        }
        val dueDay = _dueDayText.value.toIntOrNull()?.takeIf { it in 1..31 }
        viewModelScope.launch {
            repository.addFixedExpense(name = name, amountCents = cents, dueDayOfMonth = dueDay)
            _name.value = ""
            _amountText.value = ""
            _dueDayText.value = ""
            _error.value = null
        }
    }

    fun toggleActive(expense: FixedExpense) {
        viewModelScope.launch {
            repository.setFixedExpenseActive(expense, !expense.isActive)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.deleteFixedExpense(id)
        }
    }

    companion object {
        fun factory(repository: FinanceRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FixedExpensesViewModel(repository) as T
                }
            }
    }
}
