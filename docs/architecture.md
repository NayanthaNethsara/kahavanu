# Architecture

## Overview

Kahavanu is a single-activity Android app built with Kotlin and Jetpack Compose. The app follows MVVM and uses a layered structure with repositories mediating between UI and data sources.

## Layers

- UI layer: Compose screens and ViewModels in `ui/`.
- Domain layer: Models and repository interfaces in `domain/`.
- Data layer: Repository implementations, Room DAOs, and sync code in `data/`.
- DI layer: Hilt modules in `di/`.

## Data flow

- UI triggers actions -> ViewModel -> Repository -> data sources.
- UI observes state via `StateFlow` using `collectAsStateWithLifecycle()`.
- Repositories expose `Flow` and `suspend` APIs; UI never touches Firebase or Room directly.

## Offline and sync

- Room provides local persistence and immediate UI data.
- Firebase Auth + Firestore provides cloud sync.
- Sync is orchestrated through `SyncManager` and WorkManager.
- Conflict resolution prefers the Firebase version on timestamp basis.

## Navigation

- Navigation Compose with a single activity.
- Destinations are defined in `AppDestination` and wired in `MainTabsScreen`.
