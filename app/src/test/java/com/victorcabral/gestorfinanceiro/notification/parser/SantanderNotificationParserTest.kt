package com.victorcabral.gestorfinanceiro.notification.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SantanderNotificationParserTest {
    private val parser = SantanderNotificationParser()

    @Test
    fun `parses card purchase fixture`() {
        val result = parser.parse(
            title = "Compra aprovada",
            text = "Compra de R$ 120,55 no estabelecimento Mercado Central",
            bigText = null,
        )

        assertThat(result).isNotNull()
        assertThat(result!!.amountCents).isEqualTo(12055L)
        assertThat(result.origin).ignoringCase().contains("Mercado")
        assertThat(result.bankPackage).isEqualTo(KnownBankPackages.SANTANDER)
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
}
