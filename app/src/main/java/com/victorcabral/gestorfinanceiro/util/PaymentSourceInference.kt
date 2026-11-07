package com.victorcabral.gestorfinanceiro.util

import com.victorcabral.gestorfinanceiro.data.entity.PaymentSource
import com.victorcabral.gestorfinanceiro.data.entity.PaymentSourceType

/**
 * Conta/cartão normalmente vem na própria notificação bancária
 * (“compra no débito”, “compra no crédito”, Pix, etc.).
 */
object PaymentSourceInference {
    fun inferType(rawText: String?): PaymentSourceType? {
        if (rawText.isNullOrBlank()) return null
        val lower = rawText.lowercase()
        // Crédito / cartão first — “compra no crédito” is unambiguous.
        if (
            "crédito" in lower || "credito" in lower ||
            "cartão" in lower || "cartao" in lower ||
            "fatura" in lower
        ) {
            return PaymentSourceType.CREDIT_CARD
        }
        // Débito / Pix / boleto → conta.
        if (
            "débito" in lower || "debito" in lower ||
            "pix" in lower ||
            "boleto" in lower ||
            "conta" in lower
        ) {
            return PaymentSourceType.ACCOUNT
        }
        return null
    }

    fun modalityLabel(type: PaymentSourceType?): String? = when (type) {
        PaymentSourceType.ACCOUNT -> "débito / conta"
        PaymentSourceType.CREDIT_CARD -> "crédito / cartão"
        null -> null
    }

    /**
     * Prefers bankLabel + inferred type, then bankLabel alone, then type alone.
     */
    fun match(
        sources: List<PaymentSource>,
        bankLabel: String?,
        rawText: String?,
    ): PaymentSource? {
        if (sources.isEmpty()) return null
        val type = inferType(rawText)
        val bank = bankLabel?.trim()?.takeIf { it.isNotEmpty() }

        if (bank != null && type != null) {
            sources.firstOrNull {
                it.type == type && it.bankLabel.equals(bank, ignoreCase = true)
            }?.let { return it }
            sources.firstOrNull {
                it.type == type &&
                    (it.name.contains(bank, ignoreCase = true) ||
                        bank.contains(it.name, ignoreCase = true))
            }?.let { return it }
        }
        if (bank != null) {
            sources.firstOrNull { it.bankLabel.equals(bank, ignoreCase = true) }?.let { return it }
            sources.firstOrNull { it.name.contains(bank, ignoreCase = true) }?.let { return it }
        }
        if (type != null) {
            sources.firstOrNull { it.type == type }?.let { return it }
        }
        return null
    }

    fun sourceLabel(source: PaymentSource): String {
        val kind = when (source.type) {
            PaymentSourceType.ACCOUNT -> "conta"
            PaymentSourceType.CREDIT_CARD -> "cartão"
        }
        return "${source.name} ($kind)"
    }
}
