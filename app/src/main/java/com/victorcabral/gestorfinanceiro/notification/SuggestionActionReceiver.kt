package com.victorcabral.gestorfinanceiro.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

/**
 * Handles notification action taps that do not open an Activity
 * (e.g. “Agora não” — just dismiss the alert; suggestion stays pending).
 */
class SuggestionActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_DISMISS) return
        val pendingId = intent.getLongExtra(SuggestionNotifier.EXTRA_PENDING_ID, -1L)
        if (pendingId <= 0L) return
        NotificationManagerCompat.from(context).cancel(
            SuggestionNotifier.notificationIdFor(pendingId),
        )
    }

    companion object {
        const val ACTION_DISMISS = "com.victorcabral.gestorfinanceiro.action.DISMISS_SUGGESTION"
    }
}
