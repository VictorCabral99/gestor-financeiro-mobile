package com.victorcabral.gestorfinanceiro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.victorcabral.gestorfinanceiro.notification.SuggestionNotifier
import com.victorcabral.gestorfinanceiro.ui.navigation.AppNavGraph
import com.victorcabral.gestorfinanceiro.ui.navigation.resolveStartDestination
import com.victorcabral.gestorfinanceiro.ui.theme.GestorFinanceiroTheme

class MainActivity : ComponentActivity() {
    private val requestPostNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* optional local alerts; suggestion still persists without grant */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeRequestPostNotifications()

        val openPending = intent?.getBooleanExtra(SuggestionNotifier.EXTRA_OPEN_PENDING, false) == true

        setContent {
            GestorFinanceiroTheme {
                AppNavGraph(
                    startDestination = resolveStartDestination(this),
                    openPendingOnLaunch = openPending,
                )
            }
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
}
