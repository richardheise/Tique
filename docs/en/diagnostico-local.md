[Português original](../diagnostico-local.md)

# Local diagnostics — 0.5.0 procedure

Starting with 0.6.0, see the [BRL preview procedure](../en/previa-brl.md): the migration pauses the old consent, and new collection requires 48 checks. The text below records the original 0.5.0 delivery, already functionally approved by the user; export review remains separate from the real-value preview.

This delivery allows collecting real samples only for later parser development/verification. It does not interpret movements, create transactions, or calculate balances. The financial ledger in Expo Go and the proof-of-concept examples remain entirely synthetic. Nubank parsing support remains unvalidated.

## Default policy

Installation/update starts with real-data collection disabled. In advanced settings, the user checks the app found on the device, its `com.nu.production` package, version, and local signature, then chooses to start. The signature records the installed source and helps detect changes; it is not publisher certification. Package lookup is restricted to that source and does not enumerate all apps. No new broad-access, SMS, accessibility, or INTERNET permission is requested.

Each session lasts up to 24 hours, using wall-clock and monotonic clocks. Rebooting the phone, source unavailability, signature change, a limit, or revoked access stops collection. Moving the clock to before the start also invalidates the session; moving it later does not extend the monotonic limit. Killing/recreating only the process may continue the session on the same boot, without JavaScript active. Force-stop may prevent capture until the app is reopened; missed notifications are not promised to be recovered.

There are at most 200 retained observations and 20 selected reviews. Notifications must have been published after the start; there is no retroactive history collection. Source, consent, duration, and signature are checked before reading text. Diagnostics allow all notification text from that source during the window, including nonfinancial messages; there is not yet a reliable parser to filter movements. Notifications from other apps are not read.

The SQLCipher vault has its own key wrapped by Keystore. It preserves publication time, capture time, identity, reviews, and permitted text fields: title, text, expanded text, subtext, and lines. Limits are 4096 characters per field, 32 lines, and 16,000 characters total. Images, actions, full objects, RemoteViews, and arbitrary extras are not serialized. Group summaries store metadata only. Empty text may reflect what Android delivered; it is not proof that no notification existed or authorization to bypass system protection.

Originals and reviews expire seven days after capture when the core runs. Cleanup applies only to this vault. **Stop** retains records; **Delete temporary records** removes the diagnostic vault/key/reviews and does not affect examples, local copies, or synthetic backups. There is no automatic backup of diagnostic raw data; losing it does not change a financial ledger. Internal copies/reinstallation do not restore these samples. Files exported outside the app are under the user's control and are not removed by internal retention.

## Test on the A36

1. Update over 0.4.0 without uninstalling. Open **Advanced settings** and run **Run isolated native checks** before enabling real collection. Starting a session is blocked until this version's test battery passes in the current process; starting another session after ending the process requires a new battery. An already active session may continue within its original window. The 0.5.0 battery has **40 checks**, including ten new scenarios with invented fixtures. Report counters/IDs/codes if anything fails or remains unverified. The 30 passing results from 0.4.0 do not replace the ten new scenarios.
2. In **Temporary notification diagnostics**, check the installed source, package, version, Android authorization, and connection. Collection must be stopped before consent. If the app is in another/cloned profile or is not found, report that; do not try to enable another package manually.
3. Tap **Consent and start for up to 24 hours** and read the notice. A diagnostic-active indicator also appears outside advanced settings. Wait for a legitimate notification during ordinary Nubank use; there is no need to create a banking operation for the test.
4. Return and tap **Refresh diagnostics**. Check count, publication/capture times, revision, and summary marker. Close the interface, lock the screen, and check after another notification that recording continued. Do not forward originals or screenshots; the APK uses FLAG_SECURE to protect the window.
5. **Open and review** shows the original only after a tap and proposes masks in editable fields. Replace names, merchants, accounts, places, references, and personal phrases with invented examples. Recognized BRL values become `R$ 12,34`; recognized CPF/CNPJ, phone, email, URL, UUID/Pix key, and dates are masked. These rules do not guarantee anonymization: review every field and manually confirm the review to include it in the encrypted export. Closing the review or moving the app to the background clears interface text; it does not erase the encrypted record.
6. With the editor closed, enter a password and confirmation of 12 to 128 characters and tap **Export encrypted reviews**. Choose a local folder in the Android picker. Only confirmed reviews are exported; originals, real IDs, times, group keys, signature, session, and device data are not included. There is no automatic sharing/sending. The `.ffortuna-samples` file remains encrypted; it is not a fixture ready for Git.
7. Enter the password in the first field and use **Check exported file**. A valid-length wrong password should fail authentication. With the correct password, the app should report the number of valid samples without importing or changing records. Cancelling the picker does not change the vault. This format does not restore the synthetic vault; **Restore backup** will reject its contents.
8. Stop collection. New notifications must not increase the count. Optionally delete diagnostics and confirm the count returns to zero while synthetic examples remain. Rebooting the phone requires new consent; merely reopening the app preserves records. There is no need to wait seven days or change the clock: isolated scenarios exercise those limits.

The expected checkpoint is to confirm the 40 synthetic results, source identification, and local collection/review/export behavior. Do not send real data in this conversation. The user approved the functional flow on 30/10/2026 on the A36/Android 16, including capture, the correct value in the original, and review/export. The user did not provide battery counters, logs, or an exported report. Approval is not equivalent to execution by the JUnit runner or validation of a bank parser.

## Run and verify

```sh
nvm use
npm ci
npm run check
npm run native:build -- --compile-device-tests
```

The local build keeps a 12 GiB and 16 CPU ceiling through systemd/cgroup, without EAS, an Expo account, or a paid service. The APK is a release build with JavaScript bundled and no INTERNET permission; tool/dependency downloads are development steps. Expo Go cannot run the module and cannot receive real samples.

Decision, alternatives, and official sources are in [ADR 0008](adr/0008-diagnostico-real-temporario.md). Export reuses the envelope from [ADR 0006](adr/0006-backup-portatil-da-caixa.md), with PBKDF2-HMAC-SHA256/600,000 iterations and AES-256-GCM. The encrypted file is created in memory before SAF, with no plaintext temporary file; passwords are not persisted. Autofill and correction are disabled in sensitive fields. This does not control keyboard behavior or guarantee erasure of every internal platform string/buffer.

In the user's report, `R$ 12,34` appeared only in the suggestion, while the original preserved the correct value. This behavior is intentional in preparation for export. Future financial processing should consume the original; replacement values never represent real transaction amounts.
