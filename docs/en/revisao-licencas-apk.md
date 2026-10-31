[Português original](../revisao-licencas-apk.md)

# License review — synthetic-capture APK

Technical review completed on 23/10/2026 and rechecked on 26/10/2026 for version 0.6.0 of the release, arm64-v8a `capture-poc` APK. No outstanding license-text or attribution items were found in the inventory identified below. This is not validation of operation on a phone or legal certification of any future distribution.

## Scope and evidence

| Examined input | Coverage |
| --- | --- |
| Metro/Hermes combined source map | 63 npm packages with JavaScript code. |
| React Native/Expo autolinking | 22 packages with native integration; combined with JavaScript: 71 packages, including the original FFortuna module. |
| Android release graph and POMs | 189 Maven coordinates, including BOMs/metadata. This is a conservative set; it does not mean every complete artifact is distributed. |
| AAR/JAR files in the graph | LICENSE/NOTICE files, including those inside `classes.jar`; 53 notice occurrences extracted. |
| APK `.so` libraries | All 23 linked to the published artifact or to the project that compiles them. |
| Embedded C/C++ components | 24 supplemental records for libraries, subcomponents, and notices. |
| Resulting texts | 81 distinct texts, deduplicated without removing attributions. |

The verifiable inventory is in [apk-components.json](../../legal/apk-components.json), with versions, artifact hashes, text sources, and pending items. npm, Maven, C/C++ supplements, and extracted notices are in `../../legal/`. A parent-folder license is not automatically inherited; POMs follow only explicit Maven parent inheritance. Monorepo texts were associated with published versions/commits.

The broad production npm inventory includes tools and optional dependencies that are not part of this APK. Of the 53 gaps initially identified there, seven belonged to bundled JavaScript: four React Native subpackages, Expo Router, `@expo/ui`, and `standard-navigation`. Upstream texts with versioned evidence were added for all seven. The remaining 46 gaps in that broad inventory do not appear in the package union identified for this APK. This does not remove the need to review again if the distribution or its dependencies change.

In `standard-navigation@0.0.5`, the published package already declared MIT, but its gitHead did not contain a LICENSE. The text came from the later upstream commit that fixed the omission, with that chronology explicitly recorded in [npm-supplements.json](../../legal/npm-supplements.json). That file was not retroactively attributed to the published commit.

## Obligations covered

- Original FFortuna code: AGPL-3.0-only, full text in `../../LICENSE` and in the APK. Distribute source corresponding to the same APK revision, with files required to build it; pointing only to a branch that may later change is insufficient.
- MIT/ISC/BSD/Apache dependencies and icon fonts: texts and copyrights preserved. Specific notices found in distributed files were retained in addition to generic license text. Material Symbols fonts also retain Apache 2.0.
- SQLCipher Android/core, LibTomCrypt, and NDK C++ runtime: specific texts included. The SQLCipher used here employs LibTomCrypt; OpenSSL was not assumed. For Zstd, the BSD alternative made available upstream was selected.
- IJG: the acknowledgment required by libjpeg is included along with its other texts.
- OkHttp 4.9.2 Public Suffix List: MPL-2.0 and NOTICE preserved; the corresponding source is at [public_suffix_list.dat](../../legal/source/okhttp-4.9.2-public_suffix_list.dat). The compressed APK resource matches the upstream tag, SHA-256 `a1ef3d0e4ec2cbce14c1884ab0f3a4fef46e258de83530dc562ebb805bdb12fd`.

