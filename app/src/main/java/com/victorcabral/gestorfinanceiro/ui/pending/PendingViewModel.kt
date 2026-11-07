package com.victorcabral.gestorfinanceiro.ui.pending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.victorcabral.gestorfinanceiro.data.entity.PendingSuggestion
import com.victorcabral.gestorfinanceiro.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PendingViewModel(
    private val repository: FinanceRepository,
) : ViewModel() {
    val pending: StateFlow<List<PendingSuggestion>> = repository
        .observePending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun dismiss(id: Long) {
        viewModelScope.launch { repository.dismissPending(id) }
    }

    companion object {
        fun factory(repository: FinanceRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PendingViewModel(repository) as T
                }
            }
    }
}
