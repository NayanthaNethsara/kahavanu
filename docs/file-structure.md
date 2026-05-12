# File structure

```
app/src/main/java/com/kahavanu/
  core/
    config/           App configuration constants
  data/
    auth/             Firebase auth repository
    common/           Shared data utilities
    income/           Income repositories, mappers, sync, local
    expenses/         Expenses repositories, mappers, sync, local
    settings/         Settings repositories and local storage
    sync/             Base sync abstractions
  di/                 Hilt modules
  domain/
    model/            Domain models
    repository/       Repository interfaces
  ui/
    auth/             Auth screens and ViewModel
    common/           Shared UI components
    expenses/         Expenses screen
    goals/            Goals screen
    home/             Home screen
    income/           Income screens and ViewModels
    main/             Main tabs shell
    navigation/       AppDestination and nav graph
    onboarding/       Onboarding flow
    profile/          Profile screen
    theme/            Design tokens and theme

app/src/main/res/      Android resources
```

## Conventions

- Feature folders keep screens, ViewModels, and UI state close together.
- Data layer is the only place that touches Firebase, Room, or WorkManager.
- DI wiring lives in `di/`.
