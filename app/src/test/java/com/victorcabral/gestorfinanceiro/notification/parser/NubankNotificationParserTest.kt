package com.victorcabral.gestorfinanceiro.notification.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NubankNotificationParserTest {
    private val parser = NubankNotificationParser()

    @Test
    fun `parses debit purchase with merchant`() {
        val result = parser.parse(
            title = "Compra no débito",
            text = "Você pagou R$ 42,90 em Padaria Sol",
            bigText = null,
        )
        assertThat(result).isNotNull()
        assertThat(result!!.amountCents).isEqualTo(4290L)
        assertThat(result.origin).contains("Padaria")
        assertThat(result.confidence).isEqualTo("high")
        assertThat(result.needsManualReview).isFalse()
    }

    @Test
    fun `parses credit purchase fixture`() {
        val result = parser.parse(
            title = "Compra no crédito",
            text = "Compra de R$ 189,00 em Posto Ipiranga",
            bigText = null,
        )
        assertThat(result!!.amountCents).isEqualTo(18900L)
        assertThat(result.origin).ignoringCase().contains("Ipiranga")
    }

    @Test
    fun `parses pix sent fixture`() {
        val result = parser.parse(
            title = "Pix enviado",
            text = "Pix de R$ 15,00 enviado para Joao Silva",
            bigText = null,
        )
        assertThat(result!!.amountCents).isEqualTo(1500L)
        assertThat(result.origin).contains("Joao")
    }

    @Test
    fun `parses boleto with generic origin`() {
        val result = parser.parse(
            title = "Boleto pago",
            text = "Você pagou um boleto de R$ 250,40",
            bigText = null,
        )
        assertThat(result!!.amountCents).isEqualTo(25040L)
        assertThat(result.origin).isEqualTo("Boleto")
    }

    @Test
    fun `skips security code alerts`() {
        val result = parser.parse(
            title = "Nubank",
            text = "Seu código de segurança é 123456",
            bigText = null,
        )
        assertThat(result).isNull()
    }

    @Test
    fun `low confidence when amount missing keeps raw text`() {
        val result = parser.parse(
            title = "Atualização de compra",
            text = "Confira detalhes da sua compra recente no app",
            bigText = null,
        )
        assertThat(result).isNotNull()
        assertThat(result!!.amountCents).isNull()
        assertThat(result.confidence).isEqualTo("low")
        assertThat(result.needsManualReview).isTrue()
        assertThat(result.rawText).contains("compra recente")
    }

    @Test
    fun `parses thousands separator amount`() {
        val result = parser.parse(
            title = "Compra no crédito",
            text = "Você pagou R$ 1.250,99 em Magazine Luiza",
            bigText = null,
        )
        assertThat(result!!.amountCents).isEqualTo(125099L)
        assertThat(result.origin).contains("Magazine")
    }
}
