[Português](../../adr/0011-primeiro-livro-financeiro-real.md)

# First real financial ledger and joint recovery

Scope authorized by the user after approving 0.7.1: one BRL Nubank account, manual income/expenses and explicit confirmation of notifications, permanent history, balance, monthly summary by category, and encrypted recovery. The user then proposed loading data exported manually from Nubank. This step includes user-reported opening balance and balance checks; file import remains the next increment, dependent on validating formats actually available, without promising a Nubank layout.

## Storage and ledger

`CaptureRuntime`'s serial executor remains responsible for migrations, commands, and reads. The existing catalog SQLCipher database, `ffortuna-classifications-v1.db`, moves from schema 1 to 2 in a transaction, creating `finance_*` tables. The legacy name, wrapped key, and category/tag IDs are preserved. There is no second financial ledger and no sync with the Expo Go demo. Reusing the catalog database allows selection, posting, counterparts, and source link to be written atomically with foreign keys. A new separate database would require coordinated validation and recovery without a concrete benefit in this release.

Only one account is available. Opening has a date and signed amount, including zero, with its own counterpart; it does not count as income. Income has a positive cash/negative income pair; expense has a negative cash/positive expense pair. The interface does not present a chart of accounts. Transfers, cards, bills, contributions, withdrawals, reversals, and refunds are outside this flow until they have their own treatment. Pix does not determine nature.

Cents are integer `Long`, up to `9.007.199.254.740.991` in magnitude. Movement amounts must be positive; opening and observed balance may be negative or zero. `BigInteger` validates totals, balance limits at the end of each date, and monthly aggregates before commit and exposure to JavaScript. There is no floating-point monetary addition or SQL `SUM` subject to overflow. Effective dates are canonical days provided by the user, never filled with notification publication. New movements before opening or after today are rejected; there is no projection in this phase.

Opening balance must precede the movements to be recorded from that date. Re-entering operations already included in that balance would duplicate their effects; the interface explains this. Balance checks are independent dated records compared with the calculated balance through the end of that date. Differences stay visible and do not create adjustments, income, or expenses.

## Confirmation and corrections

A manual command has its own UUID and unique key. Repeating the same command returns the existing entry; different fields with the same key create a conflict. Two identical purchases with distinct UUIDs remain independent. Notification confirmation uses the observation UUID as its unique key, not amount/day. The entry, two counterparts, categories/tags, approved fields, and minimal provenance are written in the same commit. The canonical request uses IDs and sorted tags, so it can be repeated after renaming a category without overwriting the earlier decision.

No distributed write is needed in diagnostics: when reopening a candidate, the ledger checks its unique link. Interruption between commit and response does not cause another post; reprocessing the notification replaces only parsing. The confirmed financial review takes precedence in display. Another Android revision may be an update or another purchase; it remains a distinct observation with a human-review notice, without presumed financial deduplication.

The user may void an incorrect record with a reason. Voiding preserves the entry, balanced pair, source, and cancellation date/reason; the record is excluded from active balances and totals but remains visible in history. Repeating the void does not change the ledger or reactivate the record. This is not a bank reversal and does not operate in Nubank. To correct amount/nature, void and enter the correct one. A confirmed transaction is not silently changed. Opening cannot be voided: “Restart ledger” creates an empty vault with the same catalog, preserving the current one as previous and requiring new setup.

## Provenance and retention

The diagnostics inbox remains temporary and separate. Confirmation copies only observation ID, package, Android identity/revision, publication, capture, parser version, extracted-field evidence and original confidence, together with approved financial fields. It does not copy the title/expanded text/lines/raw payload set into the ledger. Approved description is a permanent financial field and may contain text the user chose to keep; it is not claimed to be anonymized.

Observation, parsing, and temporary review remain subject to diagnostics' seven-day retention. Clearing diagnostics or dismissing a notification does not delete entries or minimal provenance. The ledger and approved descriptions persist independently of that period. Capture remains optional and limited to the diagnostic session; there is no automatic posting or claim of statement completeness or support for Nubank templates.

## Backup and restore

Internal format `tique.financial-book/1` is distinct from synthetic backup. It includes the full catalog (including archived options), opening, entries, pairs, tags, checks, voids, and minimal provenance, without raw diagnostics, attachments, keys, or credentials. The portable envelope reuses `BackupCodec`: password-authenticated encryption independent of Keystore; only ciphertext is passed to Android's document provider. Invalid files, another format, broken references, unbalanced pairs, unsafe values, or unknown structures are rejected before activating restoration.

`FinanceBoxes` prepares a SQLCipher database with a new key, validates content, and switches the active pointer in one preferences commit. The previous vault becomes inactive, available to switch to or explicitly discard; there is no merge. A new restore is blocked while the previous vault exists. An interrupted stage is removed on resume without switching the active vault. Losing the active key does not prevent restoring a valid portable backup with another key; old material is preserved. Do not recreate a missing database or key over existing data.

Local recovery reuses `LocalRecoveryStore` storage and envelope with a financial-contract validator and its own namespace/key: up to three copies, at most one per 24 hours of use with changes. Old synthetic APIs remain available. An empty financial copy is not written before opening is configured, to avoid consuming the first daily window; the first automatic copy includes opening. Private copies and Keystore do not survive uninstall; portable backup saved outside the app may survive. Recovery failures remain visible without undoing a confirmed entry. The policy does not promise a copy after every individual write.

Limits for this version: up to 10,000 movement records including opening and voided items, 1,000 checks, 200 catalog options, 20 tags per movement, and an 8 MiB snapshot (the byte limit may be reached before the record limit). A write that exceeds backup capacity is rejected atomically; history is not deleted to make room. Monthly history initially shows 50 items, with “Show more.”

SQLCipher/Keystore, disabled Android backup, `FLAG_SECURE`, no INTERNET in release, and no new dependency/service are retained. Metro, downloads, and opening URLs belong to development or external actions, not the APK's offline core. Native verification scope becomes `isolated-native-checks/v5`, 72 checks: 70 callbacks shared with Android JUnit, cleanup, and a bounded window of own logs. Compiling the callbacks is not the same as running them on-device.
