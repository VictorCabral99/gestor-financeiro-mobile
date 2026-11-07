package com.victorcabral.gestorfinanceiro.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.victorcabral.gestorfinanceiro.data.entity.Expense
import com.victorcabral.gestorfinanceiro.data.repository.FinanceRepository
import com.victorcabral.gestorfinanceiro.util.MonthRange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    repository: FinanceRepository,
) : ViewModel() {
    private val _month = MutableStateFlow(MonthRange.current())
    val month: StateFlow<MonthRange> = _month.asStateFlow()

    val monthLabel: StateFlow<String> = _month
        .map { it.labelPtBr() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), _month.value.labelPtBr())

    val expenses: StateFlow<List<Expense>> = _month
        .flatMapLatest { range -> repository.observeMonthExpenses(range) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalCents: StateFlow<Long> = _month
        .flatMapLatest { range -> repository.observeMonthTotalCents(range) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val pendingCount: StateFlow<Int> = repository
        .observePendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun previousMonth() {
        _month.value = _month.value.previous()
    }

    fun nextMonth() {
        _month.value = _month.value.next()
    }

    fun goToCurrentMonth() {
        _month.value = MonthRange.current()
    }

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
