[Português](../captura-nativa.md)

# Native capture proof — Android

The synthetic proof and its historical test scripts are below. Starting with 0.5.0, the same APK also includes [optional temporary real diagnostics](diagnostico-local.md), with separate consent, its own encrypted database, and no parser/transaction posting. Fictional-data restrictions still apply to examples and the Expo Go ledger.

This delivery adds an exclusively synthetic proof APK. The financial demonstration remains in Expo Go. The APK uses `org.ffortuna.capturepoc`, with its own diagnostic screen; it does not create another financial ledger and does not accept bank notifications or personal files.

Device reported by the user: Samsung Galaxy A36, SM-A366E/DS, Android 16. The user installed version 0.2.0 and encountered the failure described below. After fix 0.2.1 became available, the user reported on 25/10/2026 that they tested the APK and all manual-script items passed. This report concerns the native proof and is separate from the earlier Expo Go demonstration approval. Also on 25/10/2026, after installing version 0.3.0, the user reported success in all eight backup/restore steps, including reinstall and persistence.

## Operation

Kotlin implements NotificationListenerService, a fictional notification emitter, the encrypted database, and recovery. The interface uses Expo Modules API for commands and queries. Capture does not depend on callbacks to JavaScript.

Before reading extras/content, the listener checks local consent, its own package, and the absence of a group-summary flag. It accepts only marker `ffortuna.synthetic.v1` and an emitter-created UUID generation. Android's listener access is broad; the local filter does not turn the system permission into per-app permission. No database can be selected in this proof.

A single native queue owns migrations, writes, parsing, and reads. An observation is committed to the database before it is interpreted. Later processing only modifies pending items; reopening the database recovers pending work. The envelope retains source, package, local ID, identity, revision, publication, capture, and payload. Parsing preserves version, fields, and evidence; the effective date comes from the fixture and is not inferred from publication time. Interpretation does not create a financial transaction in this increment.

Identity combines the Android notification key and the generation created by the emitter. The fingerprint considers payload and publication time. Identical redelivery is idempotent; an update preserves earlier observations with a new revision. Two identical purchases with distinct generations remain separate. Group summaries are ignored, and dismissing a notification does not delete the record.

## Encryption, key, and retention

SQLCipher Community 4.19.1 opens the native database; a non-empty `cipher_version` is required. A random 32-byte key is wrapped with AES-256-GCM using a non-exportable Android Keystore key. AAD binds the wrapping to the database. Only ciphertext/IV are stored in `noBackupFilesDir`; the in-memory key is needed to open the database.

Not requiring biometrics for each operation allows the key to be used while the screen is locked after the first unlock. The database uses CE storage and a service without Direct Boot; capture before that unlock is not promised. This proof does not implement biometric interface lock.

Missing/invalid key, incorrect GCM authentication, or a database with a future version blocks the operation without generating a replacement key over the database. Reset requires explicit confirmation and deletes only the active database and its key, preserving any previous database and external backups. Version 0.3.0 adds password-protected portable backup without raw payloads. Restore creates another local key and does not need the original installation's key. The manual backup script was approved on the A36; real data remains blocked until instrumented tests, file/log checks, and inactive-database retention are also complete.

Automatic backup is disabled, and extraction rules exclude the database, files, and cloud/device-transfer preferences. The final APK configuration must be inspected; manufacturer behavior still needs testing. The code emits no content logs. SQLCipher's Java logger is disabled; that alone does not prove the absence of all dependency logs.

Raw data for all observations, including pending ones, expires 30 days after capture when the core runs. Existing metadata and parsing are preserved; observations without raw data are identified and excluded from the count of processable pending items. Since 0.4.0, cleanup includes the previous database without activating it. If the previous key is unavailable, cleanup is marked blocked and capture is paused; discarding it requires an explicit action. Synthetic retention scenarios passed in the A36 interface battery, according to the user's report on 26/10/2026. Cleanup during force-stop is not promised. Integrity/header checks are diagnostic and do not replace validation of every file and backup before real data is used.

