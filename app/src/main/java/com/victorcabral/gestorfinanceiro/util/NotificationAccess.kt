package com.victorcabral.gestorfinanceiro.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import com.victorcabral.gestorfinanceiro.notification.BankNotificationListenerService

object NotificationAccess {
    fun isEnabled(context: Context): Boolean {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return false
        if (TextUtils.isEmpty(flat)) return false

        val expected = ComponentName(context, BankNotificationListenerService::class.java)
        return flat.split(':').any { segment ->
            val cn = ComponentName.unflattenFromString(segment)
            cn != null && cn.packageName == expected.packageName && cn.className == expected.className
        }
    }

    fun openSettingsIntent(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
}
