# Developer guide

## Prerequisites

- Android Studio (latest stable).
- JDK 17.
- Android SDK with API 34+.

## Setup

1. Clone the repo.
2. Set `local.properties` with your SDK path (Android Studio will do this automatically).
3. Add Firebase config:
   - Copy `app/google-services.example.json` to `app/google-services.json`.
   - Replace it with the real Firebase config for your project.

## Run

- Build a debug APK:
  ```bash
  ./gradlew :app:assembleDebug
  ```
- Run on a device or emulator from Android Studio.

## Testing

- Unit tests:
  ```bash
  ./gradlew test
  ```
- Instrumented tests:
  ```bash
  ./gradlew connectedAndroidTest
  ```

## Code rules

- UI uses `StateFlow` and `collectAsStateWithLifecycle()`.
- ViewModels expose state and actions, not one-off events.
- UI never touches Firebase/Room directly; use repositories.
- Prefer constructor injection and Hilt modules for wiring.
- **Standardized UI Patterns**: Always use `SectionHeader` and `GlassCard` for new content sections. Avoid ad-hoc styling and favor shared components in `ui/common/`.
- **Decoupled Mapping**: When adding cloud-synced models, create a dedicated `FirestoreMappers.kt` file instead of embedding transformation logic inside repositories.
