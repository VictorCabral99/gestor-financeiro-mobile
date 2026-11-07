package com.victorcabral.gestorfinanceiro.ui.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val PENDING = "pending"
    const val CATEGORIES = "categories"
    const val CONFIRM = "confirm?pendingId={pendingId}"

    fun confirm(pendingId: Long? = null): String {
        return if (pendingId == null) "confirm" else "confirm?pendingId=$pendingId"
    }
}
