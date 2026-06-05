# Kahavanu — Presentation Diagrams

All diagrams are Mermaid. They render on GitHub and in most Markdown/slide tools.
Scenario persona: **Kavindu**, a Sri Lankan freelancer with irregular, multi-source income.

> Tip for slides: paste a single diagram per slide. Use the **speaker notes** under each
> to narrate it. Keep one idea per slide.

---

## 1. Use Case Diagram

Actors: **Kavindu** (primary user) and the **Daily Background Worker** (system actor that
materialises recurring money and scans SMS without user action).

```mermaid
flowchart LR
    Kavindu(["👤 Kavindu<br/>(Freelancer)"])
    Worker(["⚙️ Daily Worker<br/>(System Actor)"])

    subgraph System["Kahavanu System"]
        direction TB
        UC1(["Sign up / Sign in<br/>(email or Google)"])
        UC2(["Log one-off income"])
        UC3(["Schedule recurring income"])
        UC4(["Log expense"])
        UC5(["Manage subscriptions"])
        UC6(["Set & track savings goal"])
        UC7(["Review SMS suggestions<br/>(approve / edit / reject)"])
        UC8(["Manage income sources"])
        UC9(["Configure currencies"])
        UC10(["View insights & history"])
        UC11(["Materialise due income<br/>/ subscription charge"])
        UC12(["Scan bank SMS<br/>→ create suggestions"])
    end

    Firebase[("Firebase<br/>Auth + Firestore")]
    Inbox[("Device SMS Inbox")]

    Kavindu --- UC1
    Kavindu --- UC2
    Kavindu --- UC3
    Kavindu --- UC4
    Kavindu --- UC5
    Kavindu --- UC6
    Kavindu --- UC7
    Kavindu --- UC8
    Kavindu --- UC9
    Kavindu --- UC10

    Worker --- UC11
    Worker --- UC12

    UC1 -. uses .-> Firebase
    UC2 -. syncs .-> Firebase
    UC7 -. "«include»<br/>creates real log" .-> UC2
    UC7 -. "«include»" .-> UC4
    UC12 -. reads .-> Inbox
    UC12 -. "«extend»<br/>matches pending" .-> UC3

    classDef uc fill:#E3F2FD,stroke:#1565C0,color:#0D47A1;
    classDef ext fill:#FAFAFA,stroke:#616161,color:#212121;
    class UC1,UC2,UC3,UC4,UC5,UC6,UC7,UC8,UC9,UC10,UC11,UC12 uc;
    class Firebase,Inbox ext;
```

**Speaker notes:** Kavindu drives the foreground use cases. The Daily Worker is a *system
actor* — it materialises due recurring income/subscriptions and scans SMS on a schedule,
with no taps. Note the `«include»`: approving an SMS suggestion *includes* the normal
"log income/expense" use case, so suggestions reuse the same trusted path. SMS scanning is
the only thing that touches the inbox, and it never auto-creates ledger entries.

---

## 2. High-Level Architecture (MVVM, 4 layers)

```mermaid
flowchart TB
    User(["👤 Kavindu"])

    subgraph UI["ui/ — UI Layer (Jetpack Compose)"]
        Screens["Compose Screens<br/>collectAsStateWithLifecycle()"]
        VMs["ViewModels<br/>StateFlow&lt;UiState&gt; + viewModelScope"]
        Screens -->|intents| VMs
        VMs -->|UiState| Screens
    end

    subgraph Domain["domain/ — Domain Layer (pure Kotlin)"]
        Ifaces["Repository Interfaces<br/>IncomeRepository, AuthRepository…"]
        Models["Domain Models<br/>IncomeLogEntry, ScheduledIncome…"]
    end

    subgraph Data["data/ — Data Layer"]
        Repos["Repository Impls<br/>DefaultIncomeRepository…"]
        Mappers["Mappers<br/>Entity ↔ Domain ↔ Firestore"]
        Sync["SyncManager + WorkManager Workers"]
        Room[("Room DB<br/>source of truth")]
        Fire[("Firebase<br/>Auth + Firestore")]
        Repos --> Mappers
        Repos --> Room
        Repos --> Fire
        Sync --> Room
        Sync --> Fire
    end

    subgraph DI["di/ — Hilt"]
        Modules["@Module / @Binds / @Provides"]
    end

    User --> Screens
    VMs ==>|"depends on<br/>(interface only)"| Ifaces
    Repos -.implements.-> Ifaces
    Repos -.returns.-> Models
    Modules -. binds iface→impl .-> Repos

    classDef ui fill:#E3F2FD,stroke:#1565C0,color:#0D47A1;
    classDef dom fill:#FFF8E1,stroke:#F57F17,color:#BF360C;
    classDef dat fill:#E8F5E9,stroke:#2E7D32,color:#1B5E20;
    classDef di fill:#F3E5F5,stroke:#6A1B9A,color:#4A148C;
    classDef ext fill:#FAFAFA,stroke:#616161,color:#212121;
    class Screens,VMs ui;
    class Ifaces,Models dom;
    class Repos,Mappers,Sync dat;
    class Room,Fire ext;
    class Modules di;
```

