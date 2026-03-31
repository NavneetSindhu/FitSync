package com.example.fitsync.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.fitsync.ui.screens.history.HistoryScreen
import com.example.fitsync.ui.screens.home.HomeScreen
import com.example.fitsync.ui.screens.log.DailyLogScreen
import com.example.fitsync.ui.screens.sync.SyncScreen
import com.example.fitsync.ui.screens.settings.SettingsScreen // Import your Settings Screen
import com.example.fitsync.ui.screens.settings.SettingsViewModel
import kotlinx.serialization.Serializable

@Serializable object Home
@Serializable object DailyLog
@Serializable object History
@Serializable object Sync
@Serializable object Settings // 1. Add the Serializable object

@Composable
fun FitSyncNavGraph(navController: NavHostController,settingsViewModel: SettingsViewModel) {
    NavHost(
        navController = navController,
        startDestination = Home
    ) {
        composable<Home> {
            HomeScreen(

                onHistoryClick = { navController.navigate(History) },
                onSyncClick = { navController.navigate(Sync) },
                // 2. Fix: Navigate using the Object, not a String
                onSettingsClick = { navController.navigate(Settings) }
            )
        }

        composable<DailyLog> {
            DailyLogScreen(
                onBackClick = { navController.popBackStack() },
                onFinishWorkout = { navController.popBackStack() }
            )
        }

        composable<History> {
            HistoryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Sync> {
            SyncScreen()
        }

        // 3. Add the Settings Destination
        composable<Settings> {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = settingsViewModel // Use the passed-in ViewModel
            )
        }
    }
}