## Build locally

Prepared environment: JDK Temurin 17.0.21+9, SDK API 36, Build Tools 36.0.0, NDK 27.1.12297006, and CMake 3.22.1 in `.local/android-toolchain`. Gradle also requested Build Tools 35.0.0 for dependencies. Download checksums and the accepted standard free SDK license are in that folder's local README. No EAS, Expo account, or build service is required.

```sh
cd /home/heise/FFortuna
nvm use
npm ci
npm run native:build
```

`FFORTUNA_JAVA_HOME` and `ANDROID_HOME` may point to custom installations. Compilation needs a network to download tools/dependencies; the packaged APK operates locally. The script uses capture-poc mode and embeds JavaScript in the release build, so Metro is not needed on the phone. INTERNET permission is removed from the manifest by configuration; confirm its absence in the final APK.

The script requires Linux/systemd with memory control and fails without the confirmed limit. The entire build tree receives MemoryMax=12 GiB, MemoryHigh=10 GiB, zero swap, and a quota of up to 16 CPUs, as later authorized by the user. Gradle uses one worker, no parallel projects, a 3 GiB heap, and in-process Kotlin. Ninja's compile pool is limited to 16 jobs; the linker stays at one and Metro at two workers, checked before compiling the app. Do not raise limits automatically. The computer rebooted during an earlier attempt; reviewed records did not confirm the cause. This protection is a session requirement, not an optional optimization.

The `android/` project is generated and Git-ignored. A configuration fingerprint preserves it between equivalent builds; changes to project config/plugin/manifest may require a new prebuild. Original Kotlin code is in `modules/ffortuna-capture`, outside the generated directory.

Signing uses a local private key in `.local/keys/`. Preserve this folder to install proof updates without changing the signature. Do not use the template's public default key, send keys/passwords to Git, or treat this test signature as a store-distribution decision.

Expected artifact: `artifacts/ffortuna-capture-poc-arm64.apk`, with SHA-256 alongside it. Version 0.3.0 (versionCode 3), build completed and inspections approved on 25/10/2026. APK size: 46,147,788 bytes. SHA-256: `46778d77d2294031697f99f52a736f0f218341564f9859b4732e7779597b42f6`.

## Install on the A36

Transfer the APK from the SSH server to your computer/phone using your existing access; there is no need to publish it online. For example, on the local computer, replace the destination with the same host used for SSH:

```sh
scp usuario@servidor:/home/heise/FFortuna/artifacts/ffortuna-capture-poc-arm64.apk .
```

Install with a file manager or ADB with authorized USB debugging:

```sh
adb install -r ffortuna-capture-poc-arm64.apk
```

