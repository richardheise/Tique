[Português](../../adr/0014-recursos-e-classificacao-1-0.md)

# Financial resources and classification in version 1.0

Decision dated 31/10/2026, after the user authorized the pending increments and publication of version 1.0.

The encrypted ledger remains single. Schema 4 extends schema 3 with resources (savings pockets and cards), commitments/bills, and a balanced journal of asset movements. Existing records are not rewritten. Card purchases enter expenses on the purchase date; bill payment reduces cash and debt without repeating the expense. Prior debt is an opening position, not new consumption. Contributions/withdrawals and transfers between savings pockets are internal movements, outside income/expense. Reported positions are not a Nubank inquiry or guarantee of complete balance/net worth.

Cards have configurable closing/due dates; the first implementation limits days to 1–28 to avoid invalid dates. A purchase through closing goes on the same month's bill; later purchases go on the next one. The user must check the cycle used by their bank. Partial payments preserve remaining debt. There is no automatic installment plan, automatic interest, external pricing, arbitrary multiple bank accounts, or automatic reversal. Extended movements still have no interface void action; do not use a fake income/expense to offset them. These limits must be included in usage notes.

Reclassification changes only category/tags for confirmed records, with before/after audit. It does not change amount, nature, date, provenance, or original idempotent request. Local rules use exact normalized descriptions and can be edited/disabled; applying them to history requires preview and confirmation. Rules do not prove nature, amount, or financial identity. Human corrections take precedence.

Financial backup version 3 includes resources, commitments, extended journal, rules, and audit. Reading formats 1 and 2 remains supported; restore retains validated vault switching without merging. New formats are not promised to restore in an older APK.

The interface starts in Analysis, followed by Movements. Space Grotesk is included locally under OFL-1.1, with short task copy and details in About Tique. Comparisons use the same portion of current and previous months without assuming full coverage. Goals and forecasts remain future work.

Rejected alternatives: treating every debit as expense would duplicate bills/contributions; a second ledger would require more internal reconciliation; rewriting original requests during reclassification would break replay. The extension maintains compatibility at the cost of cross-validating resources, commitments, and audit. Version 1.0 identifies this delivery; it is not certification of bank compatibility or a claim of physical testing that did not occur.
