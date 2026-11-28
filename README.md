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

The app has two flavor dimensions. The `channel` dimension is
`standard` or `fdroid`, and the `distribution` dimension is `gms` or
`nogms`. The `gms` flavor includes Google Mobile Services and Firebase
and uses the native Google sign-in. The `nogms` flavor has no GMS or
Firebase dependency, disables Firebase features, and uses the standard
web provider flow for Google sign-in. Every flavor combination provides
debug, localDebug and release build types. The standard build is the
release build type with no local build type and the `nogms`
distribution.
The application ID is `com.karasu256.karasulab` for the `standard`
channel and `com.karasu256.karasulab.fdroid` for the `fdroid` channel,
except for the `localDebug` build type, which adds the `.debug` suffix.
The `localDebug` build type inherits the debug configuration.
Its google-services configuration is placed in
`app/src/gmsLocalDebug/google-services.json`. The debug and release
build types use the channel application ID.
Which debug variant is available depends on the environment: the
debug variants are enabled in CI only, and the `localDebug` variants
are enabled in local builds only, so a local build uses a
`localDebug` variant and a CI build uses a `debug` variant.

The version is derived from the Release Please manifest
(`.release-please-manifest.json`). The version code is
`2000000000 + major * 10000 + minor * 100 + patch`. The offset keeps
every version code above the timestamp-based version codes that were
published to Google Play before this scheme was introduced, while
staying below the maximum version code of 2100000000. Every variant
is named `<version>-<variant>-<short commit>+<version code>`, for
example `1.15.0-fdroidNogmsRelease-0123abc+2000011500`.

Build and test the standard gms debug variant with Gradle in CI:

```sh
./gradlew :app:assembleStandardGmsDebug
./gradlew :app:testStandardGmsDebugUnitTest
```

To build the local debug variant with its own application ID:

```sh
./gradlew :app:assembleStandardGmsLocalDebug
```

To build a standard gms release APK and Android App Bundle:

```sh
./gradlew :app:assembleStandardGmsRelease :app:bundleStandardGmsRelease
```

To build the F-Droid variant, which is the fdroid channel, the nogms
distribution and the release build type:

```sh
./gradlew :app:assembleFdroidNogmsRelease
```

The fdroid variant does not include the JitPack dependency used for
Markdown rendering. Its license body is shown as plain text through
the Markdown renderer interface, and its license catalog is generated
without fetching license texts from the network. The F-Droid workflow
builds this variant with the same procedure as the build recipe in
`metadata/com.karasu256.karasulab.fdroid.yml`.

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
