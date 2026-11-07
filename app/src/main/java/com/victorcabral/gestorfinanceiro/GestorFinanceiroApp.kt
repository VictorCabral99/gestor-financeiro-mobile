package com.victorcabral.gestorfinanceiro

import android.app.Application
import com.victorcabral.gestorfinanceiro.data.AppDatabase
import com.victorcabral.gestorfinanceiro.data.repository.FinanceRepository
import com.victorcabral.gestorfinanceiro.notification.NotificationPipeline
import com.victorcabral.gestorfinanceiro.notification.SuggestionNotifier
import com.victorcabral.gestorfinanceiro.notification.sim.NotificationSimulator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GestorFinanceiroApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: AppDatabase
        private set

    lateinit var repository: FinanceRepository
        private set

    lateinit var notificationPipeline: NotificationPipeline
        private set

    lateinit var notificationSimulator: NotificationSimulator
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        repository = FinanceRepository(
            expenseDao = database.expenseDao(),
            categoryDao = database.categoryDao(),
            pendingSuggestionDao = database.pendingSuggestionDao(),
            paymentSourceDao = database.paymentSourceDao(),
        )
        val notifier = SuggestionNotifier(this).also { it.ensureChannel() }
        notificationPipeline = NotificationPipeline(
            repository = repository,
            onInserted = { suggestion -> notifier.notifyNewSuggestion(suggestion) },
        )
        notificationSimulator = NotificationSimulator(notificationPipeline)
        applicationScope.launch {
            repository.ensureDefaultsSeeded()
        }
    }
}