**Speaker notes:** The one rule — `ui/` and `data/` both depend only on `domain/`, never on
each other (bold arrow). Hilt binds the interface to its implementation at runtime, so the
ViewModel never knows Firestore exists. Swap the backend or inject a fake repo in tests
without touching a single Composable.

---

## 3. Write Path — "Kavindu logs income" (offline-first)

```mermaid
sequenceDiagram
    autonumber
    actor K as Kavindu
    participant S as IncomeLogScreen
    participant VM as IncomeViewModel
    participant R as DefaultIncomeRepository
    participant DB as Room (income_logs)
    participant FS as Firestore
    participant WM as WorkManager

    K->>S: enter amount, source, date → tap Save
    S->>VM: logIncome()
    VM->>VM: validate + isSaving=true
    VM->>R: logIncome(IncomeLogEntry)
    R->>DB: insert(entity, clientId=UUID)
    DB-->>R: localId
    alt Offline
        R->>WM: enqueue sync (KEEP)
        R-->>VM: Result.success(LOCAL_ONLY)
    else Online
        R->>FS: set users/{uid}/incomeLogs/{clientId}
        FS-->>R: ok
        R->>DB: markSynced(localId, clientId)
        R-->>VM: Result.success(SYNCED)
    end
    VM->>VM: _uiState.update(successMessage)
    Note over DB,S: Room Flow re-emits on insert →<br/>ViewModel collects → screen recomposes<br/>(new row shown without manual refresh)
```

**Speaker notes:** The insert into Room is unconditional and first — that's why the app is
fully usable offline. The network is best-effort; on failure we still return success because
the data is safe locally and a background sync is queued. The UI updates *reactively* via
the Room Flow, not by re-fetching.

---

## 4. Offline-First Sync — push / pull / conflict resolution

```mermaid
flowchart TB
    Trigger["Trigger:<br/>• after offline write<br/>• on sign-in (auth listener)<br/>• WorkManager retry/backoff"]
    Trigger --> Worker["IncomeSyncWorker<br/>(@HiltWorker, network-constrained)"]
    Worker --> Sync["IncomeSyncManager.sync()"]

    subgraph Push["1 — Push (local → cloud)"]
        P1["getUnsynced(uid)<br/>(isSynced = 0)"]
        P2["set / delete Firestore doc"]
        P3["markSynced(localId, remoteId)"]
        P1 --> P2 --> P3
    end

    subgraph Pull["2 — Pull (cloud → local, delta)"]
        L1["fetch collection"]
        L2{"createdAt ><br/>lastSyncTimestamp?"}
        L3["dedup: remoteId →<br/>clientId → logKey()"]
        L4{"remote.updatedAt ><br/>local.updatedAt?"}
        L5["upsert local"]
        L6["skip (local newer)"]
        L1 --> L2 -->|yes| L3 --> L4
        L2 -->|no| L6
        L4 -->|yes| L5
        L4 -->|no| L6
    end

    Sync --> Push --> Pull
    Pull --> Done["updateLastSyncTimestamp()<br/>SyncState = Success"]

    classDef step fill:#E8F5E9,stroke:#2E7D32,color:#1B5E20;
    class P1,P2,P3,L1,L3,L5,L6,Done step;
```

**Speaker notes:** Sync is **delta-based** — we only pull docs created after our last-sync
timestamp (kept in SharedPreferences). The three-tier dedup (remoteId → clientId →
amount/date fingerprint) prevents duplicates even for rows created offline. Conflicts are
resolved by **latest `updatedAt` wins** (Firebase-wins on timestamp). On top of this,
real-time Firestore listeners push live changes from other devices straight into Room.

---

## 5. Sieve — On-Device SMS Engine (innovation)

