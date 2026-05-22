# File structure

```
app/src/main/java/com/kahavanu/
  core/
    config/           App configuration constants
  data/
    auth/             Firebase auth repository
    common/           Shared data utilities
    expenses/         Expense repositories, mappers, sync, local DAOs
    goals/            Goal repositories, mappers, sync, local DAOs
    income/           Income repositories, mappers, sync, local DAOs
    local/            AppDatabase (Room)
    settings/         Settings repositories and local storage
    sieve/            SMS scanning and suggestion engine
    sync/             Base sync abstractions
  di/                 Hilt modules (Auth, Expenses, Goals, Income, Settings, Sync, Sms, SmsSender, SmsSuggestion, SmsEngine)
  domain/
    model/            Domain models (e.g. GoalEntry, GoalAdjustmentLog, IncomeLogEntry, SmsSuggestion)
    repository/       Repository interfaces (Auth, Expenses, Goals, Income, Settings, SmsScan, SmsSender, SmsSuggestion)
  sieve/
    engine/           Rule-based SMS classifier
  ui/
    auth/             Auth screens and ViewModel
    common/           Shared UI components (composables)
    expenses/         Expense screens, ViewModels, feature components
    goals/            Goal screens, ViewModels, feature components
    home/             Home screen, ViewModel, feature components
    income/           Income screens, ViewModels, feature components
    main/             Main tabs shell
    navigation/       AppDestination and nav graph
    onboarding/       Onboarding flow
    profile/          Profile screen
    sieve/            SMS sender settings screen
    sources/          Income sources / currency setup screens
    subscriptions/    Manage subscriptions screen
    support/          Help & support screen
    theme/            Color/typography/shape/spacing tokens
    util/             Pure (non-@Composable) helpers — formatters, palettes

app/src/main/res/      Android resources (no layout/ folder — Compose only)
```

## Conventions

- **Feature folders** keep screens, ViewModels, and UI state close together. Per-feature `components/` holds composables used by a single feature; cross-feature composables live in `ui/common/`.
- **`ui/common/` is for composables only.** Pure helpers (formatters, color/icon lookups) live in `ui/util/` — they have no Compose dependency and can be called from ViewModels.
- **`ui/theme/` is for tokens.** `Color.kt`, `Type.kt`, `DesignTokens.kt` (sizes/spacing/radius/elevation), `ExtendedColors.kt` (M3 composition local), `Theme.kt` (entry point), `AppButtonStyles.kt`, `AppModifiers.kt`.
- **Data layer is the only place that touches Firebase, Room, or WorkManager.** Repository implementations in `data/` map to/from domain models; the `domain/repository/` interfaces only mention domain types.
- **DI wiring lives in `di/`.** One Hilt module per feature concern.
- **Single activity.** [`MainActivity`](../app/src/main/java/com/kahavanu/MainActivity.kt) hosts the whole nav graph; everything else is Compose.
