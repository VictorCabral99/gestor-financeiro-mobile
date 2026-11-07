package com.victorcabral.gestorfinanceiro.notification.parser

/**
 * Stub/placeholder Santander parser.
 *
 * TODO: collect real Santander notification fixtures and refine origin extraction.
 */
class SantanderNotificationParser : BankNotificationParser {
    override val packageName: String = KnownBankPackages.SANTANDER
    override val bankLabel: String = "Santander"

    override fun parse(title: String?, text: String?, bigText: String?): ParsedBankNotification? {
        val raw = AmountParsing.combineText(title, text, bigText)
        if (raw.isBlank()) return null

        val lower = raw.lowercase()
        if (lower.contains("token") || lower.contains("senha")) {
            return null
        }

        val amount = AmountParsing.extractAmountCents(raw)
        val origin = extractOrigin(raw)

        return ParsedBankNotification(
            amountCents = amount,
            origin = origin,
            bankPackage = packageName,
            bankLabel = bankLabel,
            rawText = raw,
            confidence = if (amount != null) "medium" else "stub",
        )
    }

    private fun extractOrigin(raw: String): String? {
        val match = Regex(
            """(?i)(?:estabelecimento|local|em|no|na)[:\s]+([A-Za-z0-9À-ÿ .&\-]{2,40})""",
        ).find(raw)
        val candidate = match?.groupValues?.get(1)?.trim()?.trimEnd('.', ',', ';')
        if (!candidate.isNullOrEmpty()) return candidate
        return raw.lineSequence().firstOrNull()?.trim()?.takeIf { it.isNotEmpty() }
    }
}
