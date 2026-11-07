package com.victorcabral.gestorfinanceiro.notification.parser

/**
 * Resolves a [BankNotificationParser] by notification package name.
 * Easy to extend with additional banks after the MVP.
 */
class BankParserRegistry(
    parsers: List<BankNotificationParser> = defaultParsers(),
) {
    private val byPackage: Map<String, BankNotificationParser> =
        parsers.associateBy { it.packageName }

    fun supportedPackages(): Set<String> = KnownBankPackages.all

    fun parserFor(packageName: String): BankNotificationParser? {
        val canonical = KnownBankPackages.canonical(packageName)
        return byPackage[canonical]
    }

    fun parse(
        packageName: String,
        title: String?,
        text: String?,
        bigText: String?,
    ): ParsedBankNotification? {
        val parser = parserFor(packageName) ?: return null
        return parser.parse(title, text, bigText)?.let { parsed ->
            // Keep bankPackage as the canonical package used by the parser.
            if (parsed.bankPackage == parser.packageName) parsed
            else parsed.copy(bankPackage = parser.packageName)
        }
    }

    /**
     * Fallback when the bank parser returns null but the push still looks spend-like.
     * Stores raw text for manual review instead of dropping the notification.
     */
    fun fallbackManualReview(
        packageName: String,
        title: String?,
        text: String?,
        bigText: String?,
    ): ParsedBankNotification? {
        val parser = parserFor(packageName) ?: return null
        val raw = AmountParsing.combineText(title, text, bigText)
        if (raw.isBlank()) return null
        return ParsedBankNotification(
            amountCents = AmountParsing.extractAmountCents(raw),
            origin = null,
            bankPackage = parser.packageName,
            bankLabel = parser.bankLabel,
            rawText = raw,
            confidence = "low",
            needsManualReview = true,
        )
    }

    companion object {
        fun defaultParsers(): List<BankNotificationParser> = listOf(
            NubankNotificationParser(),
            SantanderNotificationParser(),
        )
    }
}
