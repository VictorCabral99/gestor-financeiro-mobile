package com.victorcabral.gestorfinanceiro.notification.parser

/**
 * Santander notification parser with representative PT-BR purchase / Pix patterns.
 *
 * TODO formats still thin / device-dependent:
 * - Way / cartões adicionais
 * - SMS-style truncated push copy
 * - “Compra recusada” (should ignore — covered partially)
 */
class SantanderNotificationParser : BankNotificationParser {
    override val packageName: String = KnownBankPackages.SANTANDER
    override val bankLabel: String = "Santander"

    override fun parse(title: String?, text: String?, bigText: String?): ParsedBankNotification? {
        val raw = AmountParsing.combineText(title, text, bigText)
        if (raw.isBlank()) return null

        val lower = raw.lowercase()
        if (shouldIgnore(lower)) return null

        val amount = AmountParsing.extractAmountCents(raw)
        val origin = extractOrigin(raw, lower)

        val confidence = when {
            amount != null && !origin.isNullOrBlank() -> "high"
            amount != null -> "medium"
            else -> "low"
        }

        return ParsedBankNotification(
            amountCents = amount,
            origin = origin,
            bankPackage = packageName,
            bankLabel = bankLabel,
            rawText = raw,
            confidence = confidence,
            needsManualReview = confidence == "low",
        )
    }

    private fun shouldIgnore(lower: String): Boolean {
        val ignoreSnippets = listOf(
            "token",
            "senha",
            "código de acesso",
            "codigo de acesso",
            "compra recusada",
            "não autorizad",
            "nao autorizad",
        )
        return ignoreSnippets.any { it in lower }
    }

    private fun extractOrigin(raw: String, lower: String): String? {
        val patterns = listOf(
            Regex("""(?i)estabelecimento[:\s]+([A-Za-z0-9À-ÿ*&.'\- ]{2,50})"""),
            Regex("""(?i)local[:\s]+([A-Za-z0-9À-ÿ*&.'\- ]{2,50})"""),
            Regex("""(?i)(?:compra(?:\s+aprovada)?|débito|crédito|pagou).{0,40}?\b(?:em|no|na)\s+([A-Za-z0-9À-ÿ*&.'\- ]{2,50})"""),
            Regex("""(?i)(?:pix(?:\s+enviado)?|enviou(?:\s+um)?\s+pix|transferência).{0,40}?\bpara\s+([A-Za-z0-9À-ÿ*&.'\- ]{2,50})"""),
            Regex("""(?i)\bem\s+([A-Za-z0-9À-ÿ*&.'\- ]{2,50})"""),
            Regex("""(?i)\bpara\s+([A-Za-z0-9À-ÿ*&.'\- ]{2,50})"""),
        )
        for (pattern in patterns) {
            pattern.findAll(raw).forEach { match ->
                val candidate = AmountParsing.cleanMerchant(match.groupValues[1]) ?: return@forEach
                if (candidate.lowercase() !in NON_MERCHANT) return candidate
            }
        }
        if ("boleto" in lower) return "Boleto"
        return null
    }

    companion object {
        private val NON_MERCHANT = setOf(
            "débito", "debito", "crédito", "credito", "pix", "app", "cartão", "cartao",
            "santander", "compra", "aprovada", "realizada", "você", "voce",
        )
    }
}
