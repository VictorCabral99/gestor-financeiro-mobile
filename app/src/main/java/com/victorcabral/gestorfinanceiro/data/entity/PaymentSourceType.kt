package com.victorcabral.gestorfinanceiro.data.entity

/**
 * Conta = débito implícito; cartão = crédito implícito.
 * Selecionar a fonte já define a modalidade — não pedimos débito/crédito à parte.
 */
enum class PaymentSourceType {
    ACCOUNT,
    CREDIT_CARD,
}
