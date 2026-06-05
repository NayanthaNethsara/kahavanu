# Kahavanu — Presentation Deck Outline

A slide-by-slide plan targeting the **Excellent** band of every rubric criterion.
Persona: **Kavindu**, a Sri Lankan freelancer with irregular, multi-source income.

- Diagrams to embed live in [diagrams.md](diagrams.md).
- Supporting docs: [architecture.md](architecture.md), [firestore-schema.md](firestore-schema.md).
- Suggested length: **16 slides**, ~12–15 min talk + live demo + Q&A.

Legend for each slide: **[Say]** = talking points · **[Show]** = on-screen artifact ·
**[Rubric]** = which criterion it scores.

---

## Slide 1 — Title
- **[Show]** App name *Kahavanu*, one-line tagline: "Offline-first finance tracking for
  irregular, multi-source income." Your name, course, date, app icon/screenshot.
- **[Say]** One sentence on who Kavindu is and the problem.

## Slide 2 — The Problem (Kavindu's scenario)
- **[Say]** Kavindu is a freelancer: income arrives irregularly, from several sources, often
  notified by bank SMS. Existing apps assume a fixed monthly salary, force tedious manual
  entry, and conflate *expected* with *received* money. He also travels (multi-currency) and
  has spotty connectivity.
- **[Show]** 4–5 bullet pain points.
- **[Rubric]** 1 (Problem Analysis) — frame it as *your* analysis, not a generic feature list.

## Slide 3 — Requirements (Functional + Non-Functional)
- **[Show]** Two columns.
  - *Functional:* auth; log one-off + scheduled income; categorise by source; auto-detect
    transactions from SMS (with approval); recurring income & subscriptions; savings goals
    with projections; multi-currency; cloud sync; offline entry; history/insights.
  - *Non-functional:* offline-first availability; **privacy** (SMS stays on device);
    data integrity (dedup + conflict resolution); responsiveness (StateFlow); security
    (per-user data + rules); cross-device consistency.
- **[Say]** Call out that non-functional requirements drove the architecture, not the other
  way round.
- **[Rubric]** 1, 6. **[Show]** point to your SRS + Use Case diagram (diagrams.md §1).

## Slide 4 — Solution Overview (feature map → pain points)
- **[Show]** A table: each Kavindu pain point → the Kahavanu feature that solves it
  (scheduled-vs-realised, income sources, Sieve SMS, subscriptions, goals, multi-currency).
- **[Rubric]** 1, 5.

## Slide 5 — Architecture (the headline slide)
- **[Show]** diagrams.md §2 (MVVM 4-layer).
- **[Say]** The one rule: `ui/` and `data/` depend only on `domain/`; Hilt binds interface →
  impl. Room is the source of truth; only repositories touch Room/Firestore/WorkManager.
  Strict one-way data flow: UI → ViewModel → Repository → Model.
- **[Rubric]** 2.

## Slide 6 — Code Quality: MVVM in practice
- **[Show]** Side-by-side snippets:
  - `IncomeViewModel` — `StateFlow<UiState>`, `viewModelScope`, validation, no Android types.
  - `IncomeLogScreen` — `collectAsStateWithLifecycle()`, hoists events, **zero business logic**.
  - `IncomeModule` — `@Binds` interface→impl, `@Provides` for Firestore/Room/DAOs.
- **[Say]** Coroutines + StateFlow used correctly; logic lives in ViewModel/Repository only.
- **[Rubric]** 2.

## Slide 7 — Data Flow: writing income (offline-first)
- **[Show]** diagrams.md §3 (sequence diagram).
- **[Say]** Room write is unconditional and first → usable offline. Network is best-effort;
  `LOCAL_ONLY` vs `SYNCED` is surfaced to the user. UI updates reactively via the Room Flow,
  not by re-fetching.
- **[Rubric]** 2, 4, 5 (offline edge case).

## Slide 8 — Firebase: Auth + data model
- **[Show]** Firestore collection tree from [firestore-schema.md](firestore-schema.md):
  `users/{uid}/{incomeLogs, incomeSources, scheduledIncomes, expenseLogs, subscriptions,
  goalLogs, smsSenders, settings}`. Note doc ID = `clientId`, multi-currency in `settings/config`.
- **[Say]** Email/password + Google sign-in; per-user tree; epoch-millis timestamps;
  soft-delete tombstones; `updatedAt` is the conflict winner.
- **[Rubric]** 4. **[Show]** your `firestore.rules` file (per-user `request.auth.uid == uid`).

## Slide 9 — Sync engine + real-time listeners
- **[Show]** diagrams.md §4 (push/pull/conflict).
- **[Say]** Delta sync (only docs newer than last-sync timestamp); three-tier dedup
  (remoteId → clientId → fingerprint); Firebase-wins on `updatedAt`; WorkManager runs it with
  a network constraint + exponential backoff; real-time `ListenerRegistration`s give live
  cross-device updates and re-bind on auth changes.
- **[Rubric]** 4.

