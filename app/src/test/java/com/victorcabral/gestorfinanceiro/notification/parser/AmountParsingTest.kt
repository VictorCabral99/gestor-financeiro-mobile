package com.victorcabral.gestorfinanceiro.notification.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AmountParsingTest {
    @Test
    fun `extracts brazilian currency with thousands separator`() {
        assertThat(AmountParsing.extractAmountCents("Total R$ 1.234,56 hoje"))
            .isEqualTo(123456L)
    }

    @Test
    fun `extracts plain decimal comma`() {
        assertThat(AmountParsing.extractAmountCents("Valor 9,99"))
            .isEqualTo(999L)
    }
}
