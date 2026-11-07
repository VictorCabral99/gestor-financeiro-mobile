package com.victorcabral.gestorfinanceiro.notification.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NubankNotificationParserTest {
    private val parser = NubankNotificationParser()

    @Test
    fun `parses purchase with amount and merchant`() {
        val result = parser.parse(
            title = "Compra no débito",
            text = "Você pagou R$ 42,90 em Padaria Sol",
            bigText = null,
        )

        assertThat(result).isNotNull()
        assertThat(result!!.amountCents).isEqualTo(4290L)
        assertThat(result.origin).contains("Padaria")
        assertThat(result.bankPackage).isEqualTo(KnownBankPackages.NUBANK)
    }

    @Test
    fun `parses pix-style fixture`() {
        val result = parser.parse(
            title = "Pix enviado",
            text = "Pix de R$ 15,00 para Joao Silva",
            bigText = null,
        )

        assertThat(result).isNotNull()
        assertThat(result!!.amountCents).isEqualTo(1500L)
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
    fun `returns stub confidence when amount missing`() {
        val result = parser.parse(
            title = "Atualização",
            text = "Confira novidades no app",
            bigText = null,
        )
        assertThat(result).isNotNull()
        assertThat(result!!.amountCents).isNull()
        assertThat(result.confidence).isEqualTo("stub")
    }
}