ADB allows development installation without developer registration. Android verification/installation policies vary by region and channel; if manual installation is blocked, follow the appropriate [official flow](https://developer.android.com/developer-verification/guides/faq) without disabling global protections.

Open **FFortuna · Synthetic capture**. Allow notifications from the fictional emitter, enable local consent, and open Android notification access to grant access to this proof. If Restricted setting appears, follow [Android's official guidance](https://support.google.com/android/answer/12623953) to allow this setting specifically for this app. Return to diagnostics and check that the database is ready, permission is granted, connection is active, and the emitter is authorized. Use Refresh status if the connection is still starting.

## Functional script

Start with an empty database. Totals below assume each button is used once, with no extra publications:

| Action | Expected result |
| --- | --- |
| Publish example | 1 interpreted observation; no financial transaction. |
| Reread active notifications and repeat | Still 1 observation. |
| Update last example | 2 observations; same source/generation, new revision, previous raw data and parsing preserved. |
| Two identical purchases | Total 4; two additional identities despite same amount/description. |
| Two children + summary | Total 6; summary is not counted as a movement. |
| Dismiss notifications in the shade and refresh | Total remains 6. |
| Schedule example with app closed | After 15 seconds or more, subject to Android delays. Go to Home and lock; then reopen/refresh: expected total 7. Do not force-stop at this step. |
| Reread/reprocess again | No duplicates; no pending items once processing finishes. |
| Check encryption and integrity | SQLCipher active; integrity and header checked by diagnostics. |
| Close/reopen app and reboot device | Previous data preserved; after first unlock, check reconnection and publish another example. |

Record publication and capture times separately. Scheduling is not exact and may be delayed by Doze/battery optimization. Run the first round with Samsung defaults; then compare with battery optimization enabled, recording any differences. Do not assume there were no losses or blame the database before checking listener connection.

### Disabled buttons and fix 0.2.1

On 25/10/2026, installation on the A36 was confirmed. The user granted notification access and saw an active connection, but the database appeared unavailable and consent could not be enabled. No successful capture was recorded in that test.

A deterministic version 0.2.0 bug was identified: `PRAGMA secure_delete=ON` returns a row, but initialization ran it with `execSQL`. The [version 4.19.1 driver](https://github.com/sqlcipher/sqlcipher-android/blob/v4.19.1/sqlcipher/src/main/jni/sqlcipher/android_database_SQLiteConnection.cpp) rejects `SQLITE_ROW` on that path. Version 0.2.1 uses a cursor query and checks the result, keeping protection enabled. Opening failed before migrations; the update reuses the existing key and database without an automatic reset.

Install version 0.2.1 over the previous version with the same signature; do not uninstall or clear data to apply this fix. Return to section 1, check **Encrypted database: ready**, and tap **I agree: enable synthetic capture only**. Also check Android access and emitter permission, then use **Publish example**. After receiving update instructions, the user reported approval of the manual on-device script.

The updated screen explains each pending requirement beside the buttons. A database failure displays an identifier such as `[SECURITY/SQLiteException]`, with the stage and exception type, without exposing the original message, SQL, path, payload, or key. If it remains unavailable, record the identifier; do not use the delete button as a repair attempt.

Also test revoking and granting access again, disabling consent, and process termination. With force-stop, expected behavior is interruption until the app is reopened and, if needed, access is granted/reconnected again. Do not promise capture during force-stop; alarms may also be cancelled. After interruption, reprocessing recovers what already reached the database; notifications that never arrived cannot be fully reconstructed.

## Tests and evidence limits

- On 25/10/2026, the user reported that all eight steps of the 0.3.0 script worked perfectly on the A36/Android 16: local backup, wrong-password rejection, restore into a new database, switching between databases, reinstall without the old key, and persistence/integrity after reopening. This is the user's manual validation, separate from instrumented execution.
- On 25/10/2026, after fix 0.2.1, the user reported approval of all manual-script items on the Samsung Galaxy A36 SM-A366E/DS, Android 16. No individual results or logs were supplied; this report is not attributed to instrumented tests.
- TypeScript and 30 demonstration/URL tests passed after applying the corrected lockfile.
- Thirteen Kotlin JVM tests were run and passed: filtering/identity, diagnostic privacy, PBKDF2/AES-GCM, independent OpenSSL vector with UTF-8 password, wrong password, tampering, cost/header/invalid sizes, identities, and portable-format validation.
- Thirteen instrumented tests were written: SQLCipher persistence, resume, key loss/tamper/atomic recovery, 0.2.0 regression, backup without raw data, restore without old key, previous-database preservation/switching, interrupted staging, invalid data, and searches for synthetic canaries in database/key/backup/journal/WAL/SHM. They compiled with `npm run native:build -- --compile-device-tests`; execution requires a device/emulator and was not performed.
- `adb devices -l` found no connected device in this environment; `/dev/kvm` is unavailable. No emulator was started, preserving PC resources.
- Local release build and final inspection approved: signature, no INTERNET, backup disabled, embedded JavaScript, license notices included, and 23 ELF/ZIP libraries aligned to 16 KB. Report: `artifacts/apk-inspection.json`. These checks do not prove execution on a phone.

To repeat only the JVM tests: `npm run native:test`. Device tests are in `modules/ffortuna-capture/android/src/androidTest`. Local JUnit results are in `modules/ffortuna-capture/android/build/test-results/`.

The proof does not accept real data and does not validate the Nubank package/content. Portable backup covers only this synthetic database, not the Expo Go financial database. Before real samples, complete the security conditions in the alignment document, including restore without the original key, key loss, logs/temporary files, retention, and verification of all files. The 0.3.0 script below was manually approved by the user on the A36. Inactive-database retention and the synthetic battery were approved by the user in the 0.4.0 script. Next increment: temporary protected diagnostics with consent and package verification on the device. Android JUnit runner execution remains pending; the synthetic test does not validate Nubank parsing.

The [APK license review](revisao-licencas-apk.md) records components, texts, and corresponding source for distribution.

## Portable backup — 0.3.0 script

Section **4. Backup and restore** saves only observations and parsing results from the proof. It does not export raw payloads, password, SQLCipher/Keystore key, or consent. The `.ffortuna` file is encrypted before it leaves the app; no plaintext or cache copy is created. The [format decision](adr/0006-backup-portatil-da-caixa.md) records KDF, limits, and alternatives.

Choose **Internal storage → Download**, or another local folder, in Android's picker. The app requests local destinations, but the picker and its providers are external components; `EXTRA_LOCAL_ONLY` does not guarantee every provider's behavior. Do not choose cloud storage. The operation can be tested in airplane mode and does not require EAS, Metro, external account, broad storage permission, or INTERNET in the APK. Provider error/interruption may leave an incomplete encrypted file, which will not be accepted as a valid backup.

Use a test passphrase of 12 to 128 characters. There is no password recovery or automatic normalization of spaces/accents. Fields are cleared when an operation starts or the screen is exited; autofill is disabled. This does not promise removal of every copy from platform memory.

1. Install version 0.3.0. Since the app was uninstalled, the database will start empty. Reauthorize if needed, enable capture, and publish one example and two identical purchases: expected total **3**. Note one ID and its parsing result.
2. Enter and confirm the password. Tap **Create encrypted backup**, choose a local folder, and wait for save confirmation. Keep the file and password outside the app's private data. The file must not enter Git; `.ffortuna` is ignored.
3. Publish one more example: total **4**. Try restoring the file with another password of valid length: authentication must fail and total remain **4**, with no database replacement.
4. Enter the correct password in the first field and tap **Restore backup to new database**. Confirm and choose the file: total **3**, same IDs/times/parsing as the backup, capture **disabled**, previous database preserved. Raw data is not restored. If the backup contains pending observations, they appear as **no raw data**, with no reprocessing possible; already interpreted results remain available.
5. Use **Return to previous database**: total **4**. Return again: total **3**. No merging should occur. A second restore requires **Discard previous database** with confirmation; restoring the same file still yields **3**, without duplicates.
6. To prove Keystore independence, check that the file is in a local folder outside the app and that you kept the password. Uninstall **only the synthetic proof**, reinstall this APK, and restore the file. Total should return to **3**, with identities/parsing preserved and capture disabled despite loss of the previous key. Reopening/rebooting should preserve the restored database.
7. Cancel save/open pickers: the database must remain unchanged. Also test a wrong password after reinstall. Record create/restore times on the A36 because the cost of 600,000 iterations requires physical measurement.

Restore authenticates the entire file, validates the synthetic format, and imports in a transaction into a new SQLCipher database. A durable marker allows interrupted staging to be cleaned on the next open. Only after integrity verification are the new active pointer, previous database, and disabled consent published in one preference write. JavaScript does not process concurrently. The previous database is also encrypted but depends on the old local key; if that key was lost, it can still be discarded while keeping the restored database.

The user approved **all eight steps of the 0.3.0 manual script on the A36/Android 16 on 25/10/2026**, after receiving instructions in this conversation. No timings were measured and the agent did not run instrumented tests. The 13 instrumented tests compiled and use their own library sandbox; execution is pending. Pre-release attachments and checksums preserve the original publication snapshot; later approval is recorded in this document and public release notes.

## Automatic recovery and security — 0.4.0 script

Update over version 0.3.0 without uninstalling: signature and package are unchanged. This increment remains synthetic only. The user already approved the 0.3.0 portable-backup script; these controls now live under **Advanced settings**, without requiring everyday use.

The default keeps up to **3 private encrypted copies**, without raw data, at most one every **24 hours**, and only when records/parsing have changed. The first copy is created when records exist and the core runs. No folder or password selection is required. Internal copies are lost on uninstall and depend on the local key; only the **password-protected portable backup**, saved outside the app, can protect against reinstall. Copies cover the synthetic database, not the Expo Go ledger.

1. Open the APK and check the local recovery policy/summary on the normal screen. It should say “ready,” initially with zero copies if the database is empty. With existing records, the first copy should appear automatically. Close and reopen: records and copy remain.
2. In **Advanced settings**, list local copies. Note the observation count in the most recent copy. Publish a new example, wait, and refresh status: the total increases, but no new copy should appear before 24 hours. Day rotation is tested with an isolated clock in step 5; do not change the phone clock.
3. Restore the listed copy and confirm: count returns to the noted value, IDs/parsing are preserved, no raw data; capture disabled and previous database preserved. If a previous database already exists, the interface requires restoring it or explicitly discarding it before another restore. Use **Return to previous database** to recover the later example without merging. No file search is needed.
4. Optionally disable copies and check that the list remains available. **Delete local copies and disable** requires confirmation, removes only copies, and retains observations. Re-enable to return to the default. Deletion is not required to continue the script.
5. Under **Advanced settings → Security checks**, run the battery and wait. Capture is paused. There are **30 checks**: 28 scenarios shared with the instrumented suite, interrupted-test cleanup, and the app's own log window. All use isolated files/keys and synthetic fixtures; they do not change the active database. Report passed/failed/not-verified counts and, if any, failure IDs/codes. Unavailable logs may appear as “not verified”; that is not a pass and must not be hidden. Tests include copy rotation/recovery, tampering, active/previous retention, lost key, missing/incorrect database, WAL, and interrupted staging.
6. Check that the active database total and IDs are unchanged from before the battery, capture is disabled, and **Check encryption and integrity** works. Re-enable synthetic capture, publish an example, and confirm persistence after reopening. The APK still has no Metro or INTERNET; airplane mode can be used.

Raw-data retention is 30 days from capture and runs when the core executes, including for the inactive previous database. Times and parsing remain. An unavailable old key marks **cleanup blocked** and pauses capture; no database is recreated over existing data. Explicitly discarding the previous database remains in advanced settings. This avoids requiring file management from the user and avoids hiding a policy failure.

**Evidence for this version:** JVM/TypeScript tests and APK inspection run in the build environment. The 28 instrumented tests were compiled. On 26/10/2026, the user reported **30 passed, zero failed, and zero not verified** in the interface battery on the A36/Android 16, and confirmed the rest of the script worked. No logs, exported report, or timings were provided. The battery shares callbacks with the suite, but is not execution by the JUnit runner; instrumented execution remains pending, with no ADB device on this server. A passing log covers only that process's probes and time window; it is not a universal guarantee. Real data remains blocked.

## Technical sources

[Expo: local build](https://docs.expo.dev/guides/local-app-production/), [Expo Modules API](https://docs.expo.dev/modules/overview/), [NotificationListenerService](https://developer.android.com/reference/android/service/notification/NotificationListenerService), [SQLCipher Android 4.19.1](https://github.com/sqlcipher/sqlcipher-android/tree/v4.19.1), [Keystore](https://developer.android.com/privacy-and-security/keystore), and [Android 16 KB pages](https://developer.android.com/guide/practices/page-sizes).
