package com.victorcabral.gestorfinanceiro.notification.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SantanderNotificationParserTest {
    private val parser = SantanderNotificationParser()

    @Test
    fun `parses card purchase with estabelecimento`() {
        val result = parser.parse(
            title = "Compra aprovada",
            text = "Compra de R$ 120,55 no estabelecimento Mercado Central",
            bigText = null,
        )
        assertThat(result).isNotNull()
        assertThat(result!!.amountCents).isEqualTo(12055L)
        assertThat(result.origin).ignoringCase().contains("Mercado")
        assertThat(result.confidence).isEqualTo("high")
    }

    @Test
    fun `parses debit style copy`() {
        val result = parser.parse(
            title = "Santander",
            text = "Débito de R$ 35,00 em Padaria Pao Quente",
            bigText = null,
        )
        assertThat(result!!.amountCents).isEqualTo(3500L)
        assertThat(result.origin).contains("Padaria")
    }

    @Test
    fun `parses pix enviado`() {
        val result = parser.parse(
            title = "Pix enviado",
            text = "Você enviou um Pix de R$ 80,00 para Maria Souza",
            bigText = null,
        )
        assertThat(result!!.amountCents).isEqualTo(8000L)
        assertThat(result.origin).contains("Maria")
    }

    @Test
    fun `skips token alerts`() {
        val result = parser.parse(
            title = "Santander",
            text = "Seu token é 998877",
            bigText = null,
        )
        assertThat(result).isNull()
    }

    @Test
    fun `skips declined purchase`() {
        val result = parser.parse(
            title = "Compra recusada",
            text = "Compra de R$ 10,00 em Loja X foi recusada",
            bigText = null,
        )
        assertThat(result).isNull()
    }

    @Test
    fun `manual review when unstructured`() {
        val result = parser.parse(
            title = "Aviso",
            text = "Movimentação na sua conta. Abra o app.",
            bigText = null,
        )
        assertThat(result).isNotNull()
        assertThat(result!!.needsManualReview).isTrue()
        assertThat(result.rawText).contains("Movimentação")
    }
}
