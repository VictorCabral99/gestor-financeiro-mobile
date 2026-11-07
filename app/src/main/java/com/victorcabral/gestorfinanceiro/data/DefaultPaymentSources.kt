package com.victorcabral.gestorfinanceiro.data

import com.victorcabral.gestorfinanceiro.data.entity.PaymentSource
import com.victorcabral.gestorfinanceiro.data.entity.PaymentSourceType

object DefaultPaymentSources {
    val seed: List<PaymentSource> = listOf(
        PaymentSource(
            name = "Conta Nubank",
            type = PaymentSourceType.ACCOUNT,
            bankLabel = "Nubank",
            isDefault = true,
        ),
        PaymentSource(
            name = "Cartão Nubank",
            type = PaymentSourceType.CREDIT_CARD,
            bankLabel = "Nubank",
            isDefault = true,
        ),
        PaymentSource(
            name = "Conta Santander",
            type = PaymentSourceType.ACCOUNT,
            bankLabel = "Santander",
            isDefault = true,
        ),
        PaymentSource(
            name = "Cartão Santander",
            type = PaymentSourceType.CREDIT_CARD,
            bankLabel = "Santander",
            isDefault = true,
        ),
    )
}
