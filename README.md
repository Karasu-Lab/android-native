# Karasu Lab Android

Native Android app for Karasu Lab.

## Requirements

| Environment variable | Required | Description |
| --- | --- | --- |
| `JAVA_HOME` | Yes | Path to a JDK 21 installation. |
| `ANDROID_HOME` or `ANDROID_SDK_ROOT` | Yes | Android SDK root. Install Android SDK Platform 37.0, Android SDK Build-Tools 37.0.0, and Platform-Tools. |
| `GNUPGHOME` | No | GPG home directory containing local encrypted signing files. Defaults to the repository's `.gpg` directory. Signing is optional. |
| `PINENTRY_PROGRAM` | No | Path to a pinentry executable for the GPG agent. The Nix development shell sets this automatically. |
| `SIGNING_KEY` | No | Path to the release keystore. The GitHub Actions workflow sets it to the decrypted keystore path for signed builds. |
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

## Local signing

Signing credentials are optional and should not be committed. For environment-
based signing, set both `SIGNING_KEY` and `SIGNING_PASS`; the release key alias
is `release`, and the same password is used for the keystore and key. Existing
local Gradle configuration is also supported through the `signing.storeFile`
Gradle property and the `signing.storePassword`, `signing.debugKeyPassword`,
and `signing.releaseKeyPassword` properties in the untracked `local.properties`
file.

The GitHub Actions workflow also requires the `ANDROID_GPG_PRIVATE_KEY`,
`ANDROID_GPG_PASSPHRASE`, and `ANDROID_SIGNING_KEYSTORE_GPG` secrets to decrypt
the release keystore. When none of the signing secrets are configured, release
artifacts are built unsigned. Partially configured signing secrets fail the
signing job.
