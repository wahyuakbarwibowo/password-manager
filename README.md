# Aminmart Password Manager

Offline password manager for Android. Your vault stays on the device, encrypted with AES-256-GCM using a hardware-backed Android Keystore key. No account, no cloud sync, no tracking.

**Website & APK download:** https://wahyuakbarwibowo.github.io/password-manager/

## Features

- **Master password** vault, verified with PBKDF2 (100k iterations)
- **Biometric unlock** (optional, enabled from Settings)
- **Password generator** with strength indicator
- **Categories and search**
- **Encrypted backup** export/import to a file you control
- **Copy to clipboard**
- Material 3 UI built with Jetpack Compose

## Security

| Concern | Implementation |
|---|---|
| Entry encryption | AES-256-GCM, 96-bit nonce per operation, 128-bit auth tag |
| Key storage | Android Keystore (hardware-backed when available); key never leaves the Keystore |
| Master password | PBKDF2WithHmacSHA256, 100,000 iterations, 256-bit salt, constant-time compare |
| At rest | Password and notes are stored only as ciphertext + nonce; plaintext is never persisted |
| Backups | Android cloud backup is disabled; backups are app-managed encrypted files |

The master password gates access to the vault; the entry-encryption key lives in the Keystore and is independent of it.

## Tech Stack

Kotlin 2.0 · Jetpack Compose (Material 3) · MVVM + Clean Architecture · Hilt (KSP) · SQLite via `SQLiteOpenHelper` · Navigation Compose

minSdk 26 · targetSdk 35 · JDK 17

## Build

```bash
./gradlew assembleDebug      # debug APK
./gradlew installDebug       # install on a connected device
./gradlew assembleRelease    # release APK (signed only if keystore.properties is present)
./gradlew test               # unit tests
./gradlew lint
```

Or use the Makefile shortcuts — run `make help` for the list.

## Project Structure

```
app/src/main/java/com/aminmart/passwordmanager/
├── data/
│   ├── local/        # SQLiteOpenHelper database, entities
│   ├── repository/   # Password/vault repositories, backup service
│   └── security/     # Encryption, hashing, biometric, generator
├── domain/model/     # Plaintext domain models
├── di/               # Hilt modules
└── ui/               # Compose screens, ViewModels, navigation, theme
web/                  # Landing page (deployed to GitHub Pages)
```

## License

Educational purposes only. Use at your own risk.