License/NOTICE preservation rules come from the texts themselves, including [Apache 2.0, section 4](https://www.apache.org/licenses/LICENSE-2.0), [AGPL v3](https://www.gnu.org/licenses/agpl-3.0.html), and [MPL 2.0](https://www.mozilla.org/en-US/MPL/2.0/). No code from Finan+ or PennyWise was copied.

The reviewed notices are available under **About and licenses → Libraries** in capture mode. They are also included as assets `../../assets/ffortuna-legal/THIRD_PARTY_NOTICES.txt` and `../../assets/ffortuna-legal/LICENSE.txt`, compared byte for byte with local files during APK inspection. The Expo Go demo continues to show the broad npm inventory.

## Repeat and update

With Node 24, dependencies installed, toolchain and build outputs available:

```sh
node scripts/generate-notices.mjs
python3 scripts/audit-apk-notices.py --check
npm run typecheck
```

`--check` compares the inventory, interface texts, and assets with current inputs without regenerating them. On another machine, parent POMs missing from cache may need to be downloaded from Maven Central. This belongs to review/build, not APK operation.

After changing dependencies, first obtain the new release source map/graph. Run `python3 scripts/audit-apk-notices.py` to update the texts, investigate any unresolved component, and rebuild with `npm run native:build`. Final inspection requires notices compatible with the APK and stops delivery if it finds a mismatch. A dependency change may make the first build fail this check until texts are updated; this is expected. By default, the auditor uses the previous APK; `--apk path.apk` lets it inspect the newly generated file at `../../android/app/build/outputs/apk/release/app-release.apk`.

## Artifacts and publication

Rebuilt 0.3.0 APK with notices, backup, and restore: `../../artifacts/ffortuna-capture-poc-arm64.apk`, 46,147,788 bytes, SHA-256 `46778d77d2294031697f99f52a736f0f218341564f9859b4732e7779597b42f6`.

Verified: TypeScript, inventory with no identified outstanding items, notices inside the APK, signature, disabled backup, offline bundle, no INTERNET permission, and ELF/ZIP 16 KB alignment of the 23 libraries. After fix 0.2.1, the user reported approving the manual procedure on a Galaxy A36/Android 16 on 25/10/2026. On the same date, the user also approved all eight steps of the manual 0.3.0 backup/restore procedure on that device, including reinstalling. Instrumented tests remain unexecuted.

Local artifacts include separate notices, a file with current source, and hash receipt in `../../artifacts/license-review.json`. The source file excludes local tools, databases, keys, credentials, and build outputs. Dependencies are pinned by the lockfile and build files; no byte-for-byte reproducible build is claimed.

Version 0.3.0 uses only Android JCA cryptographic APIs and Expo activity contracts already installed, without new distributed dependencies. The inventory and notices were rechecked and still have no identified outstanding items.

The license review had not published a release/tag. At a later checkpoint, the user authorized commit/push and evaluation of prerelease `v0.3.0-alpha.1`. The [prerelease](https://github.com/richardheise/Tique/releases/tag/v0.3.0-alpha.1) was published after authenticating GitHub CLI, with nine attachments and their remote hashes checked. Source corresponds to commit `11174020cc5df9236041674fd44783b2f3225a2c` from tag `v0.3.0-alpha.1`; later documentation updates do not change that snapshot. If code changes after this checkpoint, rebuild and update source/checksums before attaching. The name and notes must explicitly say **synthetic capture only**, with no Nubank support or real financial data.

## Recheck 0.4.0

APK 0.4.0 (versionCode 4), 46,207,716 bytes, SHA-256 `0ef198733e42f5e227a7b7bba4e7a22096da3d76603f618d801142fd68977085`. Automatic recovery/retention and the isolated test battery use existing Android/JCA APIs; no dependencies were added. Inventory rechecked during final inspection: 71 npm packages, 189 Maven coordinates, 23 native libraries, 24 C/C++ supplements, and zero identified outstanding items. Notices inside the APK match the reviewed files. Signature matches 0.3.0; package and Android requirements are unchanged. Absence of READ_LOGS and broad storage permissions was also checked.

Strict TypeScript, 30 TypeScript tests and 22 JVM tests passed; 28 device tests compiled, not executed. On 26/10/2026 the user approved the 0.4.0 procedure on A36/Android 16 and reported 30 interface checks passed, zero failures, and zero unverified. This was execution of callbacks through the interface, reported by the user, without logs; the JUnit runner was not run. The new distribution uses `v0.4.0-alpha.1`, without moving tags or replacing previous attachments. Exact source, receipt, licenses, and checksums accompany the APK.

The [v0.4.0-alpha.1 prerelease](https://github.com/richardheise/Tique/releases/tag/v0.4.0-alpha.1) was published with nine attachments, checked for size and remote digest. Source corresponds to commit `697fee04fa083de77c5903c18258986ca3ff1a47`; public APK download returned HTTP 200. The tag and 0.3.0 attachments remain intact.

## Recheck 0.5.0

APK 0.5.0/versionCode 5: 46,266,508 bytes, SHA-256 `45e1132681c0335b2b1112e16a215bada3fcfc26fcc99026c5b8986da1d44a38`. Local diagnostics use existing Android/JCA APIs, without new dependencies. Inventory rechecked during final inspection: 71 npm packages, 189 Maven coordinates, 23 native libraries, and 24 C/C++ supplements, with no identified outstanding items. Notices/AGPL in the APK match reviewed texts byte for byte. Source, licenses, receipt, and checksums must accompany the new tag.

Strict TypeScript, 30 TypeScript tests, and 34 JVM tests passed; 38 device tests compiled, not run by the runner on this server. The user approved 0.4.0. On 30/10/2026, the user also approved the functional 0.5.0 flow on A36/Android 16, including capture, correct original, and review/export; the user did not report this version's battery counters. This is optional, consent-based real-data collection without a parser or transaction posting: do not label the entire APK synthetic-only. No real data was collected in the development environment or added to attachments.

The [v0.5.0-alpha.1 prerelease](https://github.com/richardheise/Tique/releases/tag/v0.5.0-alpha.1) was published on 26/10/2026 with nine attachments, checked by size and remote SHA-256. The public download returned HTTP 200. Source corresponds to tag commit `72b1491203e081f56fb3d75cdc5db4e82671a4f6`, with 184 files checked byte for byte. The user's functional approval of 0.5.0 was reported on 30/10/2026; publication itself is not testing, and the Android JUnit runner remains unexecuted.

## Recheck 0.6.0

APK 0.6.0/versionCode 6: 46,301,268 bytes, SHA-256 `b82bd30d7fe4ba79fd9a7a38f6cbd8acb6016762ef747a9f179997fdcb983815`. The generic BRL extractor, migration, and review use existing dependencies, with no additional components. Inventory rechecked during final inspection: 71 npm packages, 189 Maven coordinates, 23 native libraries, 24 C/C++ supplements, and zero identified outstanding items. Notices and AGPL inside the APK match reviewed texts.

Strict TypeScript, 30 TypeScript tests, and 50 JVM tests passed; 46 Android scenarios compiled, with no runner execution on this server. Version 0.6.0 awaits the 48-check battery on the A36 and the new review procedure. Generic parsing is not validated Nubank support. Previews/corrections remain temporary, with no financial ledger or automatic posting. No real sample was used in the development environment or attachments.

## Rebrand to Tique — 0.6.1

On 30/10/2026, the user requested renaming the app/repository to Tique. Current URL: https://github.com/richardheise/Tique. The change does not alter the AGPL-3.0-only license or third-party notices. Only the notices file's own preface and source link were updated; the remainder of the license text was compared and preserved. The auditor `--check` approved the inventory with no dependency changes or identified outstanding items.

The new APK uses `tique-arm64.apk`, and the source uses `tique-source.tar.gz`. The Android namespace, formats, and legacy internal paths remain so users can update without data loss. The previous 0.6.0 snapshot was preserved in `../../artifacts/releases/v0.6.0-alpha.1/`; do not replace its attachments or move the tag. Final inspection and new hashes accompany delivery 0.6.1.

Final result 0.6.1: 50 JVM tests and 30 TypeScript tests passed; 46 Android scenarios compiled, with no runner execution. APK `tique-arm64.apk` is 46,301,248 bytes, SHA-256 `2c4e61ca9a66526c26cf4ab6d022ebd851c2c35ca9d56c28b5da18595baf34e9`. Tique name and matching signature verified; permissions/backup/offline/16 KB inspection and notice comparison approved. The new tag includes matching source and preserves previous distributions.

The [Tique v0.6.1-alpha.1 prerelease](https://github.com/richardheise/Tique/releases/tag/v0.6.1-alpha.1) was published with nine attachments checked by size/remote digest and public HTTP 200 download. Tag source commit `0d42e2abc3e0f8d2527d9e7e5760182535631dd2`, 194 files checked byte for byte. Repository renaming did not replace previous tags or attachments. Publication is not device validation.

## Increment 0.7.0 — categories and tags

Catalog and selections use existing SQLCipher/Keystore and Expo Modules libraries, without new dependencies or permissions. Final APK inspection and auditor `--check` approved: 71 npm packages, 189 Maven coordinates, 23 native libraries, 24 C/C++ supplements, and zero identified outstanding items. Notices and APK license texts checked byte for byte; signature preserved, Android backup disabled, bundle offline, no INTERNET, and ELF/ZIP 16 KB alignment approved.

Prerelease `v0.7.0-alpha.1` published, source `b3a2dbad5a3c6e7bbeb595e42a8f386dc2e58d0f`, 203 files checked. APK 0.7.0/code 8, 46,327,692 bytes, SHA-256 `04cc41bcafa659303bf70ac2aba3a514c3c681fb6a444cd33b7da0f19164b8a3`. Nine remote attachments with sizes/hashes checked; public API and HTTP 200 download without authentication. 30 TypeScript and 50 JVM tests passed; 58 Android scenarios compiled, not run by JUnit. The 60-check interface battery and functional procedure for this version await physical validation. Functional approval reported by the user in this session refers to 0.6.1, with no individual counters. Catalog and real reviews remain outside synthetic backups, an explicit limitation in the interface and ADR 0010.

## Fix 0.7.1 — classification-query arguments

Fixes null parameters for `rawQuery(String[])` without new dependency, permission, schema, or key. APK inspection and audit approved, same inventory and notices, same signature, no INTERNET, Android backup disabled, and 23 libraries aligned to 16 KB. APK 0.7.1/code 9, 46,327,700 bytes, SHA-256 `43b3cfc0f7b380fffac12a66484b6f079719649a66cac86bc4e2bca27de2b4d5`. 50 JVM tests passed and 58 Android scenarios compiled; Android JUnit was not run. TypeScript is unchanged since the 30 tests passed for 0.7.0, with no new run for this hotfix. Source `aab75fb65a540b35e2a3eb8855840640725c9181`, 204 files checked; nine remote attachments with sizes/hashes verified and public HTTP 200 download. The user reported nine failures in 0.7.0; 0.7.1 awaits rerunning the 60 checks and device procedure. Previous tags and attachments are preserved.

## Increment 0.8.0 — ledger and financial backup

No new dependencies: ledger, snapshots, and restore use existing SQLCipher, Keystore/JCA, and Expo Modules; interface UUID uses existing expo-crypto. Final APK auditor `--check` retained 71 npm packages, 189 Maven coordinates, 23 native libraries, and 24 C/C++ supplements, with zero identified notice gaps. APK notices and AGPL checked byte for byte. Package and certificate preserved, visible name Tique, Android backup also disabled in transfer rules, bundle offline, no INTERNET, and libraries aligned to 16 KB.

54 JVM and 30 TypeScript tests passed; 70 Android scenarios compiled. The 72 local interface checks and functional procedure still need to run on the device for 0.8.0. There are no real samples in the environment and no validated Nubank parser. APK 46,420,924 bytes, SHA-256 `3510dd68ce32ee74461c80d8bd6ac7caff5c40d7460ccb9b3a9d39007c73d979`. Matching source, receipts, licenses, and checksums must accompany the new tag while preserving old tags and attachments.

Final publication: `v0.8.0-alpha.2`, APK versionCode 11, source `3cd74d93a82c96b3a48b5735679375ec86db9f32` (214 files checked byte for byte). Nine remote attachment sizes/hashes checked before publication; public HTTP 200 download without authentication. The first candidate, `v0.8.0-alpha.1`, was preserved as an unpublished draft to adjust catalog reload after restore; no tag or historical asset was replaced. Version 0.8.0 still awaits the battery and device procedure.

## Increment 0.9.0 — dashboard and navigation

No new dependencies: charts and custom icons use native components, with no external library or competitor assets. Final APK inspection approved notices and AGPL byte for byte, preserved signature, offline bundle, disabled Android backup, no INTERNET, and 16 KB alignment for all 23 libraries. Inventory retained: 71 npm packages, 189 Maven coordinates, 24 C/C++ supplements, and zero identified notice gaps.

APK 0.9.0/code 12, 46,444,824 bytes, SHA-256 `b0db3dc82bdc86fbc6424835691d3d54e00c34c9428ba03cdc78793f152f8553`. Strict TypeScript and 38 TypeScript tests, plus 54 JVM tests, passed; 70 Android scenarios compiled. The 72 interface checks, financial flows, and new navigation await validation on the A36. Android JUnit was not run on this server. The auxiliary preview with HTML primitives is not React Native execution. [Delivery procedure](releases/v0.9.0-alpha.1.md). Tag-matching source and nine attachments accompany publication; earlier distributions remain intact.

Prerelease [`v0.9.0-alpha.1`](https://github.com/richardheise/Tique/releases/tag/v0.9.0-alpha.1) published with nine attachments; sizes and remote digests checked, public API and HTTP 200 download without authentication. Source corresponds to commit `8c32fcdc8b428058496197837eb0fe1803950acc`, 224 files compared byte for byte. Source archive hash `5ca09ae9f075e0ba251f0e64b73acbc38a6357b8eecc73aa53f8b53d67a4521d`; release ID `429567586`. Local snapshot preserved. Publication is not physical validation.

## Increment 0.10.0 — daily use and optional statements

No new dependencies, third-party code/assets, or external services. `--check` inventory retained: 71 npm packages, 189 Maven coordinates, 23 native libraries, 24 C/C++ supplements, and zero identified notice gaps. APK notices/AGPL checked byte for byte; signature preserved, bundle offline, Android backup disabled, no INTERNET, and libraries aligned to 16 KB.

APK 0.10.0/code 13: 46,484,428 bytes, SHA-256 `fd044bc9ee76cc483d62661224ecd483909ca8af4e6059c89a4e9be42d037452`. Strict TypeScript and 38 TypeScript tests, 61 JVM tests passed; 76 Android tests compiled, not run on the server. Source map compared with 12 final capture/interface files. Physical evaluation is through everyday use, with no new procedure or approval assumed. [Notes](releases/v0.10.0-alpha.1.md). Exact tag source and nine attachments accompany distribution; previous releases are preserved.

Prerelease [`v0.10.0-alpha.1`](https://github.com/richardheise/Tique/releases/tag/v0.10.0-alpha.1) published with nine attachments, remote sizes/digests checked, and public HTTP 200 download. Source `72a6442a6700494491fec2676688ef56aa70f416`, 235 files checked; source SHA-256 `3a2b47f19db39eaac1abe434265e5a16eac47b9c3d14b54010a93a8c5b4533f2`, release ID `429680753`. Local snapshot preserved. Publication is not Android execution; everyday-use evaluation is pending.

## Tique 1.0 — 31/10/2026

Original Space Grotesk (Regular, Medium, Bold) by Florian Karsten included as local assets, with OFL-1.1 license in `../../assets/fonts/OFL.txt` and APK notices. Expo-font aligned with SDK 57; decoder fixed in 0.5.1 and node-forge in 1.4.2. Actual inventory: 63 JavaScript packages, 22 native npm modules (70 distinct npm packages), 189 Maven coordinates, 23 native libraries, 24 C/C++ supplements, 82 deduplicated texts, and zero identified outstanding notice items. Inspection confirms the three font files byte for byte in the APK. Do not confuse notice coverage with a full security audit; remaining npm alerts are described in release notes.
