package com.example.fitsync.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.fitsync.ui.screens.auth.AuthScreen
import com.example.fitsync.ui.screens.chat.ChatScreen
import com.example.fitsync.ui.screens.chat.ChatViewModel
import com.example.fitsync.ui.screens.history.HistoryScreen
import com.example.fitsync.ui.screens.home.HomeScreen
import com.example.fitsync.ui.screens.log.LoggingScreen
import com.example.fitsync.ui.screens.settings.SettingsScreen
import com.example.fitsync.ui.screens.settings.SettingsViewModel
import com.example.fitsync.ui.screens.log.DailyLogViewModel // Added import
import com.example.fitsync.ui.screens.profile.ProfileScreen
import com.example.fitsync.ui.screens.splash.SplashScreen
import kotlinx.serialization.Serializable

@Serializable object Home
@Serializable object DailyLog
@Serializable object History
@Serializable object Sync

@Serializable object Profile
@Serializable object Settings

@Serializable object Chat
@Serializable object Splash

@Serializable object Auth

@Composable
fun FitSyncNavGraph(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Auth
    ) {

        composable<Splash> {
            SplashScreen(onAnimationFinished = {
                navController.navigate(Home) {
                    popUpTo(Splash) { inclusive = true }
                }
            })
        }
        composable<Home> {
            // Note: HomeScreen now contains LoggingScreen inside its Pager
            HomeScreen(
                onSettingsClick = { navController.navigate(Settings) },
                onHistoryClick = { navController.navigate(History) }
            )
        }

        // Keep this for full-screen logging if needed, or if triggered from another screen
        composable<DailyLog> {
            val dailyLogViewModel: DailyLogViewModel = hiltViewModel()
            val uiState by dailyLogViewModel.uiState.collectAsState()

            LoggingScreen(
                viewModel = dailyLogViewModel,
                uiState = uiState,
                onFinishWorkout = {
//                    navController.popBackStack()
                }
            )
        }

        composable<Auth> {
            AuthScreen(onAuthSuccess = {})
        }

        composable<History> {
            HistoryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Profile> {
            ProfileScreen()
        }

        composable<Chat> {
            val chatViewModel: ChatViewModel = hiltViewModel()

            ChatScreen(
                onBackClick = {navController.popBackStack()},
                viewModel = chatViewModel

            )
        }

        composable<Settings> {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = settingsViewModel
            )
        }
    }
}