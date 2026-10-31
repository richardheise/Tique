[Português](../../adr/0012-uso-diario-e-entrada-de-extratos.md)

# Daily use, movement recording, and optional statements

Decision dated 30/10/2026, authorized by the user after defining three screens: Sources, Movements, and Analysis. The goal is to track known movements, without reproducing the bank balance exactly. The user will evaluate the app in normal use, without a mandatory new manual test battery.

## Decisions

The native SQLCipher ledger remains single, with integer cents, counterparts, idempotent commands, and void audit. For a new installation, `startTracking` creates only a zero technical opening at `0001-01-01`; it does not ask for a bank balance or present it as wealth. Existing installations preserve opening, dates, keys, IDs, and movements. Historical movements may precede the old opening: the history range is no longer a blocker to tracking income/expenses. Future dates and monetary limits remain validated. Balance checking is optional and advanced, and does not generate adjustments. Analysis shows confirmed income/expenses for the period and their result, without calling it available bank balance.

The existing encrypted observations database migrates from 2 to 3, adding mode and file provenance. Old consents remain diagnostic, with their existing terms. **Enable notifications** requests new daily-use consent linked to the installed Nubank package and its signature. This mode survives process/device restart; Android may still interrupt the listener, and the first unlock after reboot is required. Revoked access, signature changes, storage errors, or capacity pause/block collection. There is no permanent foreground service, guaranteed capture, screen reading, or JavaScript dependency for persisting/processing observations.

Daily originals remain encrypted for up to 30 days and are cleaned when the core runs. Structured observations, parsing, and daily corrections remain; expiry does not delete pending items. Because the generic parser uses a notification sentence as the provisional description, that copy is also removed when the raw data expires. Imported descriptions and descriptions explicitly saved/confirmed by the user remain financial data. No anonymization is claimed. Old diagnostics keep seven-day retention and are not silently converted to daily use.

Opening the database checks SQLCipher, integrity, key, and ledger state. Daily preparation/capture actions use these internal checks; the isolated runner remains available in developer tools and is not a mandatory use task. This does not turn test compilation into physical validation or eliminate Android/storage limitations.

Statements are **optional**. The app receives a `content://` URI from the picker or Android Share/Open with. A shared URI remains in memory until the explicit receive action. Content is read with limits, without copying the file to a plaintext cache/temp, parsed in Kotlin, and written atomically as candidates in the encrypted vault. The whole file is not stored. Initially accepted formats: account CSV/OFX subset, UTF-8, up to 2 MiB/2,000 lines; reject the entire invalid file. No PDF, remote conversion, or automatic statement reading inside Nubank. Opening Nubank only launches the installed app so the user can export manually.

File amount and date have their own evidence. The signal suggests income/expense with uncertain nature: confirmation remains human because inflows/outflows may be transfers, contributions, or bill payments. Importing does not enable capture or post automatically. Account identity + FITID enables stable OFX re-import; without trusted account identity, identity is file + line. Overlapping imports without identity are presented for review/linking, not deduplicated by amount/day. Legitimate identical purchases are preserved. Conflicting content for the same statement ID requires reviewing the link/previous record.

Catalog/ledger moves from 2 to 3, including `finance_observation_links`: multiple evidence sources may point to a transaction through explicit confirmation, without reposting or changing amount. The link and provenance are validated and written in the same commit. Source, parsing version, and field evidence accompany the record. Reprocessing preserves corrections and confirmations.

Financial backup becomes `tique.financial-book/2`, including these links; reads also accept format 1 with an empty link set. Restore keeps the new-vault/key protocol, validates before switching, and preserves the previous vault. Automatic local copies and portable backup include confirmed financial records and catalog, without raw data, pending items, or import staging. New consent explains that pending items are local; uninstall loses the local vault. v2 files are not promised to restore in an older APK. Never clear data to update.

## Alternatives and consequences

- **Keep the diagnostic session as the main flow:** would require daily renewal and lose pending items through retention. Rejected for daily use.
- **Create another capture system and ledger:** would increase migrations, synchronization, and recovery complexity. Extending the evidence vault preserves reviews; the financial ledger remains separate and single.
- **Automate statements with private APIs, screens, or backend/Open Finance:** incompatible with requirements. Use explicit export and local import.
- **Deduplicate by amount/date or confirm from the signal:** would confuse identical purchases and asset movements with income/consumption. Trusted identity or human linking is required.

The cost is maintaining backup compatibility, two-source context, and two retention policies. Capture still uses a generic BRL parser, with nature/date review; there is no validated Nubank template or automatic posting. Unsupported financial types remain pending. Capacity is finite (10,000 non-diagnostic observations and 10,000 financial records, in addition to backup byte limits); limits do not authorize silent loss. The user may report daily-use difficulties to guide the next increment.

Technical references: [Android intents](https://developer.android.com/training/basics/intents/filters), [files with content URI](https://developer.android.com/training/secure-file-sharing/retrieve-info), [Expo Modules API](https://docs.expo.dev/modules/module-api/), [NotificationListenerService](https://developer.android.com/reference/android/service/notification/NotificationListenerService). The [official Nubank instructions](https://blog.nubank.com.br/extrato-bancario/) confirm requesting/exporting from the bank itself; they do not validate the layout of a user sample. No real file or notification entered Git.
