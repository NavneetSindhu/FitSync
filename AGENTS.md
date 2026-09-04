# FitSync Developer & Agent Guidelines

## 1. Package Separation: Components vs. Utils
- **Components Package (`*.components.*`)**:
  - **Definition**: Strictly for UI Composables (`@Composable`), layout structures, cards, custom Canvas charts, and bottom sheets.
  - **Screen-Specific Components**: Composable components used by a single screen reside in that screen's package:
    - `com.example.fitsync.ui.screens.home.components.*`
    - `com.example.fitsync.ui.screens.history.components.*`
    - `com.example.fitsync.ui.screens.log.components.*`
    - `com.example.fitsync.ui.screens.profile.components.*`
  - **Shared UI Components**: Generic building blocks reused across multiple screens reside in:
    - `com.example.fitsync.ui.components.*` (e.g. `EmptyStateView`, `FitSyncLineGraph`, `LottieAnimationWrapper`, `ShimmerEffect`, `FitSyncCard`, `FitSyncFilterChip`).

- **Utils Package (`com.example.fitsync.util.*` or `*.util.*`)**:
  - **Definition**: Strictly for pure non-UI logic, calculations, date/time formatting, unit conversion, and algorithm helpers. **NO `@Composable` functions or UI layouts** in `util/`.
  - **Examples**: `DateUtils`, `WorkoutMathUtils` (1RM Epley calculator, streak algorithms), `UnitConverter` (`kgToLbs`), `HapticUtils`.

---

## 2. Animation & Smooth State Transitions
- **No Static/Abrupt Layout Jumps**: Every toggle, filter switch, screen navigation, or state transition (empty <-> populated, calendar date selection, unit switch) **MUST** be animated smoothly using `AnimatedContent`, `AnimatedVisibility`, `Crossfade`, or spring physics (`animate*AsState`).
- **Spring Physics & Micro-interactions**: Use `Spring.DampingRatioLowBouncy` / `Spring.DampingRatioMediumBouncy` with `Spring.StiffnessMediumLow`, icon scale bounce on selection, and tactile haptic feedback (`HapticFeedbackType.TextHandleMove`).

---

## 3. Mock Data & Real-Data Integrity
- **Zero Mock Data**: All statistics, streaks, PRs, volume progressions, workout history, and profile statistics **MUST** be computed dynamically from Room database queries and `StateFlow` streams.
- **Dynamic Achievements**: User achievements must be evaluated and unlocked reactively based on live database aggregates.

---

## 4. Centralized Strings & Localization
- **No Hardcoded String Literals**: All user-facing text, button labels, sheet titles, empty state descriptions, dialog messages, and accessibility content descriptions **MUST** be defined in `res/values/strings.xml` and referenced using `stringResource(R.string.*)`.

---

## 5. Jetpack Compose & Composable Hierarchy
- **Top-Level Composition Providers**: Provide global theme attributes at the root level around `FitSyncTheme`.
- **Bracket & Import Integrity**: Always verify imports and exact closing braces after modifying nested composables.

---

## 6. No Text Emojis in UI / Strings
- **Strictly No Emojis**: Never include text emojis in button labels, dropdown menus, toast titles/messages, dialog headers, banner taglines, or string resources.
- **Rely on Icons & Typography**: Use Material 3 vector icons (`ImageVector`), custom drawables, and clean typography exclusively for visual accents and feedback.

