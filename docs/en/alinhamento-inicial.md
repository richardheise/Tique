[Português](../alinhamento-inicial.md)

# FFortuna — consolidated understanding

Status: scope confirmed by the user; demonstration implemented. Discussion began on 21/10/2026. TypeScript, 30 tests, and Android bundle checked. The user approved the demonstration on a phone on 22/10/2026; the subsequent dependency fix awaits installation and verification on the device. Current execution and limits are in the [README](../../README.md) and [on-device guide](teste-no-celular.md).

## Objective and decisions

FFortuna honors the Roman goddess Fortuna; the first F stands for free, referring to free software. The goal is to organize the user's financial life around their sole Nubank account, savings pots, and credit already committed. Capture should be the primary entry point in real mode, automating safe cases and focusing review on exceptions; manual entry and import remain permanent alternatives.

| Decision | Outcome |
| --- | --- |
| Platform (Q1) | Expo/React Native, strict TypeScript, and Android Kotlin integration. |
| Financial model (Q2) | Minimal, balanced internal ledger without requiring accounting knowledge in the interface. |
| First environment (Q3) | Expo Go demonstration with synthetic data only. |
| License (Q4) | AGPLv3, identifier AGPL-3.0-only; original implementation. |
| Monthly view (Q5) | Forecast income separate from received income; spending by category, account balance, and savings-pot positions. |
| Credit (Q6–Q7) | One card with initial commitments by due date, without requiring reconstruction of old purchases. |
| Starting point (Q8) | Current position with a reference date, indicating partial history coverage. |
| Import (Q9) | File exported by Nubank, interpreted by the app, without spreadsheet editing or user column mapping. |
| Earlier history (Q10) | Explicitly review the period's opening and balance, using the current position for comparison without a hidden adjustment. |
| Persistence (Q11) | Kotlin owns the only database in real mode; TypeScript/expo-sqlite adapter is for the demonstration only. |
| Savings pots (Q12) | Save/Withdraw transfer funds; Update reported value records an explicit adjustment without assuming investment returns. |
| Demonstrated automation (Q13) | Include a safe synthetic fixture that posts automatically and an incomplete fixture that can be reviewed. This replaces the initial proposal of human confirmation for every event. |
| Locked screen (Q14) | The core can use the key without per-operation biometrics; interface lock is separate. |
| Retention (Q15) | The user delegated the choice; keep the 30-day default for raw notification content. |

Relevant ADRs: [platform](adr/0001-expo-com-captura-kotlin.md), [license](adr/0002-licenca-agpl.md), [financial ledger](adr/0003-livro-equilibrado-minimo.md), [native database](adr/0004-nucleo-nativo-dono-do-banco.md), and [background key](adr/0005-chave-para-segundo-plano.md). Vocabulary is in [GLOSSARY.md](../../GLOSSARY.md).

## First executable delivery: Expo Go demonstration

A small, persistent application in Brazilian Portuguese and BRL. Every field and data item in this phase is synthetic; a permanent demo indicator is shown, with no capture permission, arbitrary import of personal files, or loading of an unavailable native module.

1. Start a fictional account with a reported opening date and balance.
2. Enter forecast monthly income without automatically creating received income.
3. Manually add income/expenses; view balance, history, and a monthly category summary.
4. Enter positions by savings pot and type, with a reference date; save, withdraw, and update the position with distinct effects.
5. Enter existing card commitments by due date; view monthly total/distribution and record a payment without creating another expense.
6. Simulate unmistakably fictional evidence: one safe item creates a transaction automatically; another incomplete item creates a candidate for review of amount, type, description, category, and applicable destination.
7. Keep the link between transaction and evidence, show why automation/review occurred, and preserve corrections when reprocessing.
8. Demonstrate import using a project-provided synthetic file, with automatic recognition, preview, confirmation, and handling of duplicates/ambiguities. OFX is the first technical target, subject to later validation of what Nubank actually exports.
9. Demonstrate history before tracking began, explicitly reconciling the opening without counting the current balance again.
10. Restart and preserve data, links, and decisions.

The interface has a simple summary, history, review, and compact forms; use standard Expo Router navigation if suitable for the selected SDK. Do not build elaborate charts or a complete rule editor. Uncertain categories may remain To classify. Deterministic rules precede any future attempt at intelligence; there will be no AI in this delivery.

