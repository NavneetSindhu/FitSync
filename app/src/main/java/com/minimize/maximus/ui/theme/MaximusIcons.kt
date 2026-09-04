package com.minimize.maximus.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import com.minimize.maximus.R

/**
 * Maximus Central Icon Registry
 * Consolidated single source of truth for all Material Icons and Custom VectorDrawables across the app.
 */
object MaximusIcons {

    // ── 1. GLOBAL & BOTTOM NAVIGATION BAR ──────────────────────────────────────
    object Navigation {
        val Home = Icons.Default.Home
        val HomeOutlined = Icons.Outlined.Home

        // Custom VectorDrawable for Stats tab (from user vector asset)
        val StatsDrawable = R.drawable.ic_nav_stats

        val Workout = Icons.Default.FitnessCenter
        val WorkoutOutlined = Icons.Outlined.FitnessCenter

        val History = Icons.AutoMirrored.Filled.ListAlt
        val HistoryOutlined = Icons.AutoMirrored.Outlined.ListAlt

        val Profile = Icons.Default.Person
        val ProfileOutlined = Icons.Outlined.Person

        val BackArrow = Icons.AutoMirrored.Filled.ArrowBack
        val ForwardArrow = Icons.AutoMirrored.Filled.ArrowForwardIos
        val Close = Icons.Default.Close
        val Check = Icons.Default.Check
        val Search = Icons.Default.Search
    }

    // ── 2. HOME SCREEN & AI COACH ──────────────────────────────────────────────
    object Home {
        // Custom VectorDrawable for AI Bot/Coach Sparkle
        val AiSparkleDrawable = R.drawable.ic_ai_sparkle

        val StreakFlame = Icons.Default.LocalFireDepartment
        val Settings = Icons.Default.Settings
        val StartWorkout = Icons.Default.PlayArrow
        val EmptyWorkout = Icons.Default.AddCircleOutline
        val Routines = Icons.AutoMirrored.Filled.ListAlt
        val SkippedAlert = Icons.Default.Info
        val RestDaySpa = Icons.Default.Spa

        // Calendar Strip
        val CalendarFreeze = Icons.Default.AcUnit
        val CalendarCompleted = Icons.Default.Check
        val CalendarRestDay = Icons.Default.Spa
        val CalendarSkipped = Icons.Default.PriorityHigh
        val ChevronRight = Icons.Default.ChevronRight
        val EditSchedule = Icons.Default.Tune
        val ExpandUp = Icons.Default.KeyboardArrowUp
        val ExpandDown = Icons.Default.KeyboardArrowDown
        val Close = Icons.Default.Close
    }

    // ── 3. WORKOUT LOGGER & REST TIMER ─────────────────────────────────────────
    object WorkoutLog {
        val Timer = Icons.Default.Timer
        val Play = Icons.Default.PlayArrow
        val Pause = Icons.Default.Pause
        val CloseDiscard = Icons.Default.Close
        val FinishCheck = Icons.Default.Check
        val AddExercise = Icons.Default.Add
        val AddCircle = Icons.Default.AddCircleOutline
        val ReorderUp = Icons.Default.KeyboardArrowUp
        val ReorderDown = Icons.Default.KeyboardArrowDown
        val PlateCalculator = Icons.Default.Calculate
        val EditMode = Icons.Default.Edit
        val Delete = Icons.Default.Delete
        val CheckCompleted = Icons.Default.CheckCircle
        val Unchecked = Icons.Default.RadioButtonUnchecked
    }

    // ── 4. ANALYTICS & STATS SCREEN ────────────────────────────────────────────
    object Stats {
        val TrendGraph = Icons.AutoMirrored.Filled.ShowChart
        val DropdownChevron = Icons.Default.KeyboardArrowDown
        val CalendarDate = Icons.Default.DateRange
        val MuscleBody = Icons.Default.Accessibility
        val Filter = Icons.Default.FilterList
        val Timeline = Icons.Default.Timeline
    }

    // ── 5. HISTORY SCREEN ──────────────────────────────────────────────────────
    object History {
        val TrophyMilestone = Icons.Default.EmojiEvents
        val Calendar = Icons.Default.CalendarMonth
        val Share = Icons.Default.Share
        val CloudBackup = Icons.Default.CloudSync
        val Delete = Icons.Default.DeleteOutline
        val ExerciseList = Icons.AutoMirrored.Filled.FormatListBulleted
    }

    // ── 6. ROUTINES & SPLIT PLANNER ────────────────────────────────────────────
    object Routines {
        val CreateNew = Icons.Default.Add
        val Duplicate = Icons.Default.ContentCopy
        val Edit = Icons.Default.Edit
        val Delete = Icons.Default.DeleteOutline
        val CalendarAssign = Icons.AutoMirrored.Filled.EventNote
        val PresetSplit = Icons.Default.DashboardCustomize
        val LibraryBooks = Icons.AutoMirrored.Filled.LibraryBooks
        val RestDaySpa = Icons.Default.Spa
    }

    // ── 7. PROFILE & SETTINGS ──────────────────────────────────────────────────
    object Profile {
        val WorkoutsCount = Icons.Default.FitnessCenter
        val WorkingSetsCount = Icons.Default.Layers
        val Streak = Icons.Default.LocalFireDepartment
        val AvgSession = Icons.Default.Timer
        val Achievements = Icons.Default.MilitaryTech
        val Theme = Icons.Default.Palette
        val Units = Icons.Default.Straighten
        val Backup = Icons.Default.Backup
        val Logout = Icons.AutoMirrored.Filled.Logout
        val EditAvatar = Icons.Default.Edit
        val TrendingUp = Icons.AutoMirrored.Filled.TrendingUp
        val RateUs = Icons.Default.Star
        val OtherApps = Icons.Default.Apps
    }
}
