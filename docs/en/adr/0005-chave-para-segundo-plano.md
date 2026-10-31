[Português](../../adr/0005-chave-para-segundo-plano.md)

# Key usable by the core while the screen is locked

The user accepted that the core may use the key without per-operation biometrics, so it can capture and process in the background with the screen locked after the first unlock since reboot. Requiring authentication to use the key would reduce availability and pause database access while unauthorized.

Consequences: protect the random SQLCipher key with a non-exportable wrapping key in Keystore and handle the interface's biometric lock separately; it does not prevent the service from accessing the database. The native implementation must have the database key available in memory. Do not promise capture before the first unlock; test behavior on the device and offer a portable encrypted backup independent of the local key before accepting real data.
