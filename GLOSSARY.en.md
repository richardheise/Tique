[Português original](GLOSSARY.md)

# Tique — Personal finance

App vocabulary for distinguishing observed facts, user decisions, and their financial effects.

## Language

**Merchant (place)**:
The name the user uses to identify where they bought something or with whom they moved money. It can have explicit aliases. When a rule identifies one place, its tag accompanies the classification; the main category remains separate.
_Avoid_: treating category and merchant as the same information

**Notification clue**:
A snippet that helps fill in a field, accompanied by its source and confidence. It is not bank confirmation. Pix identifies a payment method; a notification's publication is not a verified effective date.
_Avoid_: automatically verified transaction

**Record cancellation**:
Removing a record's effect from the app's calculations while keeping the original and the reason. It can be blocked by dependencies. It does not reverse money or execute a banking operation.
_Avoid_: erase history, undo a bank payment

**Batch confirmation**:
An explicit decision to record a selected set of candidates with the nature and classification specified by the user. The core applies each item's validations and provenance and saves the set atomically. A statement's sign does not replace confirmation of its nature.
_Avoid_: confirm everything automatically, import based on the sign

**Uncategorized**:
A confirmed record whose main category has not yet been set. It contributes to totals for its nature and period. It is not the same as a candidate with an amount or nature still awaiting review.
_Avoid_: excluded from totals, movement not yet confirmed

**Movement ledger**:
The history of movements known to the app, entered manually or derived from reviewed evidence. It may be partial and does not need to reproduce the user's bank balance or net worth. Tracking it still requires distinguishing income, expenses, and movements between the user's own resources.
_Avoid_: bank account replica, guaranteed complete statement

**Net result for a period**:
The difference between confirmed income and expenses in the selected period. It depends on available records and does not represent money available at the bank, net worth, or guaranteed savings.
_Avoid_: money available, bank balance

**Daily notification capture**:
Local, consent-based receipt of new notifications from the selected bank, without requiring daily session renewal. It preserves observations and candidates in the encrypted inbox; originals are retained for 30 days. It is not a bank query, a complete statement, or automatic confirmation.
_Avoid_: bank connection, balance synchronization

**Received statement**:
A file explicitly selected or shared by the user and interpreted locally as candidates. Preview and overlap identification come before confirmation; receiving the file does not enable capture or post transactions.
_Avoid_: automatic financial import, guaranteed full account copy

**Financial account**:
A place where the user keeps money, such as a bank account or cash.
_Avoid_: login account, card

**Observed event**:
A record of an observation received from a source, such as a notification or simulation. The observation alone does not prove that a financial movement occurred.
_Avoid_: confirmed transaction, complete statement

**Source evidence**:
Information preserved about the source and content of an observation that supports a financial interpretation.
_Avoid_: ledger entry

**Parsing result**:
An interpretation of evidence fields, with their provenance and extraction uncertainties.
_Avoid_: confirmation, user correction

**Transaction candidate**:
A proposed movement awaiting review that does not change the balance.
_Avoid_: completed transaction, confirmed entry

**Confirmed transaction**:
A financial record accepted by the user or by explicit automation criteria, with an effect on the financial ledger.
_Avoid_: notification, candidate

**User metadata**:
Information added by the user, such as category, tags, and notes.
_Avoid_: original evidence content

**User correction**:
A deliberate change to a proposed or recorded field, preserved as a decision distinct from the original interpretation.
_Avoid_: new bank evidence

**Publication time**:
The time when the source published the observation.
_Avoid_: effective time

**Capture time**:
The time when the app received the observation.
_Avoid_: publication time, effective time

**Effective time**:
The time assigned to a financial movement, when known.
_Avoid_: notification time

**Financial nature**:
The economic meaning of a movement, such as income, expense, or transfer.
_Avoid_: Pix, category, payment method

**Payment method**:
The way an amount is moved or paid, such as Pix or card.
_Avoid_: financial nature

**Reconciliation link**:
A relationship identifying records from different sources as evidence for the same financial movement.
_Avoid_: deduplication, equal amount and date

**Expected commitment**:
An expected movement that is not yet a completed transaction.
_Avoid_: available balance, confirmed transaction

**Expected income**:
An amount the user expects to receive in a period, distinct from income already received.
_Avoid_: balance, received income

**Received income**:
Income actually received, distinct from transfers between the user's own resources.
_Avoid_: expected income, withdrawal

**Savings pot**:
An organization of money set aside for a goal, distinct from the everyday account and the financial products in which the amount is invested.
_Avoid_: expense, investment type

