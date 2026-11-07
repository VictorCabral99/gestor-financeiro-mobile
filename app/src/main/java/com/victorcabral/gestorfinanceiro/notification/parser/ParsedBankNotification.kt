package com.victorcabral.gestorfinanceiro.notification.parser

/**
 * Result of parsing a bank push notification into a spend suggestion.
 * Parsers are intentionally lenient in the MVP — missing fields go to the confirm screen.
 */
data class ParsedBankNotification(
    val amountCents: Long?,
    val origin: String?,
    val bankPackage: String,
    val bankLabel: String,
    val rawText: String,
    /** "high" | "medium" | "low" | "stub" — stub until real fixtures harden parsers. */
    val confidence: String,
)
