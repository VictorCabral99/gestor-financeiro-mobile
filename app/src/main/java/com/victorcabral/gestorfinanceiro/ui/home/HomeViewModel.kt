package com.victorcabral.gestorfinanceiro.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.victorcabral.gestorfinanceiro.data.entity.Expense
import com.victorcabral.gestorfinanceiro.data.repository.FinanceRepository
import com.victorcabral.gestorfinanceiro.util.MonthRange
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    repository: FinanceRepository,
) : ViewModel() {
    private val month = MonthRange.current()

    val monthLabel: String = month.labelPtBr()

    val expenses: StateFlow<List<Expense>> = repository
        .observeMonthExpenses(month)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalCents: StateFlow<Long> = repository
        .observeMonthTotalCents(month)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val pendingCount: StateFlow<Int> = repository
        .observePendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    companion object {
        fun factory(repository: FinanceRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repository) as T
                }
            }
    }
}
