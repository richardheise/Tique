# Third-party dependency notices

[Português](THIRD_PARTY_NOTICES.md)

This broad inventory is historical. Current versions are in `package-lock.json` and the APK-specific review; the figures below do not replace that review.

Tique source code is AGPL-3.0-only. The full original text is in `LICENSE` and the `appLicense` field of [`src/legal/notices.json`](src/legal/notices.json).

The npm inventory follows the production dependency closure of `package-lock.json`, including optional and peer dependencies. It also includes build tools; the APK review separately uses the source map and Android release graph recorded in [legal/apk-components.json](legal/apk-components.json).

The JSON collects package LICENSE/NOTICE texts and verified, versioned upstream supplements in [legal/npm-supplements.json](legal/npm-supplements.json). Licenses are not automatically inherited from parent directories. Identical texts are deduplicated while retaining provenance and copyright notices.

| Direct dependency | Lockfile version | License metadata | Upstream source |
| --- | --- | --- | --- |
| `decode-uri-component` | 1.0.0 | AGPL-3.0-only | (source not declared in package.json/lockfile) |
| `decode-uri-component-fixed` | 0.5.0 | MIT | SamVerschueren/decode-uri-component |
| `expo` | 57.0.31 | MIT | https://github.com/expo/expo |
| `expo-constants` | 57.0.22 | MIT | https://github.com/expo/expo |
| `expo-crypto` | 57.0.4 | MIT | https://github.com/expo/expo |
| `expo-linking` | 57.0.13 | MIT | https://github.com/expo/expo |
| `expo-router` | 57.0.29 | MIT | https://github.com/expo/expo |
| `expo-sqlite` | 57.0.4 | MIT | https://github.com/expo/expo |
| `expo-status-bar` | 57.0.1 | MIT | https://github.com/expo/expo |
| `react` | 19.2.3 | MIT | https://github.com/facebook/react |
| `react-dom` | 19.2.3 | MIT | https://github.com/facebook/react |
| `react-native` | 0.86.3 | MIT | https://github.com/react/react-native |
| `react-native-gesture-handler` | 2.32.0 | MIT | https://github.com/software-mansion/react-native-gesture-handler |
| `react-native-reanimated` | 4.5.1 | MIT | https://github.com/software-mansion/react-native-reanimated |
| `react-native-safe-area-context` | 5.7.0 | MIT | https://github.com/AppAndFlow/react-native-safe-area-context |
| `react-native-screens` | 4.26.2 | MIT | https://github.com/software-mansion/react-native-screens |
| `react-native-worklets` | 0.10.1 | MIT | https://github.com/software-mansion/react-native-reanimated |

The npm inventory contains 570 entries and 46 missing texts among tools/optional dependencies, detailed in the JSON. This is not the APK unresolved-notice count. Binary coverage is in the [review report](docs/en/revisao-licencas-apk.md).

The Android proof also includes [native notices](src/legal/native-notices.json), displayed alongside this inventory in the interface. The npm inventory is not a complete inventory of APK Maven/NDK artifacts.

Regenerate the inventory after dependency changes:

```sh
node scripts/generate-notices.mjs
```