## Slide 10 — Innovation: Sieve on-device SMS engine
- **[Show]** diagrams.md §5.
- **[Say]** Reads only allowlisted bank senders on-device; rule-based classifier extracts
  amount/merchant/kind with a confidence score; produces **suggestions**, never auto-commits;
  hash-based dedup; raw SMS never crosses the network boundary. Approval reuses the normal
  log path.
- **[Rubric]** 5 (innovation), 4 (privacy NFR).

## Slide 11 — Handling edge cases
- **[Show]** diagrams.md §6 (scheduled → realised) + bullets.
- **[Say]**
  - *Irregular income* → expected vs realised separation; only realised counts.
  - *Multi-source* → source catalogue + per-source insights.
  - *Goal progress* → projections + trade-off simulator + adjustment logs.
  - *Offline edits & duplicates* → local-first + clientId dedup + conflict resolution.
- **[Rubric]** 5.

## Slide 12 — UI / UX with Jetpack Compose
- **[Show]** 2–3 real screenshots + your annotated wireframes; diagrams.md §7 (navigation).
- **[Say]** Material 3 theme with extended semantic colours; decomposed reusable composables
  (`ui/common/`); full navigation graph, single-activity, typed route arg. Each UX choice maps
  to a Kavindu usability fix (SMS removes manual entry, expected-vs-realised removes confusion,
  empty states + insights for clarity).
- **[Rubric]** 3.

## Slide 13 — Documentation & quality
- **[Show]** The docs set: README, architecture.md (diagrams), firestore-schema.md, SRS,
  use-case + architecture diagrams, annotated wireframes.
- **[Say]** Everything is scenario-specific and kept in-repo next to the code.
- **[Rubric]** 6.

## Slide 14 — Tech stack & engineering practices
- **[Show]** Stack table (Kotlin 2.2, Compose + M3, Hilt, Room v26, Firebase, WorkManager,
  Coil) + test list (MockK, Turbine, coroutines-test) + R8 on release.
- **[Say]** Unit-tested ViewModels prove the architecture is testable because of the interface
  boundary.
- **[Rubric]** 2, 7.

## Slide 15 — Live Demo script
- **[Say / Do]** Run order (have a pre-seeded account ready):
  1. Sign in (show auth-driven navigation).
  2. Log a one-off income → appears instantly (reactive Room Flow).
  3. Toggle airplane mode → log again → "Saved offline" → re-enable → watch it sync.
  4. Trigger an SMS scan → a suggestion appears with confidence → approve it → becomes a log.
  5. Schedule a recurring income / show a subscription.
  6. Create/track a savings goal + trade-off simulator.
  7. Show multi-currency in settings.
- **[Rubric]** 7. **[Tip]** Verify `./gradlew :app:assembleDebug` and a valid
  `google-services.json` beforehand; keep a backup APK on the device.

## Slide 16 — Q&A prep (anticipated questions)
Have crisp answers ready:
- *Why MVVM?* → testability, Compose-native StateFlow, one-way flow; show ViewModel tests.
- *Where's your business logic?* → ViewModels + repositories; Composables only render.
- *How does offline work?* → Room-first write; LOCAL_ONLY result; WorkManager re-sync.
- *How do you avoid duplicate transactions across devices?* → clientId = doc ID + three-tier
  dedup + listener reconciliation.
- *Conflict resolution?* → latest `updatedAt` wins (Firebase-wins).
- *Why on-device SMS?* → privacy NFR; raw SMS never leaves the phone; suggestions only.
- *Security model?* → per-user Firestore tree + rules locking `request.auth.uid == uid`.
- *How is irregular income handled?* → expected (scheduled/subscriptions) vs realised logs;
  only realised counts toward balances.

---

## The 6 sentences that win the Q&A
1. UI talks to ViewModels, ViewModels to domain interfaces, only repositories touch
   Room/Firestore — Hilt binds it all.
2. Room is the source of truth; every write hits Room first, so the app is fully usable offline.
3. Sync is delta-based: push unsynced rows, pull docs newer than our last-sync timestamp,
   dedup by remoteId → clientId → fingerprint, resolve conflicts by latest `updatedAt`.
4. WorkManager runs sync with a network constraint and exponential backoff; real-time Firestore
   listeners add instant cross-device updates.
5. Expected money is separate from realised money; a daily worker materialises due items, and
   only realised logs count toward balances — that's how we handle irregular income.
6. The SMS engine runs entirely on-device and only ever produces confidence-scored suggestions
   the user must approve — privacy-preserving by design.

---

## Pre-presentation checklist
- [ ] APK builds clean (`./gradlew :app:assembleDebug`) and runs on the demo device.
- [ ] `google-services.json` present and valid; test account pre-seeded with data.
- [ ] `firestore.rules` deployed (or screenshot ready).
- [ ] Diagrams render in your slide tool (export Mermaid to PNG/SVG as a fallback).
- [ ] Annotated wireframes added to Slide 12.
- [ ] Airplane-mode demo rehearsed (the offline → sync moment is your strongest live proof).
- [ ] Backup: screen recording of the full demo in case live fails.
