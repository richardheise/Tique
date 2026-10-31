# Tique

[Português](README.md) · **English** · [Contributing](CONTRIBUTING.en.md) · [Documentation](docs/en/README.md)

A free software Android app for tracking personal financial activity in Brazil. **Tique** (Τύχη, *Tyche*) is the Greek goddess of fortune. The former FFortuna name remains in internal identifiers to preserve updates and existing data.

<img src="assets/branding/icon.png" alt="Tique symbol: two gold squares and a central diamond on black" width="96" />

**[Download Tique 1.1.2](https://github.com/richardheise/Tique/releases/download/v1.1.2/tique-arm64.apk)** · [Release notes and limitations](docs/en/releases/v1.1.2.md). Update over the existing installation without uninstalling. The app tracks known records; it does not guarantee a complete replica of your bank balance.

## What the APK supports

- Analytics as the opening screen and Movements as the second tab; category charts and monthly comparisons.
- Manual income/expenses and individual or batch review of notifications and account CSV/OFX statements.
- Categories, tags, and local description or merchant rules. A place can have alternate names and its own tag, with an optional category.
- Savings pockets with an investment type, reported position, deposits, withdrawals, transfers between pockets, and explicit adjustments.
- Cards, purchases, closing/due dates, and partial bill payments. A purchase is an expense once; paying the bill does not count it again.
- Audited voiding that retains the original and reason; inconsistent dependencies block the operation without partial writes.
- A native SQLCipher ledger, automatic local recovery, and password-protected portable backups. Financial backups include confirmed records and configuration, excluding pending items and raw notifications.
- Consented daily Android capture, persisted before processing and independent of JavaScript. Field suggestions retain provenance and human corrections.

Pix is a payment method, not income or expense. Notifications can be missing or incomplete: **there is no Nubank parser certified against real samples or new automatic posting**. The release APK has no backend, external account, app telemetry, banking credentials, or INTERNET permission. Opening Nubank is only a shortcut; the app does not read screens or private operations.

Installments, financial refunds, shared expenses, and complete forecasts remain future work. Savings pockets are reported positions without automatic yield calculations. Card cycles use days 1–28; check your bill. See [release notes](docs/en/releases/v1.1.2.md) and the [glossary](GLOSSARY.en.md).

## Develop and test

Use Node 24 (`.nvmrc`). Expo 57, React Native 0.86, and React 19.2 are pinned. Check compatibility before upgrading dependencies.

```sh
nvm use
npm ci
npm run check
```

### Demo — Expo Go

```sh
npm start -- --lan
```

Computer and phone must share a network; open the QR code in an SDK 57 compatible Expo Go. Over SSH, `npm start -- --tunnel` uses a development relay. The demo accepts **invented data only**, in unencrypted local SQLite. It does not capture real notifications or import personal financial files. Expo Go cannot run the custom Kotlin module.

### Local Android APK

No EAS, Expo account, or paid service is required. Configure JDK/SDK and signing as described in [native capture](docs/en/captura-nativa.md). Preserve existing keys and databases.

```sh
npm run native:build -- --compile-device-tests
```

Output: `artifacts/tique-arm64.apk`. The script limits the build to 12 GiB/16 CPUs. Do not run native builds in parallel.

With an Android x86_64 emulator connected, instrumented tests use:

```sh
npm run native:test -- --run-device-tests
```

This mode reserves up to 8 GiB/12 CPUs for Gradle and 4 GiB/4 CPUs for the emulator. Stop the emulator before the ARM64 build. Expo Go, Metro, downloads, and development tools may use the network; this is separate from the final APK's offline operation.

In 1.1.1, **46 TypeScript, 72 JVM, and 87 Android tests** passed. Android tests ran on an Android 16 x86_64 emulator; this is not physical Samsung A36 validation or testing with real Nubank notifications. Each release's results are recorded in its notes. Compiling tests is not executing them. Users are not required to run another manual test battery.

## Architecture and privacy

A modular monolith. In the real APK, Kotlin owns the ledger, migrations, and serial executor; the TypeScript interface uses Expo Modules API. An encrypted evidence inbox feeds reviewable candidates and one financial ledger with balanced entries, safe integer cents, links, and idempotent commands. Publication, capture, and effective transaction dates are distinct. Equal amounts and dates do not prove duplication.

| Location | Responsibility |
| --- | --- |
| `modules/ffortuna-capture/` | Android capture, parsing, SQLCipher ledger, rules, migrations, and recovery. |
| `src/capture/` | Real-data interface, review, classification, resources, and analytics. |
| `src/core/`, `src/application/`, `src/processing/`, `src/persistence/`, `src/demo/` | Synthetic demo domain and service; they do not synchronize a second real ledger. |
| `app/`, `src/ui/`, `src/legal/` | Navigation, typography, components, and notices. |

Daily originals have a 30-day retention limit, cleaned when the core runs. Structured observations and confirmed records persist. The old diagnostic mode is separate, optional, and temporary. Force-stop, reboot, access revocation, and Android policies can interrupt capture. Biometric UI locking does not replace encryption.

Internal copies are lost on uninstall. To move phones, export a password-protected backup and keep both the file and password. **Never upload real notifications, statements, databases, backups, keys, or credentials to GitHub.** Use invented fixtures and remove personal information from bug reports.

## Contributing and documentation

Issues and contributions are welcome in Portuguese or English. Read [CONTRIBUTING.en.md](CONTRIBUTING.en.md), browse the [documentation](docs/en/README.md), and use the [issue forms](https://github.com/richardheise/Tique/issues/new/choose). Portuguese is the current interface language; bilingual documentation does not imply app localization.

[Continuity](docs/en/continuidade.md) · [Architecture decisions](docs/en/adr/) · [Dependencies](docs/en/dependencias.md) · [License review](docs/en/revisao-licencas-apk.md)

## License

Original code is [AGPL-3.0-only](LICENSE). Dependencies and fonts retain their licenses: [notices](THIRD_PARTY_NOTICES.en.md), [APK inventory](legal/apk-components.json), and [review](docs/en/revisao-licencas-apk.md). Releases include the APK, corresponding source, notices, and checksums. Space Grotesk uses OFL-1.1.

Finan+ and PennyWise are conceptual references; no code or assets from them were incorporated. There is no affiliation with those projects or Nubank. Code identifiers and original license terms are not translated.
