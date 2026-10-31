[Português](../../design/tique-interface-proposta.md)

# Tique — interface and daily use

## Direction 1.0

Analytics is the first tab and the default landing screen, followed by Transactions. The Space Grotesk font is included in the APK (Regular, Medium, and Bold), with weights adapted by component. Tasks use short, plain-language text; security, capture, retention, and backups are explained in About Tique under Settings. Data-loss and irreversible-action warnings remain next to the relevant actions. The approved palette and straight shapes are preserved. Reclassification, rules, savings pots, and cards are panels, not new tabs.

## Current direction — two tabs, batch review

On 31/10/2026, the user asked to implement the first delivery in the [daily-use prompt](../prompt-sol-uso-diario.md). Transactions becomes the opening screen and shares navigation with Analytics. Sources moves to **Data and capture** in the gear menu; importing and sharing remain available, without requiring a statement or a balance to get started. Pending items are identified and remain outside totals. Batch review requires explicit selection and movement type; the months of confirmed records can be opened in Analytics. The list offers All, To review, and To classify. Preserve context between tabs and details, while respecting information lock when the app goes to the background.

The approved visual direction remains: black, orange amber, cyan, and off-white; small corners, compact actions, accessible controls. Additional comparisons, reusable rules, and editing metadata on confirmed records remain in the backlog. Implementation and its checks are recorded in the release notes; do not assume an on-device test.

## History — movement tracking and three screens

On 30/10/2026, the user asked to use the app for a few days and note impressions, without more specific test scripts. They asked for three screens, simple navigation, little text, and icons as a priority. They clarified that they want to **record money movements**, without reproducing their exact money or requiring the calculated balance to match the bank. This scope was authorized for implementation in 0.10.0; it is not a physical approval of 0.9.0. The user reiterated that statements are optional and wants to share them with Tique.

Keep black, orange amber, cyan, and off-white, with straight shapes. The internal ledger and existing data remain preserved. Opening and bank reconciliation are no longer the interface's central task; the proposed experience does not require reporting assets, a real opening balance, or correcting differences before recording begins. Imports must still respect ledger dates and rules; implementing this simpler start requires resolving those constraints in the service without fabricating balances or discarding history.

### Three main destinations

| Screen | Task and content |
| --- | --- |
| **Sources** | First opening: **Import statement**, **Enable notifications**, and an option to start manually. Later: capture status, pause/resume, last observation received, and import history. Import is optional and provides a preview, source, new records, already linked records, and ambiguous records. |
| **Transactions** | The usual screen after getting started. List by day, search/filters, category with icon, and record source. Notification items awaiting review appear on this same screen, identified and separated from confirmed records. The **+** button opens a compact income/expense form; categories and tags are accessible from the review itself. Details show source evidence on demand, without exposing technical payloads in the list. |
| **Analytics** | Income, expenses, and result **from recorded entries** for the period, categories, and spending trends. Tapping a chart opens Transactions with the filter applied. Show the period and number of pending items, without claiming that notifications represent a complete statement or that the result is available bank balance. |

These are three stable tabs with an icon and short label. Forms, details, filters, categories, and settings open in panels/modals, preserving context. The gear in the header groups backup, recovery, categories/tags, and privacy; technical tools remain in an advanced section. First launch opens Sources; regular use opens Transactions. Sources/import do not block manual entries.

### HCI and usage practices

