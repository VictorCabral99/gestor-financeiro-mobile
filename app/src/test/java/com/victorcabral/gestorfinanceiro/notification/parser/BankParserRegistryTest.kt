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

    @Test
    fun `fallback manual review keeps raw text`() {
        val fallback = registry.fallbackManualReview(
            packageName = KnownBankPackages.SANTANDER,
            title = "Aviso",
            text = "Algo aconteceu com R$ 10,00",
            bigText = null,
        )
        assertThat(fallback).isNotNull()
        assertThat(fallback!!.needsManualReview).isTrue()
        assertThat(fallback.amountCents).isEqualTo(1000L)
        assertThat(fallback.rawText).contains("Algo aconteceu")
    }
}
