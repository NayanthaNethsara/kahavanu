---
description: "Project requirements: coroutines, state, MVVM, Material 3, Room offline caching."
applyTo: "app/src/main/java/**"
---

# Project Requirements

## Async and concurrency
- Use Kotlin coroutines for async work.
- All async work must run from `viewModelScope` (UI should only call ViewModel actions).
- Repositories should expose `suspend` functions or `Flow`/`StateFlow` and avoid blocking calls.

## State management
- Use `StateFlow` (preferred) or `LiveData` for UI state.
- Avoid `mutableStateOf` in UI for screen state; state must live in the ViewModel.
- UI collects state via `collectAsStateWithLifecycle()`.

## Architecture
- Strict one-way flow: UI -> ViewModel -> Repository -> Model.
- UI must not call data sources directly (network, Firebase, Room, DataStore).
- ViewModels depend on repositories, not on platform types (`Context`, `Activity`).

## UI
- Use Material Design 3 composables.
- No XML layout files for UI (Compose only).

## Navigation
- Use Navigation Compose with a single-activity architecture.
- Prefer typed route arguments when navigation requires parameters.

## Local caching
- Use Room for offline persistence where data is needed by UI.
- Repository should read from Room and expose cached data immediately.
- Network failures must not block UI; show cached data and update when available.
