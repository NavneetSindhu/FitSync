# 🏋️ FitSync: Modern Fitness Tracker

**FitSync** is a reactive, local-first Android application built to help athletes track their gym and calisthenics progress with seamless cloud synchronization. 

Developed as a core portfolio project for my **2027 Software Engineering placements**, FitSync demonstrates modern Android development patterns including **Jetpack Compose**, **Clean Architecture**, and **Dependency Injection**.

---


## 📸 App Showcase

| Home (Light) | Home (Dark) | Workout History |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/87daf160-1001-4613-bd17-e183af2b13ae" width="280"> | <img src="https://github.com/user-attachments/assets/8e258d28-dc05-46de-b0b7-cb525765328f" width="280"> | <img src="https://github.com/user-attachments/assets/807ff5cf-4d7f-4a77-bf5c-cbff350bfa17" width="280"> |
| **Add Exercise** | **Cloud Sync** | **Settings** |
| <img src="https://github.com/user-attachments/assets/8d9f8753-f26b-4a05-84a2-0d81a9a2d819" width="280"> |<img src="https://github.com/user-attachments/assets/3a00077a-0742-4f69-8571-4c8f74189982" width="280">  | <img src="https://github.com/user-attachments/assets/500ed033-ff51-4ae4-8999-f918767bbc8e" width="280"> |
---

## 🚀 Key Features

* **Local-First Architecture:** Full offline support using Room Database. Your data stays on your device even without internet.
* **Cloud Synchronization:** Real-time sync to the cloud via Ktor and JSONBin.io integration, featuring unique Hex-ID session management.
* **Material 3 UI:** A beautiful, responsive interface with dynamic theming and full **Dark Mode** support.
* **Workouts Management:** Track exercises, sets, reps, and weights with a high-performance reactive UI.
* **Coming Soon:** Integrated rest timers and advanced progress analytics.

---

## 🛠️ Technical Stack

| Category | Technology |
| :--- | :--- |
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Local Database** | Room Persistence Library |
| **Networking** | Ktor Client |
| **Dependency Injection** | Hilt (Dagger) |
| **Serialization** | Kotlinx Serialization |
| **Architecture** | MVVM (Model-View-ViewModel) + Repository Pattern |

---

## 🏗️ Architecture Overview

FitSync follows the **Clean Architecture** principles to ensure the code is scalable and testable:

1.  **UI Layer (Compose):** Uses State Hoisting and ViewModels to maintain a unidirectional data flow (UDF).
2.  **Domain/Repository Layer:** Handles the logic between local (Room) and remote (Ktor) data sources.
3.  **Data Layer:** * **Room:** Manages local SQLite storage.
    * **Ktor:** Handles RESTful API communication with asynchronous Coroutines.

---

## 💡 Engineering Highlights

* **Conflict Resolution:** Implemented a sync-state logic where local changes are flagged and pushed to the cloud only when a network connection is detected.
* **Dependency Management:** Leveraged **Hilt** to decouple the API and Database implementation from the UI, making the app modular and easy to extend.
* **AI-Assisted Development:** Developed using an iterative pair-programming approach with AI to architect complex cloud-sync logic and Material 3 design systems.

---

## 🛠️ Setup & Installation

1.  Clone the repository: `git clone https://github.com/NavneetSindhu/FitSync.git`
2.  Open the project in **Android Studio (Ladybug or newer)**.
3.  Add your JSONBin.io Master Key to the `ApiService` (or `local.properties`).
4.  Build and run on an Emulator or Physical Device.

---

## 👨‍💻 Author
**Navneet Sindhu** *Third-Year B.Tech Student (Graduating 2027)* *Passionate about Android Development, DSA, and Calisthenics.*
