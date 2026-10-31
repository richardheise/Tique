# Contributing to Tique

[Português](CONTRIBUTING.md) · **English**

Contributions, questions, and bug reports are welcome in Portuguese or English.

## Reporting a problem

Search [existing issues](https://github.com/richardheise/Tique/issues) and read the [README](README.en.md) and your version's release notes. Open the [form](https://github.com/richardheise/Tique/issues/new/choose), provide the Tique version, device/Android version, and describe expected and actual behavior. Updating is optional when updating itself is the problem.

Use invented examples. **Do not send real notifications, statements, databases, backups, people's names, account numbers, keys, passwords, or tokens.** Screenshots must hide personal and financial information. Logs are optional: share only reviewed technical excerpts without financial content. Never erase your data to reproduce a bug.

The checkboxes follow the careful-reading idea of [end-4's template](https://github.com/end-4/dots-hyprland/blob/main/.github/ISSUE_TEMPLATE/1-issue.yml), with original wording adapted to Tique. The “checked without reading” option is optional; there is no bot automatically closing issues because of it.

## Environment setup

Read the [README](README.en.md), [decisions](docs/en/adr/), and [continuity notes](docs/en/continuidade.md). Use Node 24, respect the lockfile, and preserve existing changes.

```sh
nvm use
npm ci
npm run check
```

Expo Go is a demo using exclusively synthetic data. Real mode requires a custom APK, Kotlin/SQLCipher, and the local toolchain described in [native capture](docs/en/captura-nativa.md). No EAS, Expo account, or paid service is required. Do not run native builds in parallel; the script limits resources. Do not replace existing signing keys.

## Proposing a change

For large changes, explain the problem and intended behavior in an issue before expanding scope. Make small changes with validation appropriate to the behavior. A pull request should explain the problem, outcome, checks actually run, and known limitations. Distinguish tests written, compiled, and executed; emulator results do not establish physical-device validation.

Preserve these invariants:

- One real, local, offline financial ledger; no required backend, telemetry, banking credentials, or private APIs.
- Safe integer cents, atomic operations, stable IDs and idempotent commands, retained evidence and human corrections.
- Transfers, deposits/withdrawals, and bill payments must not inflate income or consumption. Card purchases are expenses once.
- Notifications are not complete statements. Publication dates do not prove effective transaction dates. Equal amounts/dates do not prove duplicates.
- Real data belongs only in the app's protected storage, never fixtures, logs, plaintext temporary files, or Git.
- Release APKs have no INTERNET permission; development-tool access is separate.

Do not use `npm audit fix --force` or upgrade the SDK for convenience. Check official compatibility, lockfiles, and the license inventory when changing dependencies.

## Documentation and images

Keep Portuguese and English versions equivalent. Originals live at the root and under `docs/`; English versions use `.en.md` at the root and `docs/en/`. Preserve commands, identifiers, hashes, version names, and historical facts. Old notes describe their own release; do not turn compiled tests into executed tests while translating.

The symbol is the one used in the header. To regenerate SVGs and PNGs from the symbol geometry, install `rsvg-convert` as a local development tool and run:

```sh
node scripts/generate-app-icons.mjs
```

Do not add third-party trademarks, assets, or code without checking licensing. Preserve original license and notice texts; explanatory translations do not replace them.

## Licensing and releases

Contributions are distributed under [AGPL-3.0-only](LICENSE); retain third-party notices. No additional CLA is required. Releases must include the APK, corresponding source, notices, and checksums. Published tags and assets are immutable; fixes use a new version/code while preserving package, certificate, and migrations. Never upload private signing material.
