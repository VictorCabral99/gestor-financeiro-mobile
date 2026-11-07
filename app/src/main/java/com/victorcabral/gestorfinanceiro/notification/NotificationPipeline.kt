package com.victorcabral.gestorfinanceiro.notification

import com.victorcabral.gestorfinanceiro.data.entity.PendingSuggestion
import com.victorcabral.gestorfinanceiro.data.repository.FinanceRepository
import com.victorcabral.gestorfinanceiro.notification.dedupe.SuggestionDedupe
import com.victorcabral.gestorfinanceiro.notification.parser.BankParserRegistry
import com.victorcabral.gestorfinanceiro.notification.parser.KnownBankPackages

/**
 * Pure-ish orchestration: filter packages → parse → dedupe → persist.
 * Unit-testable without Android Notification APIs.
 */
class NotificationPipeline(
    private val registry: BankParserRegistry = BankParserRegistry(),
    private val repository: FinanceRepository,
    private val clock: () -> Long = { System.currentTimeMillis() },
) {
    fun isSupportedPackage(packageName: String): Boolean =
        packageName in KnownBankPackages.all && registry.parserFor(packageName) != null

    suspend fun handleNotification(
        packageName: String,
        title: String?,
        text: String?,
        bigText: String?,
        postedAtEpochMs: Long? = null,
    ): HandleResult {
        if (!isSupportedPackage(packageName)) {
            return HandleResult.Ignored("unsupported_package")
        }

        val parsed = registry.parse(packageName, title, text, bigText)
            ?: return HandleResult.Ignored("parser_skipped")

        val receivedAt = postedAtEpochMs ?: clock()
        val dedupeKey = SuggestionDedupe.key(
            bankPackage = parsed.bankPackage,
            rawText = parsed.rawText,
            receivedAtEpochMs = receivedAt,
        )

        val suggestion = PendingSuggestion(
            amountCents = parsed.amountCents,
            origin = parsed.origin,
            rawText = parsed.rawText,
            bankPackage = parsed.bankPackage,
            bankLabel = parsed.bankLabel,
            dedupeKey = dedupeKey,
            receivedAtEpochMs = receivedAt,
            parseConfidence = parsed.confidence,
        )

        val rowId = repository.tryInsertPending(suggestion)
        return if (rowId == -1L) {
            HandleResult.Duplicate(dedupeKey)
        } else {
            HandleResult.Inserted(rowId, suggestion)
        }
    }

    sealed class HandleResult {
        data class Inserted(val id: Long, val suggestion: PendingSuggestion) : HandleResult()
        data class Duplicate(val dedupeKey: String) : HandleResult()
        data class Ignored(val reason: String) : HandleResult()
    }
}
