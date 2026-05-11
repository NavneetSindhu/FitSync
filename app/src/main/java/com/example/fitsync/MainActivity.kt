package com.example.fitsync

import android.os.Build
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint

import com.example.fitsync.ui.*
import com.example.fitsync.ui.screens.settings.SettingsViewModel
import com.example.fitsync.ui.theme.FitSyncTheme

// ── IMPORT GLOBALS FROM THEME ───────────────────────────────────────────────
import com.example.fitsync.ui.theme.LocalAccentColor
import com.example.fitsync.ui.theme.LocalCompactCards
import com.example.fitsync.ui.theme.LocalNavStyle

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val systemSplashScreen = installSplashScreen()
        systemSplashScreen.setKeepOnScreenCondition { false }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()

            // ── 1. COLLECT ALL GLOBAL SETTINGS ──────────────────────────────
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            val accentColorInt by settingsViewModel.accentColor.collectAsState()
            val fontScale by settingsViewModel.fontScale.collectAsState()
            val compactCards by settingsViewModel.compactCards.collectAsState()
            val navStyle by settingsViewModel.navStyle.collectAsState()

            val currentAccent = Color(accentColorInt.toLong() and 0xFFFFFFFF)

            // ── 2. PASS TO THEME (Theme now handles the CompositionLocals) ──
            FitSyncTheme(
                darkTheme = isDarkMode,
                dynamicColor = false,
                accentColor = currentAccent,
                fontScale = fontScale,
                compactCards = compactCards,
                navStyle = navStyle
            ) {
                FitSyncAppContainer(settingsViewModel)
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
        !dest.hasRoute<Settings>() && !dest.hasRoute<Splash>() && !dest.hasRoute<Chat>() && !dest.hasRoute<Auth>()
    } ?: true

    // Grab the current nav style from our global state
    val currentNavStyle = LocalNavStyle.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            AnimatedVisibility(
                visible = shouldShowBottomBar,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeOut()
            ) {
                val currentAccent = LocalAccentColor.current
                val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

                val routeName = when {
                    currentDestination?.hasRoute<Home>() == true -> "Home"
                    currentDestination?.hasRoute<Chat>() == true -> "Chat"
                    currentDestination?.hasRoute<History>() == true -> "History"
                    currentDestination?.hasRoute<Profile>() == true -> "Profile"
                    else -> null
                }

                val onNavigate: (String) -> Unit = { route ->
                    val target = when (route) {
                        "Home" -> Home
                        "Chat" -> Chat
                        "History" -> History
                        "Profile" -> Profile
                        else -> Home
                    }
                    navController.navigate(target) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                // ── 3. DYNAMIC NAVIGATION BAR ROUTER ────────────────────────
                if (currentNavStyle == "Classic Bar") {
                    ClassicFitSyncNavBar(
                        currentDestination = routeName,
                        accentColor = currentAccent,
                        onNavigate = onNavigate
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                        FloatingFitSyncNavBar(
                            currentDestination = routeName,
                            accentColor = currentAccent,
                            isDarkTheme = isDarkTheme,
                            onNavigate = onNavigate
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Dynamically adjust padding so standard nav bar doesn't cover content,
        // but floating pill lets content scroll behind it.
        val bottomPadding = if (currentNavStyle == "Classic Bar") innerPadding.calculateBottomPadding() else 0.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                    bottom = bottomPadding
                )
        ) {
            FitSyncNavGraph(
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }
    }
}

// ── NEW COMPONENT: Classic Material 3 Bottom Bar ──────────────────────────────
@Composable
fun ClassicFitSyncNavBar(
    currentDestination: String?,
    accentColor: Color,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        val tabs = listOf(
            Triple("Home", Icons.Filled.Home, Icons.Outlined.Home),
            Triple("Chat", Icons.Filled.ChatBubbleOutline, Icons.Outlined.ChatBubbleOutline),
            Triple("History", Icons.Filled.DateRange, Icons.Outlined.DateRange),
            Triple("Profile", Icons.Filled.Person, Icons.Outlined.Person)
        )

        tabs.forEach { (route, filledIcon, outlinedIcon) ->
            val isSelected = currentDestination == route

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) filledIcon else outlinedIcon,
                        contentDescription = route
                    )
                },
                label = { Text(route, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = accentColor,
                    selectedTextColor = accentColor,
                    indicatorColor = accentColor.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// ── EXISTING COMPONENT: Floating Glass Pill ───────────────────────────────────
@Composable
fun FloatingFitSyncNavBar(
    currentDestination: String?,
    accentColor: Color,
    isDarkTheme: Boolean,
    onNavigate: (String) -> Unit
) {
    val glassBackgroundColor = if (isDarkTheme) Color(0xFF1C1C1E).copy(alpha = 0.95f) else Color(0xFFF2F2F7).copy(alpha = 0.90f)
    val glassBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.90f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .imePadding()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color.Black.copy(alpha = if (isDarkTheme) 0.6f else 0.15f),
                spotColor = Color.Black.copy(alpha = if (isDarkTheme) 0.5f else 0.25f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(glassBackgroundColor)
            .border(width = 1.dp, color = glassBorderColor, shape = RoundedCornerShape(32.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = true, onClick = {})
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { renderEffect = BlurEffect(radiusX = 25f, radiusY = 25f, edgeTreatment = TileMode.Clamp) }
            )
        }

        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple("Home", Icons.Filled.Home, Icons.Outlined.Home),
                Triple("Chat", Icons.Filled.ChatBubbleOutline, Icons.Outlined.ChatBubbleOutline),
                Triple("History", Icons.Filled.DateRange, Icons.Outlined.DateRange),
                Triple("Profile", Icons.Filled.Person, Icons.Outlined.Person)
            )

            tabs.forEach { (route, filledIcon, outlinedIcon) ->
                val isSelected = currentDestination == route
                AnimatedNavItem(label = route, icon = if (isSelected) filledIcon else outlinedIcon, isSelected = isSelected, accentColor = accentColor, onClick = { onNavigate(route) })
            }
        }
    }
}

@Composable
fun AnimatedNavItem(label: String, icon: ImageVector, isSelected: Boolean, accentColor: Color, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent
    val contentColor = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = if (isSelected) 16.dp else 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(24.dp))
            AnimatedVisibility(visible = isSelected) {
                Text(text = label, color = contentColor, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}