The demo expanded to include the requested financial view, but uses manually reported positions and aggregated commitments. A full bill/installment engine, new installment purchases, recurring items, budgeting, advanced reconciliation, 30/60/90-day projections, shared expenses, bill/PDF import, and investment accounting are future increments. Additional formats are not automatically in scope.

## Financial rules and coverage

BRL uses integer cents. Monetary input is interpreted as decimal digits without floating-point currency conversion. Validate safe limits and every sum before writing; every value and accumulator crossing TypeScript must be representable as a safe integer. The Kotlin core must also validate operations and conversions, without relying only on the capacity of Long or SQLite INTEGER.

Every confirmed transaction has balanced counterparts. Opening balances, initial liabilities, and adjustments have their own counterparts; they are not salary or consumption. The interface does not expose a configurable chart of accounts. Card payment reduces cash and liability; own-account transfer, deposit, and withdrawal are not expenses; redemption and reversal are not ordinary income. Unsupported types remain in review rather than being conveniently converted to income/expense.

Savings-pot positions are reported values, without a guarantee of withdrawable net value or liquidity. Save/Withdraw are asset movements. Update reported value calculates and shows the difference from the tracked position, considering known movements, before recording an explicit adjustment. Do not infer returns, taxes, or the source investment when evidence omits them.

Existing card commitments do not create new spending in the month they are entered. Without original purchases, their category distribution is unknown; due date is not the consumption date. The summary identifies coverage limits and does not calculate available bank credit or financing costs.

Quick start uses the current position; having no records does not mean spending was zero. Adding earlier history requires a new explicit opening and reconciliation against the current position already reported. Differences remain visible for resolution; no income, expense, or balance correction is created silently. An imported statement does not guarantee complete period coverage.

## Shared processing contract

The demonstration, native capture, and import share the same observable contract for input, processing, and results. Parsers are source-specific; the simulator does not pretend to be a real Nubank notification.

- Observed event: local identifier, source, identity available from the source, revision, publication, capture, and content/evidence. Effective time is a separate field and may be unknown.
- Parsing result: parser version, extracted fields, evidence references, and confidence per field; it does not overwrite the original observation.
- Candidate: revisable proposal with no effect on the balance, including ambiguities and insufficient fields.
- Confirmed transaction: human or automatic decision, counterparts, metadata, and source link. Corrections have their own provenance, separate from extraction.
- Reconciliation: link between evidence items for one movement, without deleting the source or counting its effects again. Advanced reconciliation comes later.

Automation requires verifiable deterministic criteria for movement, amount, type, applicable account/source/destination, and conflict-free identity. Unknown effective date must not silently be replaced by publication time; insufficient time information for a safe posting requires review. An uncertain category alone does not block confirmation. Rules and reasons will be visible; fixture-derived probabilities will not be claimed as calibrated.

Re-delivery of the same observation/revision returns the existing result; a new revision stores a new interpretation. A contradictory update preserves confirmation/corrections and opens a conflict for review. Two identical purchases with distinct identities are preserved. A notification and statement for the same movement require linking, not a second transaction. Amount/day/description are not enough to exclude records. Dismissing a notification does not delete a transaction; a group summary does not become an independent purchase.

Confirmation is an idempotent command: it writes counterparts, link, and processing state in the same database transaction. Uniqueness constraints and revision comparison support this guarantee beyond button disabling. Failure before commit leaves no partial transaction; retry after commit returns the confirmed result. Updates to financial fields must not unbalance the ledger or replace prior decisions without an audit trail.

## Persistence, modules, and the second phase

Modular monolith: financial domain, capture, parsing, classification, persistence, and interface have separate responsibilities. Projection/reconciliation boundaries are anticipated; do not create empty modules or infrastructure just to anticipate the roadmap.

In Expo Go, a TypeScript adapter runs the synthetic flow using expo-sqlite, with versioned migrations, serialized write commands, transactions, and integrity constraints. In the real implementation, Kotlin owns a single SQLCipher Community database, migrations, durable capture, recoverable processing, and financial operations. The interface calls typed operations through Expo Modules API; it does not open another ledger. Schema migration finishes before commands/processing are enabled. Migration/key failure blocks writes and must be visible.

Prefer evidence and ledger tables in the same encrypted native database, avoiding a cross-database protocol at this stage. Persist the observation first, then process it, with recovery from persisted state and uniqueness. Kotlin and JavaScript must not compete as independent writers to the real database. The demo adapter is not synchronized with real mode; the same scenarios should check contract equivalence without claiming equivalence to Kotlin before its tests are implemented and run.

