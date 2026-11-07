package com.victorcabral.gestorfinanceiro.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.victorcabral.gestorfinanceiro.MainActivity
import com.victorcabral.gestorfinanceiro.R
import com.victorcabral.gestorfinanceiro.data.entity.PendingSuggestion
import com.victorcabral.gestorfinanceiro.util.MoneyFormat

/**
 * Optional local notification when a new bank suggestion is persisted.
 */
class SuggestionNotifier(private val context: Context) {
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.suggestion_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.suggestion_channel_desc)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    fun notifyNewSuggestion(suggestion: PendingSuggestion) {
        ensureChannel()
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val amountLabel = MoneyFormat.formatCents(suggestion.amountCents)
        val origin = suggestion.origin ?: "revisar manualmente"
        val title = context.getString(R.string.suggestion_notif_title)
        val body = context.getString(
            R.string.suggestion_notif_body,
            suggestion.bankLabel,
            amountLabel,
            origin,
        )

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_PENDING, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            suggestion.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_BASE_ID + suggestion.id.toInt(),
                notification,
            )
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS missing on API 33+ — ignore; suggestion still in DB.
        }
    }

    companion object {
        const val CHANNEL_ID = "pending_suggestions"
        const val EXTRA_OPEN_PENDING = "open_pending"
        private const val NOTIFICATION_BASE_ID = 7000
    }
}
