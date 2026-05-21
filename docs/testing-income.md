# Income Module — Unit Test Reference

This document catalogs every unit test for the `income` parts of the app:
which class is under test, what each test verifies, what is mocked, and
what the expected outcome is.

## Layout

```
app/src/test/java/com/kahavanu/
├── testing/
│   └── MainDispatcherRule.kt          # JUnit rule swapping Dispatchers.Main with a TestDispatcher
└── ui/income/
    ├── IncomeViewModelTest.kt         # Form / log flow for new income entries
    ├── IncomeSourcesViewModelTest.kt  # CRUD for IncomeSource + currency settings
    ├── IncomeOverviewViewModelTest.kt # Derived StateFlows + SMS suggestion actions
    └── components/
        └── IncomeUtilsTest.kt         # Pure helper functions
```

## Tooling

| Concern             | Library / Approach                                        |
|---------------------|-----------------------------------------------------------|
| Test runner         | JUnit 4 (`libs.junit`)                                    |
| Mocking             | MockK (`io.mockk:mockk:1.13.13`)                          |
| Coroutines          | `kotlinx-coroutines-test:1.10.2` (`runTest`, `StandardTestDispatcher`, `advanceUntilIdle`) |
| Flow assertions     | Turbine (`app.cash.turbine:turbine:1.2.0`) + raw `Flow.first { ... }` |
| Main dispatcher swap| Custom `MainDispatcherRule` (see below)                   |

### `MainDispatcherRule`

`viewModelScope` runs work on `Dispatchers.Main`. On the JVM there is no
Main looper, so we install a `StandardTestDispatcher` for the duration of
each test:

```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()
```

This lets us deterministically `advanceUntilIdle()` to flush all
coroutines launched inside `viewModelScope`.

### Dependency-injection strategy

Each ViewModel uses constructor injection (via Hilt in production). In
tests we bypass Hilt entirely and pass MockK doubles directly:

```kotlin
incomeRepository  = mockk(relaxed = true)
settingsRepository = mockk(relaxed = true)
smsSuggestionRepository = mockk(relaxed = true)
```

`observe*` methods return `MutableStateFlow`s owned by the test, so we
can push new values mid-test to simulate database / network updates.

---

## `IncomeUtilsTest` — pure helper functions

File: [IncomeUtilsTest.kt](../app/src/test/java/com/kahavanu/ui/income/components/IncomeUtilsTest.kt)

No mocks. These are pure functions in [IncomeUtils.kt](../app/src/main/java/com/kahavanu/ui/income/components/IncomeUtils.kt).

| # | Test                                                          | What it verifies                                                                          |
|---|---------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| 1 | `isPending returns true only for pending sourceType`          | `"pending"` → true; everything else (including `null`, `""`) → false.                     |
| 2 | `isRecurrent returns true only for recurrent sourceType`      | `"recurrent"` → true; everything else → false.                                            |
| 3 | `isPersistent is true for pending and recurrent`              | True for `"pending"` and `"recurrent"`; false for `"one_time"`, `null`, unknown strings.  |
| 4 | `formatAmount renders currency prefix and two-decimal amount` | Forces `Locale.US` so the test is deterministic; checks `LKR 1,234.50`, `USD 0.00`, etc.  |

---

## `IncomeViewModelTest`

File: [IncomeViewModelTest.kt](../app/src/test/java/com/kahavanu/ui/income/IncomeViewModelTest.kt)

Class under test: [IncomeViewModel](../app/src/main/java/com/kahavanu/ui/income/IncomeViewModel.kt)

### Test fixtures

| Fixture            | Value                                                                                          |
|--------------------|------------------------------------------------------------------------------------------------|
| `defaultSources`   | `Salary` (RECURRENT + ONE_TIME), `Freelance` (ONE_TIME + PENDING), `Bonus` (ONE_TIME)          |
| `sourcesFlow`      | `MutableStateFlow(defaultSources)` returned by `incomeRepository.observeIncomeSources()`       |
| `currencyFlow`     | `MutableStateFlow(LKR to USD)` returned by `settingsRepository.observeCurrencySettings()`      |

### Tests

