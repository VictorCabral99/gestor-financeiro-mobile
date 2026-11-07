package com.victorcabral.gestorfinanceiro.notification.parser

/**
 * Result of parsing a bank push notification into a spend suggestion.
 * Incomplete parses still produce a result so the UI can “revisar manualmente”.
 */
data class ParsedBankNotification(
    val amountCents: Long?,
    val origin: String?,
    val bankPackage: String,
    val bankLabel: String,
    val rawText: String,
    /** "high" | "medium" | "low" — low means manual review (raw text kept). */
    val confidence: String,
    val needsManualReview: Boolean = confidence == "low",
)
