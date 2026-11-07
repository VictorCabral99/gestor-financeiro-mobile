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
import com.victorcabral.gestorfinanceiro.ui.prompt.SuggestionPromptActivity
import com.victorcabral.gestorfinanceiro.util.MoneyFormat

/**
 * Local alert when a bank suggestion is persisted — heads-up + full-screen
 * prompt so the user can register even with the app closed.
 */
class SuggestionNotifier(private val context: Context) {
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.suggestion_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.suggestion_channel_desc)
            enableVibration(true)
            setShowBadge(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
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

        val promptIntent = SuggestionPromptActivity.intent(context, suggestion.id)
        val promptPending = PendingIntent.getActivity(
            context,
            requestCode(suggestion.id, REQ_PROMPT),
            promptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val confirmIntent = MainActivity.confirmIntent(context, suggestion.id)
        val confirmPending = PendingIntent.getActivity(
            context,
            requestCode(suggestion.id, REQ_CONFIRM),
            confirmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val laterIntent = Intent(context, SuggestionActionReceiver::class.java).apply {
            action = SuggestionActionReceiver.ACTION_DISMISS
            putExtra(EXTRA_PENDING_ID, suggestion.id)
        }
        val laterPending = PendingIntent.getBroadcast(
            context,
            requestCode(suggestion.id, REQ_DISMISS),
            laterIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(promptPending)
            .setFullScreenIntent(promptPending, true)
            .addAction(
                0,
                context.getString(R.string.suggestion_action_register),
                confirmPending,
            )
            .addAction(
                0,
                context.getString(R.string.suggestion_action_later),
                laterPending,
            )
            .setAutoCancel(true)
            .setTimeoutAfter(5 * 60_000L)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                notificationIdFor(suggestion.id),
                notification,
            )
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS missing on API 33+ — suggestion still in DB.
            return
        }

        // Best-effort: also bring the prompt up immediately when the OS allows
        // background activity starts (FSI / OEM exceptions). Heads-up still works otherwise.
        try {
            context.startActivity(promptIntent)
        } catch (_: Exception) {
            // Blocked by BAL — user still has heads-up + fullScreenIntent.
        }
    }

    fun canUseFullScreenIntent(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
        val manager = context.getSystemService(NotificationManager::class.java) ?: return true
        return manager.canUseFullScreenIntent()
    }

    fun openFullScreenIntentSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
        val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    companion object {
        /** High-importance channel (new id so upgrades get heads-up). */
        const val CHANNEL_ID = "pending_suggestions_prompt"
        const val EXTRA_OPEN_PENDING = "open_pending"
        const val EXTRA_OPEN_CONFIRM = "open_confirm"
        const val EXTRA_PENDING_ID = "pending_id"

        private const val NOTIFICATION_BASE_ID = 7000
        private const val REQ_PROMPT = 1
        private const val REQ_CONFIRM = 2
        private const val REQ_DISMISS = 3

        fun notificationIdFor(pendingId: Long): Int =
            NOTIFICATION_BASE_ID + pendingId.toInt()

        private fun requestCode(pendingId: Long, kind: Int): Int =
            (pendingId.toInt() * 10) + kind
    }
}
