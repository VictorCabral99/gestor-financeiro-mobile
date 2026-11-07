package com.victorcabral.gestorfinanceiro.notification.parser

/**
 * Stub/placeholder Nubank parser.
 *
 * TODO: harden against real device fixtures (purchase, Pix, boleto, card).
 * Current heuristic: extract first BRL amount; treat remainder after "em"/"no" as origin.
 */
class NubankNotificationParser : BankNotificationParser {
    override val packageName: String = KnownBankPackages.NUBANK
    override val bankLabel: String = "Nubank"

    override fun parse(title: String?, text: String?, bigText: String?): ParsedBankNotification? {
        val raw = AmountParsing.combineText(title, text, bigText)
        if (raw.isBlank()) return null

        // Ignore obvious non-spend alerts early (expand with fixtures).
        val lower = raw.lowercase()
        if (lower.contains("código de segurança") || lower.contains("acesso pelo app")) {
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
        // Prefer merchant after "em" (common in Nubank purchase copy).
        val preferred = listOf(
            Regex("""(?i)\bem\s+([A-Za-z0-9À-ÿ .&\-]{2,40})"""),
            Regex("""(?i)\bpara\s+([A-Za-z0-9À-ÿ .&\-]{2,40})"""),
            Regex("""(?i)(?:no|na)\s+([A-Za-z0-9À-ÿ .&\-]{2,40})"""),
        )
        for (pattern in preferred) {
            pattern.findAll(raw).forEach { match ->
                val candidate = cleanCandidate(match.groupValues[1]) ?: return@forEach
                if (candidate !in NON_MERCHANT_TOKENS) return candidate
            }
        }
        return titleAsOrigin(raw)
    }

    private fun cleanCandidate(value: String): String? {
        val candidate = value.trim().trimEnd('.', ',', ';')
        if (candidate.isEmpty() || candidate.startsWith("R$", ignoreCase = true)) return null
        return candidate
    }

    private fun titleAsOrigin(raw: String): String? {
        val firstLine = raw.lineSequence().firstOrNull()?.trim().orEmpty()
        return firstLine.takeIf { it.isNotEmpty() && !it.contains("R$", ignoreCase = true) }
    }

    companion object {
        private val NON_MERCHANT_TOKENS = setOf(
            "débito", "credito", "crédito", "débito.", "pix", "app", "cartão", "cartao",
        )
    }
}
