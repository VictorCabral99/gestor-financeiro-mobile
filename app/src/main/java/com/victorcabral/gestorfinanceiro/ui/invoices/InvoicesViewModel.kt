package com.victorcabral.gestorfinanceiro.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.victorcabral.gestorfinanceiro.data.entity.CardInvoice
import com.victorcabral.gestorfinanceiro.data.entity.PaymentSource
import com.victorcabral.gestorfinanceiro.data.entity.PaymentSourceType
import com.victorcabral.gestorfinanceiro.data.repository.FinanceRepository
import com.victorcabral.gestorfinanceiro.util.MoneyFormat
import com.victorcabral.gestorfinanceiro.util.MonthRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InvoiceRowUi(
    val invoice: CardInvoice?,
    val card: PaymentSource,
)

class InvoicesViewModel(
    private val repository: FinanceRepository,
) : ViewModel() {
    private val month = MonthRange.current()
    val monthLabel: String = month.labelPtBr()

    private val cards = repository.observeVisiblePaymentSources().map { sources ->
        sources.filter { it.type == PaymentSourceType.CREDIT_CARD }
    }

    val rows: StateFlow<List<InvoiceRowUi>> = combine(
        cards,
        repository.observeCardInvoices(month),
    ) { cardList, invoices ->
        val bySource = invoices.associateBy { it.paymentSourceId }
        cardList.map { card ->
            InvoiceRowUi(invoice = bySource[card.id], card = card)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedCardId = MutableStateFlow<Long?>(null)
    val selectedCardId: StateFlow<Long?> = _selectedCardId.asStateFlow()

    private val _amountText = MutableStateFlow("")
    val amountText: StateFlow<String> = _amountText.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun onSelectCard(id: Long) {
        _selectedCardId.value = id
        val existing = rows.value.firstOrNull { it.card.id == id }?.invoice
        _amountText.value = existing
            ?.let { (it.amountCents / 100.0).toString().replace('.', ',') }
            ?: ""
    }

    fun onAmountChange(value: String) {
        _amountText.value = value
    }

    fun save() {
        val cardId = _selectedCardId.value
        val cents = MoneyFormat.parseToCents(_amountText.value)
        if (cardId == null) {
            _error.value = "Escolha o cartão"
            return
        }
        if (cents == null || cents < 0L) {
            _error.value = "Informe um valor válido"
            return
        }
        val currentPaid = rows.value.firstOrNull { it.card.id == cardId }?.invoice?.isPaid ?: false
        viewModelScope.launch {
            repository.upsertCardInvoice(
                paymentSourceId = cardId,
                year = month.year,
                month = month.month,
                amountCents = cents,
                isPaid = currentPaid,
            )
            _error.value = null
            _amountText.value = ""
            _selectedCardId.value = null
        }
    }

    fun togglePaid(invoice: CardInvoice) {
        viewModelScope.launch {
            repository.setCardInvoicePaid(invoice, !invoice.isPaid)
        }
    }

    companion object {
        fun factory(repository: FinanceRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return InvoicesViewModel(repository) as T
                }
            }
    }
}
