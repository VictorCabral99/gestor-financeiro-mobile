package com.victorcabral.gestorfinanceiro.util

import com.google.common.truth.Truth.assertThat
import com.victorcabral.gestorfinanceiro.data.entity.PaymentSource
import com.victorcabral.gestorfinanceiro.data.entity.PaymentSourceType
import org.junit.Test

class PaymentSourceInferenceTest {
    private val sources = listOf(
        PaymentSource(id = 1, name = "Conta Nubank", type = PaymentSourceType.ACCOUNT, bankLabel = "Nubank"),
        PaymentSource(id = 2, name = "Cartão Nubank", type = PaymentSourceType.CREDIT_CARD, bankLabel = "Nubank"),
        PaymentSource(id = 3, name = "Conta Santander", type = PaymentSourceType.ACCOUNT, bankLabel = "Santander"),
        PaymentSource(id = 4, name = "Cartão Santander", type = PaymentSourceType.CREDIT_CARD, bankLabel = "Santander"),
    )

    @Test
    fun `infers account from debito notification`() {
        assertThat(
            PaymentSourceInference.inferType("Compra no débito Você pagou R$ 42,90 em Padaria Sol"),
        ).isEqualTo(PaymentSourceType.ACCOUNT)
    }

    @Test
    fun `infers card from credito notification`() {
        assertThat(
            PaymentSourceInference.inferType("Compra no crédito Compra de R$ 189,00 em Posto"),
        ).isEqualTo(PaymentSourceType.CREDIT_CARD)
    }

    @Test
    fun `infers account from pix`() {
        assertThat(
            PaymentSourceInference.inferType("Pix de R$ 15,00 enviado para Joao"),
        ).isEqualTo(PaymentSourceType.ACCOUNT)
    }

    @Test
    fun `matches nubank account for debito`() {
        val matched = PaymentSourceInference.match(
            sources = sources,
            bankLabel = "Nubank",
            rawText = "Compra no débito Você pagou R$ 42,90 em Padaria Sol",
        )
        assertThat(matched?.id).isEqualTo(1L)
    }

    @Test
    fun `matches nubank card for credito`() {
        val matched = PaymentSourceInference.match(
            sources = sources,
            bankLabel = "Nubank",
            rawText = "Compra no crédito Compra de R$ 189,00 em Posto Ipiranga",
        )
        assertThat(matched?.id).isEqualTo(2L)
    }

    @Test
    fun `matches santander card`() {
        val matched = PaymentSourceInference.match(
            sources = sources,
            bankLabel = "Santander",
            rawText = "Compra no crédito aprovada em Mercado",
        )
        assertThat(matched?.id).isEqualTo(4L)
    }
}
