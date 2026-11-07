package com.victorcabral.gestorfinanceiro.ui.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.victorcabral.gestorfinanceiro.data.entity.PaymentSource
import com.victorcabral.gestorfinanceiro.data.entity.PaymentSourceType
import com.victorcabral.gestorfinanceiro.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaymentSourcesViewModel(
    private val repository: FinanceRepository,
) : ViewModel() {
    val sources: StateFlow<List<PaymentSource>> = repository
        .observeAllPaymentSources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _bankLabel = MutableStateFlow("")
    val bankLabel: StateFlow<String> = _bankLabel.asStateFlow()

    private val _type = MutableStateFlow(PaymentSourceType.ACCOUNT)
    val type: StateFlow<PaymentSourceType> = _type.asStateFlow()

    fun onNameChange(value: String) {
        _name.value = value
    }

    fun onBankLabelChange(value: String) {
        _bankLabel.value = value
    }

    fun onTypeChange(type: PaymentSourceType) {
        _type.value = type
    }

    fun add() {
        val name = _name.value.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            repository.addPaymentSource(
                name = name,
                type = _type.value,
                bankLabel = _bankLabel.value,
            )
            _name.value = ""
            _bankLabel.value = ""
        }
    }

    fun toggleHidden(source: PaymentSource) {
        viewModelScope.launch {
            repository.setPaymentSourceHidden(source, !source.isHidden)
        }
    }

    companion object {
        fun factory(repository: FinanceRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PaymentSourcesViewModel(repository) as T
                }
            }
    }
}