After the demonstration, run a native proof on a physical phone: local Android build, without EAS/Expo account, Kotlin/Expo Modules API module, and NotificationListenerService. Explicit consent, database selection, Nubank package confirmed on-device, filter before storing content, and visible authorization/connection status. Do not use expo-notifications to read external notifications or depend on events sent only to JavaScript. No permanent foreground service for convenience.

The proof starts with synthetic notifications from a test emitter and encrypted storage. Real samples only after the security conditions below are met. Validate Nubank content, including redemptions, before enabling automation for each pattern. Missing fields or notifications remain limitations; do not promise lossless capture. This proof comes before expanding dashboards, investments, or additional importers.

## Nubank import without technical preparation

In real mode, select a file exported by Nubank, recognize the supported format, and show a financial preview. Do not ask users to write a parser, convert a spreadsheet, adjust separators, or map columns. An unrecognized file gets a clear message; do not try unsafe generic rules. The first delivery exercises this path with a synthetic fixture and no arbitrary file picker in Expo Go.

OFX is the first target: the [NuCommunity announcement](https://comunidade.nubank.com.br/novidades/post/exporte-extratos-diretamente-de-sua-conta-nubank-pelo-app-rbRYnw8qndyPd2S) describes PDF/OFX for personal and business accounts; the [Nubank blog](https://www.blog.nubank.com.br/extrato-bancario/) describes requesting and receiving a statement. This does not prove the format of this user's current file. No Nubank schema was found and no real file was validated. Section 3.2.3 of the [OFX specification](https://www.financialdataexchange.org/common/Uploaded%20files/OFX%20files/OFX%202.2.pdf) defines FITID within an account/institution; its presence/stability in Nubank exports requires local validation.

Repeating the same import does not duplicate transactions. Overlapping periods use validated stable identity or assisted comparison; conflicts are not discarded. The preview separates new, already linked, and ambiguous records, preserves metadata/corrections, and shows invalid rows. An amount signal does not establish financial type. Card payments, deposits, and redemptions need the appropriate links; they do not become spending/income by default.

## Security and offline operation

Local core, with no backend, external account, subscription, bank credentials, telemetry, remote/local AI, SMS, private Nubank API, scraping, accessibility-service use, or banking automation. Do not send notifications/statements to third parties. Opening an external URL and obtaining development dependencies are separate from the core operation.

In real mode, generate a random SQLCipher key protected by a non-exportable wrapping key in Keystore. The core may use the key without per-operation biometrics while the screen is locked, after the first unlock following reboot. The database key must be used in native memory. Biometrics protect the interface separately; do not confuse that with encryption. Do not promise capture before first unlock or while the system prevents the service.

Before any real data, including diagnostics, verify encryption of the database/evidence/required attachments; no plaintext financial logs/caches/temporary files; incompatible automatic backups blocked; password-encrypted portable backup independent of Keystore and using authenticated encryption; restore without the original key; and handling of wrong password/lost key without overwriting data. Choose and validate a maintained derivation/encryption library in the native phase. SQLCipher does not automatically protect every app file. Import must avoid the picker's default cache copy for real data; the original file outside the app remains under the user's control.

Retain raw notification content for up to 30 days, including pending items, removing expired content on the next run after inactivity as well. Do not promise cleanup during force-stop. Preserve financial records, decisions, required fields, and links; full reprocessing requires raw data still to be available. Portable backup excludes raw content by default. Diagnostics are local, temporary, and encrypted; export is explicit, anonymized, and encrypted. External exports remain under user control. Never put real data, keys, or credentials in Git.

Release APK goal: remove INTERNET and verify the final manifest and offline operation. Metro/development builds may need a network; downloading dependencies does not require the final APK to have a backend, EAS, or Expo account.

## Checks and completion of each phase

First delivery: test BRL parsing/formatting and limits; income/expenses and balances; balanced counterparts; opening/savings pots/adjustments/payments without inflating income or spending; forecast income without automatic credit; synthetic automation and review; repeated confirmation; reprocessing; two identical purchases; conflicting update/preserved corrections; reimport/overlapping periods; historical opening with a difference; persistence and migrations after reopening the database. Test the adapter's actual SQLite implementation as well as the pure domain.

Native proof: locked screen, Doze, battery optimization, reboot and first unlock, process termination, force-stop and its limits, revocation/regrant, grouping/updates, and recovery after interruption. Validate encryption, key/backup loss and restoration, retention, source filters, and operation without an active JS runtime. Record device/Android and distinguish tests written, executed, and unavailable.

Declare the first delivery ready only after available checks and the on-phone test script, with exact commands actually validated, results, and uncertainties. If the device is unavailable, do not claim an on-device test. Runnable commands and results for this delivery are in the [README](../../README.md); physical validation remains pending.

## Investigation and known limits

The workspace started empty, with a local Git repository on main. Completion and publication status is in [continuity](continuidade.md). No code from the references was copied. Applicable RTK.md and AGENTS.md instructions were consulted; no additional AGENTS.md was found in the app folder. Official skills read during revision [5beb35b](https://github.com/mattpocock/skills/tree/5beb35b040e3de97e54fa9e386d7e42d15c295c2): grill-with-docs, dependency grilling/domain-modeling, and ADR/GLOSSARY formats. There is no Skill tool in this session; files were loaded by explicit reading.

| Source | Finding and limitation |
| --- | --- |
| [Finan+ 36379aad](https://github.com/finanplus-web/finan_plus_android/tree/36379aad7921f20bb1cc1903fce5f57e045792f3) | GPL-3.0-or-later, Kotlin/Compose, Long cents, separate accounts/cards, and bill/projection tests. Model has income/expense exceptions and an encrypted file; do not adopt it wholesale as ledger/SQLCipher/capture. No reference tests were run. |
| [PennyWise 7a3ac7b](https://github.com/sarim2000/pennywiseai-tracker/tree/7a3ac7b4742dc320de9cb6e73a8ea70b1c9b695e) | AGPL v3, separate parsers, Room evidence, and tests; content/window deduplication can merge legitimate purchases. Nubank/PT-BR support is unproven; SMS/AI and other integrations exceed this scope. No reference tests were run. |
| [Expo SQLite](https://docs.expo.dev/versions/latest/sdk/sqlite/) | Persistent SQLite in Expo Go; SQLCipher requires a custom build. PRAGMA key may be ignored without SQLCipher: verify it is present before real data. |
| [Local build](https://docs.expo.dev/guides/local-app-development/) and [Modules API](https://docs.expo.dev/modules/overview/) | Local Android build and Kotlin integration are supported; Expo Go does not run a custom module. Full compatibility has not yet been compiled. |
| [DocumentPicker](https://docs.expo.dev/versions/latest/sdk/document-picker/) | Available in Expo Go, but default configuration copies a file to cache without an encryption guarantee. |
| [Keystore](https://developer.android.com/privacy-and-security/keystore) and [SQLCipher Community](https://www.zetetic.net/sqlcipher/community/) | Basis for the native design; they do not replace tests for protection of files, keys, and restoration. |
| [Nubank notifications](https://comunidade.nubank.com.br/novidades/post/por-onde-andam-as-notificacoes-do-app-vem-ca-ver-DN9VtTNRy1FrB8O) | Communication exists about transaction notifications, without a schema/guaranteed coverage of redemptions and fields. Package and samples have not been validated on-device. |

Initially inspected environment: Node 26.8.2, npm 11.19.1, pnpm, Java 27; no adb/sdkmanager on PATH and no Android SDK in usual locations. Prepare a compatible Android toolchain and JDK before the native proof. [React Native guidance](https://reactnative.dev/docs/next/set-up-your-environment) recommends JDK 17; prefer a supported Node LTS.

The [SDK 58 announcement](https://expo.dev/changelog/sdk-58) reports the Expo Go store transition. The project version must match the Expo Go actually in use; recheck official versions before installing, pin a compatible set, and do not mix latest packages with an earlier SDK. This is technical work and does not require a user version choice.

AGPLv3 was chosen for the original app. Conceptual inspiration, component reuse, and derivative work are different situations: the chosen license does not authorize copying code/assets/marks. Check material, compatibility, and notices before any reuse. LICENSE, README, and gitignore are included; the inventory and its limitations are in [third-party notices](../../THIRD_PARTY_NOTICES.md).

## Next step

Run the [phone demonstration guide](teste-no-celular.md). The immediate next increment is the native proof with a local Android build and encrypted persistence; real data remains blocked until the security conditions are met.
