# Karasu Lab Android

Native Android app for Karasu Lab.

## Requirements

| Environment variable | Required | Description |
| --- | --- | --- |
| `JAVA_HOME` | Yes | Path to a JDK 21 installation. |
| `ANDROID_HOME` or `ANDROID_SDK_ROOT` | Yes | Android SDK root. Install Android SDK Platform 37.0, Android SDK Build-Tools 37.0.0, and Platform-Tools. |
| `GNUPGHOME` | No | GPG home directory containing local encrypted signing files. Defaults to the repository's `.gpg` directory. Signing is optional. |
| `PINENTRY_PROGRAM` | No | Path to a pinentry executable for the GPG agent. The Nix development shell sets this automatically. |
| `SIGNING_KEY_BASE64` | No | Base64-encoded release keystore for GitHub Actions. The workflow decodes it and writes it to `.keystore`. |
| `SIGNING_STORE_PASSWORD` | No | Release keystore store password. |
| `SIGNING_KEY_PASSWORD` | No | Release key password. |
| `SIGNING_ALIAS` | No | Release key alias. In GitHub Actions, set it as a public variable in each environment. |
| `SIGNING_PASS` | No | Release keystore and key password. Set with `SIGNING_KEY`; in GitHub Actions, configure it as an environment secret. |

Without signing material, debug builds use the standard Android debug key and
release builds are unsigned.

## Build

The app has two distribution flavors. The `gms` flavor includes Google
Mobile Services and Firebase and uses the native Google sign-in. The
`nogms` flavor has no GMS or Firebase dependency, disables Firebase
features, and uses the standard web provider flow for Google sign-in.
Both flavors provide debug and release build types.
The application ID is `com.karasu256.karasulab`, except for the
`localDebug` build type, which uses `com.karasu256.karasulab.debug`.
The `localDebug` build type inherits the debug configuration and is
only available with the gms flavor, as the `gmsLocalDebug` variant.
Its google-services configuration is placed in
`app/src/gmsLocalDebug/google-services.json`. The debug and release
build types use the base application ID.

Build and test the gms debug variant with Gradle:

```sh
./gradlew :app:assembleGmsDebug
./gradlew :app:testGmsDebugUnitTest
```

To build the local debug variant with its own application ID:

```sh
./gradlew :app:assembleGmsLocalDebug
```

To build a gms release APK and Android App Bundle:

```sh
./gradlew :app:assembleGmsRelease :app:bundleGmsRelease
```

To build the F-Droid variant without GMS or Firebase:

```sh
./gradlew :app:assembleNogmsDebug :app:assembleNogmsRelease
```

The license catalog shown in the app is generated at build time from the
resolved dependencies of each variant and is included in the APK as
generated source.

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

For signed GitHub Actions builds, configure `SIGNING_KEY_BASE64` (the base64-encoded
release keystore), `SIGNING_STORE_PASSWORD`, and `SIGNING_KEY_PASSWORD`
as environment secrets in both `preview` and `production`, and `SIGNING_ALIAS`
as a public environment variable in both environments. Same-repository pull requests use `preview`; pushes to the
default branch use `production`. The workflow decodes `SIGNING_KEY_BASE64` and places
it at the project root as `.keystore`. When neither secret is configured, the
signed job is skipped; a partially configured set
fails and reports the missing secret name. Fork pull requests do not receive
environment secrets.
