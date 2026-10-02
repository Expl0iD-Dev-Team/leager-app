package com.ledger.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ledger.app.LedgerApplication
import com.ledger.app.ui.screen.accounts.AccountsScreen
import com.ledger.app.ui.screen.add.AddTransactionScreen
import com.ledger.app.ui.screen.categories.CategoriesScreen
import com.ledger.app.ui.screen.detail.TransactionDetailScreen
import com.ledger.app.ui.screen.home.HomeScreen
import com.ledger.app.ui.screen.pin.PinScreen
import com.ledger.app.ui.screen.settings.SettingsScreen
import com.ledger.app.ui.screen.stats.StatsScreen
import com.ledger.app.ui.screen.transactions.TransactionsScreen

object Routes {
    const val PIN            = "pin"
    const val PIN_SETUP      = "pin_setup"
    const val HOME           = "home"
    const val TRANSACTIONS   = "transactions"
    const val ADD_TRANSACTION = "add_transaction?type={type}"
    const val ADD_TRANSACTION_BASE = "add_transaction"
    const val EDIT_TRANSACTION = "edit_transaction/{id}"
    const val TRANSACTION_DETAIL = "transaction_detail/{id}"
    const val ACCOUNTS       = "accounts"
    const val CATEGORIES     = "categories"
    const val STATS          = "stats"
    const val SETTINGS       = "settings"
}

@Composable
fun LedgerNavHost(
    navController: NavHostController,
    app: LedgerApplication,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.PIN) {
            PinScreen(
                app = app,
                onSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PIN_SETUP) {
            PinScreen(
                app = app,
                forceSetup = true,
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onAddClick = { type -> navController.navigate("${Routes.ADD_TRANSACTION_BASE}?type=${type.name}") },
                onTransactionClick = { id ->
                    navController.navigate("transaction_detail/$id")
                },
                onSeeAllClick = {
                    navController.navigate(Routes.TRANSACTIONS) {
                        launchSingleTop = true
                        popUpTo(Routes.HOME) { saveState = true }
                        restoreState = true
                    }
                },
                onAccountsClick = { navController.navigate(Routes.ACCOUNTS) },
                onCategoriesClick = { navController.navigate(Routes.CATEGORIES) }
            )
        }

        composable(Routes.TRANSACTIONS) {
            TransactionsScreen(
                onTransactionClick = { id ->
                    navController.navigate("transaction_detail/$id")
                }
            )
        }

        composable(
            route = Routes.ADD_TRANSACTION,
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = "EXPENSE"
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val typeArg = backStackEntry.arguments?.getString("type") ?: "EXPENSE"
            AddTransactionScreen(
                app = app,
                initialType = typeArg,
                transactionId = null,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EDIT_TRANSACTION,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            AddTransactionScreen(
                app = app,
                initialType = "EXPENSE",
                transactionId = id,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.TRANSACTION_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            TransactionDetailScreen(
                app = app,
                transactionId = id,
                onEdit = { navController.navigate("edit_transaction/$id") },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.ACCOUNTS) {
            AccountsScreen(
                app = app,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.CATEGORIES) {
            CategoriesScreen(
                app = app,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.STATS) {
            StatsScreen()
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                app = app,
                onSetPin = { navController.navigate(Routes.PIN_SETUP) },
                onAccountsClick = { navController.navigate(Routes.ACCOUNTS) },
                onCategoriesClick = { navController.navigate(Routes.CATEGORIES) }
            )
        }
    }
}
