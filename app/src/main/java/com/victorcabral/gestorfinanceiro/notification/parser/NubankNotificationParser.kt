package com.victorcabral.gestorfinanceiro.notification.parser

/**
 * Nubank notification parser with representative PT-BR purchase / Pix patterns.
 *
 * Still may miss uncommon copy — those fall through as low-confidence manual review
 * (raw text preserved). Clear non-spend alerts return null (ignored by pipeline).
 *
 * TODO formats still thin / device-dependent:
 * - Cartão virtual / Ultravioleta copy variations
 * - Notificações agrupadas (EXTRA_TEXT truncated)
 * - Transferências recebidas (crédito — fora do fluxo de despesa)
 */
class NubankNotificationParser : BankNotificationParser {
    override val packageName: String = KnownBankPackages.NUBANK
    override val bankLabel: String = "Nubank"

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
            "código de segurança",
            "codigo de seguranca",
            "acesso pelo app",
            "nova tentativa de acesso",
            "lembrete de fatura", // bill reminder — not a spend event itself
            "fatura fechou",
        )
        return ignoreSnippets.any { it in lower }
    }

    private fun extractOrigin(raw: String, lower: String): String? {
        // Pix enviado: "Pix de R$ X enviado para Nome" / "Você enviou um Pix de R$ X para Nome"
        pixRecipient(raw)?.let { return it }

        // Compra: "Compra de R$ X em Merchant" / "Você pagou R$ X em Merchant"
        val emPatterns = listOf(
            Regex("""(?i)(?:pagou|compra(?:\s+aprovada)?|compra no (?:débito|crédito)|débito|crédito).{0,40}?\bem\s+([A-Za-z0-9À-ÿ*&.'\- ]{2,50})"""),
            Regex("""(?i)\bem\s+([A-Za-z0-9À-ÿ*&.'\- ]{2,50})"""),
            Regex("""(?i)\bno\s+([A-Za-z0-9À-ÿ*&.'\- ]{2,50})"""),
            Regex("""(?i)\bna\s+([A-Za-z0-9À-ÿ*&.'\- ]{2,50})"""),
        )
        for (pattern in emPatterns) {
            pattern.findAll(raw).forEach { match ->
                val candidate = AmountParsing.cleanMerchant(match.groupValues[1]) ?: return@forEach
                if (candidate.lowercase() !in NON_MERCHANT) return candidate
            }
        }

        // Boleto: keep a generic origin when amount present
        if ("boleto" in lower) return "Boleto"

        return null
    }

    private fun pixRecipient(raw: String): String? {
        val patterns = listOf(
            Regex("""(?i)(?:enviado|enviou(?:\s+um)?\s+pix|pix(?:\s+de)?).{0,40}?\bpara\s+([A-Za-z0-9À-ÿ*&.'\- ]{2,50})"""),
            Regex("""(?i)\bpara\s+([A-Za-z0-9À-ÿ*&.'\- ]{2,50})"""),
        )
        for (pattern in patterns) {
            val match = pattern.find(raw) ?: continue
            val candidate = AmountParsing.cleanMerchant(match.groupValues[1]) ?: continue
            if (candidate.lowercase() !in NON_MERCHANT) return candidate
        }
        return null
    }

    companion object {
        private val NON_MERCHANT = setOf(
            "débito", "debito", "crédito", "credito", "pix", "app", "cartão", "cartao",
            "você", "voce", "nubank", "compra", "aprovada", "realizada",
        )
    }
}
