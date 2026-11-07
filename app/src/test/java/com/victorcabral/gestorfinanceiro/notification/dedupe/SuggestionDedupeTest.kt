package com.victorcabral.gestorfinanceiro.notification.dedupe

import com.google.common.truth.Truth.assertThat
import com.victorcabral.gestorfinanceiro.notification.parser.KnownBankPackages
import org.junit.Test

class SuggestionDedupeTest {
    @Test
    fun `same text and amount within window shares key`() {
        val t0 = 1_700_000_000_000L
        val t1 = t0 + 30_000L
        val a = SuggestionDedupe.key(KnownBankPackages.NUBANK, "Compra R$ 10,00 em X", t0, 1000L)
        val b = SuggestionDedupe.key(KnownBankPackages.NUBANK, "Compra R$ 10,00 em X", t1, 1000L)
        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `whitespace differences normalize away`() {
        val t = 1_700_000_000_000L
        val a = SuggestionDedupe.key(KnownBankPackages.NUBANK, "Compra   R$ 10,00", t, 1000L)
        val b = SuggestionDedupe.key(KnownBankPackages.NUBANK, "compra R$ 10,00", t, 1000L)
        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `far timestamps produce different keys`() {
        val t0 = 1_700_000_000_000L
        val t1 = t0 + SuggestionDedupe.DEFAULT_WINDOW_MS + 1
        val a = SuggestionDedupe.key(KnownBankPackages.NUBANK, "same", t0, null)
        val b = SuggestionDedupe.key(KnownBankPackages.NUBANK, "same", t1, null)
        assertThat(a).isNotEqualTo(b)
    }

    @Test
    fun `different amounts produce different keys`() {
        val t = 1_700_000_000_000L
        val a = SuggestionDedupe.key(KnownBankPackages.NUBANK, "same text", t, 1000L)
        val b = SuggestionDedupe.key(KnownBankPackages.NUBANK, "same text", t, 2000L)
        assertThat(a).isNotEqualTo(b)
    }
}
