[Português original](../previa-brl.md)

# BRL value preview — 0.6.0 procedure

This procedure describes content version 0.6.0, included in the Tique-branded 0.6.1 build (versionCode 7). The new branding preserves the application ID and data from the previous app to allow an update. Build 0.6.1 was published and functionally approved by the user on 30/10/2026, with no individual counters reported. In 0.7.0, also follow the categories and tags procedure; the battery then grew to 60 checks.

Version 0.6.0 interprets the original text of an observation into a reviewable preview within encrypted diagnostics. No reviewed samples are available in this environment to support Nubank templates. The `notification-brl/1` parser is generic: it recognizes values explicitly marked with `R$` or `BRL`, using a comma and two decimal places, without guessing the movement.

Examples in this document and the tests are invented; they do not represent real Nubank messages. `R$ 99,90` becomes 9990 integer cents. Thousands grouped with a period and Unicode spaces between the marker and number are accepted. Numbers without a marker, incomplete/foreign formats, negative values, zero, and values above 9,007,199,254,740,991 cents are not selected. Multiple possibilities, repetition in the same field, or truncation leave the value unknown; identical repetitions in different fields may represent the same expanded text. Evidence shows the original snippet and the field that supported the interpretation.

Nature, account, and effective date are not inferred. A message with a price might also be an offer or a balance; finding BRL does not prove a transaction. Pix is a payment method, not income/expense. Transfers between one's own resources, bill payments, reversals, contributions, or refunds must not be classified as ordinary income/expense. The interface lets the nature remain undefined or be set to “Other” in this preview; none of these fields changes the balance.

## Test on the A36

1. Update over 0.5.0 without uninstalling. The migration preserves originals, the key, and export reviews; it pauses the old session because the previous consent did not cover automatic interpretation. In **Advanced settings**, run the **48 isolated native checks**. Report counters/codes only. Previous-version counters do not replace the eight new scenarios.
2. In an existing individual observation, tap **Interpret original**. It should show the text with the real value, parser version, evidence snippet, reason, and candidate value. If there are multiple possibilities or an unrecognized format, the field remains empty for review. Group summaries do not offer this action.
3. Check that nature and effective date start undefined. Correct the amount, nature, description, and category; enter an effective date only if known. **Save review** stores these encrypted fields separately from the parser result. Unknown fields may remain blank; this does not confirm a transaction. You can also mark a candidate as dismissed, preserving the original.
4. Tap **Reprocess** after saving. The parser's raw result remains visible, but your correction must remain in the form. Another candidate for the same observation must not appear. Close/reopen the panel and app: the saved review remains during retention. Unsaved edits prevent reprocessing; going to the background clears the interface edit.
5. If another observation arrives with the same Android identity, the interface shows the relationship and preserves the earlier review. It could be an update or another legitimate movement; the app does not conclude they are the same transaction or financially sum/deduplicate them. Observations with equal content but distinct identities remain separate.
6. For new notifications, consent to a new session after the 48 checks. The new notice describes preliminary interpretation. Wait for a notification during normal use, including with the interface closed/screen locked. On return, the list should show when the preview was interpreted; processing occurs in Kotlin after the evidence is saved and does not depend on open JavaScript. Use **Interpret original** to check and correct it.
7. Check the separation: **Prepare copy for export** still replaces recognized amounts with `R$ 12,34`, while parser preview uses the original. Neither previews nor their corrections are included in that export or synthetic backups. There is no need to repeat the 0.5.0 password/backup procedure, already approved, unless you find a new failure.

**Previews and corrections at this stage are temporary: they expire with the diagnostic observation seven days after capture, when the core runs. They are not permanent financial history.** Do not use the review as the only record of your finances. We have not created a second ledger or put real data in the Expo Go database. The next step depends on validating these interpretations and obtaining synthetic fixtures based on reviewed samples for specific rules before adding a real financial ledger.

## Local verification and limits

```sh
nvm use
npm ci
npm run check
npm run native:build -- --compile-device-tests
```

The build remains limited to 12 GiB and up to 16 CPUs. No new dependency, remote service, or permission was added. The APK is offline, with the same signature and SQLCipher/Keystore data. Expo Go keeps the synthetic demo and cannot run this module.

Relevant tests cover monetary limits, UTF-16 evidence, incomplete/ambiguous values, original versus mask, migration 1→2 preserving key/data, reopening, corrections after reprocessing, updates, and cascade cleanup. JVM tests run in the build environment; Android scenarios compile and share callbacks with the interface battery, but the JUnit runner was not run on this server. Do not claim Nubank parsing validation or complete capture. Never send originals, passwords, or financial information in this conversation.

Decision and alternatives: [ADR 0009](adr/0009-previa-brl-no-diagnostico.md). Collection/export policy: [local diagnostics](diagnostico-local.md).
