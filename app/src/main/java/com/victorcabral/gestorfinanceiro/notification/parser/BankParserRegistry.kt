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

    fun supportedPackages(): Set<String> = byPackage.keys

    fun parserFor(packageName: String): BankNotificationParser? = byPackage[packageName]

    fun parse(
        packageName: String,
        title: String?,
        text: String?,
        bigText: String?,
    ): ParsedBankNotification? {
        return parserFor(packageName)?.parse(title, text, bigText)
    }

    companion object {
        fun defaultParsers(): List<BankNotificationParser> = listOf(
            NubankNotificationParser(),
            SantanderNotificationParser(),
        )
    }
}
