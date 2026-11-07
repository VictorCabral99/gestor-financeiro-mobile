package com.victorcabral.gestorfinanceiro

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.victorcabral.gestorfinanceiro.notification.SuggestionNotifier
import com.victorcabral.gestorfinanceiro.ui.navigation.AppNavGraph
import com.victorcabral.gestorfinanceiro.ui.navigation.resolveStartDestination
import com.victorcabral.gestorfinanceiro.ui.theme.GestorFinanceiroTheme

class MainActivity : ComponentActivity() {
    private val requestPostNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* optional local alerts; suggestion still persists without grant */ }

    private var launchDeepLink by mutableStateOf<LaunchDeepLink?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeRequestPostNotifications()
        launchDeepLink = parseDeepLink(intent)

        setContent {
            GestorFinanceiroTheme {
                AppNavGraph(
                    startDestination = resolveStartDestination(this),
                    openPendingOnLaunch = launchDeepLink is LaunchDeepLink.PendingList,
                    openConfirmPendingId = (launchDeepLink as? LaunchDeepLink.Confirm)?.pendingId,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        launchDeepLink = parseDeepLink(intent)
    }

    private fun parseDeepLink(intent: Intent?): LaunchDeepLink? {
        if (intent == null) return null
        val pendingId = intent.getLongExtra(SuggestionNotifier.EXTRA_PENDING_ID, -1L)
        return when {
            intent.getBooleanExtra(SuggestionNotifier.EXTRA_OPEN_CONFIRM, false) && pendingId > 0L ->
                LaunchDeepLink.Confirm(pendingId)
            intent.getBooleanExtra(SuggestionNotifier.EXTRA_OPEN_PENDING, false) ->
                LaunchDeepLink.PendingList
            else -> null
        }
    }

    private fun maybeRequestPostNotifications() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestPostNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private sealed class LaunchDeepLink {
        data object PendingList : LaunchDeepLink()
        data class Confirm(val pendingId: Long) : LaunchDeepLink()
    }

    companion object {
        fun confirmIntent(context: Context, pendingId: Long): Intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(SuggestionNotifier.EXTRA_OPEN_CONFIRM, true)
                putExtra(SuggestionNotifier.EXTRA_PENDING_ID, pendingId)
            }
    }
}
