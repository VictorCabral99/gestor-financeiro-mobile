package com.victorcabral.gestorfinanceiro.notification.parser

/**
 * Shared helpers for extracting BRL amounts from notification text.
 * Kept package-visible for unit tests and bank-specific parsers.
 */
internal object AmountParsing {
    // Matches R$ 12,34 / R$12.345,67 / 12,34
    private val brlRegex = Regex(
        """(?:R\$\s*)?(\d{1,3}(?:\.\d{3})*,\d{2}|\d+,\d{2}|\d+(?:\.\d{2})?)""",
        RegexOption.IGNORE_CASE,
    )

    fun extractAmountCents(text: String): Long? {
        val match = brlRegex.find(text) ?: return null
        val raw = match.groupValues[1]
        val normalized = when {
            raw.contains(',') -> raw.replace(".", "").replace(',', '.')
            raw.contains('.') && raw.substringAfterLast('.').length == 2 -> raw
            else -> "$raw.00"
        }
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
}
