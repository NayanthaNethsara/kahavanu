# Decisions

## Tech choices

- Kotlin + Coroutines: async work and Flow-based streams.
- Jetpack Compose + Material 3: UI toolkit.
- MVVM + Repository pattern: predictable UI state and data flow.
- Room: local persistence and offline access.
- Firebase Auth + Firestore: authentication and cloud data.
- WorkManager: background sync and retries.
- Hilt: dependency injection.
- KSP: annotation processing for Room and Hilt.

## Build choices

- Java 17 and Kotlin toolchain 17.
- Version catalog for dependency management.
- R8 enabled in release builds.
