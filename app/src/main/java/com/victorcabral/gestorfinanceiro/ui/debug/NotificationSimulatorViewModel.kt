package com.victorcabral.gestorfinanceiro.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.victorcabral.gestorfinanceiro.notification.NotificationPipeline
import com.victorcabral.gestorfinanceiro.notification.sim.NotificationSimulator
import com.victorcabral.gestorfinanceiro.notification.sim.SimulatedBankNotification
import com.victorcabral.gestorfinanceiro.util.MoneyFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationSimulatorViewModel(
    private val simulator: NotificationSimulator,
) : ViewModel() {
    private val _lastStatus = MutableStateFlow<String?>(null)
    val lastStatus: StateFlow<String?> = _lastStatus.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    fun inject(sample: SimulatedBankNotification) {
        viewModelScope.launch {
            _busy.value = true
            try {
                when (val result = simulator.inject(sample)) {
                    is NotificationPipeline.HandleResult.Inserted -> {
                        val amount = MoneyFormat.formatCents(result.suggestion.amountCents)
                        val origin = result.suggestion.origin ?: "(sem origem)"
                        val review = if (result.suggestion.needsManualReview) " · revisar" else ""
                        _lastStatus.value =
                            "Inserida #${result.id}: $amount · $origin$review — veja Pendentes"
                    }
                    is NotificationPipeline.HandleResult.Duplicate -> {
                        _lastStatus.value =
                            "Duplicada (dedupe) — mesma notificação na janela de 2 min"
                    }
                    is NotificationPipeline.HandleResult.Ignored -> {
                        _lastStatus.value =
                            "Ignorada pelo parser (${result.reason}) — esperado p/ segurança/recusada"
                    }
                }
            } finally {
                _busy.value = false
            }
        }
    }

    companion object {
        fun factory(simulator: NotificationSimulator): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NotificationSimulatorViewModel(simulator) as T
                }
            }
    }
}
