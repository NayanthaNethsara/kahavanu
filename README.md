# Kahavanu

Kahavanu is a Kotlin-based Android finance app for tracking income, expenses, goals, and recurring cash flow. It is designed as an offline-first app with Room as the local source of truth and Firebase used for authentication and cloud synchronization.

The project leans on a clean MVVM architecture, Compose UI, and repository-based data access so the app stays responsive offline while syncing changes back to Firebase when connectivity returns.

## What the app focuses on

- Fast local-first data entry and browsing.
- Recurrent income tracking and scheduled cash flow handling.
- Firebase-backed sign-in and sync.
- A structured UI built with Material 3 and Compose.
- Clear separation between UI, domain, and data layers.

## Quick start

1. Copy `app/google-services.example.json` to `app/google-services.json`.
2. Open the project in Android Studio.
3. Build the app:
   ```bash
   ./gradlew :app:assembleDebug
   ```
4. Run the `app` configuration on an emulator or device.

## Tech stack

- Kotlin 2.2.10
- Jetpack Compose + Material 3
- Navigation Compose
- Room
- Firebase Auth
- Firebase Firestore
- Hilt
- WorkManager
- Coil

## Architecture summary

- UI layer: screens, shared components, and ViewModels in `app/src/main/java/com/kahavanu/ui`.
- Domain layer: models and repository contracts in `app/src/main/java/com/kahavanu/domain`.
- Data layer: repository implementations, Room entities, DAOs, and sync logic in `app/src/main/java/com/kahavanu/data`.
- DI layer: Hilt modules in `app/src/main/java/com/kahavanu/di`.

The app uses one-way data flow:

UI -> ViewModel -> Repository -> local database / Firebase -> state updates -> UI

## Documentation

- [Docs index](docs/README.md)
- [Quick start](docs/quickstart.md)
- [Architecture](docs/architecture.md)
- [Developer guide](docs/development.md)
- [Dependencies](docs/dependencies.md)

## Repository layout

- `app/`: Android application module.
- `docs/`: Project documentation and developer references.
- `gradle/`: version catalog and wrapper config.
- `build.gradle.kts`, `settings.gradle.kts`: Gradle project configuration.

## Working conventions

- Keep UI logic in Compose screens and ViewModels.
- Keep Firebase and Room calls inside repository implementations.
- Prefer coroutines and `Flow`/`StateFlow` for async state.
- Use Room for offline persistence and sync from repositories.
- Follow the existing feature-based package layout when adding new screens.

## Build notes

- Debug builds currently run with:
  ```bash
  ./gradlew :app:assembleDebug
  ```
- The project uses Java 17 and Kotlin toolchains aligned to 17.
- Release builds are minified with R8 enabled.

## Status

The app is actively evolving. Current focus areas are offline-first sync, recurring income management, and making the income flow easier to understand and maintain.
