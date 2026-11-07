package com.victorcabral.gestorfinanceiro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.victorcabral.gestorfinanceiro.BuildConfig
import com.victorcabral.gestorfinanceiro.GestorFinanceiroApp
import com.victorcabral.gestorfinanceiro.ui.categories.CategoriesScreen
import com.victorcabral.gestorfinanceiro.ui.categories.CategoriesViewModel
import com.victorcabral.gestorfinanceiro.ui.confirm.ConfirmExpenseScreen
import com.victorcabral.gestorfinanceiro.ui.confirm.ConfirmExpenseViewModel
import com.victorcabral.gestorfinanceiro.ui.debug.NotificationSimulatorScreen
import com.victorcabral.gestorfinanceiro.ui.debug.NotificationSimulatorViewModel
import com.victorcabral.gestorfinanceiro.ui.home.HomeScreen
import com.victorcabral.gestorfinanceiro.ui.home.HomeViewModel
import com.victorcabral.gestorfinanceiro.ui.onboarding.OnboardingScreen
import com.victorcabral.gestorfinanceiro.ui.pending.PendingScreen
import com.victorcabral.gestorfinanceiro.ui.pending.PendingViewModel
import com.victorcabral.gestorfinanceiro.util.NotificationAccess

@Composable
fun AppNavGraph(
    startDestination: String,
    openPendingOnLaunch: Boolean = false,
) {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as GestorFinanceiroApp
    val repository = remember { app.repository }

    LaunchedEffect(openPendingOnLaunch) {
        if (openPendingOnLaunch && startDestination == Routes.HOME) {
            navController.navigate(Routes.PENDING)
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onContinue = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.HOME) {
            val vm: HomeViewModel = viewModel(
                factory = HomeViewModel.factory(repository),
            )
            HomeScreen(
                viewModel = vm,
                onOpenPending = { navController.navigate(Routes.PENDING) },
                onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                onAddManual = { navController.navigate(Routes.confirm(null)) },
                onOpenOnboarding = { navController.navigate(Routes.ONBOARDING) },
                onOpenDebugSimulator = if (BuildConfig.DEBUG) {
                    { navController.navigate(Routes.DEBUG_SIMULATOR) }
                } else {
                    null
                },
            )
        }

        composable(Routes.PENDING) {
            val vm: PendingViewModel = viewModel(
                factory = PendingViewModel.factory(repository),
            )
            PendingScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onConfirm = { id -> navController.navigate(Routes.confirm(id)) },
            )
        }

        composable(Routes.CATEGORIES) {
            val vm: CategoriesViewModel = viewModel(
                factory = CategoriesViewModel.factory(repository),
            )
            CategoriesScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }

        if (BuildConfig.DEBUG) {
            composable(Routes.DEBUG_SIMULATOR) {
                val vm: NotificationSimulatorViewModel = viewModel(
                    factory = NotificationSimulatorViewModel.factory(app.notificationSimulator),
                )
                NotificationSimulatorScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onOpenPending = { navController.navigate(Routes.PENDING) },
                )
            }
        }

        composable(
            route = "confirm?pendingId={pendingId}",
            arguments = listOf(
                navArgument("pendingId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            val pendingId = entry.arguments?.getString("pendingId")?.toLongOrNull()
            val vm: ConfirmExpenseViewModel = viewModel(
                factory = ConfirmExpenseViewModel.factory(repository, pendingId),
            )
            ConfirmExpenseScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}

fun resolveStartDestination(context: android.content.Context): String {
    return if (NotificationAccess.isEnabled(context)) Routes.HOME else Routes.ONBOARDING
}
