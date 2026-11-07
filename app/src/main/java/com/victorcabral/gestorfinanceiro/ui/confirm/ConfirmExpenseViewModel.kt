package com.victorcabral.gestorfinanceiro.ui.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.victorcabral.gestorfinanceiro.data.entity.Category
import com.victorcabral.gestorfinanceiro.data.entity.PaymentSource
import com.victorcabral.gestorfinanceiro.data.repository.FinanceRepository
import com.victorcabral.gestorfinanceiro.util.MoneyFormat
import com.victorcabral.gestorfinanceiro.util.PaymentSourceInference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConfirmUiState(
    val amountText: String = "",
    val origin: String = "",
    val description: String = "",
    val selectedCategoryId: Long? = null,
    val selectedSubcategoryId: Long? = null,
    val selectedPaymentSourceId: Long? = null,
    /** Hint shown when conta/cartão was inferred from the bank notification. */
    val paymentSourceHint: String? = null,
    val bankPackage: String? = null,
    val bankLabel: String? = null,
    val rawText: String? = null,
    val needsManualReview: Boolean = false,
    val occurredAtEpochMs: Long = System.currentTimeMillis(),
    val pendingId: Long? = null,
    val error: String? = null,
    val saved: Boolean = false,
    val discarded: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmExpenseViewModel(
    private val repository: FinanceRepository,
    private val pendingId: Long?,
) : ViewModel() {
    private val _state = MutableStateFlow(ConfirmUiState(pendingId = pendingId))
    val state: StateFlow<ConfirmUiState> = _state.asStateFlow()

    val categories: StateFlow<List<Category>> = repository
        .observeVisibleRootCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val subcategories: StateFlow<List<Category>> = _state
        .flatMapLatest { ui ->
            val catId = ui.selectedCategoryId
            if (catId == null) flowOf(emptyList())
            else repository.observeVisibleSubcategories(catId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val paymentSources: StateFlow<List<PaymentSource>> = repository
        .observeVisiblePaymentSources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        if (pendingId != null) {
            viewModelScope.launch {
                val pending = repository.getPending(pendingId) ?: return@launch
                val sources = repository.observeVisiblePaymentSources().first()
                val matched = PaymentSourceInference.match(
                    sources = sources,
                    bankLabel = pending.bankLabel,
                    rawText = pending.rawText,
                )
                val modality = PaymentSourceInference.modalityLabel(
                    PaymentSourceInference.inferType(pending.rawText),
                )
                val hint = when {
                    matched != null && modality != null ->
                        "Detectado na notificação: ${matched.name} · $modality"
                    matched != null ->
                        "Detectado na notificação: ${matched.name}"
                    modality != null ->
                        "A notificação indica $modality — escolha a fonte correspondente"
                    else ->
                        "Na maioria das compras o banco já indica conta (débito) ou cartão (crédito)"
                }
                _state.update {
                    it.copy(
                        amountText = pending.amountCents?.let { cents ->
                            String.format(java.util.Locale("pt", "BR"), "%.2f", cents / 100.0)
                        }.orEmpty(),
                        origin = pending.origin.orEmpty(),
                        description = pending.origin.orEmpty(),
                        bankPackage = pending.bankPackage,
                        bankLabel = pending.bankLabel,
                        rawText = pending.rawText,
                        needsManualReview = pending.needsManualReview || pending.amountCents == null,
                        occurredAtEpochMs = pending.receivedAtEpochMs,
                        pendingId = pending.id,
                        selectedPaymentSourceId = matched?.id,
                        paymentSourceHint = hint,
                    )
                }
            }
        }
    }

    fun onAmountChange(value: String) = _state.update { it.copy(amountText = value, error = null) }
    fun onOriginChange(value: String) = _state.update { it.copy(origin = value, error = null) }
    fun onDescriptionChange(value: String) = _state.update { it.copy(description = value, error = null) }

    fun onCategorySelected(id: Long?) = _state.update {
        it.copy(selectedCategoryId = id, selectedSubcategoryId = null)
    }

    fun onSubcategorySelected(id: Long?) = _state.update { it.copy(selectedSubcategoryId = id) }

    fun onPaymentSourceSelected(id: Long?) = _state.update {
        it.copy(
            selectedPaymentSourceId = id,
            // Keep the detection hint so the user understands the default.
            paymentSourceHint = it.paymentSourceHint,
        )
    }

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
                subcategoryId = current.selectedSubcategoryId,
                paymentSourceId = current.selectedPaymentSourceId,
                bankPackage = current.bankPackage,
                occurredAtEpochMs = current.occurredAtEpochMs,
                pendingIdToDismiss = current.pendingId,
            )
            _state.update { it.copy(saved = true, error = null) }
        }
    }

    fun discard() {
        val id = _state.value.pendingId ?: run {
            _state.update { it.copy(discarded = true) }
            return
        }
        viewModelScope.launch {
            repository.dismissPending(id)
            _state.update { it.copy(discarded = true) }
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