| # | Test | What it verifies | Expected result |
|---|------|------------------|-----------------|
| 1 | `init ensures default sources and populates state from flows` | Construction calls `ensureDefaultSources()` once and the state mirrors the upstream flows. | `state.sources == defaultSources`, `selectedSourceId == 1L` (first source supporting ONE_TIME), currencies = `[LKR, USD]`, `currency == LKR`. |
| 2 | `onAmountChange strips non-numeric and collapses multiple decimal separators` | Input sanitization. | `"1a2b3.45.6"` → `"123.45"`; `"abc"` → `""`; `"12.34"` → `"12.34"`. |
| 3 | `changing income type re-resolves source if current source does not support new type` | Switching from ONE_TIME to PENDING while `Bonus` is selected must move selection to `Freelance` (id 2), since `Bonus` doesn't support PENDING. | `selectedSourceId == 2L`, `incomeType == PENDING`. |
| 4 | `changing income type keeps source if it still supports the new type` | Switching from ONE_TIME to RECURRENT with `Salary` selected leaves selection unchanged. | `selectedSourceId == 1L`. |
| 5 | `onCurrencyChange updates currency` | Trivial setter. | `currency == USD`. |
| 6 | `onDateChange updates date and closes the picker` | Side-effect of date selection. | `receivedDate == 2025-01-15`, `isDatePickerOpen == false`. |
| 7 | `onContactSaved and onClearContact mutate contact fields` | Set then clear contact info. | After save: name + number populated. After clear: both `null`. |
| 8 | `currency settings flow resets currency to primary when current is no longer available` | If the settings flow emits a new pair that doesn't contain the user's selection, fall back to primary. | After switching to `USD`, settings flow emits `(LKR, EUR)`. Expect `currency == LKR`, available = `[LKR, EUR]`. |
| 9 | `logIncome shows validation error for blank description` | Description required. | `errorMessage == "Enter a client/description and valid amount"`; repository **never** called. |
| 10 | `logIncome shows validation error for non-positive amount` | Amount `"0"` is rejected. | Same error message as above. |
| 11 | `logIncome shows error when no source is selected` | `sourcesFlow` set to empty before VM construction. | `errorMessage == "Select an income source"`. |
| 12 | `logIncome with ONE_TIME persists IncomeLogEntry and posts synced message` | Happy path for one-time income. Captures the entry passed to `incomeRepository.logIncome(...)`. | Entry fields match inputs (title `"ACME"`, amount `250.50`, currency `USD`, source `Salary`, contact `Bob`/`+1000`). `successMessage == "Income logged"`. Form is reset. |
| 13 | `logIncome falls back to contact name when description is blank` | When description is empty but a contact was saved, use the contact name as the title. Repository returns `LOCAL_ONLY`. | `entry.title == "Charlie"`; `successMessage == "Saved offline. Will sync when online."`. |
| 14 | `logIncome for RECURRENT persists ScheduledIncome with frequency` | Recurring income calls `upsertScheduledIncome`, not `logIncome`. | `scheduled.type == RECURRENT`, `scheduled.frequency == "Weekly"`, `successMessage == "Scheduled income saved"`. |
| 15 | `logIncome for PENDING persists ScheduledIncome without frequency` | Pending income calls `upsertScheduledIncome` with `frequency == null`. | `scheduled.type == PENDING`, `scheduled.frequency == null`. |
| 16 | `logIncome surfaces repository error message on failure` | Repository returns `Result.failure(IllegalStateException("disk full"))`. | `errorMessage == "disk full"`, `isSaving == false`. |
| 17 | `logIncome uses generic error when failure has no message` | Repository returns a `RuntimeException()` (null message). | `errorMessage == "Could not save income"`. |
| 18 | `mutating state clears prior error and success messages` | Any subsequent state mutation must clear stale banners. | After a successful save (`successMessage != null`), calling `onAmountChange` clears both `successMessage` and `errorMessage` to `null`. |
| 19 | `uiState emits sources update when repository flow emits new sources` | The VM keeps subscribing to `observeIncomeSources()`. Uses Turbine. | After pushing a new list to `sourcesFlow`, `uiState.sources` reflects the addition. |

---

## `IncomeSourcesViewModelTest`

File: [IncomeSourcesViewModelTest.kt](../app/src/test/java/com/kahavanu/ui/income/IncomeSourcesViewModelTest.kt)

Class under test: [IncomeSourcesViewModel](../app/src/main/java/com/kahavanu/ui/income/IncomeSourcesViewModel.kt)

### Test fixtures

| Fixture          | Value                                                                                       |
|------------------|---------------------------------------------------------------------------------------------|
| `existingSource` | `IncomeSource(id = 7, name = "Freelance", types = {ONE_TIME, PENDING})`                     |
| `sourcesFlow`    | `MutableStateFlow(listOf(existingSource))`                                                  |
| `currencyFlow`   | `MutableStateFlow(LKR to USD)`                                                              |

### Tests

