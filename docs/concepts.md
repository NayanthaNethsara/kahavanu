# Kahavanu — Concepts Explained In Depth

A study guide to every major concept the app uses. Read it to *understand* the code, not just
describe it. Each concept follows: **What → Why → How → Where in Kahavanu → Q&A gotcha.**

Contents:
1. [Kotlin language foundations](#1-kotlin-language-foundations)
2. [Coroutines & async](#2-coroutines--async)
3. [Flow, StateFlow, reactive streams](#3-flow-stateflow-and-reactive-streams)
4. [MVVM & clean architecture](#4-mvvm--clean-architecture)
5. [Repository pattern & single source of truth](#5-repository-pattern--single-source-of-truth)
6. [Dependency Injection (Hilt)](#6-dependency-injection-hilt)
7. [Jetpack Compose](#7-jetpack-compose)
8. [Navigation](#8-navigation)
9. [Room (local database)](#9-room-local-database)
10. [Firebase: Auth & Firestore](#10-firebase-auth--firestore)
11. [Offline-first & sync theory](#11-offline-first--sync-theory)
12. [WorkManager (background work)](#12-workmanager-background-work)
13. [The Sieve SMS engine](#13-the-sieve-sms-engine)
14. [Notifications & permissions](#14-notifications--permissions)
15. [Testing concepts](#15-testing-concepts)
16. [Glossary](#16-glossary)

---

## 1. Kotlin language foundations

**Data classes.** `data class IncomeLogEntry(...)` auto-generates `equals`, `hashCode`,
`copy`, `toString`. The `copy(...)` function is the backbone of immutable state updates:
`_uiState.update { it.copy(isSaving = true) }` makes a new state object rather than mutating
the old one — essential for Compose to detect change.

**Sealed classes / interfaces.** A closed set of subtypes known at compile time. Used for:
- Navigation routes — [AppDestination](../app/src/main/java/com/kahavanu/ui/navigation/AppDestination.kt)
  (`sealed class AppDestination(val route: String)`).
- Sync state — `sealed class SyncState { Idle, Syncing, Success, Error }`.
The compiler can exhaustively check `when` over a sealed type, so you can't forget a case.

**Enums.** Fixed value sets with data, e.g. [CurrencyOption](../app/src/main/java/com/kahavanu/domain/model/CurrencyOption.kt)
(`LKR("LKR","Rs","Sri Lankan Rupee")`), `SuggestionKind`, `SuggestionStatus`,
`IncomeSourceType`.

**Null safety.** `String?` vs `String`. The `?.`, `?:` (Elvis), and `!!` operators. Example:
`auth.currentUser?.uid ?: return Result.failure(...)` — "if not signed in, bail out."

**`Result<T>`.** Kotlin's built-in success/failure wrapper. Repositories return
`Result<Unit>` / `Result<IncomeLogResult>` so callers handle errors explicitly without
try/catch everywhere. `.map { }`, `.fold(onSuccess, onFailure)`, `.getOrThrow()`,
`.exceptionOrNull()` are the common operations.

**Extension functions.** Add methods to existing types without inheritance. The app uses
them for mapping (`IncomeLogEntity.toDomain()`, `DocumentSnapshot.toIncomeLogEntity()`) and
for bridging Firebase `Task` → coroutine `Result` (`Task.awaitResult()`).

**Higher-order functions & lambdas.** Functions taking/returning functions. Compose is built
on them (`onClick: () -> Unit`), and the SMS rules store a parser lambda per rule
(`parse = { sender, body, receivedAt -> ... }` in [SmsRules](../app/src/main/java/com/kahavanu/sieve/engine/SmsRules.kt)).

> **Q&A gotcha:** "Why immutable state with `copy()`?" → Compose recomposes by comparing
> object references/equality; mutating in place can be missed and causes stale UI.

---

## 2. Coroutines & async

**What.** Coroutines are Kotlin's way to write asynchronous, non-blocking code that *reads*
like sequential code. A `suspend` function can pause ("suspend") at a slow operation
(network, disk) and resume later **without blocking the thread**.

**Why.** Android's main thread must never block (it draws the UI at 60–120 fps). Old
solutions (callbacks, `AsyncTask`, RxJava) were verbose or leaky. Coroutines give readable
async + automatic cancellation.

**How — key pieces:**
- **`suspend fun`** — a function that may suspend. Can only be called from another suspend
  function or a coroutine builder. Repos expose `suspend fun logIncome(...): Result<...>`.
- **CoroutineScope** — defines the lifetime of coroutines. When the scope is cancelled, all
  its coroutines are cancelled (structured concurrency — no leaks).
- **`viewModelScope`** — a scope tied to the ViewModel; auto-cancelled in `onCleared()`.
  Every async action in a ViewModel runs here: `viewModelScope.launch { ... }`.
- **Dispatchers** — which thread pool runs the work. `Dispatchers.IO` for disk/network
  (the repository uses `CoroutineScope(Dispatchers.IO)` for its background listeners),
  `Dispatchers.Main` for UI. Room/Firestore suspend functions already switch off the main
  thread internally.
- **Builders** — `launch` (fire-and-forget) vs `async`/`await` (returns a value).
- **`suspendCancellableCoroutine`** — bridges a callback API into a suspend function. The app
  uses it in [DefaultAuthRepository](../app/src/main/java/com/kahavanu/data/auth/DefaultAuthRepository.kt#L84-L108)
  to turn a Firebase `Task` into a suspend `Result`.

**Where in Kahavanu.** ViewModels launch in `viewModelScope`; repositories run IO work;
`CoroutineWorker` lets WorkManager run suspend functions in the background.

> **Q&A gotcha:** "What cancels your coroutines?" → Structured concurrency: `viewModelScope`
> cancels when the screen/ViewModel is destroyed; WorkManager cancels its worker's scope on
> stop. You don't manually manage threads.

---

## 3. Flow, StateFlow, and reactive streams

**Flow (cold stream).** An asynchronous sequence of values over time. "Cold" = it does
nothing until someone `collect`s it, and each collector triggers a fresh run. Room DAO
queries return `Flow<List<Entity>>` — the database re-emits the list whenever the table
changes. Operators transform streams: `.map { entities -> entities.map { it.toDomain() } }`.

**StateFlow (hot stream).** A Flow that always holds a current value and emits updates to all
collectors. "Hot" = it exists independently of collectors. This is the **state holder** of a
ViewModel:
```kotlin
private val _uiState = MutableStateFlow(IncomeUiState())   // writable, private
val uiState: StateFlow<IncomeUiState> = _uiState           // read-only, public
```
The UI collects `uiState`; the ViewModel updates via `_uiState.update { it.copy(...) }`.

**SharedFlow.** Like StateFlow but without a "current value" — used for one-off events. (This
app deliberately models everything as state, per its own rules: "ViewModels expose state, not
one-off events.")

**`collect` / `collectAsStateWithLifecycle()`.** In a coroutine you `collect` a Flow. In
Compose, `collectAsStateWithLifecycle()` collects a StateFlow into Compose `State` **and**
stops collecting when the screen is not at least STARTED (saves work/battery, avoids updates
to an off-screen UI). Seen in [MainActivity](../app/src/main/java/com/kahavanu/MainActivity.kt#L40)
and every screen.

**How they chain in the app:**
```
Room Flow<List<Entity>>  →(map)→  Flow<List<DomainModel>>  →(collect in VM)→
  _uiState (StateFlow<UiState>)  →(collectAsStateWithLifecycle)→  Compose recomposition
```

> **Q&A gotcha:** "Cold vs hot?" → Room query = cold (re-runs per collector, re-emits on data
> change). ViewModel `uiState` = hot (one shared value, survives across recompositions).

---

## 4. MVVM & clean architecture

**MVVM = Model–View–ViewModel.**
- **Model** — the data and business rules. Here split into `domain/` (models + interfaces)
  and `data/` (implementations, Room, Firestore).
- **View** — the Compose screens. Dumb: render state, forward events. No logic.
- **ViewModel** — the mediator. Holds UI state (`StateFlow`), exposes actions, calls
  repositories, survives configuration changes (rotation).

**Why MVVM.** Separates *what the screen shows* (state) from *how it's drawn* (Compose) from
*where data comes from* (repository). Makes the ViewModel unit-testable without an emulator.

**Clean architecture / layering.** Four layers with a strict dependency direction:
```
ui/  →  domain/  ←  data/         di/ wires data→domain bindings
```
- **Dependency Inversion Principle:** high-level policy (ViewModels) depends on an
  *abstraction* (`IncomeRepository` interface), not a concrete class. The concrete
  `DefaultIncomeRepository` also depends on that abstraction (by implementing it). Neither
  knows the other; Hilt connects them. Result: the `domain/` layer has **zero** outgoing
  dependencies and is the stable core.
- **Separation of concerns:** UI rendering, state management, and data access never mix.
- **Single Responsibility:** each class does one job (a DAO queries, a mapper converts, a
  ViewModel holds state).

**Unidirectional Data Flow (UDF).** State flows **down** (ViewModel → UI), events flow **up**
(UI → ViewModel). The UI never mutates state directly; it asks the ViewModel to. This makes
state changes predictable and traceable.

**State hoisting.** A Compose pattern where a composable doesn't own its state; the state and
the "change" callback are passed in from above (ultimately the ViewModel). Makes composables
reusable and testable.

**Where in Kahavanu.** [IncomeViewModel](../app/src/main/java/com/kahavanu/ui/income/IncomeViewModel.kt)
(state + actions), [IncomeRepository](../app/src/main/java/com/kahavanu/domain/repository/IncomeRepository.kt)
(abstraction), [DefaultIncomeRepository](../app/src/main/java/com/kahavanu/data/income/DefaultIncomeRepository.kt)
(implementation), [IncomeModule](../app/src/main/java/com/kahavanu/di/IncomeModule.kt) (the binding).

> **Q&A gotcha:** "How is this testable?" → Because the ViewModel depends on an *interface*,
> a test injects a fake `IncomeRepository` (no Firebase, no Room) and asserts the emitted
> `UiState`. See the unit tests under `app/src/test/`.

---

## 5. Repository pattern & single source of truth

**Repository pattern.** A repository is the single API the rest of the app uses to read/write
a kind of data. It hides *where* the data lives (Room? Firestore? both?). Callers see only
domain types and `Flow`/`suspend` functions.

**Single Source of Truth (SSOT).** Exactly one place is authoritative for the UI's data.
In Kahavanu that's **Room**. The UI always observes Room (`observeIncomeLogs()` returns a
Room Flow). Firestore writes happen *behind* Room — they never feed the UI directly. This
guarantees the screen shows the same data offline and online, and avoids flicker/races.

**Mapping at the boundary.** Three representations of the same record:
- **Domain model** (`IncomeLogEntry`) — what the app logic uses.
- **Room entity** (`IncomeLogEntity`) — what's persisted locally (adds sync columns).
- **Firestore document** (a `Map`) — what's stored in the cloud.
Dedicated mappers convert between them (`IncomeMappers.kt`, `IncomeFirestoreMappers.kt`), so
Room/Firestore types never leak past the repository.

> **Q&A gotcha:** "Why not let the UI read Firestore directly?" → It would break offline,
> bypass the cache, couple the UI to Firebase, and create two sources of truth. The
> repository + Room give one consistent, offline-capable source.

---

## 6. Dependency Injection (Hilt)

**What is DI.** Instead of a class building its own dependencies (`val db = Room.database(...)`),
they're **provided from outside** (passed into the constructor). "Inversion of Control" — the
framework, not the class, decides how to build the graph.

**Why.** Decoupling (a class doesn't know how its deps are constructed), testability (swap in
fakes), and lifecycle correctness (singletons live exactly as long as they should).

**Hilt** is Google's DI library built on Dagger, generating the wiring at compile time.
Key annotations in Kahavanu:
- **`@HiltAndroidApp`** on [KahavanuApplication](../app/src/main/java/com/kahavanu/KahavanuApplication.kt)
  — generates the app-wide dependency container.
- **`@AndroidEntryPoint`** on [MainActivity](../app/src/main/java/com/kahavanu/MainActivity.kt)
  — lets Android components receive injected fields.
- **`@HiltViewModel` + `@Inject constructor`** — Hilt builds ViewModels with their repos.
- **`@Module` + `@InstallIn(SingletonComponent::class)`** — a recipe book installed for the
  app's lifetime. See [IncomeModule](../app/src/main/java/com/kahavanu/di/IncomeModule.kt).
- **`@Binds`** — "when someone asks for interface X, give them implementation Y." Used to bind
  `IncomeRepository` → `DefaultIncomeRepository`. Zero runtime cost.
- **`@Provides`** — "here's how to *construct* this type" (for things Hilt can't build itself,
  like `FirebaseFirestore.getInstance()`, the Room database, DAOs).
- **`@Singleton`** / Components — scope = lifetime. `SingletonComponent` = one instance for
  the whole app. Repositories and the DB are singletons.
- **Assisted injection (`@HiltWorker`, `@AssistedInject`, `@Assisted`)** — WorkManager creates
  workers with runtime params (`Context`, `WorkerParameters`) that Hilt can't know in advance;
  assisted injection mixes those with Hilt-provided deps. See `IncomeSyncWorker`. The bridge
  is the `HiltWorkerFactory` set in the Application.

**The dependency graph** is resolved at compile time, so missing/cyclic dependencies are build
errors, not crashes.

> **Q&A gotcha:** "`@Binds` vs `@Provides`?" → `@Binds` maps an interface to an existing
> implementation (no body, abstract). `@Provides` contains code that *builds* an object. Use
> `@Provides` when you need to call a constructor/factory you don't own.

---

## 7. Jetpack Compose

**Declarative UI.** You describe *what* the UI should look like for a given state; the
framework figures out *how* to update the screen. Contrast with the old imperative XML +
`findViewById` + manual `view.setText(...)`. The app has **no XML layouts** — Compose only.

**Composable functions.** `@Composable fun IncomeScreen(...)` are functions that emit UI.
They can call other composables, forming a tree.

**Recomposition.** When state a composable reads changes, Compose re-invokes just that
composable (and those affected) to produce the new UI. Cheap and automatic. This is why
immutable `copy()` state matters — Compose compares to decide what to redraw.

**State in Compose.**
- `remember { mutableStateOf(...) }` — local UI state that survives recomposition (e.g. "is
  this sheet open?"). The project restricts this to trivial view-only state; *screen* state
  lives in the ViewModel.
- `collectAsStateWithLifecycle()` — bridges a ViewModel `StateFlow` into Compose `State`,
  lifecycle-aware.
- **State hoisting** — pass state down, callbacks up.

**Material Design 3.** The design system: `colorScheme`, `Typography`, components (`Scaffold`,
`Card`, `TopAppBar`, etc.). Kahavanu wraps it in `KahavanuTheme` and adds an **`ExtendedColors`
composition local** for semantic tokens M3 lacks (`accentIncome`, `accentExpense`, category
palette). See [ui/theme/](../app/src/main/java/com/kahavanu/ui/theme/).

**CompositionLocal.** A way to pass values implicitly down the composable tree without
threading them through every parameter (how the theme exposes `MaterialTheme.extendedColors`).

**Composable decomposition.** Breaking a screen into small reusable composables. Cross-feature
ones live in [ui/common/](../app/src/main/java/com/kahavanu/ui/common/) (`GlassCard`,
`SectionHeader`, `BottomNavBar`…); feature-specific ones in each `components/` folder.

> **Q&A gotcha:** "What triggers a redraw?" → Reading a Compose `State`/`StateFlow`-backed
> value that changes. If the UI is stale, usually you mutated state instead of replacing it,
> or read a plain variable instead of `State`.

---

## 8. Navigation

**Navigation Compose.** Declares a graph of destinations (`NavHost { composable(route) {...} }`)
and navigates by route string. **Single-activity architecture** — one `Activity`
(`MainActivity`), all screens are composables.

**Two-level graph in Kahavanu.** The outer [AppNavGraph](../app/src/main/java/com/kahavanu/ui/navigation/AppNavGraph.kt)
gates onboarding/auth vs. the main app. The inner graph inside
[MainTabsScreen](../app/src/main/java/com/kahavanu/ui/main/MainTabsScreen.kt) holds the five
bottom-nav tabs and their sub-screens.

**Type-safe routes.** Destinations are sealed objects with a `route` string
([AppDestination](../app/src/main/java/com/kahavanu/ui/navigation/AppDestination.kt)), so you
navigate via a known set, not magic strings scattered around.

**Typed arguments.** `IncomeHistory` carries a `?filter={filter}` arg parsed with
`NavType.StringType`; a `createRoute(filter)` helper builds the URL. This is the "prefer typed
route arguments" rule.

**Back stack & state.** `popUpTo`, `launchSingleTop`, `saveState`/`restoreState` control the
history. The bottom bar uses `saveState`/`restoreState` so each tab remembers its scroll/state
when you switch away and back.

**Auth-driven navigation.** A single `LaunchedEffect(isAuthenticated)` redirects to Home or
AuthChoice — one source of truth, no scattered checks. `LaunchedEffect` runs a coroutine tied
to composition that re-runs when its key changes.

> **Q&A gotcha:** "How do you avoid duplicate destinations on the back stack?" →
> `launchSingleTop = true` and `popUpTo(...) { inclusive }` when switching auth states.

---

## 9. Room (local database)

**What.** Room is an ORM (object-relational mapper) over SQLite. You define Kotlin classes;
Room generates the SQL and type-safe access.

**Three pieces:**
- **`@Entity`** = a table. [IncomeLogEntity](../app/src/main/java/com/kahavanu/data/income/local/IncomeLogEntity.kt)
  has `@PrimaryKey(autoGenerate=true) localId`, an `@Index("userId")`, and columns. It
  implements the shared `SyncEntity` interface for sync bookkeeping (`clientId`, `remoteId`,
  `isSynced`, `isDeleted`, `updatedAtEpochMillis`).
- **`@Dao`** = the query interface. [IncomeLogDao](../app/src/main/java/com/kahavanu/data/income/local/IncomeLogDao.kt):
  `@Query`, `@Insert`, `@Upsert`, plus reactive `fun observeLogs(): Flow<List<...>>` and
  one-shot `suspend` queries used by sync.
- **`@Database`** = the DB holder listing entities + version.
  [AppDatabase](../app/src/main/java/com/kahavanu/data/local/AppDatabase.kt) — 11 entities,
  `version = 26`.

**Reactive queries.** A DAO function returning `Flow` makes Room emit a fresh result every
time the underlying table changes — the engine of the app's live UI.

**Migrations & schema versioning.** Each schema change bumps the version and supplies a
`Migration` so existing user data survives an app update. See the long migration chain wired
in [IncomeModule](../app/src/main/java/com/kahavanu/di/IncomeModule.kt#L47-L70). There's a
`fallbackToDestructiveMigration` safety net (wipes & rebuilds if no path exists — fine for an
offline cache that can re-sync from Firestore).

**Indices & keys.** `@Index("userId")` speeds up per-user queries; `autoGenerate` primary keys
give each row a stable local id (`localId`) independent of the cloud id (`remoteId`).

> **Q&A gotcha:** "Why both `localId` and `clientId`/`remoteId`?" → `localId` is the SQLite
> row id (local only). `clientId` is a UUID generated on device and reused as the Firestore
> document id, so the same record can't duplicate across device/cloud. `remoteId` caches the
> server id.

---

## 10. Firebase: Auth & Firestore

**Firebase Auth.**
- Identity provider. Kahavanu supports **email/password** and **Google Sign-In** (via the
  Credentials API + a Google ID token exchanged for a Firebase credential —
  [DefaultAuthRepository.signInWithGoogleIdToken](../app/src/main/java/com/kahavanu/data/auth/DefaultAuthRepository.kt#L59-L62)).
- **Auth state** is observable: an `AuthStateListener` pushes the current user into a
  `StateFlow<UserSession?>`. Sign-out flips it to null, which the nav graph reacts to.
- Every signed-in user has a stable **`uid`** — the namespace for all their data.

**Firestore (Cloud Firestore).**
- A **NoSQL document database**: data is **collections → documents → (sub)collections**.
  No tables/joins; you model for your read patterns.
- Kahavanu's model is a **per-user tree**: `users/{uid}/incomeLogs/{clientId}`,
  `.../expenseLogs`, `.../scheduledIncomes`, `.../subscriptions`, `.../goalLogs`,
  `.../smsSenders`, `.../settings/config`. Full reference in
  [firestore-schema.md](firestore-schema.md).
- **Document id = `clientId`** (the device-generated UUID), so writes are idempotent.
- **Money = `Double`, timestamps = epoch-millis `Long`** (simple Room↔Firestore round-trip).

**Real-time listeners (snapshot listeners).** `collection(...).addSnapshotListener { ... }`
returns a `ListenerRegistration` and fires whenever matching docs change — locally or from
another device. Kahavanu attaches them per collection, keyed to `uid`, and tears them down on
sign-out ([DefaultIncomeRepository](../app/src/main/java/com/kahavanu/data/income/DefaultIncomeRepository.kt#L53-L70)).

**Offline persistence.** Firestore has its own local cache, but Kahavanu treats **Room** as
the source of truth and runs an explicit sync layer for full control over conflict resolution
and dedup.

**Security rules.** Server-side authorization (`firestore.rules`). The core rule is per-user
isolation:
```
match /users/{uid}/{document=**} {
  allow read, write: if request.auth != null && request.auth.uid == uid;
}
```
This guarantees a user can only touch their own subtree, regardless of client code.

> **Q&A gotcha:** "Why denormalise `userId` into each doc?" → It's a cheap copy that supports
> collection-group queries and makes rules/auditing simpler. NoSQL favors duplication over
> joins.

---

## 11. Offline-first & sync theory

**Offline-first.** The app is designed to work fully without a network; the cloud is an
enhancement, not a requirement. Every feature reads/writes locally first.

**Local-first write.** A write hits Room immediately and returns success; cloud sync is
best-effort and asynchronous. Surfaced to the user as
`IncomeLogResult.LOCAL_ONLY` vs `SYNCED`.

**Eventual consistency.** After enough time/connectivity, all devices converge to the same
state. They may differ momentarily — that's accepted in exchange for offline availability.
(This is the CAP-theorem trade-off: prioritise Availability + Partition tolerance.)

**Delta sync.** Pull only what changed since the last sync, not the whole dataset. Kahavanu
stores a `lastSyncTimestamp` in SharedPreferences
([FirebaseSyncManager](../app/src/main/java/com/kahavanu/data/sync/FirebaseSyncManager.kt#L50-L56))
and skips remote docs with `createdAt <= lastSync`.

**Conflict resolution.** When the same record changed in two places, pick a winner. Kahavanu
uses **Last-Write-Wins by `updatedAt`** ("Firebase wins on timestamp"): apply the remote only
if `remote.updatedAt > local.updatedAt`. Simple, deterministic, good enough for single-user
multi-device.

**Idempotency & deduplication.** Doing the same sync twice must not create duplicates. Two
mechanisms:
- **Stable ids** — `clientId` UUID is the document id, so re-pushing overwrites instead of
  duplicating.
- **Three-tier matching** on pull — find the local row by `remoteId`, else `clientId`, else a
  semantic fingerprint (`logKey()` = amount+date+title) to catch rows created offline before
  any id existed. See [IncomeSyncManager.pullIncomeLogs](../app/src/main/java/com/kahavanu/data/income/sync/IncomeSyncManager.kt#L174-L205).

**Soft delete / tombstones.** Deleting needs to propagate. Most collections hard-delete; for
some (e.g. `smsSenders`) a `isDeleted: true` tombstone is written so other devices observe the
removal via the listener. On pull, any locally-synced `remoteId` missing from the server is
marked deleted.

**Push then pull.** Each sync cycle pushes unsynced local rows first, then pulls remote
changes — so local edits aren't clobbered before they're uploaded.

> **Q&A gotcha:** "What if two devices edit the same goal offline?" → Both carry `updatedAt`;
> on sync the higher timestamp wins. The loser's change is overwritten — an accepted trade-off
> for a single-user app. (A multi-user app would need field-level merge or CRDTs.)

---

## 12. WorkManager (background work)

**What.** The Android library for **deferrable, guaranteed** background work — it survives app
death and reboots, and respects system battery rules. The right tool for sync and scheduled
jobs.

**Why not a coroutine in the ViewModel?** Because sync must run even when no screen is open and
must retry reliably after the process dies. WorkManager persists the work request in its own DB.

**Concepts in Kahavanu** ([IncomeSyncScheduler](../app/src/main/java/com/kahavanu/data/income/sync/IncomeSyncScheduler.kt)):
- **OneTimeWorkRequest** — run once (the sync after an offline write).
- **PeriodicWorkRequest** — repeat on an interval (the 24h "materialise scheduled income" job).
- **Constraints** — preconditions, e.g. `NetworkType.CONNECTED` (don't try to sync offline).
- **Backoff policy** — on failure, retry with growing delay (`EXPONENTIAL` from 30s).
- **Unique work** — `enqueueUniqueWork(name, KEEP, request)` collapses duplicate enqueues so
  ten offline writes don't schedule ten syncs.
- **`Result.success / retry / failure`** — the worker's outcome; `retry` triggers backoff.
- **`CoroutineWorker`** — a worker whose `doWork()` is a suspend function, so it can call the
  suspend sync API directly ([IncomeSyncWorker](../app/src/main/java/com/kahavanu/data/income/sync/IncomeSyncWorker.kt)).
- **`@HiltWorker`** — lets Hilt inject the `SyncManager` into the worker (via the
  `HiltWorkerFactory` configured in the Application).

> **Q&A gotcha:** "Why disable the default WorkManager initializer in the manifest?" → Because
> the app provides its own Hilt-aware `Configuration` (so workers can be injected); the
> `AndroidManifest` removes the default initializer and the Application supplies the config.

---

## 13. The Sieve SMS engine

**Problem it solves.** Kavindu's transactions arrive as bank SMS. Manual entry is tedious and
error-prone. Sieve reads those messages and proposes entries.

**Design principles (state these in the viva):**
- **On-device only.** Reading, parsing, classification, scoring all happen locally. Raw SMS
  content **never** crosses the network boundary. Privacy by construction.
- **Human-in-the-loop.** The engine never writes to the ledger. It produces *suggestions* with
  a confidence score; only the user's approval creates a real entry — via the same repository
  path as manual entry.

**Concepts:**
- **Allowlist.** Only messages from user-authorized senders are scanned (`smsSenders`).
- **Rule-based classification.** [SmsRules](../app/src/main/java/com/kahavanu/sieve/engine/SmsRules.kt)
  is an ordered list of `SmsRule`s (sender regex + body regex + a parser). First match wins
  ([RuleBasedSmsClassifier](../app/src/main/java/com/kahavanu/sieve/engine/RuleBasedSmsClassifier.kt)).
  Rules cover Sri Lankan banks (ComBank, Sampath, HNB, BOC/NSB), services (PickMe, Keells), and
  generic fallbacks.
- **Regex extraction.** `extractAmount` / `extractMerchant` pull structured fields (amount,
  merchant) out of free text.
- **Confidence score** (0–1). Specific bank rules score ~0.9; generic fallbacks ~0.6. Used to
  rank/emphasise suggestions — **never** to auto-approve.
- **Idempotency via hashing.** `SmsHasher.hash(sender, body, receivedAt)`; if the hash exists,
  skip. Re-scanning never duplicates suggestions.
- **Pending matching.** `PendingMatcher` links a detected payment to an existing scheduled
  income (mark it `SETTLE_PENDING`) or recognises a subscription charge already auto-logged
  (skip the duplicate). See [SmsScanWorker](../app/src/main/java/com/kahavanu/data/sieve/sms/SmsScanWorker.kt).
- **Approval path.** [DefaultSmsSuggestionRepository.confirm/dismiss](../app/src/main/java/com/kahavanu/data/sieve/DefaultSmsSuggestionRepository.kt#L42-L48)
  flips suggestion status; the review UI creates the real income/expense log on approval.

**Why "rule-based" not ML?** Deterministic, debuggable, no training data, runs instantly on
device, easy to extend (add a rule). The clean `SmsClassifier` interface means an ML classifier
could replace it later with zero changes elsewhere.

> **Q&A gotcha:** "Isn't reading SMS a privacy risk?" → Runtime `READ_SMS` permission, an
> explicit sender allowlist, on-device processing, and no auto-commit. Only user-approved,
> structured records (amount/category/date) ever sync.

---

## 14. Notifications & permissions

**Notification channels (Android 8+).** Every notification must belong to a channel (category
the user can control). Created once at startup —
[NotificationChannels.ensureChannels](../app/src/main/java/com/kahavanu/notifications/NotificationChannels.kt)
called from the Application. `AppNotifier` posts notifications (e.g. when Sieve detects a
transaction).

**Runtime permissions.** Dangerous permissions must be granted at runtime, not just declared.
- `POST_NOTIFICATIONS` (Android 13+) — requested in
  [MainActivity.maybeRequestNotificationPermission](../app/src/main/java/com/kahavanu/MainActivity.kt#L62-L71)
  via the Activity Result API.
- `READ_SMS`, `READ_CONTACTS` — declared in the manifest, requested when the relevant feature
  is used. The user's choice is always respected (the app degrades gracefully if denied).

**In-app notification feed.** Separate from system notifications: a Room-backed list
(`NotificationEntity`) with read timestamps and auto-purge, surfaced in the top-bar bell.

> **Q&A gotcha:** "What happens if the user denies SMS permission?" → The scan worker simply
> finds no authorized senders / can't read, returns success with nothing to do; the rest of
> the app works normally. No feature hard-depends on it.

---

## 15. Testing concepts

**Unit testing the ViewModel.** Because ViewModels depend on repository *interfaces*, tests
inject fakes/mocks — no emulator, no Firebase. See `app/src/test/.../IncomeViewModelTest.kt`.

**MockK.** A Kotlin mocking library to create fake repositories and stub their suspend
functions (`coEvery { repo.logIncome(any()) } returns Result.success(...)`).

**Turbine.** A library for testing `Flow`/`StateFlow` — it lets a test `awaitItem()` on each
emission and assert the sequence of `UiState`s a ViewModel produces.

**`kotlinx-coroutines-test` + a `MainDispatcherRule`.** Coroutine tests need a controllable
dispatcher so `viewModelScope` work runs deterministically. The custom `MainDispatcherRule`
(`app/src/test/.../testing/`) swaps the Main dispatcher for a test one.

**Why this matters for the rubric.** Testability is *evidence* of clean architecture — you can
only unit-test the ViewModel in isolation because the boundaries are clean.

> **Q&A gotcha:** "How do you test async state?" → Test dispatcher to control coroutines +
> Turbine to assert each `StateFlow` emission (loading → success/error).

---

## 16. Glossary

| Term | One-line meaning |
| --- | --- |
| **MVVM** | UI ↔ ViewModel (state) ↔ Repository (data) separation. |
| **UDF** | Unidirectional data flow: state down, events up. |
| **SSOT** | Single source of truth — Room here. |
| **DI / IoC** | Dependencies provided from outside, not self-constructed. |
| **Coroutine** | Lightweight thread you can suspend/resume without blocking. |
| **`suspend`** | A function that may pause at slow work. |
| **Scope** | Lifetime boundary for coroutines (`viewModelScope`). |
| **Flow** | Cold async stream; re-runs per collector. |
| **StateFlow** | Hot stream holding a current value (UI state). |
| **Recomposition** | Compose re-running composables when read state changes. |
| **State hoisting** | Lifting state out of a composable to its caller. |
| **Entity / DAO** | Room table / query interface. |
| **Migration** | Schema-version upgrade preserving user data. |
| **clientId** | Device UUID reused as the Firestore doc id (idempotency). |
| **Delta sync** | Pull only changes since the last sync timestamp. |
| **LWW** | Last-write-wins conflict resolution by timestamp. |
| **Tombstone** | A "deleted" marker that propagates deletions. |
| **Idempotent** | Repeating the operation has no extra effect. |
| **Constraint (Work)** | Precondition for a background job (e.g. network). |
| **Backoff** | Growing retry delay after failure. |
| **Snapshot listener** | Firestore callback firing on any matching change. |
| **Security rule** | Server-side authorization for Firestore access. |
| **Eventual consistency** | All replicas converge given time/connectivity. |
| **Confidence score** | Sieve's 0–1 certainty for an SMS suggestion. |
| **Human-in-the-loop** | Engine proposes; user must approve. |
