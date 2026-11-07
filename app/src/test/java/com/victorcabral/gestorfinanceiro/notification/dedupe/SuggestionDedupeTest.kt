package com.victorcabral.gestorfinanceiro.notification.dedupe

import com.google.common.truth.Truth.assertThat
import com.victorcabral.gestorfinanceiro.notification.parser.KnownBankPackages
import org.junit.Test

class SuggestionDedupeTest {
    @Test
    fun `same text within window shares key`() {
        val t0 = 1_700_000_000_000L
        val t1 = t0 + 30_000L
        val a = SuggestionDedupe.key(KnownBankPackages.NUBANK, "Compra R$ 10,00 em X", t0)
        val b = SuggestionDedupe.key(KnownBankPackages.NUBANK, "Compra R$ 10,00 em X", t1)
        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `whitespace differences normalize away`() {
        val t = 1_700_000_000_000L
        val a = SuggestionDedupe.key(KnownBankPackages.NUBANK, "Compra   R$ 10,00", t)
        val b = SuggestionDedupe.key(KnownBankPackages.NUBANK, "compra R$ 10,00", t)
        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `far timestamps produce different keys`() {
        val t0 = 1_700_000_000_000L
        val t1 = t0 + SuggestionDedupe.DEFAULT_WINDOW_MS + 1
        val a = SuggestionDedupe.key(KnownBankPackages.NUBANK, "same", t0)
        val b = SuggestionDedupe.key(KnownBankPackages.NUBANK, "same", t1)
        assertThat(a).isNotEqualTo(b)
    }
}
