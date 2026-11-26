# Karasu Lab Android

Native Android app for Karasu Lab.

## Requirements

| Environment variable | Required | Description |
| --- | --- | --- |
| `JAVA_HOME` | Yes | Path to a JDK 21 installation. |
| `ANDROID_HOME` or `ANDROID_SDK_ROOT` | Yes | Android SDK root. Install Android SDK Platform 37.0, Android SDK Build-Tools 37.0.0, and Platform-Tools. |
| `GNUPGHOME` | No | GPG home directory containing local encrypted signing files. Defaults to the repository's `.gpg` directory. Signing is optional. |
| `PINENTRY_PROGRAM` | No | Path to a pinentry executable for the GPG agent. The Nix development shell sets this automatically. |
| `SIGNING_KEY` | No | Path to the local release keystore. For GitHub Actions, this secret contains the OpenPGP-encrypted keystore; the workflow decrypts it and sets this variable to the resulting path. |
| `SIGNING_PASS` | No | Release keystore and key password. Set with `SIGNING_KEY`; in GitHub Actions, configure it as a secret. |

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

Signing credentials are optional and should not be committed. For environment-
based signing, set both `SIGNING_KEY` and `SIGNING_PASS`; the release key alias
is `release`, and the same password is used for the keystore and key. Existing
local Gradle configuration is also supported through the `signing.storeFile`
Gradle property and the `signing.storePassword`, `signing.debugKeyPassword`,
and `signing.releaseKeyPassword` properties in the untracked `local.properties`
file.

For signed GitHub Actions builds, configure these repository or organization
Actions secrets: `SIGNING_KEY` (the OpenPGP-encrypted release keystore) and
`SIGNING_PASS` (its decryption passphrase and the keystore/key password). The
workflow decrypts `SIGNING_KEY` and passes the resulting file path to Gradle.
When neither secret is configured, release artifacts are built unsigned. A
partially configured set fails the main-branch signing check and reports which
secret name is missing. Main-branch pushes and manual runs can be signed; pull
request and non-main builds remain unsigned.
