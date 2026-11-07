package com.victorcabral.gestorfinanceiro.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.victorcabral.gestorfinanceiro.data.dao.CategorySpendRow
import com.victorcabral.gestorfinanceiro.data.entity.Category
import com.victorcabral.gestorfinanceiro.data.repository.FinanceRepository
import com.victorcabral.gestorfinanceiro.data.repository.MonthGuideTotals
import com.victorcabral.gestorfinanceiro.util.MonthRange
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CategorySpendUi(
    val categoryId: Long?,
    val name: String,
    val totalCents: Long,
)

class DashboardViewModel(
    repository: FinanceRepository,
) : ViewModel() {
    private val month = MonthRange.current()

    val monthLabel: String = month.labelPtBr()

    val guide: StateFlow<MonthGuideTotals> = repository
        .observeMonthGuide(month)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            MonthGuideTotals(0, 0, 0),
        )

    val totalCents: StateFlow<Long> = repository
        .observeMonthTotalCents(month)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val expenseCount: StateFlow<Int> = repository
        .observeMonthCount(month)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val suggestionCount: StateFlow<Int> = repository
        .observePendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val topCategories: StateFlow<List<CategorySpendUi>> = combine(
        repository.observeSpendByCategory(month),
        repository.observeVisibleRootCategories(),
    ) { rows: List<CategorySpendRow>, roots: List<Category> ->
        val names = roots.associate { it.id to it.name }
        rows.take(5).map { row ->
            CategorySpendUi(
                categoryId = row.categoryId,
                name = row.categoryId?.let { names[it] } ?: "Sem categoria",
                totalCents = row.totalCents,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        fun factory(repository: FinanceRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DashboardViewModel(repository) as T
                }
            }
    }
}
