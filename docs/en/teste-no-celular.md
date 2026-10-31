[Português original](../teste-no-celular.md)

# Test the demo on Android

On 22/10/2026, the user reported testing and approving the demo on a phone, without specifying the device or individual steps. The later dependency fix still needs installation and verification.

This procedure uses invented values only. It is a procedure to perform on a device, not a report that a physical test has already been carried out. The delivery runs inside Expo Go; there is not yet a FFortuna-specific APK.

## Prepare

On the computer, use Node 24 and run:

```sh
cd /home/heise/FFortuna
nvm use
npm ci
npm start -- --lan
```

Install an Expo Go version compatible with SDK 57 on Android, keep the computer and phone on the same network, and scan the QR code. An Expo account is not required. If the SDK is incompatible, see [Expo Go for Android](https://expo.dev/go). Do not update packages individually to try to fix this.

If the LAN connection fails, check Wi-Fi isolation, VPN, and access to the port shown by Metro (usually 8081). Metro must remain active to serve/reload this development version. This differs from the future dedicated APK, which should work without a computer.

If the server is remote and you are connected through SSH, use `npm start -- --tunnel`. The CLI may request the ngrok package; the global install command is `npm install --global @expo/ngrok`. The server and phone need internet access, but not the same network. The tunnel is an external development service.

The **DEMONSTRAÇÃO · Só dados fictícios** badge (**DEMO · Fictional data only**) should remain visible. The app must not request notification access, bank login, or selection of a personal statement.

## Main flow

Start in a new demo ledger. Use today's date for all entries in this flow and select the current month at the top. Do not tap simulation buttons again except where indicated: each tap creates a new observation.

| Action | Expected result |
| --- | --- |
| In **Resumo** (Summary), enter opening balance `1.000,00`, opening date today, and expected income `3.000,00`. Tap **Criar meu resumo** (Create my summary). | Balance R$ 1.000,00; expected income R$ 3.000,00; income and expenses zero. |
| In **Novo lançamento** (New entry), choose **Receita** (Income), amount `500,00`, description “Renda fictícia” (Fictional income), and category “Renda” (Income). Save. | Balance R$ 1.500,00; income R$ 500,00. |
| Add an **Despesa** (Expense) entry of `100,00`, description “Mercado fictício” (Fictional market), and category “Alimentação” (Food). | Balance R$ 1.400,00; expenses R$ 100,00; Food category R$ 100,00. |
| In **Revisão** (Review), tap **Simular seguro** (Simulate safe) once. | Fictional coffee for R$ 19,90 posted automatically; balance R$ 1.380,10. |
| Tap **Simular revisão** (Simulate review). | Pending candidate with no balance effect. Amount and nature are not confirmed. |
| Choose Expense, amount `15,00`, description “Café corrigido” (Corrected coffee), and Leisure category. Confirm. | Balance R$ 1.365,10; expenses R$ 134,90; this pending item is no longer in the inbox. |
| In **Histórico** (History), open **Ver evidência da origem** (View source evidence) for the corrected coffee. | Content is marked synthetic; extraction and human decision are distinct. |
| In Review → **Últimos processados** (Recently processed), tap **Reprocessar** (Reprocess) on the corrected event. | Same balance and one entry; corrections preserved. |
| On the same event, tap **Atualizar evento fictício** (Update fictional event). | Conflict in the inbox; corrected entry remains R$ 15,00. Tap **Manter lançamento existente** (Keep existing entry). Balance unchanged. |
| Tap **Simular compra repetida** (Simulate repeated purchase) twice. | Two additional legitimate purchases of R$ 19,90 with distinct identities; balance R$ 1.325,30. |

To check the filter, open a different month at the top: income/expenses/history should follow the period; **saldo atual** (current balance) remains cumulative. An invalid month should show a message without changing the queried month.

## Savings pots and committed credit

Continue the previous flow in the current month:

| Action in **Meu dinheiro** (My money) | Expected result |
| --- | --- |
| New savings pot: name “Reserva fictícia” (Fictional reserve), type “CDB fictício” (Fictional CDB), initial position `200,00`, date today. | Account remains R$ 1.325,30; savings pot R$ 200,00. This is an already existing position. |
| In the pot, **Ação → Guardar / resgatar → Guardar** (Action → Save/withdraw → Save) `50,00`. | Account R$ 1.275,30; savings pot R$ 250,00; expenses unchanged. |
| Withdraw `20,00`. | Account R$ 1.295,30; savings pot R$ 230,00; income unchanged. |
| Update position: new value `240,00`, reason “Conferência fictícia” (Fictional check), date today. | Difference preview +R$ 10,00; savings pot R$ 240,00; account unchanged; no yield/income is assumed. |
| New debt: “Cartão fictício” (Fictional card), due today or later, amount `300,00`. | Debt R$ 300,00; account and expenses unchanged. |
| Record partial payment of `100,00`, date today. | Account R$ 1.195,30; remaining debt R$ 200,00; expenses unchanged. |
| Reopen the payment and pay the remaining `200,00`. | Account R$ 995,30; zero debt; no new expense. |

## Synthetic import and overlap

In Review → demo import, tap **Atual** (Current). The preview should contain income of R$ 500,00 and an expense of R$ 50,00. Review nature, category, description, and date; the OFX sign is a suggestion, not authorization for automatic posting.

In the flow above, both lines may appear as **Ambíguo** (Ambiguous): the R$ 500,00 income matches the manual income, and the R$ 50,00 expense matches a contribution on the same date. This does not prove duplication. For this procedure, treat the examples as different movements and explicitly select **Criar lançamento revisado** (Create reviewed entry) for each ambiguous line. Do not link the expense to the contribution. Ambiguous lines start ignored until chosen; in a real case, you would need to check whether they represent the same movement.

Confirm: expected balance **R$ 1.445,30**. Tap Current again: both lines should appear linked; confirming again keeps the balance unchanged. Tap **Com sobreposição** (With overlap): two lines already linked and one new R$ 25,00 expense. Confirm only the new one: balance **R$ 1.420,30**. The previous decision and metadata must remain. Run this part on the same day: fixture identities are stable for the reference date.

For earlier history, tap **Histórico** (History). There are two movements before today's opening: +R$ 500,00 and −R$ 300,00. The new opening must be **R$ 800,00**, before the first line, because R$ 800 + R$ 200 = the known initial position of R$ 1.000. Confirm acknowledgment that the balance predates the lines and import. Current balance should remain **R$ 1.420,30**. An inconsistent opening must block the entire operation, without a partial import. This example uses the original R$ 1.000,00 opening; other amounts need their own check.

## Persistence and completion

Fully close Expo Go, reopen the project with Metro active, and check balance, savings pots, debt, history, and links. This tests actual persistence integration on the device. Reloading the bundle should not erase the database; clearing Expo Go data or uninstalling it may. There is no backup in this demo.

Open **Sobre e licenças · AGPLv3** (About and licenses · AGPLv3), review AGPLv3 and Libraries, and close it. No financial or license query should require a FFortuna backend.

Record device model, Android/Expo Go version, actions performed, expected and observed results. Use synthetic screenshots only. The approval report does not document individual checks; next time, record layout/keyboard behavior, expo-sqlite execution in Expo Go, persistence after closing, and compatibility with the installed Expo Go.

## What has already been checked on a computer

- `npm run check`: strict TypeScript and 30 passing tests in an isolated copy (including 3 URL compatibility/security tests), with real SQLite through Node, file reopening, and migration.
- `npx expo install --check`: dependency set compatible with SDK 57.
- `npm run export:android`: Android/Hermes bundle generated; this does not produce an APK or prove Android execution.
- Additional interface flow in Chromium/RN Web with a real service and in-memory SQLite: forms, balance, corrections, reprocessing, conflict, equal purchases, savings pots, debt, overlap, and licenses. This test does not cover Android runtime.

Native capture, real encrypted database, Nubank package, real notification content, and Nubank files were not implemented/validated in this stage. The next increment is the native local proof on a phone, starting with synthetic data and encrypted persistence.
