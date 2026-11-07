package com.victorcabral.gestorfinanceiro.notification.dedupe

import java.security.MessageDigest

/**
 * Builds a stable dedupe key from package + normalized text + optional amount +
 * a coarse time window. Same purchase that fires multiple notifications within
 * [windowMs] collapses to one key.
 */
object SuggestionDedupe {
    const val DEFAULT_WINDOW_MS: Long = 120_000L // 2 minutes

    fun key(
        bankPackage: String,
        rawText: String,
        receivedAtEpochMs: Long,
        amountCents: Long? = null,
        windowMs: Long = DEFAULT_WINDOW_MS,
    ): String {
        val normalized = normalize(rawText)
        val window = receivedAtEpochMs / windowMs
        val amountPart = amountCents?.toString() ?: "_"
        val material = "$bankPackage|$normalized|$amountPart|$window"
        return sha256(material)
    }

    fun normalize(rawText: String): String {
        return rawText
            .lowercase()
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(value.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
