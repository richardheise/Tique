[Português](../../adr/0013-revisao-em-lote.md)

# Explicit batch review and two tabs

Decision dated 31/10/2026 for the delivery requested in `docs/prompt-sol-uso-diario.md`.

Movements and Analysis are the daily destinations. Data and capture moves to settings, keeping Android sharing and importing optional.

Receiving a statement continues to create evidence and candidates, without confirming nature from the signal. A manual selection with explicit nature allows confirming multiple candidates using the same validations and provenance as individual confirmation. Unknown category does not block recording. Conflicts and possible links require individual attention. Types not yet represented financially remain pending.

The native core serializes commands and writes the batch to the same ledger in one transaction. Retries use each event's identity. A failure must not leave some entries committed. No second ledger is created and the backup policy is unchanged: confirmed records and links enter financial backup; staging remains local.

Analysis queries only confirmed records and displays the month. After confirmation, the interface offers months present in the set without adding candidates to totals. No period is automatically considered complete.

Rejected alternatives: confirming every file row from its signal would distort income/consumption; confirming line by line prevents convenient use; running independent commands without reporting partial results risks repeating work after failures. The batch transaction requires limits and full validation in exchange for clear recovery and outcome.

Reusable rules, editing confirmed metadata, new comparisons, and support for savings pockets/cards remain in the prompt backlog. They are not prerequisites for this delivery.