**Investment type**:
The type of financial investment associated with a portion of the money set aside.
_Avoid_: savings pot name, expense category

**Financial ledger**:
A set of records representing the financial effects of confirmed transactions and their counterparts.
_Avoid_: notification history, bank statement

**Counterpart entry**:
A complementary record explaining the source or destination of an amount in a balanced movement.
_Avoid_: another independent transaction

**Reported position**:
An amount in a financial resource declared by the user for a reference date, without assuming the movement history that produced it.
_Avoid_: calculated yield, money available to withdraw

**Opening balance**:
The amount in an account on the date chosen to begin tracking, distinct from income and expenses after that point.
_Avoid_: initial income, salary

**Balance check**:
A dated comparison between a user-reported balance and the balance calculated by the ledger through the end of that date. It records the difference without creating an adjustment or classifying the discrepancy as income or expense.
_Avoid_: statement import, automatic balance correction

**Record cancellation**:
An explicit correction to an entry made by mistake, with a reason and date. It preserves the source and counterpart entries for audit, shows the record as cancelled, and removes its effect from active balances and totals.
_Avoid_: bank reversal, erase evidence, operate the bank account

**Card opening commitment**:
An amount already owed on a card when tracking begins, associated with a due date and without assuming details of earlier purchases.
_Avoid_: new expense, identified installment purchase, verified closed statement

**Import batch**:
A set of records from a manual file upload, identified by source and period for review.
_Avoid_: guaranteed full statement, already-confirmed transactions

**Savings-pot contribution**:
A movement of money from the everyday account to an investment associated with a savings pot.
_Avoid_: expense, consumption

**Savings-pot withdrawal**:
A movement of money from an investment associated with a savings pot to the everyday account; its composition may include principal, yield, and deductions.
_Avoid_: ordinary income, salary

**Position adjustment**:
An explicitly recorded difference that aligns a resource with the position reported by the user, without automatically attributing its cause to yield, income, or expense.
_Avoid_: calculated yield, silent balance correction

**Automatic posting**:
Confirmation of a transaction based on explicit safety criteria for financial fields and observation identity, without requiring an individual user action.
_Avoid_: complete statement, necessarily correct category

**Portable backup**:
An encrypted file using an independent password that can restore records in another installation. In the native proof of concept, it preserves synthetic observations and parsing results, without raw payloads.
_Avoid_: automatic Android backup, copy of the Keystore key

**Previous vault**:
An encrypted vault preserved when a restore is activated. It remains inactive and is not merged into the active vault; it can be reactivated if its local key is available or explicitly discarded.
_Avoid_: second financial ledger, portable backup

**Automatic local recovery**:
Up to three private encrypted copies of records and parsing results, without raw payloads, created by the core on days of use when changes occur, at most once every 24 hours. They can be restored through the interface and depend on the installation and its local key.
_Avoid_: portable backup, protection against uninstall

**Raw-payload retention**:
The period after which only original evidence payloads are removed, while records and parsing results remain. In the proof of concept, this is 30 days from capture, applied when the core also runs against the previous vault.
_Avoid_: delete transactions, guaranteed deadline while the app is closed

**Temporary notification diagnostics**:
Local collection with explicit, time-limited consent to examine evidence actually delivered by Android. It preserves data in a separate encrypted vault and allows generic reviewable previews, without posting transactions.
_Avoid_: validated bank capture, complete statement, bank-account access

**Reviewed sample**:
Text fields selected from a diagnostic observation, with suggestions to remove personal data and manual confirmation before encrypted export. Automatic rules do not guarantee anonymization of free text.
_Avoid_: raw payload, confirmed transaction, guaranteed anonymization

**Temporary parsing preview**:
A reviewable interpretation of an original diagnostic observation, with parser version and field-level evidence. It is not a transaction, does not change the balance, and expires with the diagnostic observation.
_Avoid_: confirmed entry, permanent financial history, anonymized copy

**Preview correction**:
Fields saved by the user in an area separate from the parser result. They take precedence in the interface and remain after reprocessing the same observation, without changing the original.
_Avoid_: original text, new notification, transaction confirmation

**Main category**:
A single optional subject for a review, such as Food, Leisure, or Bills. It uses a stable ID from the catalog; it does not determine whether the movement is income, expense, or transfer.
_Avoid_: financial nature, payment method

**Tag**:
An optional context marker, such as Travel or Work. A review can have several tags, managed by the user and linked by ID.
_Avoid_: required category, automatic classification

**Archived option**:
A category or tag unavailable for new selections while preserved in existing links. It can be renamed and reactivated without changing its ID.
_Avoid_: deleted option, erased review
