[Português](../../adr/0009-previa-brl-no-diagnostico.md)

# Temporary BRL preview in diagnostics

Increment 0.6 authorized by the user on 30/10/2026. No real or reviewed samples are available in this environment; therefore, this step does not invent Nubank notification rules. The parser is generic and limited to textual evidence of BRL amounts.

`notification-brl/1` recognizes only values with the `R$` or `BRL` marker, a decimal comma followed by exactly two digits, and optional thousands groups separated by periods. Unicode spaces may separate the marker from the number but do not group digits. The value is calculated in integer cents, limited to `9.007.199.254.740.991`, JavaScript's maximum exact integer. No floating-point conversion is used. Values without a marker, multiple values, or invalid values remain unselected. Incomplete, negative, zero, truncated, invalidly grouped, or over-limit values are also not selected. Evidence preserves the exact original snippet, field, and UTF-16 offsets, together with the parser version.

The parser does not infer nature, account, or effective date. Publication date is not equivalent to effective date. No value, even when extraction is consistent and unique, creates an automatic entry. Null values remain unknown. Nature `other` or `transfer` is not converted into income or used to change the balance.

`CandidateStore` uses `DiagnosticStore`'s connection and serial executor. The transactional schema 1→2 migration creates separate parsing and editing tables; it preserves existing originals, key, and export revisions and disables the old session because 0.5 consent did not cover automatic parsing. New observations are parsed only after durable commit and with current explicit consent. Parsing failure does not delete the notification or prevent capture. Old records are parsed only after explicit “Interpret” action. There is one preview per observation/revision ID. Reprocessing replaces only the parser row; user corrections remain in a separate table and are preserved.

A new notification revision may represent an update or a distinct financial operation. The link between revisions is shown for human review; it is not financial deduplication. Local review may be partial or discarded. Neither a reviewed revision nor `dismissed` state is a confirmed transaction.

Preview and editing are temporary diagnostic data, not a financial ledger or balance. They expire within seven days with the source observation, via cascading foreign keys. They are not included in backups, exported files, or remote posting. Preview uses the original; it does not reuse the synthetic `R$ 12,34` mask used in revised-export wording suggestions.

Real capture remains protected by the approved native test battery required by the new 48-check scope, `isolated-native-checks/v3`. Approval of the previous 40 checks belongs to another scope and does not authorize this collection. It is not claimed that the new checks were run on-device.

Rejected alternatives: waiting for samples before implementing any generic reading would prevent testing extraction of explicitly marked amounts; guessing keywords or Nubank templates without samples would invent behavior; creating a full ledger now would mix diagnostics with financial processing. Validated templates and migration to a financial ledger belong in another phase, with their own evidence and design. No new dependency is added; the implementation uses APIs and libraries already present.

Sources: [Unicode CLDR, Portuguese](https://www.unicode.org/cldr/charts/latest/summary/pt.html), [ECMAScript `Number.MAX_SAFE_INTEGER`](https://tc39.es/ecma262/#sec-number.max_safe_integer), and [SQLite Foreign Key Support](https://www.sqlite.org/foreignkeys.html).
