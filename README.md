# Karasu Lab Android

Native Android app for Karasu Lab.

## Requirements

| Environment variable | Required | Description |
| --- | --- | --- |
| `JAVA_HOME` | Yes | Path to a JDK 21 installation. |
| `ANDROID_HOME` or `ANDROID_SDK_ROOT` | Yes | Android SDK root. Install Android SDK Platform 37.0, Android SDK Build-Tools 37.0.0, and Platform-Tools. |
| `GNUPGHOME` | No | GPG home directory containing local encrypted signing files. Defaults to the repository's `.gpg` directory. Signing is optional. |
| `PINENTRY_PROGRAM` | No | Path to a pinentry executable for the GPG agent. The Nix development shell sets this automatically. |

Without signing material, debug builds use the standard Android debug key and
release builds are unsigned.

## Build

Build and test the debug variant with Gradle:

```sh
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

To build a release APK and Android App Bundle:

```sh
./gradlew :app:assembleRelease :app:bundleRelease
```

## Commit messages

With Node.js 22.12+ and pnpm installed, run the following to enable the commit
message hook:

```sh
pnpm install --frozen-lockfile
```

Commit messages must use Conventional Commit format without a scope. The allowed
types are `feat`, `chore`, `fix`, and `revert`; other types and non-Conventional
formats are rejected by the Git `commit-msg` hook.

## Local signing

Signing credentials are optional and should not be committed. The signing
keystore path is supplied as the Gradle property `signing.storeFile`. Passwords
are read from the untracked `local.properties` file using the
`signing.storePassword`, `signing.debugKeyPassword`, and
`signing.releaseKeyPassword` properties. Encrypted local key material can be
decrypted from the GPG home selected by `GNUPGHOME`.
