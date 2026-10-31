[Português original](../prompt-sol-uso-diario.md)

# Prompt for Sol — daily use, imports, and analytics

Prepared on 31/10/2026. This document is an instruction for the next implementation, not a record of delivered features.

---

You will continue development of **Tique**, a free-software Android app for tracking financial movements. Work in the existing repository `/home/heise/FFortuna`, remote `richardheise/Tique`. Communicate in Brazilian Portuguese. Implement the first delivery described below and complete its verification; later ideas are references, not a requirement to implement everything now.

## Context and objective

The user uses Nubank and a Samsung A36 SM-A366E/DS running Android 16. They want to track money movements without exactly reproducing the bank balance. Statements are optional, but the user wants to export/share a file directly to Tique without manually transforming data.

The published reference version is `v0.10.0-alpha.1`, versionCode 13. Check the repository's current state before assuming this is still the latest version. The user reported that importing worked, but the dashboards did not change afterward. They also considered the Sources tab unnecessary for everyday use and proposed keeping only Movements and Analytics.

Today, importing creates pending candidates; charts read only confirmed transactions for the selected month. This explains unchanged charts while items remain unconfirmed, but it does not prove there is no refresh defect after confirmation. Investigate both paths. The user's report is not approval of all version tests.

The goal of this delivery is to make **import → review in a batch → record → view in analytics** a short, understandable flow without sacrificing financial correctness.

## Before changing anything

- Read the global instructions, `/home/heise/.codex/RTK.md`, applicable `AGENTS.md` files, and Git status. Preserve existing changes.
- Read the current sections of [continuity](continuidade.md), [ADR 0012](adr/0012-uso-diario-e-entrada-de-extratos.md), [interface proposal](design/tique-interface-proposta.md), [`GLOSSARY.en.md`](../../GLOSSARY.en.md), and the latest release notes. Historical documents do not supersede the priorities in this prompt.
- Inspect the code and verify testable assumptions without asking the user. Resolve routine choices with your judgment. Ask only if an ambiguity changes financial meaning or puts existing data at risk.
- Do not recreate the project, change the stack, or update dependencies for convenience. If an API needs research, consult current official documentation.

## Required first delivery

### 1. Two everyday tabs

- **Movements** is the start screen, including on a new installation. Offer starting manually or importing without requiring an opening balance, statement, or notification authorization.
- **Analytics** is the second and only other primary tab.
- Move Sources to settings as **Data and capture**, with import, authorization, start/pause, connection state, and reconnection. Import must also remain accessible through Android sharing; a contextual shortcut on the `+` button is acceptable.
- Keep a coherent movement list with **All**, **Needs review**, and **Uncategorized** filters, in addition to useful existing filters. Distinguish pending nature/amount from a missing category only. A confirmed transaction without a category contributes to totals.
- When confirming a candidate, show it as recorded without a second row for the same movement. Evidence linked to an existing record also must not create another movement.
- Preserve period, filters, and list position across tabs/details, without weakening existing protection when the app goes to the background. Switching/restoring the ledger must invalidate corresponding state.
- Incoming shares during editing must not discard the form. Respect Android back, keyboard, safe areas, empty states, and busy buttons.
- Show capture state discreetly; relevant interruptions should offer an action to resolve them. Do not create a permanent technical screen in the normal flow.

### 2. Import preview and batch review

- After a valid read, show the file period, item count, already known/linked items, and pending items. Use clear terms: “Received” or “Needs review” does not mean “Recorded.”
- Offer multi-selection, joint editing of nature/category/tags when applicable, and **Confirm selected**. Showing amount, date, and description should allow review without opening each row.
- No broad selection may confirm nature based only on the file's positive/negative sign. The user's action must make explicit the nature applied to the selected items. Ambiguous or conflicting items must not silently enter the selection ready for confirmation.
- Valid amount and date, supported nature, and no conflict are prerequisites for confirmation. An unknown category may remain **Uncategorized**.
- Transfers between the user's own resources, contributions, withdrawals, bill payments, reversals, and refunds must not be automatically reduced to ordinary income/expense. Until corresponding financial support exists, keep them identified for review and out of those totals without deleting them. Do not implement a complete card/investment core to solve this delivery.
- Link suggestions should allow associating a statement with a transaction already recorded from a notification or manually. Equal amount and date do not prove duplication. Do not discard two legitimate purchases with the same amount/date.
- Reimporting, double tapping, resuming after failure, or reprocessing must not duplicate entries or lose corrections. Resolve concurrency and idempotency in the native core, not only by disabling a React button.
- Prefer one atomic native operation for the selected set: validate before writing and preserve selection/corrections if it fails. If sets need to be split, document and implement recovery using stable identifiers, with an unambiguous result of what was confirmed. Do not report full success after a partial result.
- The operation result must indicate recorded, linked, and still-pending items. Do not expose internal details or financial content in logs.

### 3. Refreshed analytics and explicit period

- Individual or batch confirmation, linking, cancellation, and relevant correction must refresh data consumed by both tabs. Audit races among refresh, navigation, foreground return, and native command completion.
- After confirmation, offer **View imported period**. If the file spans multiple months, allow choosing one of the months present, without inventing a range filter the core does not support.
- Keep the period visible and show how many pending items are outside the totals. Do not claim a month is complete merely because a file was imported.
- Charts remain based on the confirmed ledger and must not mix provisional values. **Uncategorized** items count in totals; uncertain nature does not.
- Tapping a category should open movements for the same period and filter. Preserve the option to hide amounts.
- Also reproduce the case of a transaction already confirmed in the displayed month: if it does not appear, fix the actual refresh/query failure rather than attributing it to a pending review.

