package com.victorcabral.gestorfinanceiro.notification

import com.victorcabral.gestorfinanceiro.data.entity.PendingSuggestion
import com.victorcabral.gestorfinanceiro.data.repository.FinanceRepository
import com.victorcabral.gestorfinanceiro.notification.dedupe.SuggestionDedupe
import com.victorcabral.gestorfinanceiro.notification.parser.BankParserRegistry
import com.victorcabral.gestorfinanceiro.notification.parser.KnownBankPackages

/**
 * Pure-ish orchestration: filter packages → parse → fallback → dedupe → persist.
 * Unit-testable without Android Notification APIs.
 */
class NotificationPipeline(
    private val registry: BankParserRegistry = BankParserRegistry(),
    private val insertPending: suspend (PendingSuggestion) -> Long,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val onInserted: (suspend (PendingSuggestion) -> Unit)? = null,
) {
    constructor(
        repository: FinanceRepository,
        registry: BankParserRegistry = BankParserRegistry(),
        clock: () -> Long = { System.currentTimeMillis() },
        onInserted: (suspend (PendingSuggestion) -> Unit)? = null,
    ) : this(
        registry = registry,
        insertPending = { repository.tryInsertPending(it) },
        clock = clock,
        onInserted = onInserted,
    )

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

        // null = ignore (security/token/declined). Incomplete parses still return
        // a ParsedBankNotification with confidence=low and rawText for manual review.
        val parsed = registry.parse(packageName, title, text, bigText)
            ?: return HandleResult.Ignored("parser_skipped")

        val receivedAt = postedAtEpochMs ?: clock()
        val dedupeKey = SuggestionDedupe.key(
            bankPackage = parsed.bankPackage,
            rawText = parsed.rawText,
            receivedAtEpochMs = receivedAt,
            amountCents = parsed.amountCents,
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
            needsManualReview = parsed.needsManualReview,
        )

        val rowId = insertPending(suggestion)
        return if (rowId == -1L) {
            HandleResult.Duplicate(dedupeKey)
        } else {
            val inserted = suggestion.copy(id = rowId)
            onInserted?.invoke(inserted)
            HandleResult.Inserted(rowId, inserted)
        }
    }

    sealed class HandleResult {
        data class Inserted(val id: Long, val suggestion: PendingSuggestion) : HandleResult()
        data class Duplicate(val dedupeKey: String) : HandleResult()
        data class Ignored(val reason: String) : HandleResult()
    }
}
