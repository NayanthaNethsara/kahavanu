# Decisions

## Tech choices

- **Kotlin + Coroutines** — async work and Flow-based streams.
- **Jetpack Compose + Material 3** — UI toolkit. Compose-only; no XML layouts.
- **MVVM + Repository pattern** — predictable UI state and data flow. ViewModels depend on `domain/repository/` interfaces, never on concrete data-layer classes.
- **Room** — local persistence and offline access. Single `AppDatabase` with 9 entities.
- **Firebase Auth + Firestore** — authentication and cloud data.
- **WorkManager** — background sync and retries; wrapped by `SmsScanRepository` and per-feature sync schedulers.
- **Hilt** — dependency injection. One module per feature concern.
- **KSP** — annotation processing for Room and Hilt.

## Build choices

- Java 17 and Kotlin toolchain 17.
- Version catalog for dependency management.
- R8 enabled in release builds.

## UI / theme choices

- **Material 3 `colorScheme` as primary surface for color** — every M3-standard slot (primary, surfaceVariant, outlineVariant, …) is wired in [Theme.kt](../app/src/main/java/com/kahavanu/ui/theme/Theme.kt).
- **`ExtendedColors` composition local for non-M3 tokens** — brand tonal scale, income/expense accents, category palette, decorative glows. Accessed via `MaterialTheme.extendedColors.*`. See [ExtendedColors.kt](../app/src/main/java/com/kahavanu/ui/theme/ExtendedColors.kt). Equivalent top-level `val`s in [Color.kt](../app/src/main/java/com/kahavanu/ui/theme/Color.kt) are exposed for non-composable callers (ViewModels, util fns).
- **No raw-palette indirection.** Hex literals live in `Color.kt`; there is no `RawColors` module. This keeps the palette flat and prevents drift between tokens.
- **Inter font + standard M3 weights.** `titleMedium`, `titleSmall` and all `labelXxx` styles use `FontWeight.Medium` per the M3 spec; the rest use `Normal`. Call sites that need a different weight override explicitly — but the common case (e.g. a card title at `titleSmall`) needs no override.
- **`ui/common/` for composables, `ui/util/` for pure helpers.** A composable belongs in `common/` only if it returns a UI tree. Pure functions (`formatAmount`, `categoryColor(name)`, `goalCategoryIcon(GoalCategory)`) live in `util/` and have no Compose dependency.

## Architecture rules

- **Domain types only at the repository boundary.** Room `@Entity` and Firestore `DocumentSnapshot` types stay inside `data/`. The `domain/repository/` interfaces only mention domain models or domain-level result types (e.g. `Result<Unit>`).
- **ViewModels inject repository interfaces, not platform/data-layer concretes.** No `Context`, `Activity`, `WorkManager`, `FirebaseAuth`, or `*Scheduler` in a ViewModel constructor. Where a non-data concern needs to be invoked (e.g. SMS scan), a domain interface (`SmsScanRepository`) wraps it.
- **UI collects state via `collectAsStateWithLifecycle()`.** Plain `collectAsState()` is reserved for short-lived flows scoped to a sub-composable (e.g. a sheet).
- **No hard-coded fallback / demo data in ViewModels.** UI state mirrors what the repository emits. Empty-state UX is the composable's job, not the ViewModel's.

## Reusable components — keep the bar high

Components only graduate to `ui/common/` when they have **two or more existing callers** with substantially the same shape. Single-use composables stay private to their feature folder. Recent extractions:

- `QuickActionRow` + `QuickAction` — replaces 3 feature-specific `*ActionButtons` files.
- `StatusBadge` — replaces `CategoryBadge` (expenses) and `TypeBadge` (income history).
- `DetailSheet` (`SheetLabel`, `DetailRow`) — extracted from duplicated private helpers in `ExpenseListItem` and `HistoryListItem`.
- `EmptyState` — replaces inline icon-title-subtitle blocks across goals, expenses, and income screens.
- `CategoryPalette` / `GoalCategoryPalette` (in `ui/util/`) — canonical category-to-color and category-to-icon mappings, used wherever a category is rendered.
