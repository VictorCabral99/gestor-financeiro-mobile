package com.victorcabral.gestorfinanceiro.notification.sim

import com.victorcabral.gestorfinanceiro.notification.NotificationPipeline

/**
 * Debug helper: injects sample bank notification text into the **same**
 * [NotificationPipeline] used by [com.victorcabral.gestorfinanceiro.notification.BankNotificationListenerService].
 *
 * Why not NotificationManager with another app's package?
 * Android always attributes posted notifications to *this* app's package, so the
 * listener filter (`com.nu.production` / `com.santander.app`) would never see them.
 * Injection with the fixture's [SimulatedBankNotification.bankPackage] exercises
 * parse → dedupe → PendingSuggestion identically to a real bank push.
 *
 * Local “Nova sugestão” alerts come from the pipeline `onInserted` hook.
 */
class NotificationSimulator(
    private val pipeline: NotificationPipeline,
) {
    /**
     * Runs [sample] through [NotificationPipeline]. Local suggestion alerts are
     * fired by the pipeline's `onInserted` hook (same as the real listener).
     */
    suspend fun inject(sample: SimulatedBankNotification): NotificationPipeline.HandleResult {
        return pipeline.handleNotification(
            packageName = sample.bankPackage,
            title = sample.title,
            text = sample.text,
            bigText = sample.bigText,
            postedAtEpochMs = System.currentTimeMillis(),
        )
    }
}
