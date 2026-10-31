[Português](../../adr/0001-expo-com-captura-kotlin.md)

# Expo with Kotlin capture integration

User-approved decision: use Expo/React Native/TypeScript to speed up interface validation and integrate Android capture in Kotlin. A fully Kotlin implementation would simplify integration and lifecycle handling, but would lose the initially requested Expo Go demonstration flow.

Consequences: maintain two languages and test the integration; custom native features require a custom build, while Expo Go is limited to the synthetic demonstration. [ADR 0004](0004-nucleo-nativo-dono-do-banco.md) defines Kotlin as the owner of the real database, migrations, and processing while the interface is closed. The demonstration uses a separate TypeScript service exclusively for synthetic data; this duplication requires equivalence tests before the real mode can be declared compatible.