| # | Test | What it verifies | Expected result |
|---|------|------------------|-----------------|
| 1 | `init pulls sources and currency settings into state` | Construction wires up both flows and calls `ensureDefaultSources()`. | `state.sources == [existingSource]`, primary = LKR, secondary = USD, drafts initialized to match. |
| 2 | `startCurrencyEdit toggles editing flag and seeds drafts from current values` | Even if the draft was mutated earlier, opening the editor must reset the drafts to the current settings. | `isCurrencyEditing == true`, `primaryCurrencyDraft == LKR`. |
| 3 | `cancelCurrencyEdit clears the editing flag` | Simple toggle. | `isCurrencyEditing == false`. |
| 4 | `saveCurrencySettings forwards drafts to the repository` | Repository call uses the drafts, not the current settings. | `settingsRepository.updateCurrencySettings(EUR, GBP)` called once; editor closed. |
| 5 | `onTypeToggle adds a type when missing and removes when present` | Add / remove behavior of toggling. | Initial set contains all 3 types. After 1st toggle of RECURRENT: removed. After 2nd: re-added. |
| 6 | `openSheet and closeSheet manage sheet visibility, closing also resets edit state` | `closeSheet` runs `cancelEdit` → wipes editing id / name / selectedTypes. | After close: `isSheetOpen == false`, `editingSourceId == null`, `nameInput == ""`, `selectedTypes == all`. |
| 7 | `showDeleteConfirmation stores the candidate, dismissDeleteConfirmation clears it` | Delete confirmation state. | After show: `sourceToDelete == existingSource`. After dismiss: `null`. |
| 8 | `saveSource refuses blank names` | Name validation. | `errorMessage == "Enter a source name"`; repository not called. |
| 9 | `saveSource refuses empty type selection` | After toggling all 3 types off, save is rejected. | `errorMessage == "Select at least one type"`. |
| 10 | `saveSource inserts a new source when editingSourceId is null` | Verifies the captured `IncomeSource`. After 2 toggles (PENDING off, RECURRENT off) only ONE_TIME remains. | `source.id == 0`, `source.name == "Tutoring"` (trimmed), `source.types == {ONE_TIME}`. `successMessage == "Source added"`, sheet closed, inputs reset. |
| 11 | `saveSource updates an existing source when editing` | After `startEdit(existingSource)`, the saved object keeps the original id and types. | `source.id == 7`, `source.name == "Freelance Updated"`, `source.types == existingSource.types`. `successMessage == "Source updated"`. |
| 12 | `saveSource surfaces repository error message` | Repository failure. | `errorMessage == "nope"`, `isSaving == false`. |
| 13 | `deleteSource clears candidate on success` | Happy path delete. | `sourceToDelete == null`. |
| 14 | `deleteSource surfaces an error and still clears the candidate on failure` | The current implementation clears `sourceToDelete` even when delete fails. | `errorMessage == "constraint violation"`, `sourceToDelete == null`. |
| 15 | `mutating state via updateState clears prior messages` | `updateState` zeroes error/success messages on every other field update. | After a failed delete (`errorMessage != null`), `onNameChange("Anything")` clears `errorMessage`. |

---

## `IncomeOverviewViewModelTest`

File: [IncomeOverviewViewModelTest.kt](../app/src/test/java/com/kahavanu/ui/income/IncomeOverviewViewModelTest.kt)

Class under test: [IncomeOverviewViewModel](../app/src/main/java/com/kahavanu/ui/income/IncomeOverviewViewModel.kt)

### Test fixtures

| Fixture                  | Value                                                                                |
|--------------------------|--------------------------------------------------------------------------------------|
| `incomeLogsFlow`         | `MutableStateFlow(emptyList())`                                                      |
| `scheduledFlow`          | `MutableStateFlow(emptyList())`                                                      |
| `currencyFlow`           | `MutableStateFlow(LKR to USD)`                                                       |
| `observePendingByKinds`  | Returns `emptyFlow()` unless a test overrides it                                     |
| `sampleSuggestion(...)`  | Helper factory for `SmsSuggestion` so each test can customize `kind` / matched id    |

### Why `first { ... }` instead of `awaitItem()` for the derived StateFlows

`incomeLogs` itself is `stateIn(WhileSubscribed, initialValue = emptyList())`. When
the derived flow first subscribes:

1. `incomeLogs` immediately replays `emptyList`.
2. `combine` runs and emits the "seeded" map `{LKR: 0.0, USD: 0.0}`.
3. The upstream subscription to `incomeLogsFlow` then propagates the real list.
4. `combine` runs again and emits the final map.

`isNotEmpty()` would falsely match the seeded map. We therefore wait with a
stricter predicate such as `first { (it["LKR"] ?: 0.0) > 0.0 }`.

### Tests

