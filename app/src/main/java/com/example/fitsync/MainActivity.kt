package com.example.fitsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitsync.ui.* import com.example.fitsync.ui.screens.settings.SettingsViewModel
import com.example.fitsync.ui.theme.DefaultAccent
import com.example.fitsync.ui.theme.FitSyncTheme
import com.example.fitsync.ui.theme.LocalAccentColor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val systemSplashScreen = installSplashScreen()
        systemSplashScreen.setKeepOnScreenCondition { false }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 1. Get the shared ViewModel here
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

            val accentColorInt by settingsViewModel.accentColor.collectAsState(initial = DefaultAccent.toArgb())
            val currentAccent = Color(accentColorInt)

            CompositionLocalProvider(LocalAccentColor provides currentAccent) {
                FitSyncTheme(darkTheme = isDarkMode, dynamicColor = false) {
                    FitSyncAppContainer(settingsViewModel)
                }
            }
        }
    }
}

@Composable
fun FitSyncAppContainer(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val shouldShowBottomBar = currentDestination?.let { dest ->
        !dest.hasRoute<Settings>() && !dest.hasRoute<Splash>()
    } ?: true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            AnimatedVisibility(
                visible = shouldShowBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Home") },
                        selected = currentDestination?.hasRoute<Home>() == true,
                        onClick = {
                            navController.navigate(Home) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = navItemColors()
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DateRange, null) },
                        label = { Text("History") },
                        selected = currentDestination?.hasRoute<History>() == true,
                        onClick = {
                            navController.navigate(History) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = navItemColors()
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CloudSync, null) },
                        label = { Text("Sync") },
                        selected = currentDestination?.hasRoute<Sync>() == true,
                        onClick = {
                            navController.navigate(Sync) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = navItemColors()
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            // 3. Pass it into your NavGraph
            FitSyncNavGraph(
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }
    }
}

@Composable
fun navItemColors() = NavigationBarItemDefaults.colors(
    // Grab the dynamic color from your custom theme engine
    selectedIconColor = LocalAccentColor.current,
    selectedTextColor = LocalAccentColor.current,
    unselectedIconColor = Color.Gray,
    unselectedTextColor = Color.Gray,
    indicatorColor = Color.Transparent
)