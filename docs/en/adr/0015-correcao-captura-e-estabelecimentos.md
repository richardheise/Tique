[Português](../../adr/0015-correcao-captura-e-estabelecimentos.md)

# Audited corrections, capture hints, and merchants

Decision dated 31/10/2026. The user prioritized safe voids, running Android tests, and improving capture, focusing on purchases and Pix. The user asked for the place to be a tag, giving BLUMENAUENSE → Food as an example, and authorized asking the user to fill it in within the app.

## Decisions

Voiding extended movements keeps rows, reason, and evidence links. Effects are recalculated in one transaction: voiding a payment reopens debt; voiding a purchase reduces the bill. Dependencies that would leave a savings pocket negative or debt inconsistent block the operation without partial change. Resources are not deleted, and repeating the same command/reason is idempotent. Financial correction means voiding and re-entering, preserving history; it is not a bank reversal.

Catalog moves to schema 6 (4→5 changes the accepted cancellation semantics; 5→6 adds merchant-rule fields). Financial backup v5 includes these fields; older versions are read with exact mode, empty aliases, and null merchant tag. Downgrade must not reinterpret new data as an older version.

A merchant rule has a name, explicit alternate names, a required place tag, and an optional category. Matching uses normalized words, not loose substrings. The tag is created/reused by ID and also accompanies manual postings and batch confirmations, preserving the chosen category and other tags. Automatic tag additions are audited without changing the original request used for idempotency. Existing exact-description rules remain valid; when exactly one merchant is recognized, its tag also accompanies an exact rule. Ambiguous merchants require review. Applying to history requires preview and confirmation; rules suggest classification for new unreviewed candidates and do not override human corrections. The review allows entering a place without posting the transaction.

A complementary parser suggests nature only from explicit words and keeps confidence uncertain. Pix is a payment method, not income/expense by itself. Effective date comes only from an explicit label and valid calendar date, never publication date. Reversal, bill, card, contribution, and withdrawal require their own handling. Evidence records field, text, and positions; financial-source validation was extended for these optional fields without rejecting old sources. Reprocessing preserves human edits and confirmations; derived snippets do not escape the retention policy.

There is no new automatic posting and no declared validated Nubank template. Fixtures are synthetic. A third-order Markov chain does not provide the same explanation or identity guarantee as an explicit merchant link. Trigrams may eventually help suggest similar names without automatic classification; they are not implemented here.

## Tests and consequences

The instrumented suite runs on an Android 16 x86_64 emulator with KVM; it is not equivalent to a Samsung A36 or real bank notifications. Emulator is limited to 4 GiB/4 CPUs and Gradle tests to 8 GiB/12 CPUs, totaling up to 12 GiB/16 CPUs. The emulator is shut down before the ARM64 build, which keeps its own 12 GiB/16 CPUs limit.

The first run exposed tests with old expectations and a real restore failure caused by `direction` being non-nullable in the decoder. Integration of the new evidence with financial validation was also corrected; it previously accepted only amount evidence. The final suite is recorded in release notes. Do not ask the user for another manual test battery.

References: [Emulator acceleration](https://developer.android.com/studio/run/emulator-acceleration) and [NotificationListenerService limitations](https://developer.android.com/reference/android/service/notification/NotificationListenerService). Android APIs do not guarantee notification delivery or provide bank statements.
