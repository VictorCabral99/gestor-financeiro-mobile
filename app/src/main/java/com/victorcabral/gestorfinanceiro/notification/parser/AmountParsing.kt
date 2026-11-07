package com.victorcabral.gestorfinanceiro.notification.parser

/**
 * Shared helpers for extracting BRL amounts from notification text.
 * Kept package-visible for unit tests and bank-specific parsers.
 */
internal object AmountParsing {
    // Matches R$ 12,34 / R$12.345,67 / 12,34 (PT-BR) and occasional 12.34
    private val brlRegex = Regex(
        """R\$\s*(\d{1,3}(?:\.\d{3})*,\d{2}|\d+,\d{2})""" +
            """|(?<![A-Za-z0-9])(\d{1,3}(?:\.\d{3})*,\d{2}|\d+,\d{2})(?![A-Za-z0-9])""",
        RegexOption.IGNORE_CASE,
    )

    fun extractAmountCents(text: String): Long? {
        val match = brlRegex.find(text) ?: return null
        val raw = match.groupValues.drop(1).firstOrNull { it.isNotBlank() } ?: return null
        val normalized = raw.replace(".", "").replace(',', '.')
        val value = normalized.toDoubleOrNull() ?: return null
        return Math.round(value * 100.0)
    }

    fun combineText(title: String?, text: String?, bigText: String?): String {
        return listOfNotNull(title, text, bigText)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .joinToString("\n")
    }

    fun cleanMerchant(value: String): String? {
        val candidate = value
            .trim()
            .trimEnd('.', ',', ';', ':', '-', '—')
            .replace("\\s+".toRegex(), " ")
        if (candidate.length < 2) return null
        if (candidate.startsWith("R$", ignoreCase = true)) return null
        return candidate
    }
}
