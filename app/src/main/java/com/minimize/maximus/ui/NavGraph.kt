package com.minimize.maximus.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.minimize.maximus.ui.screens.analytics.BodyAnalyticsScreen
import com.minimize.maximus.ui.screens.analytics.StatsScreen
import com.minimize.maximus.ui.screens.analytics.StatsViewModel
import com.minimize.maximus.ui.screens.auth.AuthScreen
import com.minimize.maximus.ui.screens.chat.ChatScreen
import com.minimize.maximus.ui.screens.chat.ChatViewModel
import com.minimize.maximus.ui.screens.exercises.ExerciseLibraryScreen
import com.minimize.maximus.ui.screens.history.HistoryScreen
import com.minimize.maximus.ui.screens.home.HomeScreen
import com.minimize.maximus.ui.screens.log.DailyLogViewModel
import com.minimize.maximus.ui.screens.log.LoggingScreen
import com.minimize.maximus.ui.screens.onboarding.OnboardingScreen
import com.minimize.maximus.ui.screens.profile.ProfileScreen
import com.minimize.maximus.ui.screens.routines.RoutineBuilderScreen
import com.minimize.maximus.ui.screens.routines.RoutineBuilderViewModel
import com.minimize.maximus.ui.screens.routines.RoutinesScreen
import com.minimize.maximus.ui.screens.settings.SettingsScreen
import com.minimize.maximus.ui.screens.settings.SettingsViewModel
import kotlinx.serialization.Serializable

@Serializable object Onboarding
@Serializable object Home
@Serializable object Routines
@Serializable data class RoutineBuilder(val routineId: Long = 0L)
@Serializable object ExerciseLibrary
@Serializable object DailyLog
@Serializable object History
@Serializable object Stats
@Serializable object Profile
@Serializable object Settings
@Serializable object Chat
@Serializable object Auth
@Serializable object BodyAnalytics

@Composable
fun MaximusNavGraph(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    dailyLogViewModel: DailyLogViewModel = hiltViewModel()
) {
    val isFirstRun by settingsViewModel.isFirstRun.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (isFirstRun) Onboarding else Home,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                initialOffset = { it / 6 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 0.98f, animationSpec = tween(220, easing = FastOutSlowInEasing))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                targetOffset = { it / 6 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
            ) + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                    scaleOut(targetScale = 0.98f, animationSpec = tween(180, easing = FastOutSlowInEasing))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                initialOffset = { it / 6 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                    scaleIn(initialScale = 0.98f, animationSpec = tween(220, easing = FastOutSlowInEasing))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                targetOffset = { it / 6 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
            ) + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                    scaleOut(targetScale = 0.98f, animationSpec = tween(180, easing = FastOutSlowInEasing))
        }
    ) {
        composable<Onboarding> {
            OnboardingScreen(
                settingsViewModel = settingsViewModel,
                onOnboardingFinished = {
                    navController.navigate(Home) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Home> {
            HomeScreen(
                onSettingsClick = { navController.navigate(Settings) },
                onRoutinesClick = {
                    navController.navigate(Routines) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onStatsClick = {
                    navController.navigate(Stats) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onStartWorkout = { navController.navigate(DailyLog) },
                onResumeWorkout = { navController.navigate(DailyLog) },
                onAICoachClick = { navController.navigate(Chat) }
            )
        }

        composable<Routines> {
            RoutinesScreen(
                onCreateRoutineClick = { navController.navigate(RoutineBuilder(0L)) },
                onEditRoutineClick = { id -> navController.navigate(RoutineBuilder(id)) },
                onStartWorkout = { navController.navigate(DailyLog) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<RoutineBuilder> { backStackEntry ->
            val builderViewModel: RoutineBuilderViewModel = hiltViewModel()
            val selectedExerciseFlow = backStackEntry.savedStateHandle.getStateFlow<String?>("selected_exercise_name", null)
            val selectedExerciseName by selectedExerciseFlow.collectAsState()

            LaunchedEffect(selectedExerciseName) {
                if (!selectedExerciseName.isNullOrBlank()) {
                    builderViewModel.addExerciseByName(selectedExerciseName!!)
                    backStackEntry.savedStateHandle["selected_exercise_name"] = null
                }
            }

            RoutineBuilderScreen(
                viewModel = builderViewModel,
                onBackClick = { navController.popBackStack() },
                onOpenExerciseLibrary = { navController.navigate(ExerciseLibrary) },
                onSaved = { navController.popBackStack() }
            )
        }

        composable<ExerciseLibrary> {
            ExerciseLibraryScreen(
                onBackClick = { navController.popBackStack() },
                onExerciseSelected = { exerciseDef ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_exercise_name", exerciseDef.name)
                    navController.popBackStack()
                }
            )
        }

        composable<Stats> {
            val statsViewModel: StatsViewModel = hiltViewModel()
            StatsScreen(
                onBackClick = { navController.popBackStack() },
                onStartTodayWorkout = { navController.navigate(DailyLog) },
                viewModel = statsViewModel
            )
        }

        composable<DailyLog>(
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn()
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
                ) + fadeOut()
            }
        ) {
            val uiState by dailyLogViewModel.uiState.collectAsState()
            LoggingScreen(
                viewModel = dailyLogViewModel,
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onFinishWorkout = {
                    navController.navigate(Home) {
                        popUpTo(Home) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Auth> {
            AuthScreen(onAuthSuccess = {
                navController.navigate(Home) { popUpTo<Auth> { inclusive = true } }
            })
        }

        composable<History> {
            HistoryScreen(
                onBackClick = { navController.popBackStack() },
                onEditWorkout = { workout ->
                    dailyLogViewModel.loadWorkoutForEditing(workout)
                    navController.navigate(DailyLog)
                }
            )
        }

        composable<Profile> {
            ProfileScreen(
                onNavigateToSettings = { navController.navigate(Settings) }
            )
        }

        composable<Chat> {
            val chatViewModel: ChatViewModel = hiltViewModel()
            val userName by settingsViewModel.userName.collectAsState()
            ChatScreen(
                userName = userName,
                onBackClick = { navController.popBackStack() },
                onOpenLibrary = { navController.navigate(ExerciseLibrary) },
                viewModel = chatViewModel
            )
        }

        composable<Settings> {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = settingsViewModel
            )
        }

        composable<BodyAnalytics> {
            BodyAnalyticsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}