## Ideas for later increments

Briefly record these as backlog; do not implement all of them in this delivery:

1. **Reclassify confirmed records**, individually and in batches, changing category/tags without cancelling and recreating the transaction. Keep original evidence and correction audit. Distinguish metadata changes from changes to amount/nature/date.
2. **Reusable local rules**, offered by explicit choice while correcting a classification. Start with counterparty/merchant and category, with match preview, editing, and deactivation. Do not turn a category rule into proof of amount, nature, or identity. Reprocessing preserves human corrections.
3. **Additional analytics**: comparison with the same part of the previous month, largest categories/merchants, and amount still uncategorized. Indicate incomplete coverage; no records do not prove there were no expenses. Avoid charts that repeat the same information.
4. **Dedicated financial handling for transfers and savings pots**, followed by cards/statements, before automating these cases. Bill payment does not repeat an expense; a withdrawal is not salary; a contribution is not consumption.

Conceptual references: [Actual imports and duplicates](https://actualbudget.org/docs/transactions/importing/) and [Actual rules](https://actualbudget.org/docs/budgeting/rules/). Examine the sources if decisions need to be based on them. Do not copy code, assets, or brands; do not adopt deduplication heuristics that violate our invariants.

## Architecture, data, and interface constraints

- Preserve Expo/React Native/strict TypeScript, Kotlin, native processing independent of JavaScript, SQLite/SQLCipher, and Keystore. One financial ledger; separate evidence vault with idempotent consumption.
- BRL values are integer cents with safe limits. Do not use floating point for financial calculations. Notification publication/capture is not the transaction's effective date.
- Preserve IDs, sources, parser version, field evidence, corrections, links, and installed data. Migrations must work on existing databases; keep read compatibility for previous backups. If adding persistent data, explicitly assess backup inclusion, restore, and limits.
- Do not change package/application ID, key aliases, or signing identity for navigation changes. Do not ask for uninstall or data clearing.
- Local/offline core, no backend, telemetry, bank credentials, private APIs, scraping, accessibility, SMS, or remote/local AI. Release APK remains without INTERNET. Metro/development build is distinct from the app build.
- No real data, statement, key, backup, or financial payload in Git/logs/plaintext temporary files. Fixtures must be explicitly synthetic and must not claim to reproduce real Nubank notifications.
- Preserve the Expo Go demo as exclusively synthetic; do not load an unavailable native module or offer real imports there. Do not present web validation as APK testing.
- Concise interface, accessible icons, and short labels when needed. Black `#080B10`, orange amber `#FFB020`, cyan `#9DECF4`, off-white `#F3EFE6`; squared shapes and small corners. Avoid elaborate new dashboards and unnecessary visual dependencies.

## Starting points for investigation

Interface: `../../src/capture/CaptureScreen.tsx`, `FinanceNavigation.tsx`, `SourcesPanel.tsx`, `DailyInbox.tsx`, `CandidatePanel.tsx`, `FinancePanel.tsx`, `FinanceDashboard.tsx`, types/contracts, and pure dashboard functions.

In module `../../modules/ffortuna-capture/android/src/main/java/org/ffortuna/capture/`: `CaptureRuntime.kt`, `FinancialLedger.kt`, `ClassificationCatalog.kt`, `FinancialBackup.kt`, `StatementParser.kt`, `StatementStaging.kt`, `StatementIntake.kt`, `CandidateStore.kt`, `DiagnosticStore.kt`, and Expo bridge. Investigate dependencies before editing; these names are entry points, not a required file list.

## Verification and delivery

- Cover relevant behavior: pending import does not change totals; batch confirmation updates the correct month; missing category does not exclude a transaction; retry/resume does not duplicate; legitimate equal purchases remain distinct; linking does not repost; conflicts do not confirm silently; corrections survive; batch failure leaves no partial financial state.
- Check updates after confirmation when returning from the background, files from different months, persistence, migrations, and restore if affected. Preserve existing financial and privacy tests.
- Use existing scripts: `npm run check`, `npm run native:test`, and `npm run native:build -- --compile-device-tests`, as appropriate. Node 24 is installed at `/home/heise/.nvm/versions/node/v24.20.0/bin`. Inspect scripts/environment before running; the existing build limits resources to 12 GiB and 16 CPUs. Do not run native builds in parallel.
- Distinguish tests executed, tests only compiled, and physical checks pending. The user wants to use the app normally without another mandatory manual battery. Do not invent Samsung results or claim full Nubank support based on fixtures.
- Update continuity notes and usage instructions. Create an ADR only for a relevant new persistence/semantics decision; do not create empty or excessive documentation.
- Finish with clear commits and push under existing authorization. Follow the documented release process: new version/code, same signature, offline/backup/permission inspection, AGPL and third-party notices, matching source, hashes, and update APK. Never overwrite tags or assets of published versions. Publish the prerelease only after appropriate checks; if something blocks it, record the exact blocker and preserve the work.
- In the response to the user, briefly say what changed, what was actually verified, relevant limits, and the APK link if published. Do not declare these proposals ready before implementing them.
