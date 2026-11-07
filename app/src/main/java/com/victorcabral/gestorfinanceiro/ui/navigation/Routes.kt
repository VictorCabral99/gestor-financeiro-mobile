package com.victorcabral.gestorfinanceiro.ui.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val MAIN = "main"
    const val PENDING = "pending"
    const val CATEGORIES = "categories"
    const val PAYMENT_SOURCES = "payment_sources"
    const val DEBUG_SIMULATOR = "debug_simulator"
    const val CONFIRM = "confirm?pendingId={pendingId}"

    fun confirm(pendingId: Long? = null): String {
        return if (pendingId == null) "confirm" else "confirm?pendingId=$pendingId"
    }
}

enum class MainTab {
    HOME,
    REPORTS,
    SETTINGS,
}
