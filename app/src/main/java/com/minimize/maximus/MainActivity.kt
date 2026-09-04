package com.minimize.maximus

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.res.painterResource
import com.minimize.maximus.ui.*
import com.minimize.maximus.ui.components.MaximusToastHost
import com.minimize.maximus.ui.components.MaximusToastManager
import com.minimize.maximus.ui.components.LocalMaximusToast
import com.minimize.maximus.ui.screens.settings.SettingsViewModel
import com.minimize.maximus.ui.theme.MaximusIcons
import com.minimize.maximus.ui.theme.MaximusTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()

            val isMetric by settingsViewModel.isMetric.collectAsState()
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            val accentInt by settingsViewModel.accentColor.collectAsState()
            val navStyle by settingsViewModel.navStyle.collectAsState()
            val fontScale by settingsViewModel.fontScale.collectAsState()
            val compactCards by settingsViewModel.compactCards.collectAsState()
            val forceShimmer by settingsViewModel.forceShimmer.collectAsState()
            val rawAccent = Color(accentInt.toLong() and 0xFFFFFFFF)
            val accentColor = when {
                isDarkMode && rawAccent.luminance() < 0.2f -> Color.White
                !isDarkMode && rawAccent.luminance() > 0.85f -> Color(0xFF18181B)
                else -> rawAccent
            }
            val toastManager = remember { MaximusToastManager() }

            MaximusTheme(
                darkTheme = isDarkMode,
                accentColor = accentColor,
                fontScale = fontScale,
                compactCards = compactCards,
                forceShimmer = forceShimmer
            ) {
                CompositionLocalProvider(
                    LocalMaximusToast provides toastManager
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MainAppScaffold(
                            settingsViewModel = settingsViewModel,
                            currentNavStyle = navStyle,
                            currentAccent = accentColor,
                            isDarkTheme = isDarkMode
                        )
                        MaximusToastHost(toastManager = toastManager)
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    settingsViewModel: SettingsViewModel,
    currentNavStyle: String,
    currentAccent: Color,
    isDarkTheme: Boolean
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isHome = currentDestination?.hasRoute<Home>() == true
    val isRoutines = currentDestination?.hasRoute<Routines>() == true
    val isStats = currentDestination?.hasRoute<Stats>() == true
    val isHistory = currentDestination?.hasRoute<History>() == true
    val isProfile = currentDestination?.hasRoute<Profile>() == true
    val isChat = currentDestination?.hasRoute<Chat>() == true
    val showBottomBar = isHome || isRoutines || isStats || isHistory || isProfile

    val toastManager = LocalMaximusToast.current
    val headerShiftY by animateDpAsState(
        targetValue = if (toastManager.currentToast != null) 64.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "HeaderContentShiftY"
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                val routeName = when {
                    isRoutines -> "Routines"
                    isStats -> "Stats"
                    isHistory -> "History"
                    isProfile -> "Profile"
                    else -> "Home"
                }

                val onNavigate: (String) -> Unit = { target ->
                    if (target == "Home") {
                        if (isHome) {
                            settingsViewModel.requestHomeScrollToTop()
                        } else {
                            navController.navigate(Home) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    } else {
                        val destinationObject = when (target) {
                            "Routines" -> Routines
                            "Stats" -> Stats
                            "History" -> History
                            "Profile" -> Profile
                            "AI Coach" -> Chat
                            else -> Home
                        }
                        navController.navigate(destinationObject) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }

                if (currentNavStyle == "Classic Bar") {
                    ClassicMaximusNavBar(
                        currentDestination = routeName,
                        accentColor = currentAccent,
                        onNavigate = onNavigate
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingMaximusNavBar(
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
        val bottomPadding = if (currentNavStyle == "Classic Bar") innerPadding.calculateBottomPadding() else 0.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = headerShiftY, bottom = bottomPadding)
        ) {
            MaximusNavGraph(
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }
    }
}

// ── Navigation Tab Models ───────────────────────────────────────────────────
sealed class NavIconSource {
    data class Vector(val filled: androidx.compose.ui.graphics.vector.ImageVector, val outlined: androidx.compose.ui.graphics.vector.ImageVector) : NavIconSource()
    data class DrawableRes(val resId: Int) : NavIconSource()
}

data class NavTabItem(
    val route: String,
    val iconSource: NavIconSource
)

private val AppNavTabs = listOf(
    NavTabItem("Home", NavIconSource.Vector(MaximusIcons.Navigation.Home, MaximusIcons.Navigation.HomeOutlined)),
    NavTabItem("Routines", NavIconSource.Vector(MaximusIcons.Navigation.Workout, MaximusIcons.Navigation.WorkoutOutlined)),
    NavTabItem("Stats", NavIconSource.DrawableRes(MaximusIcons.Navigation.StatsDrawable)),
    NavTabItem("History", NavIconSource.Vector(MaximusIcons.Navigation.History, MaximusIcons.Navigation.HistoryOutlined)),
    NavTabItem("Profile", NavIconSource.Vector(MaximusIcons.Navigation.Profile, MaximusIcons.Navigation.ProfileOutlined))
)

// ── Classic Material 3 Bottom Bar ─────────────────────────────────────────
@Composable
fun ClassicMaximusNavBar(
    currentDestination: String?,
    accentColor: Color,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        AppNavTabs.forEach { tab ->
            val isSelected = currentDestination == tab.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(tab.route) },
                icon = {
                    when (val source = tab.iconSource) {
                        is NavIconSource.Vector -> {
                            Icon(
                                imageVector = if (isSelected) source.filled else source.outlined,
                                contentDescription = tab.route,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        is NavIconSource.DrawableRes -> {
                            Icon(
                                painter = painterResource(id = source.resId),
                                contentDescription = tab.route,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = accentColor,
                    indicatorColor = accentColor.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// ── Floating Glass Pill with UniSwap Spring Physics ────────────────────────
@Composable
fun FloatingMaximusNavBar(
    currentDestination: String?,
    accentColor: Color,
    isDarkTheme: Boolean,
    onNavigate: (String) -> Unit
) {
    val glassBackgroundColor = if (isDarkTheme) Color(0xFF1C1C1E).copy(alpha = 0.95f) else Color(0xFFF8F8FA).copy(alpha = 0.94f)
    val glassBorderColor = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    val selectedIndex = AppNavTabs.indexOfFirst { it.route == currentDestination }.coerceAtLeast(0)

    Box(
        modifier = Modifier
            .widthIn(max = 340.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .imePadding()
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = if (isDarkTheme) 0.5f else 0.12f),
                spotColor = Color.Black.copy(alpha = if (isDarkTheme) 0.4f else 0.18f)
            )
            .clip(CircleShape)
            .background(glassBackgroundColor)
            .border(width = 1.dp, color = glassBorderColor, shape = CircleShape)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = true, onClick = {})
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { renderEffect = BlurEffect(radiusX = 25f, radiusY = 25f, edgeTreatment = TileMode.Clamp) }
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth()
        ) {
            val tabWidth = maxWidth / AppNavTabs.size
            val indicatorOffset by androidx.compose.animation.core.animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "nav_sliding_indicator"
            )

            // Single Sliding Pill Indicator
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth)
                    .height(44.dp)
                    .padding(horizontal = 2.dp, vertical = 2.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f))
            )

            // Tab Icons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppNavTabs.forEachIndexed { index, tab ->
                    val isSelected = index == selectedIndex

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "nav_icon_scale_$index"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                onNavigate(tab.route)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when (val source = tab.iconSource) {
                            is NavIconSource.Vector -> {
                                Icon(
                                    imageVector = if (isSelected) source.filled else source.outlined,
                                    contentDescription = tab.route,
                                    tint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                    modifier = Modifier
                                        .size(22.dp)
                                        .graphicsLayer {
                                            scaleX = iconScale
                                            scaleY = iconScale
                                        }
                                )
                            }
                            is NavIconSource.DrawableRes -> {
                                Icon(
                                    painter = painterResource(id = source.resId),
                                    contentDescription = tab.route,
                                    tint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                    modifier = Modifier
                                        .size(22.dp)
                                        .graphicsLayer {
                                            scaleX = iconScale
                                            scaleY = iconScale
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}