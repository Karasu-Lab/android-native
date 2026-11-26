# Android release builds

The `Android Build` workflow in `.github/workflows/build.yml` creates release APK
and AAB artifacts for pull requests, pushes to `main`, and manual runs. Pull
requests and non-`main` runs are unsigned. A push to `main` is signed only when
all four Android signing secrets are configured; otherwise it succeeds and
uploads unsigned release artifacts.

Configure these repository or environment **Actions secrets** to enable signing:

- `ANDROID_GPG_PRIVATE_KEY`: ASCII-armored GPG private key used to decrypt the
  Android signing material.
- `ANDROID_GPG_PASSPHRASE`: passphrase for the GPG private key.
- `ANDROID_SIGNING_KEYSTORE_GPG`: ASCII-armored GPG-encrypted release JKS.
- `ANDROID_SIGNING_PROPERTIES_GPG`: ASCII-armored GPG-encrypted Java properties
  file containing non-empty `signing.storePassword` and
  `signing.releaseKeyPassword` entries.

Encrypt the JKS and properties file to the imported GPG key's public key. The
GPG job imports the private key and passes its temporary GPG home to the Android
job as a one-day artifact. The Android job decrypts the signing files only for a
`main` build, then Gradle signs both release outputs with the `release` key alias.
Do not store unencrypted keystores, passwords, or private keys in the repository.