| # | Test | What it verifies | Expected result |
|---|------|------------------|-----------------|
| 1 | `currencySettings exposes primary and secondary as codes` | Maps `(CurrencyOption, CurrencyOption)` → `(String, String)` codes. | First emission `"LKR" to "USD"`; after pushing `(EUR, GBP)`, emission `"EUR" to "GBP"`. |
| 2 | `primaryCurrency tracks the primary currency code` | Just the first half of the pair. | `"LKR"` then `"EUR"` after settings update. |
| 3 | `totalIncomeByCurrency aggregates current-month logs per currency` | Sums all current-month logs grouped by currency. Out-of-month and blank-currency entries are excluded. | `totals["LKR"] == 150.0`, `totals["USD"] == 25.0`. |
| 4 | `totalIncomeByCurrency seeds primary and secondary even when there are no logs` | Empty log list still yields a map keyed by current primary/secondary with 0.0 values. | `totals.containsKey("LKR") && totals.containsKey("USD")`; both `== 0.0`. |
| 5 | `totalReceivedByCurrency excludes pending entries` | Same as #3 but filters out `sourceType == "pending"`. | Pending 500.0 LKR is dropped; `totals["LKR"] == 200.0`, `totals["USD"] == 100.0`. |
| 6 | `pendingLogs only includes persistent entries` | Filter keeps `pending` and `recurrent`, drops `one_time`. | `filtered == [pending, recurrent]`. |
| 7 | `markAsReceived delegates to repository` | Thin wrapper. | `incomeRepository.markScheduledAsReceived(42L)` invoked once. |
| 8 | `disableScheduled delegates to repository` | Thin wrapper. | `incomeRepository.deleteScheduledIncome(7L)` invoked once. |
| 9 | `dismissIncomeSuggestion converts the id and forwards it` | Valid id → forwarded; invalid id → ignored. | `smsSuggestionRepository.dismiss(99L)` called; no other ids passed. |
| 10 | `confirmIncomeSuggestion logs as new income for INCOME kind` | For `kind = INCOME`, persists via `logIncome` then `confirm`s the suggestion. | `logIncome` called with title/amount/currency from the suggestion, `sourceType == "sms"`. `confirm(11L)` called. `markScheduledAsReceived` **not** called. |
| 11 | `confirmIncomeSuggestion settles matched scheduled income for SETTLE_PENDING with a match` | When `matchedScheduledIncomeId != null`, only marks scheduled income as received. | `markScheduledAsReceived(555L)` called. `logIncome` **not** called. `confirm(11L)` called. |
| 12 | `confirmIncomeSuggestion falls back to logIncome when SETTLE_PENDING has no match` | When `matchedScheduledIncomeId == null`, behaves like an INCOME confirm. | `logIncome` called; `markScheduledAsReceived` not called; `confirm(11L)` called. |
| 13 | `confirmIncomeSuggestion is a no-op for invalid id` | `"oops".toLongOrNull() == null` returns early. | `getById` / `confirm` never invoked. |
| 14 | `confirmIncomeSuggestion is a no-op when suggestion not found` | `getById(42L)` returns `null` → returns early. | `logIncome`, `markScheduledAsReceived`, `confirm` never invoked. |

---

## Running the tests

```bash
# Run the entire income test suite
./gradlew :app:testDebugUnitTest \
    --tests "com.kahavanu.ui.income.*" \
    --tests "com.kahavanu.ui.income.components.*"

# Run a single class
./gradlew :app:testDebugUnitTest --tests "com.kahavanu.ui.income.IncomeViewModelTest"

# Run a single test method (use the literal Kotlin name, asterisks for spaces)
./gradlew :app:testDebugUnitTest \
    --tests "com.kahavanu.ui.income.IncomeViewModelTest.logIncome*for*RECURRENT*"
```

HTML report: `app/build/reports/tests/testDebugUnitTest/index.html`.

---

## Adding new tests — conventions

1. **Always install `MainDispatcherRule`** — without it any test that touches `viewModelScope` will fail with "Module with the Main dispatcher had failed to initialize".
2. **Wrap test bodies in `runTest { ... }`** and call `advanceUntilIdle()` after triggering a `viewModelScope.launch { ... }` to make sure work has finished.
3. **Mock repositories at the interface level** (`IncomeRepository`, `SettingsRepository`, `SmsSuggestionRepository`). Don't import the `DefaultIncomeRepository` or any Room/Firestore classes.
4. **Use `MutableStateFlow` for `observe*` methods** so you can push new values during the test.
5. **Capture arguments with `slot<T>()`** when you need to assert on the object passed to a `coVerify` call (preferred over building `match { ... }` lambdas for complex objects).
6. **For derived `stateIn(WhileSubscribed)` flows, wait with `first { predicate }`** rather than asserting on the first emitted item — the upstream chain may emit a seeded value before propagating the real one.
