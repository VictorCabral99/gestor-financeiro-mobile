package com.victorcabral.gestorfinanceiro.notification.parser

/**
 * Parses notification title/text for a specific bank [packageName].
 * Implementations must be pure/unit-testable (no Android framework dependencies).
 */
interface BankNotificationParser {
    /** Android package name this parser handles, e.g. [KnownBankPackages.NUBANK]. */
    val packageName: String

    val bankLabel: String

    /**
     * @return parsed suggestion, or null when the notification is irrelevant
     * (e.g. marketing, login alert).
     */
    fun parse(title: String?, text: String?, bigText: String?): ParsedBankNotification?
}
