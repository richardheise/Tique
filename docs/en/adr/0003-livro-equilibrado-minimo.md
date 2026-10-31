[Português](../../adr/0003-livro-equilibrado-minimo.md)

# Balanced financial ledger with a simple interface

The user accepted a minimal internal ledger of balanced entries: income, expenses, and movements between the user's own resources have counterparts, while the interface uses common financial actions. The alternative was to record only income/expenses and add exceptions as savings pockets, cards, and reversals came up; the ledger provides one consistent rule now to distinguish spending from money held in savings at the user's sole banking institution.

Consequences: require additional validation and atomic writes; do not expose a configurable chart of accounts in this release. Balance does not prove completeness, correct classification, or transaction identity. Opening positions, prior commitments, and adjustments have their own counterparts; they will not be silently converted to income/expense.

After discussion, the user also accepted distinguishing Save/Withdraw movements from Update reported value: the former transfer resources; the update records an explicit position adjustment without automatically classifying the difference as income or expense. Manual actions are alternatives to capture and import; they are not mandatory steps for events that can be safely recognized.

When incorporating history from before the start, explicitly review the opening date and balance for the period, preserving the current position as a comparison. Adding those movements to the current balance would duplicate their effects; differences must remain visible without hidden adjustments.