```mermaid
flowchart TB
    Inbox[("📩 Device SMS Inbox")]
    Worker["SmsScanWorker (@HiltWorker)"]

    subgraph Engine["On-device engine — content never leaves the phone"]
        direction TB
        Allow["Filter by authorized senders<br/>(user allowlist)"]
        Read["SmsReader.readSince(lastScan, ≤14 days)"]
        Hash{"existsByHash()?<br/>(dedup)"}
        Classify["RuleBasedSmsClassifier + SmsRules<br/>→ amount, merchant, kind, confidence"]
        Match["PendingMatcher<br/>(settle scheduled / skip subscription dupes)"]
        Store[("SmsSuggestion<br/>status = PENDING")]
        Allow --> Read --> Hash
        Hash -->|seen| Skip["skip"]
        Hash -->|new| Classify --> Match --> Store
    end

    Notify["AppNotifier → system notification"]
    Review["Review UI (sheet)"]
    Approve(["Kavindu: approve / edit / reject"])
    IncLog[("incomeLogs")]
    ExpLog[("expenseLogs")]
    Net>"🚫 Network boundary — raw SMS never crosses"]

    Inbox --> Worker --> Allow
    Store --> Notify
    Store --> Review --> Approve
    Approve ==>|approve income| IncLog
    Approve ==>|approve expense| ExpLog
    Approve -->|reject| Store

    classDef eng fill:#E8F5E9,stroke:#2E7D32,color:#1B5E20;
    classDef ext fill:#FAFAFA,stroke:#616161,color:#212121;
    classDef bound fill:#FFF,stroke:#C62828,stroke-dasharray:6 4,color:#B71C1C;
    class Allow,Read,Classify,Match eng;
    class Inbox,Store,IncLog,ExpLog ext;
    class Net bound;
    class Worker eng;
```

**Speaker notes:** Two invariants — (1) raw SMS **never leaves the device**: parsing,
classification and scoring are all local; (2) **nothing auto-commits**: the engine produces
confidence-scored *suggestions*, and only Kavindu's explicit approval (bold arrows) creates a
real ledger entry through the same repository path as manual entry. The hash check makes
scans idempotent (no duplicate suggestions).

---

## 6. Scheduled → Realised Pipeline (irregular & recurring income)

```mermaid
flowchart LR
    subgraph Expected["Expected — money the app is waiting on"]
        Sched["Scheduled Income<br/>(one-off / recurrent)"]
        Subs["Subscription<br/>(recurrent expense)"]
    end

    Worker{{"Daily Worker (24h periodic)<br/>processScheduledIncomes()"}}

    subgraph Realised["Realised — money that moved (counts toward balance)"]
        IncLog[("incomeLogs")]
        ExpLog[("expenseLogs")]
    end

    Kavindu(["Kavindu (manual / confirm)"])

    Sched -->|due & recurrent| Worker
    Subs -->|billing day| Worker
    Worker ==>|materialise income| IncLog
    Worker ==>|materialise expense| ExpLog
    Worker -. advance next-due .-> Sched
    Worker -. advance next billing .-> Subs
    Kavindu -->|mark received| IncLog
    Kavindu -->|record ad-hoc| ExpLog

    classDef exp fill:#FFF8E1,stroke:#F57F17,color:#BF360C;
    classDef real fill:#E8F5E9,stroke:#2E7D32,color:#1B5E20;
    class Sched,Subs exp;
    class IncLog,ExpLog real;
```

**Speaker notes:** This is how Kavindu's *irregular* income is handled cleanly. Expected
money (forecast) is kept separate from realised money (truth). Only realised logs feed
balances and analytics, so a scheduled-but-not-yet-received payment never inflates the
balance. The same machinery drives subscriptions on the expense side.

---

## 7. Navigation Map (single activity, two-level)

```mermaid
flowchart TB
    Start(["App start"]) --> Auth{Authenticated?}
    Auth -->|no| Onb["Onboarding"] --> Choice["AuthChoice"]
    Choice --> Login["Login"]
    Choice --> Signup["Signup"]
    Auth -->|yes| Home

    subgraph Shell["MainTabsScreen — inner NavHost (bottom nav)"]
        Home["🏠 Home"]
        Income["💰 Income"]
        Expenses["💸 Expenses"]
        Goals["🎯 Goals"]
        Profile["👤 Profile"]
    end

    Income --> IncomeLog["Log Income"]
    Income --> Recur["Recurring Manager"]
    Income --> IncHist["Income History (?filter=)"]
    Expenses --> ExpLog["Log Expense"]
    Expenses --> ExpHist["Expense History"]
    Expenses --> ManageSubs["Manage Subscriptions"]
    Goals --> GoalSetup["Goal Setup"]
    Goals --> GoalStats["Goal Stats"]
    Goals --> ManageGoals["Manage Goals"]
    Profile --> Sms["SMS Sender Settings"]
    Profile --> Sources["Income Sources"]
    Profile --> Help["Help & Support"]

    Login -. on success .-> Home
    Signup -. on success .-> Home

    classDef tab fill:#E3F2FD,stroke:#1565C0,color:#0D47A1;
    class Home,Income,Expenses,Goals,Profile tab;
```

**Speaker notes:** Single `MainActivity` hosts everything. The outer graph gates auth vs.
main; the inner graph in `MainTabsScreen` holds the five tabs and their sub-screens. Routes
are type-safe sealed objects (`AppDestination`); `IncomeHistory` uses a typed argument.
A single `LaunchedEffect(isAuthenticated)` is the one source of truth for auth redirects.