- One primary action per context; frequent controls within thumb reach. **+** is available in Transactions. Closing a panel or going back preserves period, search, and position when applicable.
- Recognizable icons with a minimum 48 dp touch area, accessible name, and visible label when the meaning is not obvious. Navigation keeps its three names; do not rely on color, hidden gestures, or abstract icons for important actions. Respect TalkBack and font scaling. [Android guidance](https://support.google.com/accessibility/android/answer/7101858?hl=pt-BR).
- Keep flow text short; provide contextual help and details on demand. Consent, retention, backup-loss risk, and destructive-action confirmation remain clear. Empty states offer a concrete action; errors explain the issue and how to continue.
- Pending items do not count toward totals. A notification may create a candidate, but it must never look like a confirmed transaction before a decision. An uncertain category may remain **To classify**; uncertain amount/type requires review.
- Record anything missed manually; review pending items when convenient. Do not require balance-reconciliation routines, daily diagnostic renewal, or manual execution of development checks.
- Correcting/removing a record should be a simple action and retain an audit trail. Avoid double recording the same movement from a notification and a statement; equal amounts do not prove duplication. Do not use a generic chart-exclusion option to disguise unsupported financial types.
- Cash inflows and outflows differ from income and expenses: own-account transfers, reserve deposits/withdrawals, and bill payments must not distort reports, even when bank balances are not being reproduced.
- Automatic local recovery remains the default. Password-protected portable backup stays in settings, with the uninstall difference explained in context. Financial records are not development logs or plaintext files.

### Capture and statements: actual behavior, not a visual promise

`NotificationListenerService` receives notification events; it does not provide access to Nubank statements or screens. Within local requirements, without credentials or scraping, offer **Enable notifications** and **Import statement**, without a connect button that promises to obtain history. **Open Nubank** may help users export on their own after checking that the app is installed, without automating its interface. [Nubank documents requesting a statement in the app and receiving it by email](https://blog.nubank.com.br/extrato-bancario/); the format accepted by Tique still needs validation, without requiring manual file conversion.

Capture available in 0.9.0 is still diagnostic: a 24-hour session, 200-observation limit, and seven-day retention. The daily-use proposal needs its own flow, with explicit consent, pause/revocation, visible status, durable encrypted storage, and resumption independent of JavaScript. Do not silently renew diagnostic consent or merely hide its controls. Define raw-data retention and preservation of candidates/records before enabling this flow; a pending item must not silently disappear when a sample expires. Confirmed financial history remains independent of raw data. Posting automation continues to depend on evidence and validated parsers; notification access alone does not establish full Nubank support.

Automated development checks continue. The user prefers to assess the next version during normal use, without a new script or mandatory manual test battery. Replacing any manual gate with suitable internal checks is implementation work and must preserve key, database, and migration protections; simply removing the visual gate is not enough.

### References reviewed and scope of the ideas

Official pages were consulted and public interface images inspected. Help/promotional images are references for patterns, not a guarantee of each app's current appearance. No third-party code, marks, assets, or screenshots will be incorporated into the app or repository.

- **[Organizze — getting started on Android](https://ajuda.organizze.com.br/hc/pt-br/articles/360044787873-Come%C3%A7ando-a-usar-o-Organizze-no-Android):** **+** action for income/expense and guided onboarding. Adapt to start without an external account or checklist of accounts/balances. Its commercial bank connection is not a proposed integration for Tique.
- **[Mobills — charts and reports](https://mobills.zendesk.com/hc/pt-br/articles/360049912794-Como-analisar-minhas-despesas-receitas-por-Gr%C3%A1ficos-e-Relat%C3%B3rios):** period selector, categories with icons/values, and category/trend charts. Keep comparable bars in Tique's angular theme; avoid successive menus to reach analytics.
- **[Cashew — official guides](https://cashewapp.web.app/faq.html):** compact entry, period statistics with access to movements, and category goals. Reuse interaction patterns; do not bring highly configurable navigation, automatic balance corrections, or sync services.

For the next scope, prioritize a list by day, **To review/To classify** filter, quick category assignment, discreet provenance, and charts linked to history. Daily trends are a small Analytics improvement; period comparisons must make partial periods clear. Monthly category limits and recurring-item recognition come after basic daily use. Do not add everything in one delivery or show fake controls for unimplemented features.

## Historical reference — visual revision 02 / APK 0.9.0

Status: **revision 02 approved by the user and implemented in 0.9.0; visual validation on-device is pending**. The user asked for fewer curves, a stronger yellow leaning toward orange, and to keep cyan and off-white. Revision 02 follows that direction; the user approved it and asked for charts in the app. Physical validation of the 0.8.0 ledger is still pending and must not be treated as approved by this discussion.

![Three conceptual screens with fictional data](../../design/tique-interface-proposta-v2.png)

The [original SVG](../../design/tique-interface-proposta-v2.svg) is the editable source of the image. These are Android screen concepts, with no product code, bank data, competitor assets, or screenshots. Values, movements, and reconciliation states in the image are fictional.

## Visual direction

Dark, modern, geometric interface. Black predominates; vivid amber gold marks identity, actions, and selection; cyan communicates information, links, and income. Main values and text use warm off-white. An original angular symbol with eight faces and straight rays accompanies the Tique name. The brand uses simple typography; the final design can be refined later.

| Use | Color |
| --- | --- |
| Main background | `#080B10` |
| Cards | `#121820` |
| Raised surface | `#1B2430` |
| Amber gold / orange-yellow | `#FFB020` |
| Light cyan | `#9DECF4` |
| Main off-white text | `#F3EFE6` |
| Secondary text | `#A2AEBE` |
| Dividers | `#263240` |

Amber-gold buttons use black text and solid fill. Cards, fields, filters, and actions become rectangles with corners of about 4 dp; chart bars have straight ends. The central `+` button is also rectangular with a small radius, and icons use more angular strokes. Dark surfaces remain distinct through contrast and thin lines. Red/coral may indicate a negative difference or error, always alongside text/icon. Ordinary expenses must not be presented as errors.

Sans-serif typography, highly legible numbers, consistent value alignment, and space between sections. Implementation must respect Android font scaling, safe areas, and touch targets of at least 48 dp. Do not use color alone to distinguish income, expenses, selection, or cancellation. The composition shown is a reference, not a fixed screen size.

## Main navigation

Bottom bar with four destinations and a central action button:

| Destination | Content |
| --- | --- |
| **Home** | Selected month, calculated account balance, income, expenses, monthly result, categories, and access to recent transactions. |
| **Transactions** | History, search, filters by type/category/tag, details, and cancellation of a record. |
| **+ New** | Short form for income or expense. Other types appear when financially supported correctly. |
| **Review** | Pending observations/candidates, uncertain fields, original evidence, and explicit confirmation in the ledger. |
| **More** | Account, categories/tags, balance reconciliation, protection/backup, capture, and settings. |

The `+` button opens an action; it is not a fifth page. Review shows a count only when items are pending. Categories, tags, backup, and tests do not compete with the dashboard.

Under **More**, organize by task: **Finances** (account, categories/tags, reconcile balance), **Protection and data** (backup/recovery and, when ready, statement import), and **Settings** (capture/permissions, privacy/appearance, and advanced tools). Diagnostics, tests, and discarding vaults stay in the advanced area. Automatic copies remain the default; file-based tasks are optional.

Additional accounts, cards, bills, savings pots, and planning may enter a Wallet/Planning section when implemented. Do not show empty pages or disabled controls to simulate future features. Search and filters in the image are also interaction proposals, not features already delivered in the current APK.

## Dashboard composition

1. Accessible month selector at the top, with previous/next.
2. **Calculated available account balance** card, with a control to hide values and the actual status of the last reconciliation. Balance is current, while income/expenses/results refer to the selected month; labels must make that scope clear.
3. Two compact cards for monthly income and expenses; result appears separately as income minus expenses, without calling the difference wealth, profit, or guaranteed savings.
4. Spending by category in ordered horizontal bars, with amount and percentage. Tapping a category opens filtered transactions. Bars make values and long labels easier to compare; a doughnut chart may be a later alternative, but is not needed for the first interface.
5. Recent transactions after categories, accessible by scrolling. The static concept shows the first visible area; it is not a complete list of screen content.

Use confirmed ledger data only. Cancelled items do not enter totals; transfers and bill payments do not become expenses for charts. Empty months show an empty state, without invented data or fictitious trends. A reconciliation difference remains visible and does not automatically adjust the balance.

In Review, the summary opens the form to check amount, type, category, effective date, and account. The visual must not suggest that the parser recognized fields that remain unknown; “Review and confirm” does not bypass existing financial validations. Capture remains subject to the consent and limits of the installed version.

## Visual history

The [first proposal](../../design/tique-interface-proposta.png) and its [SVG](../../design/tique-interface-proposta.svg) are preserved for comparison. Revision 02 replaces soft gold and rounded cards with more saturated amber gold, off-white, and straighter geometry. Cyan and navigation destinations were retained. Version 0.9.0 applies the concept with charts, filters, and navigation; the images remain conceptual references with fictional data, not screenshots from the APK.

## References consulted

- [Organizze — official overview](https://www.organizze.com.br/): organizing accounts/movements and understanding spending by category. The reference is the clarity of financial tasks.
- [Wallet — dashboard and cards](https://support.budgetbakers.com/hc/en-us/articles/7150077480850-Add-or-Modify-Dashboard-Cards-Widgets): arranging an overview into cards. Drag-to-reorder customization is outside this initial proposal.
- [Mobills — category management](https://mobills.zendesk.com/hc/pt-br/articles/360051317613-Como-criar-categorias-para-despesas-e-receitas): access to categories from More and distinction between income and expenses.

These are conceptual references. No code, icons, logos, screenshots, or interface text from these apps was reused. Services and integrations of the reference products are not part of Tique's technical proposal.
