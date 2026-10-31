[Português](../../adr/0004-nucleo-nativo-dono-do-banco.md)

# Kotlin core owns the database in real mode

The user accepted that the Kotlin core owns the single encrypted financial database in real mode, its migrations, evidence, recoverable processing, and atomic confirmations; the Expo/TypeScript interface uses typed module operations. Keeping the ledger only in TypeScript would make sharing code with the demo easier, but would make financial processing depend on the JavaScript runtime.

Consequences: use SQLCipher Community in the native core and keep a TypeScript/expo-sqlite demo adapter that is exclusively synthetic, without syncing ledgers between modes. The same contracts and test scenarios must validate behavioral equivalence; equivalence is not claimed before testing. Capture is persisted before complex processing, and migrations, serialized writes, and uniqueness are owned by the native core.
