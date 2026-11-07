package com.victorcabral.gestorfinanceiro.notification.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BankParserRegistryTest {
    private val registry = BankParserRegistry()

    @Test
    fun `routes nubank package`() {
        val parsed = registry.parse(
            packageName = KnownBankPackages.NUBANK,
            title = "Compra",
            text = "R$ 20,00 em Cafe",
            bigText = null,
        )
        assertThat(parsed).isNotNull()
        assertThat(parsed!!.bankLabel).isEqualTo("Nubank")
    }

    @Test
    fun `unknown package returns null`() {
        val parsed = registry.parse(
            packageName = "com.example.otherbank",
            title = "Compra",
            text = "R$ 20,00",
            bigText = null,
        )
        assertThat(parsed).isNull()
    }
}
