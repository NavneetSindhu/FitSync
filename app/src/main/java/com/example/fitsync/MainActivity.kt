package com.example.fitsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint

import com.example.fitsync.ui.* import com.example.fitsync.ui.screens.settings.SettingsViewModel
import com.example.fitsync.ui.theme.FitSyncTheme
import com.example.fitsync.ui.theme.LocalAccentColor

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val systemSplashScreen = installSplashScreen()
        systemSplashScreen.setKeepOnScreenCondition { false }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            val accentColorInt by settingsViewModel.accentColor.collectAsState()

            val currentAccent = Color(accentColorInt.toLong() and 0xFFFFFFFF)

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
        !dest.hasRoute<Settings>() && !dest.hasRoute<Splash>() && !dest.hasRoute<Chat>()
    } ?: true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            // --- NEW: Added a Box wrapper with navigationBarsPadding to create the safe bottom margin ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding() // Pushes it above the system gesture pill
            ) {
                AnimatedVisibility(
                    visible = shouldShowBottomBar,
                    // --- NEW: Smooth slide + fade animations ---
                    enter = slideInVertically(
                        initialOffsetY = { it }, // Slide up from bottom
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        targetOffsetY = { it }, // Slide down off screen
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeOut()
                ) {
                    val currentAccent = LocalAccentColor.current

                    val routeName = when {
                        currentDestination?.hasRoute<Home>() == true -> "Home"
                        currentDestination?.hasRoute<Chat>() == true -> "Chat"
                        currentDestination?.hasRoute<History>() == true -> "History"
                        currentDestination?.hasRoute<Sync>() == true -> "Sync"
                        else -> null
                    }

                    FloatingFitSyncNavBar(
                        currentDestination = routeName,
                        accentColor = currentAccent,
                        onNavigate = { route ->
                            val target = when(route) {
                                "Home" -> Home
                                "Chat" -> Chat
                                "History" -> History
                                "Sync" -> Sync
                                else -> Home
                            }
                            navController.navigate(target) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            FitSyncNavGraph(
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }
    }
}

// --- PREMIUM FLOATING NAVIGATION BAR COMPONENTS ---

@Composable
fun FloatingFitSyncNavBar(
    currentDestination: String?,
    accentColor: Color,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            // --- UPDATED: Padding changed to adjust margin from sides and bottom ---
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .imePadding(), // Ensure it moves up if keyboard happens to open on a screen where this is visible
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple("Home", Icons.Filled.Home, Icons.Outlined.Home),
                Triple("Chat", Icons.Filled.ChatBubbleOutline, Icons.Outlined.ChatBubbleOutline),
                Triple("History", Icons.Filled.DateRange, Icons.Outlined.DateRange),
                Triple("Sync", Icons.Filled.Sync, Icons.Outlined.Sync)
            )

            tabs.forEach { (route, filledIcon, outlinedIcon) ->
                val isSelected = currentDestination == route

                AnimatedNavItem(
                    label = route,
                    icon = if (isSelected) filledIcon else outlinedIcon,
                    isSelected = isSelected,
                    accentColor = accentColor,
                    onClick = { onNavigate(route) }
                )
            }
        }
    }
}

@Composable
fun AnimatedNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent
    val contentColor = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = if (isSelected) 16.dp else 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            AnimatedVisibility(visible = isSelected) {
                Text(
                    text = label,
                    color = contentColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}