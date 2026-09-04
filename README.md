# 🏋️‍♂️ FitSync — Elite Local-First Workout Tracker & AI Coach

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20(M3)-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Database-Room%20(Local--First)-4285F4.svg?style=flat&logo=sqlite)](https://developer.android.com/training/data-storage/room)
[![AI](https://img.shields.io/badge/AI-Google%20Gemini%20API-FF6F00.svg?style=flat&logo=googlegemini)](https://ai.google.dev/)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture%20%2B%20MVI%2FMVVM-009688.svg?style=flat)]()

**FitSync** is an offline-first, high-performance strength and hypertrophy training companion engineered with **Jetpack Compose**, **Room Database**, and **Google Gemini Generative AI**. Built for serious lifters, bodybuilders, and calisthenics athletes, FitSync pairs science-backed progressive overload algorithms with a bespoke **3D Holographic Anatomical Muscle Model**, an automated **Streak Freeze Vault**, and interactive **Rich Markdown AI Coaching**.

---

## 📸 App Showcase

| Home (Today's Hero & Split) | Calendar Heatmap & Day Logs | 3D Holographic Muscle Anatomy |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/e2999ac9-d264-4e45-aff6-5da4fbf7706b" width="260" alt="Home Screen" /> | <img src="https://github.com/user-attachments/assets/a6c6d027-3842-44bd-b84e-794823f4c24d" width="260" alt="Calendar Heatmap" /> | <img src="https://github.com/user-attachments/assets/830ade12-46e6-4232-8acf-cf1cca8284cb" width="260" alt="Anatomy Screen" /> |

| Active Workout Logger | Routine Builder & Reorder | AI Coach & Nutrition Vision |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/02cf6c53-e7b9-4f9e-8ffd-235a6ecfa4c6" width="260" alt="Active Workout Log" /> | <img src="https://github.com/user-attachments/assets/848015c6-1079-46c1-9a7c-8ce7eb2b6d02" width="260" alt="Routine Builder" /> | <img src="https://github.com/user-attachments/assets/419c3495-1fec-4ccb-852c-599bc76aecc7" width="260" alt="AI Coach Vision" /> |

---

## 🌟 Key Features

### 1. 🤖 AI Coach & Smart Nutrition Vision
* **Rich Markdown Streaming**: Formats coaching recommendations into high-contrast Matter headings, bullet lists, numbered routines, and highlight callouts.
* **Multimodal Meal Vision**: Snap or upload meal photos to instantly estimate calories, macros (Protein, Carbs, Fats), and micronutrient balance via **Google Gemini 1.5 Flash/Pro**.
* **Personalized Chat**: Context-aware recommendations for warmups, deload weeks, exercise alternatives, and periodization strategies.

### 2. 🩻 3D Holographic Anatomical Muscle Model
* **Interactive Front/Back Canvas Rendering**: Custom vector-rendered human silhouette displaying individual muscle groups (Chest, Lats, Quads, Traps, Delts, Hamstrings, Glutes, Calves, etc.).
* **Dynamic Recovery & Fatigue Heatmap**: Computes real-time muscle fatigue, optimal rest windows, and overall recovery percentage based on past workout sets and volume load.

### 3. 📈 Progressive Overload & 1RM Progression Engine
* **Epley Formula 1RM Calculator**: Evaluates estimated One-Rep Max (1RM) dynamically across all historical working sets.
* **Interactive Progression Curves**: Visual line charts tracking strength gains, session-by-session volume progression, and personal record milestones.
* **Plate Calculator**: Quick barbell plate breakdown assistant with metric (`kg`) and imperial (`lbs`) unit support.

### 4. 🏋️ Custom Routine Builder & Split Planner
* **Weekly Split Presets**: Pre-configured templates for *Push-Pull-Legs (PPL)*, *Upper-Lower*, *Arnold Split*, *Full Body*, and custom split schedules.
* **In-Screen Exercise Picker**: Fast search and category filter sheet to add exercises without leaving your routine blueprint.
* **Tactile Reordering**: Step indicators, drag handles, and spring-animated reordering controls.

### 5. 🧊 Automated Streak Freeze Vault & Protection
* **Zero Cheating Continuity**: Background auto-freeze logic that preserves momentum when rest or missed days occur (capped at 2 consecutive protected days).
* **Clear Continuity Visuals**: Minimalist vault bottom sheet displaying live momentum indicators and active freeze inventory.

### 6. 📅 Collapsible Production Calendar Heatmap
* **Week-by-Default Layout**: Clean 7-day strip by default that expands smoothly into a full 6-row month grid with zero height shifting.
* **Multi-State Visualization**: Frosted cyan badges for Frozen days (`🧊`), green volume gradients for Workout days (`🔥`), and amber indicators for Skipped days (`!`).
* **Instant Jump to Today**: Auto-appearing `Today ⏩` action button to quickly reset view when exploring past logs.

---

## 🛠️ Tech Stack & Architecture

```
FitSync App Architecture (Offline-First Clean Architecture)
 ├── Presentation Layer (Jetpack Compose, Material 3, Matter Typography, Spring Animations)
 ├── Domain Layer (Use Cases, Pure Mathematical Calculators, Immutable Models)
 └── Data Layer (Room SQLite DAOs, Preference DataStore, Google Generative AI SDK)
```

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Language** | Kotlin 2.0+ | Modern idiomatic Kotlin with Coroutines & Flow |
| **UI Framework** | Jetpack Compose + Material 3 | Declarative UI with custom Glassmorphism & Matter typography |
| **Local Database** | Room Database | 100% offline-first persistence with reactive `Flow` queries |
| **Generative AI** | Google Generative AI SDK | Gemini Vision & Pro for fitness guidance & meal macro analysis |
| **Calendar Engine** | Kizitonwose Calendar | High-performance Compose Calendar with stable 6-row grid |
| **Dependency Injection** | Dagger Hilt | Clean, testable constructor injection across ViewModels & Repositories |
| **Animation Physics** | Compose Spring Physics | Bouncy damping ratios (`DampingRatioLowBouncy`), smooth layout transitions |
| **Serialization** | Kotlinx Serialization | Type-safe JSON serialization |
| **Navigation** | Navigation Compose | Type-safe Compose navigation routes |

---

## 📂 Project Structure

```
com.example.fitsync/
├── data/
│   ├── local/            # Room Database, DAOs, Entities, PreferenceManager
│   ├── remote/           # GeminiService (AI Coach & Meal Analysis)
│   └── repository/       # Offline-first repositories (Workout, Routine, Chat)
├── di/                   # Hilt Dependency Injection Modules (Database, Network, Service)
├── domain/
│   ├── model/            # Immutable Domain Models (Workout, Routine, Muscle, Macros)
│   └── repository/       # Repository Interfaces
├── ui/
│   ├── components/       # Reusable UI widgets, charts, bottom sheets, 3D Silhouette
│   ├── screens/
│   │   ├── home/         # Today's Hero Card, Week Strip, Streak Vault
│   │   ├── analytics/    # Stats Overview, Overload Charts, Body Anatomy
│   │   ├── log/          # Active Workout Logger, Plate Calc, Add Exercise Sheet
│   │   ├── routines/     # Weekly Split Planner, Routine Builder
│   │   ├── chat/         # AI Coach Markdown Messenger
│   │   ├── exercises/    # 120+ Movement Library & Custom Creator
│   │   └── profile/      # Personal Bests, Avatar Customizer, Settings
│   └── theme/            # Material 3 Color Schemes, Matter Typography, Dimens
└── util/                 # Math calculators (1RM Epley), HapticUtils, DateUtils
```

---

## ⚡ Getting Started & Setup

### Prerequisites
- **Android Studio Ladybug (2024.2.1+)** or newer.
- **JDK 17** or **JDK 21**.
- Android SDK 34 / 35.

### 1. Clone the Repository
```bash
git clone https://github.com/NavneetSindhu/FitSync.git
cd FitSync
```

### 2. Configure Gemini API Key
To enable the AI Coach and Meal Vision features, obtain a free API key from [Google AI Studio](https://aistudio.google.com/):

Add your key to `local.properties` (or `gradle.properties`):
```properties
GEMINI_API_KEY=AIzaSyYourActualApiKeyHere
```
*(The root `build.gradle.kts` automatically loads and exposes this to `BuildConfig.GEMINI_API_KEY` at compile time).*

### 3. Build & Run
- Open the project in Android Studio.
- Sync Gradle project with files.
- Select your target device or emulator (Android 8.0+ / API 26+) and click **Run (Shift + F10)**.

---

## 📐 Design & Engineering Principles

1. **Zero Mock Data Policy**: All analytics, PR records, recovery scores, and volume distributions are computed dynamically from live Room database queries.
2. **Pure Package Separation**: Strict boundary between UI Composables (`*.components.*`) and pure business/math helpers (`*.util.*`).
3. **Smooth Physical State Transitions**: All screen state changes, sheet displays, and calendar expansions utilize spring physics (`Spring.DampingRatioLowBouncy`).
4. **Centralized Typography & Localization**: 100% of typography is anchored to the custom **Matter** font family and user-facing strings are strictly centralized in `res/values/strings.xml`.

---

## 👨‍💻 Author

**Navneet Sindhu**  
- **GitHub**: [@NavneetSindhu](https://github.com/NavneetSindhu)  
- **LinkedIn**: [Navneet Sindhu](https://www.linkedin.com/in/navneetsindhu/)  
- **Specialization**: Android Native Development (Jetpack Compose, Clean Architecture, Kotlin Coroutines, On-Device AI).

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
