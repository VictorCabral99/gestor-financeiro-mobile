package com.victorcabral.gestorfinanceiro.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.victorcabral.gestorfinanceiro.GestorFinanceiroApp
import com.victorcabral.gestorfinanceiro.notification.parser.KnownBankPackages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * System notification listener filtered to Nubank and Santander packages.
 * Parsing/persistence is delegated to [NotificationPipeline].
 */
class BankNotificationListenerService : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var pipeline: NotificationPipeline
    private lateinit var notifier: SuggestionNotifier

    override fun onCreate() {
        super.onCreate()
        val app = application as GestorFinanceiroApp
        notifier = SuggestionNotifier(applicationContext)
        notifier.ensureChannel()
        pipeline = NotificationPipeline(
            repository = app.repository,
            onInserted = { suggestion -> notifier.notifyNewSuggestion(suggestion) },
        )
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val packageName = sbn.packageName ?: return
        if (packageName !in KnownBankPackages.all) return

        val extras = sbn.notification.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val bigText = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        scope.launch {
            val result = pipeline.handleNotification(
                packageName = packageName,
                title = title,
                text = text,
                bigText = bigText,
                postedAtEpochMs = sbn.postTime,
            )
            Log.d(TAG, "notification handled: $result")
        }
    }

    companion object {
        private const val TAG = "BankNotifListener"
    }
}
