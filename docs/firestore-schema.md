# Firestore schema

This document is the authoritative reference for Kahavanu's Firestore layout. It is derived from the `toFirestoreMap()` serializers on each [SyncEntity](../app/src/main/java/com/kahavanu/data/sync/SyncEntity.kt) and the snapshot mappers in `data/*/…FirestoreMappers.kt`. If you change either side, update this file.

## Conventions

- **Per-user tree.** Every document lives under `users/{uid}/…` where `{uid}` is the Firebase Auth UID. There is no cross-user data.
- **Document ID = `clientId`.** New documents are pushed with their locally generated `clientId` as the document ID, so the server- and client-side identifiers are the same value (`remoteId` on the local row is just a cached copy). Snapshot listeners reconcile by document ID.
- **Timestamps are epoch millis (`Long`).** Fields suffixed with `At` / `Date` / `Generated` are UTC `System.currentTimeMillis()` values, not Firestore `Timestamp` objects. Choice made for trivial Room ↔ Firestore round-tripping.
- **Money is `Double`.** The `amount`, `targetAmount`, `currentAmount` fields are stored as doubles; `currency` is the ISO code (`"LKR"`, `"USD"`, …).
- **Soft delete via tombstones.** Most collections delete documents outright when a row is marked `isDeleted` (see [IncomeSyncManager.kt](../app/src/main/java/com/kahavanu/data/income/sync/IncomeSyncManager.kt#L100-L110)). [`smsSenders`](#userssmssenderssenderid) is the exception — it writes an `isDeleted: true` tombstone so other devices observe the removal through the listener.
- **`updatedAt` is the conflict winner.** Sync compares `updatedAt` and keeps the higher value. Always bump it when mutating a document.
- **Local-only fields are not written.** `localId`, `isSynced`, `remoteId` exist only in Room and never appear in Firestore documents.

## Collection map

```
users/{uid}/
├── settings/config                       single doc — user preferences
├── incomeLogs/{clientId}                 realised income entries
├── incomeSources/{clientId}              catalogue of income source profiles
├── scheduledIncomes/{clientId}           recurring/pending income templates
├── expenseLogs/{clientId}                realised expense entries
├── subscriptions/{clientId}              recurring expense templates
├── goalLogs/{clientId}                   savings goals
└── smsSenders/{clientId}                 trusted SMS sender allowlist
```

There is no document at `users/{uid}` itself — the UID is just a path segment that namespaces the subcollections.

---

## `users/{uid}/settings/config`

Single document. Created on first sign-in by [`DefaultSettingsRepository.ensureDefaultSettings`](../app/src/main/java/com/kahavanu/data/settings/DefaultSettingsRepository.kt#L199-L230). All writes use `SetOptions.merge()` so partial updates are safe.

```json
{
  "primaryCurrency": "LKR",
  "secondaryCurrency": "USD",
  "isAutoMatchDepositsEnabled": true,
  "isPushAlertsEnabled": true,
  "lastSmsScanEpochMillis": 1674532800000,
  "updatedAt": 1674532800000
}
```

- `primaryCurrency` (String, required): `CurrencyOption.name`, e.g. `"LKR"`. Default `LKR`.
- `secondaryCurrency` (String, required): Same enum as above. Default `USD`.
- `isAutoMatchDepositsEnabled` (Boolean, optional): Defaults to `true` when absent. Toggles SMS deposit auto-matching.
- `isPushAlertsEnabled` (Boolean, optional): Defaults to `true` when absent.
- `lastSmsScanEpochMillis` (Long, optional): High-water mark for the on-device SMS scan worker.
- `updatedAt` (Long, required): Epoch millis. Conflict resolution field.

---

## `users/{uid}/incomeLogs/{clientId}`

Realised income — one document per recognised payment. Defined by [`IncomeLogEntity`](../app/src/main/java/com/kahavanu/data/income/local/IncomeLogEntity.kt) and [`IncomeFirestoreMappers.toIncomeLogEntity`](../app/src/main/java/com/kahavanu/data/income/IncomeFirestoreMappers.kt#L8-L37). Deletes are hard deletes.

```json
{
  "clientId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "user123",
  "title": "Salary deposit",
  "amount": 50000.0,
  "currency": "LKR",
  "receivedAt": 1674532800000,
  "createdAt": 1674532800000,
  "updatedAt": 1674532800000,
  "sourceId": 1,
  "sourceName": "Acme Ltd",
  "sourceType": "salary",
  "frequency": "monthly",
  "isInvoiceSent": false,
  "contactName": "Payroll Dept",
  "contactNumber": null
}
```

- `clientId` (String, required): UUID generated on device. Document ID === this value.
- `userId` (String, required): Denormalised copy of `{uid}`. Useful for collection-group rules.
- `title` (String, required): User-facing label.
- `amount` (Double, required): Always positive.
- `currency` (String, required): ISO code.
- `receivedAt` (Long, required): Epoch millis the income was received.
- `createdAt` (Long, required): Epoch millis the log was created locally.
- `updatedAt` (Long, required): Conflict resolution field.
- `sourceId` (Long, optional): Local `incomeSources.localId` reference. Nullable.
- `sourceName` (String, optional): Denormalised name from the source.
- `sourceType` (String, optional): One of the strings in the parent source's `types` array.
- `frequency` (String, optional): `"daily" | "weekly" | "monthly" | "yearly"` for recurring.
- `isInvoiceSent` (Boolean, optional): Defaults `false`.
- `contactName` (String, optional): Optional payer info.
- `contactNumber` (String, optional): Optional payer info.

---

## `users/{uid}/incomeSources/{clientId}`

Reusable income-source profiles (e.g. "Acme Ltd — payroll"). Defined by [`IncomeSourceEntity`](../app/src/main/java/com/kahavanu/data/income/local/IncomeSourceEntity.kt). Deletes are hard deletes; the sync manager removes the document when `isDeleted` is set locally.

```json
{
  "clientId": "550e8400-e29b-41d4-a716-446655440001",
  "userId": "user123",
  "name": "Acme Ltd",
  "types": ["salary", "bonus"],
  "createdAt": 1674532800000,
  "updatedAt": 1674532800000
}
```

- `clientId` (String, required): Document ID.
- `userId` (String, required): Denormalised UID.
- `name` (String, required): Display name.
- `types` (Array<String>, required): Allowed `sourceType` values for income logs from this source.
- `createdAt` (Long, required): Epoch millis.
- `updatedAt` (Long, required): Conflict resolution field.

---

## `users/{uid}/scheduledIncomes/{clientId}`

Recurring or pending income templates. Each device runs a worker that generates realised `incomeLogs` from these on schedule. Defined by [`ScheduledIncomeEntity`](../app/src/main/java/com/kahavanu/data/income/local/ScheduledIncomeEntity.kt).

```json
{
  "clientId": "550e8400-e29b-41d4-a716-446655440002",
  "userId": "user123",
  "title": "Monthly salary",
  "amount": 50000.0,
  "currency": "LKR",
  "type": "pending",
  "frequency": "monthly",
  "scheduledDate": 1677124800000,
  "lastGenerated": 1674532800000,
  "occurrenceCount": 5,
  "sourceId": 1,
  "sourceName": "Acme Ltd",
  "isInvoiceSent": false,
  "contactName": "Payroll",
  "contactNumber": null,
  "updatedAt": 1674532800000
}
```

- `clientId` (String, required): Document ID.
- `userId` (String, required): Denormalised UID.
- `title` (String, required): Display label.
- `amount` (Double, required): Per-occurrence amount.
- `currency` (String, required): ISO code.
- `type` (String, required): `"pending"` by default. Free-form category tag (e.g. `"salary"`).
- `frequency` (String, optional): `"daily" | "weekly" | "monthly" | "yearly"`. Null for one-off.
- `scheduledDate` (Long, required): Next occurrence, epoch millis.
- `lastGenerated` (Long, optional): Epoch millis of the most recent generated `incomeLogs` row.
- `occurrenceCount` (Long, required): Total realisations generated to date.
- `sourceId` (Long, optional): Local `incomeSources.localId` reference.
- `sourceName` (String, optional): Denormalised source name.
- `isInvoiceSent` (Boolean, optional): Defaults `false`.
- `contactName` (String, optional): Optional.
- `contactNumber` (String, optional): Optional.
- `updatedAt` (Long, required): Conflict resolution field.

---

## `users/{uid}/expenseLogs/{clientId}`

Realised expense entries. Defined by [`ExpenseLogEntity`](../app/src/main/java/com/kahavanu/data/expenses/local/ExpenseLogEntity.kt) and [`ExpenseFirestoreMappers`](../app/src/main/java/com/kahavanu/data/expenses/ExpenseFirestoreMappers.kt). Hard deletes.

```json
{
  "clientId": "550e8400-e29b-41d4-a716-446655440003",
  "userId": "user123",
  "title": "Groceries",
  "amount": 2500.0,
  "currency": "LKR",
  "category": "food",
  "spentAt": 1674532800000,
  "createdAt": 1674532800000,
  "updatedAt": 1674532800000,
  "merchant": "Keels Super",
  "notes": "Weekly shopping",
  "paymentMethod": "card"
}
```

- `clientId` (String, required): Document ID.
- `userId` (String, required): Denormalised UID.
- `title` (String, required): Display label. The reader also accepts legacy `description` and `name` fields when `title` is absent.
- `amount` (Double, required): Always positive. Stored as `Double`; readers coerce `Long`/`Number` for legacy rows.
- `currency` (String, required): ISO code.
- `category` (String, required): Free-form category id.
- `spentAt` (Long, required): Epoch millis. Reader falls back to legacy `spentAtEpochMillis` / `receivedAt`.
- `createdAt` (Long, required): Epoch millis created locally.
- `updatedAt` (Long, required): Conflict resolution field.
- `merchant` (String, optional): Optional.
- `notes` (String, optional): Optional.
- `paymentMethod` (String, optional): Optional.

---

## `users/{uid}/subscriptions/{clientId}`

Recurring expense templates ("Netflix, monthly"). The local worker generates one-off `expenseLogs` from these. Defined by [`SubscriptionEntity`](../app/src/main/java/com/kahavanu/data/expenses/local/SubscriptionEntity.kt). Hard deletes.

```json
{
  "clientId": "550e8400-e29b-41d4-a716-446655440004",
  "userId": "user123",
  "title": "Netflix Premium",
  "amount": 799.0,
  "currency": "LKR",
  "category": "entertainment",
  "frequency": "monthly",
  "scheduledDate": 1677124800000,
  "lastGenerated": 1674532800000,
  "occurrenceCount": 12,
  "isPaused": false,
  "updatedAt": 1674532800000
}
```

- `clientId` (String, required): Document ID.
- `userId` (String, required): Denormalised UID.
- `title` (String, required): Display label.
- `amount` (Double, required): Per-occurrence amount.
- `currency` (String, required): ISO code.
- `category` (String, required): Category id.
- `frequency` (String, required): `"daily" | "weekly" | "monthly" | "yearly"` (case-insensitive read).
- `scheduledDate` (Long, required): Next due date, epoch millis.
- `lastGenerated` (Long, optional): Epoch millis of the most recent generated expense.
- `occurrenceCount` (Long, required): Total occurrences materialised to date.
- `isPaused` (Boolean, required): When `true` the worker skips generation.
- `updatedAt` (Long, required): Conflict resolution field.

---

## `users/{uid}/goalLogs/{clientId}`

Savings goals. Defined by [`GoalLogEntity`](../app/src/main/java/com/kahavanu/data/goals/local/GoalLogEntity.kt). Hard deletes.

```json
{
  "clientId": "550e8400-e29b-41d4-a716-446655440005",
  "userId": "user123",
  "title": "Emergency Fund",
  "targetAmount": 100000.0,
  "currentAmount": 45000.0,
  "currency": "LKR",
  "category": "OTHER",
  "targetDate": 1704067200000,
  "isCompleted": false,
  "isActive": true,
  "priority": 1,
  "createdAt": 1674532800000,
  "updatedAt": 1674532800000
}
```

- `clientId` (String, required): Document ID.
- `userId` (String, required): Denormalised UID.
- `title` (String, required): Display label.
- `targetAmount` (Double, required): Goal target.
- `currentAmount` (Double, required): Progress so far.
- `currency` (String, required): ISO code. Defaults `"LKR"`.
- `category` (String, required): Defaults `"OTHER"`.
- `targetDate` (Long, optional): Epoch millis. Nullable for goals without a deadline.
- `isCompleted` (Boolean, required): Latched `true` when `currentAmount >= targetAmount`.
- `isActive` (Boolean, required): Whether the goal counts toward the active dashboard.
- `priority` (Long, required): Integer (sent as Long by Firestore). Lower = higher priority.
- `createdAt` (Long, required): Epoch millis.
- `updatedAt` (Long, required): Conflict resolution field.

---

## `users/{uid}/smsSenders/{clientId}`

Trusted SMS sender allowlist for the on-device transaction parser. Defined by [`SmsSenderEntity`](../app/src/main/java/com/kahavanu/data/sieve/local/SmsSenderEntity.kt). **Soft delete** — deleted senders are written back with `isDeleted: true` rather than removed, so other devices observe the removal through the snapshot listener.

```json
{
  "clientId": "550e8400-e29b-41d4-a716-446655440006",
  "userId": "user123",
  "senderName": "HNB",
  "subtitle": "Hatton National Bank",
  "isEnabled": true,
  "isDeleted": false,
  "createdAt": 1674532800000,
  "updatedAt": 1674532800000
}
```

- `clientId` (String, required): Document ID.
- `userId` (String, required): Denormalised UID.
- `senderName` (String, required): Short sender ID (e.g. `"HNB"`).
- `subtitle` (String, required): Display subtitle / description.
- `isEnabled` (Boolean, required): Whether the parser should consume messages from this sender.
- `isDeleted` (Boolean, required): Tombstone flag. Documents with `true` should be filtered out.
- `createdAt` (Long, required): Epoch millis.
- `updatedAt` (Long, required): Conflict resolution field.

**Note:** Raw SMS content is never written to Firestore — only the sender allowlist is synced. See [Architecture › SMS analysis engine](architecture.md) for the on-device-only contract.

---

## Suggested security rules

The rules below match the per-user tree above. They have not been hardened beyond ownership checks; tighten field-level validation as needed.

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

## Adding a new collection

1. Define the Room entity, implementing [`SyncEntity`](../app/src/main/java/com/kahavanu/data/sync/SyncEntity.kt) (provides `localId`, `remoteId`, `isSynced`, `isDeleted`, `updatedAtEpochMillis`, `toFirestoreMap()`).
2. Write `toFirestoreMap()` on the entity and a `DocumentSnapshot.toXEntity(uid, remoteId, localId)` mapper on the read side. Field names must match in both directions.
3. Always include `clientId`, `userId`, and `updatedAt` in the map — the sync layer expects them.
4. Place the collection under `users/{uid}/<collectionName>` and document the schema in this file.
