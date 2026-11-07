package com.victorcabral.gestorfinanceiro.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object MoneyFormat {
    private val brl: NumberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).apply {
        currency = Currency.getInstance("BRL")
    }

    fun formatCents(amountCents: Long?): String {
        if (amountCents == null) return "—"
        return brl.format(amountCents / 100.0)
    }

    /**
     * Parses PT-BR currency-ish input into cents.
     * Accepts: "12,34", "R$ 12,34", "12.34", "1234".
     */
    fun parseToCents(raw: String): Long? {
        val cleaned = raw
            .trim()
            .replace("R$", "", ignoreCase = true)
            .replace("\\s".toRegex(), "")
        if (cleaned.isEmpty()) return null

        val normalized = when {
            cleaned.contains(',') && cleaned.contains('.') ->
                cleaned.replace(".", "").replace(',', '.')
            cleaned.contains(',') ->
                cleaned.replace(',', '.')
            else -> cleaned
        }

        val value = normalized.toDoubleOrNull() ?: return null
        return Math.round(value * 100.0)
    }
}
