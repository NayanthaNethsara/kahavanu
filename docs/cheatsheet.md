# Kahavanu — One-Page Viva Cheat Sheet

Print this. It combines the file→concept map, the Q&A answers, and the glossary.
Full detail: [concepts.md](concepts.md) · [technical-deep-dive.md](technical-deep-dive.md).

---

## 30-second pitch
> Kahavanu is an **offline-first** Compose finance app for **Kavindu**, a freelancer with
> **irregular, multi-source income**. **MVVM + clean architecture**: UI → ViewModel →
> Repository → Model, with **Room as the single source of truth** and **Firebase** for auth +
> cloud sync. It auto-detects transactions from bank SMS **on-device** (the *Sieve* engine),
> separates **expected vs realised** money, and syncs reliably via **WorkManager**.

---

## File → concept map (where to point during the demo)
| Concept | File |
| --- | --- |
| App startup, Hilt root, WorkManager config | `KahavanuApplication.kt` |
| Single activity, theme, permission, start dest | `MainActivity.kt` |
| MVVM ViewModel (StateFlow, viewModelScope) | `ui/income/IncomeViewModel.kt` |
| Repository interface (domain) | `domain/repository/IncomeRepository.kt` |
| Repository impl (offline-first write, listeners) | `data/income/DefaultIncomeRepository.kt` |
| Hilt wiring (`@Binds`/`@Provides`) | `di/IncomeModule.kt` |
| Room entity (+sync columns) / DAO (Flow) | `data/income/local/IncomeLog{Entity,Dao}.kt` |
| Room database (v26, 11 entities) | `data/local/AppDatabase.kt` |
| Sync algorithm (push/pull/dedup/LWW) | `data/income/sync/IncomeSyncManager.kt` |
| WorkManager scheduler (constraints/backoff) | `data/income/sync/IncomeSyncScheduler.kt` |
| Worker (CoroutineWorker, @HiltWorker) | `data/income/sync/IncomeSyncWorker.kt` |
| Auth (Firebase → StateFlow) | `data/auth/DefaultAuthRepository.kt` |
| Auth-driven navigation | `ui/navigation/AppNavGraph.kt` |
| Routes (sealed, typed arg) | `ui/navigation/AppDestination.kt` |
| Bottom-nav shell (inner NavHost) | `ui/main/MainTabsScreen.kt` |
| Sieve worker (scan → suggest) | `data/sieve/sms/SmsScanWorker.kt` |
| Sieve rules / classifier | `sieve/engine/SmsRules.kt`, `RuleBasedSmsClassifier.kt` |
| Firestore schema (per-user tree) | `docs/firestore-schema.md` |

---

## Anticipated questions → crisp answers
- **Why MVVM?** Testable ViewModels, Compose-native StateFlow, predictable one-way data flow.
- **Where's the business logic?** ViewModels + repositories. Composables only render state.
- **Why does the UI never touch Firestore?** Room is the single source of truth → consistent
  offline + online, no two-sources-of-truth races.
- **How does offline work?** Write hits Room first and returns success; Firestore is best-effort;
  WorkManager re-syncs when online. UI updates via the Room Flow.
- **Conflict resolution?** Last-write-wins by `updatedAt` (Firebase wins on timestamp).
- **How do you avoid duplicate transactions across devices?** Doc id = device `clientId`
  (idempotent) + three-tier dedup on pull: `remoteId` → `clientId` → amount/date/title
  fingerprint.
- **What is delta sync?** Pull only docs with `createdAt >` last-sync timestamp (in SharedPrefs).
- **Why WorkManager not a coroutine?** Guaranteed, survives app death/reboot, network
  constraint + exponential backoff, unique-work dedup.
- **`@Binds` vs `@Provides`?** `@Binds` maps interface→existing impl (no code). `@Provides`
  builds an object you don't own (Firestore, Room, DAOs).
- **Cold vs hot flow?** Room query = cold (re-emits on data change). `uiState` = hot StateFlow
  (one shared current value).
- **Why on-device SMS?** Privacy: raw SMS never leaves the phone; allowlist + runtime
  permission; engine emits confidence-scored **suggestions**, never auto-commits.
- **How is irregular income handled?** Expected (scheduled/subscriptions) is separate from
  realised logs; a daily worker materialises due items; only realised counts toward balances.
- **Firebase security?** Per-user tree `users/{uid}/…` + rules `request.auth.uid == uid`.
- **How is it testable?** ViewModel depends on the repo *interface* → inject a fake (MockK),
  assert `StateFlow` emissions with Turbine + a test dispatcher.
- **Why immutable state + `copy()`?** StateFlow/Compose de-dupe by equality; new instance on
  change → correct minimal recomposition; in-place mutation → stale UI.

---

## The 6 sentences that win the Q&A
1. UI → ViewModel → domain interface → repository; Hilt binds interface to impl; `ui/` and
   `data/` never import each other.
2. Room is the single source of truth; every write hits Room first, so the app works fully
   offline.
3. Sync = push unsynced rows, pull docs newer than the last-sync timestamp, dedup by
   remoteId→clientId→fingerprint, resolve conflicts by latest `updatedAt`.
4. WorkManager runs sync with a network constraint + exponential backoff; real-time Firestore
   listeners add instant cross-device updates.
5. Expected money is separate from realised money; a daily worker materialises due items, and
   only realised logs count toward balances — that handles irregular income.
6. The SMS engine runs entirely on-device and only produces confidence-scored suggestions the
   user must approve.

---

## Glossary (rapid revision)
**MVVM** UI↔ViewModel(state)↔Repository(data) · **UDF** state down, events up ·
**SSOT** one authoritative source (Room) · **DI/IoC** deps provided, not self-built ·
**coroutine** suspendable lightweight thread · **suspend** function that may pause ·
**scope** coroutine lifetime (`viewModelScope`) · **Flow** cold async stream ·
**StateFlow** hot stream with current value · **recomposition** Compose re-running on state
change · **state hoisting** lift state to caller · **entity/DAO** Room table/query ·
**migration** schema upgrade keeping data · **clientId** device UUID = Firestore doc id ·
**delta sync** pull only changes since last sync · **LWW** last-write-wins by timestamp ·
**tombstone** deletion marker that propagates · **idempotent** repeating has no extra effect ·
**constraint** precondition for a Work job · **backoff** growing retry delay ·
**snapshot listener** Firestore change callback · **security rule** server-side authz ·
**eventual consistency** replicas converge over time · **confidence score** Sieve 0–1 certainty
· **human-in-the-loop** engine proposes, user approves.

---

## Demo run order (rehearse this)
1. Sign in → lands on Home (auth-driven nav).
2. Log income → appears instantly (reactive Room Flow).
3. Airplane mode → log again → "Saved offline" → reconnect → watch it sync. ← **strongest proof**
4. SMS scan → suggestion with confidence → approve → becomes a real log.
5. Schedule recurring income / show a subscription.
6. Create + track a savings goal (trade-off simulator).
7. Show multi-currency in settings.

**Pre-flight:** `./gradlew :app:assembleDebug` clean · valid `google-services.json` · seeded
account · `firestore.rules` deployed · screen-recording backup of the whole demo.
