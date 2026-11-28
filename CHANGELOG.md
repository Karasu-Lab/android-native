# Changelog

## [1.16.4](https://github.com/Karasu-Lab/android-native/compare/v1.16.3...v1.16.4) (2026-11-28)


### Bug Fixes

* include the shared sources as Kotlin sources and skip Google Services for fdroid builds ([0bba29f](https://github.com/Karasu-Lab/android-native/commit/0bba29f489acc97cd6e18456a9af4c401ceb8307))

## [1.16.3](https://github.com/Karasu-Lab/android-native/compare/v1.16.2...v1.16.3) (2026-11-28)


### Bug Fixes

* drop the redundant standard label from the version name ([bbaa10f](https://github.com/Karasu-Lab/android-native/commit/bbaa10f2cd9a379f913da20bacacd4aaf4bf0076))

## [1.16.2](https://github.com/Karasu-Lab/android-native/compare/v1.16.1...v1.16.2) (2026-11-28)


### Bug Fixes

* increase the version code with every commit so store updates are delivered ([f7be3a7](https://github.com/Karasu-Lab/android-native/commit/f7be3a756535ce12c06cd8772a7b910f2294a026))

## [1.16.1](https://github.com/Karasu-Lab/android-native/compare/v1.16.0...v1.16.1) (2026-11-28)


### Bug Fixes

* resolve the variant artifacts before generating the license catalog ([ed81121](https://github.com/Karasu-Lab/android-native/commit/ed811213ec8cc339ec614b0e01038a53d2bd5481))

## [1.16.0](https://github.com/Karasu-Lab/android-native/compare/v1.15.0...v1.16.0) (2026-11-28)


### Features

* add the fdroid variant ([99422d3](https://github.com/Karasu-Lab/android-native/commit/99422d33eaac14e854ecd7691d5762fe504becd9))
* apply Roboto across the app when GMS is available ([bbe0a6e](https://github.com/Karasu-Lab/android-native/commit/bbe0a6e6f976a6b9cbea627e353fea7954c6326f))


### Bug Fixes

* install the preview SDK in the F-Droid metadata build ([7dd01c4](https://github.com/Karasu-Lab/android-native/commit/7dd01c4b5430c68afb514d9877d892c90d24bf42))
* run the signed build on pull request edits ([06717d4](https://github.com/Karasu-Lab/android-native/commit/06717d434133ba2a49557c190ac473ed97fc29a6))

## [1.15.0](https://github.com/Karasu-Lab/android-native/compare/v1.14.1...v1.15.0) (2026-11-28)


### Features

* attach nogms APKs to GitHub releases ([75e0f74](https://github.com/Karasu-Lab/android-native/commit/75e0f7489e52c9b637abbf3239cca65cde7f8da2))
* include debug variants in GitHub release APKs ([fe766a0](https://github.com/Karasu-Lab/android-native/commit/fe766a0099adffef5ad4a4d42a20e2f1f960fdb4))

## [1.14.1](https://github.com/Karasu-Lab/android-native/compare/v1.14.0...v1.14.1) (2026-11-28)


### Bug Fixes

* Complete Google sign-in on the signing-in screen ([2fcfdd6](https://github.com/Karasu-Lab/android-native/commit/2fcfdd63d53d8d7a6b5c2452527fc91311995e17))
* Show the signing-in screen as a sign-in step ([50fa30f](https://github.com/Karasu-Lab/android-native/commit/50fa30f2a7a6277dc2843f511295497b22252aeb))

## [1.14.0](https://github.com/Karasu-Lab/android-native/compare/v1.13.1...v1.14.0) (2026-11-28)


### Features

* Add a build information section to the settings screen ([5903508](https://github.com/Karasu-Lab/android-native/commit/590350841ee768e4bcbecd2a36b59b12dd33a132))


### Bug Fixes

* Enable the local debug variant only in local builds ([8cd984f](https://github.com/Karasu-Lab/android-native/commit/8cd984fb9afda4ffe08daddc2b4b81170a67d9d9))
* Read the default version name from the Release Please manifest ([3844e14](https://github.com/Karasu-Lab/android-native/commit/3844e1486a7a92660234ae38a4f20956f1fee719))

## [1.13.1](https://github.com/Karasu-Lab/android-native/compare/v1.13.0...v1.13.1) (2026-11-28)


### Bug Fixes

* Build for Google Play publishing online ([667a606](https://github.com/Karasu-Lab/android-native/commit/667a606258cec33d0d8bd086014d6ad09e0bce02))

## [1.13.0](https://github.com/Karasu-Lab/android-native/compare/v1.12.3...v1.13.0) (2026-11-28)


### Features

* Add a local debug build type with its own application ID ([0910225](https://github.com/Karasu-Lab/android-native/commit/091022507348e2942a1035928378500d0f2dc5c9))
* Use a separate application ID for debug builds ([2a83eab](https://github.com/Karasu-Lab/android-native/commit/2a83eab4494c74837dee5000179ff43e3b5ae52a))


### Bug Fixes

* Limit the debug application ID suffix to local builds ([c45b803](https://github.com/Karasu-Lab/android-native/commit/c45b8035da70d00740798afc83e93fe90e2a65e5))
* Use the AGP 9 variant builder property to disable the nogms local debug variant ([f9b3f33](https://github.com/Karasu-Lab/android-native/commit/f9b3f33f09d0cf627eb4b183b7a48b37ddde0507))

## [1.12.3](https://github.com/Karasu-Lab/android-native/compare/v1.12.2...v1.12.3) (2026-11-28)


### Bug Fixes

* restore the license detail screen and render the text with compose-markdown ([5da14a7](https://github.com/Karasu-Lab/android-native/commit/5da14a7155a1a87e75ee432c02468ca951e3fd45))

## [1.12.2](https://github.com/Karasu-Lab/android-native/compare/v1.12.1...v1.12.2) (2026-11-28)


### Bug Fixes

* open the Play Store signing key with the repository store password ([6b3ae5f](https://github.com/Karasu-Lab/android-native/commit/6b3ae5f6da2eb23533bdca264d48c8590a8c1302))

## [1.12.1](https://github.com/Karasu-Lab/android-native/compare/v1.12.0...v1.12.1) (2026-11-28)


### Bug Fixes

* keep the original signing keys and skip the signed build when its key cannot be opened ([5b492b1](https://github.com/Karasu-Lab/android-native/commit/5b492b17556d3de7da398fe6b1b770da21357e80))
* sign with the Play Store passphrase now that the signing key was replaced ([4891332](https://github.com/Karasu-Lab/android-native/commit/4891332ab12f952698765d85846aa9ba33f33f18))
* use the keystore store password separately from the Play Store key passphrase ([d1e3829](https://github.com/Karasu-Lab/android-native/commit/d1e3829f48ed625178f24a81a0f3f6a91150ec90))
* use the settings components correctly in the licenses screen ([f2846eb](https://github.com/Karasu-Lab/android-native/commit/f2846ebe241fbafc8f530b80bc3c43512eba2151))

## [1.12.0](https://github.com/Karasu-Lab/android-native/compare/v1.11.0...v1.12.0) (2026-11-28)


### Features

* publish to Google Play in a dedicated workflow with a Play Store signing key ([d7d4ca3](https://github.com/Karasu-Lab/android-native/commit/d7d4ca301abd0624d76ec119476c51443a1650ec))

## [1.11.0](https://github.com/Karasu-Lab/android-native/compare/v1.10.5...v1.11.0) (2026-11-28)


### Features

* add the nogms flavor for F-Droid and a licenses screen ([6f2e3b5](https://github.com/Karasu-Lab/android-native/commit/6f2e3b51727fff39cb12f9939e71d2f38cf35e8a))


### Bug Fixes

* build the debug variant without a debugger on CI ([88186f0](https://github.com/Karasu-Lab/android-native/commit/88186f0af492ac95f3e48a6ac9993704693a82dc))
* make the license generation task configuration cache compatible ([5ac0615](https://github.com/Karasu-Lab/android-native/commit/5ac06156118b362f7ee52df93db1b341a63bb134))
* place google-services.json in the variant source sets ([944d2cd](https://github.com/Karasu-Lab/android-native/commit/944d2cd7dc72de83c2295103188095273c864874))
* use the flavor output paths in the build workflows ([3370695](https://github.com/Karasu-Lab/android-native/commit/3370695b10717f1e2694d005a619427e980b6dd3))

## [1.10.5](https://github.com/Karasu-Lab/android-native/compare/v1.10.4...v1.10.5) (2026-11-28)


### Bug Fixes

* add an APK signature verification step to the unsigned build ([6e49997](https://github.com/Karasu-Lab/android-native/commit/6e4999778b718601d8f378aa0584b0ff46be1f6f))
* publish to Google Play in a single step ([d663149](https://github.com/Karasu-Lab/android-native/commit/d663149239e0627b2e6a8f67ef51b9e79b082f94))
* read the signing alias from environment variables ([af8f1da](https://github.com/Karasu-Lab/android-native/commit/af8f1da02380690483ecc9ff74e5c6433f6c1899))

## [1.10.4](https://github.com/Karasu-Lab/android-native/compare/v1.10.3...v1.10.4) (2026-11-28)


### Bug Fixes

* make the current session check a suspend function ([7d15392](https://github.com/Karasu-Lab/android-native/commit/7d15392a21736dec1ced265ec9cf5a27667ba140))
* revoke active sessions in parallel ([1056073](https://github.com/Karasu-Lab/android-native/commit/1056073c109e0a3266d0fa1ebce634f1e9a80071))

## [1.10.3](https://github.com/Karasu-Lab/android-native/compare/v1.10.2...v1.10.3) (2026-11-28)


### Bug Fixes

* run the signed build for manually dispatched runs ([8932609](https://github.com/Karasu-Lab/android-native/commit/893260944fea3a0a7af5ed3d1aa14f58e6d5a7db))

## [1.10.2](https://github.com/Karasu-Lab/android-native/compare/v1.10.1...v1.10.2) (2026-11-28)


### Bug Fixes

* build the debug variant for preview builds ([4995af5](https://github.com/Karasu-Lab/android-native/commit/4995af5ac03241eb96ecbffbbd476b7c244ee117))
* keep the internal test publishing for the default branch ([5acf0d4](https://github.com/Karasu-Lab/android-native/commit/5acf0d41e07d525c88a12d4b73a2c92fc8eea5b4))
* publish the preview build to the internal test track on preview merges ([a1ed072](https://github.com/Karasu-Lab/android-native/commit/a1ed0724f322e94c3f327371920853c0c3db8047))

## [1.10.1](https://github.com/Karasu-Lab/android-native/compare/v1.10.0...v1.10.1) (2026-11-28)


### Bug Fixes

* resolve the unsigned build's google-services.json from its environment ([e9570d8](https://github.com/Karasu-Lab/android-native/commit/e9570d8f5e64833804519dcbebfcf95e06cbb8a6))

## [1.10.0](https://github.com/Karasu-Lab/android-native/compare/v1.9.1...v1.10.0) (2026-11-28)


### Features

* add developer options unlocked by tapping the client version ([386a2d1](https://github.com/Karasu-Lab/android-native/commit/386a2d19ad979a5598176695c02488900bc90969))

## [1.9.1](https://github.com/Karasu-Lab/android-native/compare/v1.9.0...v1.9.1) (2026-11-28)


### Bug Fixes

* drive the linked accounts placeholder icons from the shared provider catalog ([8615a09](https://github.com/Karasu-Lab/android-native/commit/8615a09d2e3c88f6dfd26e6824e5ea9c138a7ead))

## [1.9.0](https://github.com/Karasu-Lab/android-native/compare/v1.8.0...v1.9.0) (2026-11-27)


### Features

* organize the settings menu into sections ([2aedb50](https://github.com/Karasu-Lab/android-native/commit/2aedb5059e6bceab98b3839864fc72643c4aaecd))

## [1.8.0](https://github.com/Karasu-Lab/android-native/compare/v1.7.1...v1.8.0) (2026-11-27)


### Features

* run account linking through an overridable link flow class ([f652f00](https://github.com/Karasu-Lab/android-native/commit/f652f0050a07e72051bfc28ddfb3f23e8fdcb2ba))
* run provider sign-in through an overridable login flow class ([25b4a0a](https://github.com/Karasu-Lab/android-native/commit/25b4a0a9e8a10ab962db29a53529c3f99a012bb5))


### Bug Fixes

* build the profile icon options from the live providers and profiles ([fa7ee70](https://github.com/Karasu-Lab/android-native/commit/fa7ee70a44a77ee71767787be8ca11ee9ab5571e))

## [1.7.1](https://github.com/Karasu-Lab/android-native/compare/v1.7.0...v1.7.1) (2026-11-27)


### Bug Fixes

* apply the profile cache and linkable state per provider on the linked accounts screen ([ffc33fe](https://github.com/Karasu-Lab/android-native/commit/ffc33fedc11b7b6fd2799ce8ababab9b060738a2))
* keep the provider icon placeholder on the linked accounts screen ([4c94d89](https://github.com/Karasu-Lab/android-native/commit/4c94d89dff9fb35365da82a1a7aaca677cff6f06))
* restore the native Google sign-in and linking through Credential Manager ([1a3e515](https://github.com/Karasu-Lab/android-native/commit/1a3e515c53e154e2143e061d432aaa0d116028af))

## [1.7.0](https://github.com/Karasu-Lab/android-native/compare/v1.6.1...v1.7.0) (2026-11-27)


### Features

* label the avatar delete scope in authorized apps ([c616dd2](https://github.com/Karasu-Lab/android-native/commit/c616dd2ea90bc577507e6d7a5020d887d542c0cb))
* sign in with Google through the shared OAuth flow ([532d772](https://github.com/Karasu-Lab/android-native/commit/532d7726975e6ee5f07f09e3751e65188a9f68ef))

## [1.6.1](https://github.com/Karasu-Lab/android-native/compare/v1.6.0...v1.6.1) (2026-11-27)


### Bug Fixes

* shorten the debug build step names ([a81a1bc](https://github.com/Karasu-Lab/android-native/commit/a81a1bcce78734b43e7036efb115c2cfd00e6575))

## [1.6.0](https://github.com/Karasu-Lab/android-native/compare/v1.5.1...v1.6.0) (2026-11-27)


### Features

* add a manually dispatched debug build workflow signed with the preview key ([ac97170](https://github.com/Karasu-Lab/android-native/commit/ac9717046807f1db0b33dc2eda5b8b3bb65d2468))

## [1.5.1](https://github.com/Karasu-Lab/android-native/compare/v1.5.0...v1.5.1) (2026-11-27)


### Bug Fixes

* point the debug build at dev-api.karasu256.com ([e9b1279](https://github.com/Karasu-Lab/android-native/commit/e9b12791650f42274cf79c960c8232e05560fe67))

## [1.5.0](https://github.com/Karasu-Lab/android-native/compare/v1.4.0...v1.5.0) (2026-11-27)


### Features

* write google-services.json from the GOOGLE_SERVICE_JSON variable ([dbd6f4b](https://github.com/Karasu-Lab/android-native/commit/dbd6f4ba33a09a2b5394035e80cbc1e4be4b8a8a))

## [1.4.0](https://github.com/Karasu-Lab/android-native/compare/v1.3.0...v1.4.0) (2026-11-27)


### Features

* show a signing-in overlay while an OAuth callback completes ([84b0db0](https://github.com/Karasu-Lab/android-native/commit/84b0db0cdb25cc38a4591c54fbd70e217f9b3aa4))


### Bug Fixes

* attach release files on the default branch ([975cc49](https://github.com/Karasu-Lab/android-native/commit/975cc494457ed9dc3e4c2191fe043c6e4a6b7a73))

## [1.3.0](https://github.com/Karasu-Lab/android-native/compare/v1.2.2...v1.3.0) (2026-11-27)


### Features

* report errors and crashes to Firebase Crashlytics ([2ad9905](https://github.com/Karasu-Lab/android-native/commit/2ad9905955677c4384a293e47018d5ae0d1cba4d))
* upload Crashlytics symbols for debug builds ([5ee29ae](https://github.com/Karasu-Lab/android-native/commit/5ee29ae0f986962e147f01a944dd66bd552e784f))


### Bug Fixes

* configure the Crashlytics extension with configure in the debug build type ([222e9d5](https://github.com/Karasu-Lab/android-native/commit/222e9d5f3c8c9bd23054eddbfa51f918861bdb59))
* hook logging into Crashlytics through an extended Log class ([8261b4b](https://github.com/Karasu-Lab/android-native/commit/8261b4b74af0fa701d3a7e5418526ceb93af0ee8))
* log the server reason when OAuth link initiation fails ([658fbe6](https://github.com/Karasu-Lab/android-native/commit/658fbe645507b528971717852d8d4898ea64203f))
* report errors through the standard Crashlytics API instead of a wrapper ([a8080ec](https://github.com/Karasu-Lab/android-native/commit/a8080ecd9e9cfa572151b9cbd70a39e3483dfe4a))
* use the Firebase.crashlytics accessor for Crashlytics reporting ([347ab7e](https://github.com/Karasu-Lab/android-native/commit/347ab7ee3128f079fe19beb034acdeef0bca9528))

## [1.2.2](https://github.com/Karasu-Lab/android-native/compare/v1.2.1...v1.2.2) (2026-11-27)


### Bug Fixes

* attach signed and unsigned APK files to the GitHub Release ([2d60a2d](https://github.com/Karasu-Lab/android-native/commit/2d60a2dd982f080ca89817f1f67b73e31ecb4a5e))

## [1.2.1](https://github.com/Karasu-Lab/android-native/compare/v1.2.0...v1.2.1) (2026-11-27)


### Bug Fixes

* serialize Google Play uploads and retry with a new edit ([9d248df](https://github.com/Karasu-Lab/android-native/commit/9d248df3d8b79d35cb407e7ef56eab5f3951598e))

## [1.2.0](https://github.com/Karasu-Lab/android-native/compare/v1.1.0...v1.2.0) (2026-11-27)


### Features

* add account creation and connection info to the Android auth screen ([0644d23](https://github.com/Karasu-Lab/android-native/commit/0644d235deae1c6a66efd63320ead8ceaea787d2))
* add an animated splash and a step-based loading screen to the Android app ([8e2b361](https://github.com/Karasu-Lab/android-native/commit/8e2b3619a2e18f6ab3219dfaafe39c3990bc3e83))
* add Android signing environment variables ([b6218c3](https://github.com/Karasu-Lab/android-native/commit/b6218c313a9ff97eaf5c9efaaa492fdf4e7c3fb2))
* apply the Material Theme Builder theme to the Android app ([063c24d](https://github.com/Karasu-Lab/android-native/commit/063c24d16ec97ad0a29d852778f3932c0b5b8614))
* build signed and unsigned artifacts in parallel ([dca4f78](https://github.com/Karasu-Lab/android-native/commit/dca4f787d3fc5e79f2f7ff43034767a8500cfad4))
* create, list and delete passkeys in the Android app ([ec72026](https://github.com/Karasu-Lab/android-native/commit/ec720268f8da0d295d204642ef3e377fc62e7400))
* initialize Firebase Auth, Crashlytics and Analytics in the Android app ([ee1f4e4](https://github.com/Karasu-Lab/android-native/commit/ee1f4e4ac9e16ed38d276bc90dbfb1909ba6fd97))
* move the better-auth server to the api-auth worker ([fc8fa48](https://github.com/Karasu-Lab/android-native/commit/fc8fa4811d784fedbde50524270219f53044d873))
* rebuild the sign-in and account screens after auth and accounts ([cf2e5dc](https://github.com/Karasu-Lab/android-native/commit/cf2e5dc1d17d46eab88564539133a30cf4fb17cf))
* refine the account screens, transitions and error logging ([15ee3e8](https://github.com/Karasu-Lab/android-native/commit/15ee3e895449f8d0d49cccedabb808686f1a32d0))
* restore native OAuth and deep link support ([bafe154](https://github.com/Karasu-Lab/android-native/commit/bafe1544e0d58dd4d01aa3a007f486ed9d7dbf9e))
* select the Android app's remote hosts per build type ([69da633](https://github.com/Karasu-Lab/android-native/commit/69da633594c8708904bcd67293b7596f4fc90313))
* show per-host connection status in the Android auth footer ([4972c78](https://github.com/Karasu-Lab/android-native/commit/4972c7891bb61dee9362211729383018e4a52b2a))
* show per-host connection status in the Android auth footer ([1507961](https://github.com/Karasu-Lab/android-native/commit/150796176c72afec0d0ee064375f4b36e4d5c83b))
* sign Android builds with debug and release keys from one keystore ([4cec315](https://github.com/Karasu-Lab/android-native/commit/4cec3158489b91fd2f966356a8ee362e065b6e54))
* sign debug builds with a gpg-decrypted key from the direnv environment ([3c47c64](https://github.com/Karasu-Lab/android-native/commit/3c47c646158f6120ed55f5371d9e702921c19273))
* use icon buttons for Google and passkey sign-in in the Android app ([769f598](https://github.com/Karasu-Lab/android-native/commit/769f598eeb3ac96ae236eaae50f6b412b19c2bac))


### Bug Fixes

* clarify Android signing secrets ([579557c](https://github.com/Karasu-Lab/android-native/commit/579557c4f60e03a40bb3811737eb5baccb59733c))
* compile against Android API 37.1 ([de877df](https://github.com/Karasu-Lab/android-native/commit/de877dfc0588c25d64cc74e1c6536ae3628852bc))
* decoded a pgp key from base64 ([aef8c23](https://github.com/Karasu-Lab/android-native/commit/aef8c232b2bf4092a825773de631235cf88193f3))
* enable signing on manual main builds ([86ee1d7](https://github.com/Karasu-Lab/android-native/commit/86ee1d736aa4ad30daf9f6f91edf95d07a2b54a4))
* fixed actions variables ([569a5f1](https://github.com/Karasu-Lab/android-native/commit/569a5f13c8da8f8829bd4fa1b11a5569465e3868))
* fixed Build errors ([422a0df](https://github.com/Karasu-Lab/android-native/commit/422a0dfb18770f41b3db45de5689239d183fc913))
* fixed Build errors ([e80d3c9](https://github.com/Karasu-Lab/android-native/commit/e80d3c93caecd1bc2e362b40f6831d592a78e18d))
* fixed Build errors ([98f5e51](https://github.com/Karasu-Lab/android-native/commit/98f5e5155b782506755db80b793edbd09507b4e9))
* fixed Build errors ([6312aae](https://github.com/Karasu-Lab/android-native/commit/6312aae11ace28ea843ec688660e94d4675c3714))
* fixed Build errors ([7beb725](https://github.com/Karasu-Lab/android-native/commit/7beb7259ebfd1c7906fc0eed9682d45cc62cacbe))
* fixed CI build steps ([0134de3](https://github.com/Karasu-Lab/android-native/commit/0134de3e5bdac093bb2ec3481e374b48172400b3))
* fixed CI build steps ([e402917](https://github.com/Karasu-Lab/android-native/commit/e4029171b9d38af1818e826353cc5324c2eccf1c))
* fixed CI build steps ([baecb30](https://github.com/Karasu-Lab/android-native/commit/baecb3044f7be4a9809f448370ef4ca6a3c60ee2))
* fixed CI build steps ([08ca03c](https://github.com/Karasu-Lab/android-native/commit/08ca03cb4a9482ea31ea23ab7924a51677ba8329))
* fixed publish job ([ccd53b2](https://github.com/Karasu-Lab/android-native/commit/ccd53b27cf9518fd531fa45798a01ccf6c9b539c))
* fixed publish job ([080b5e9](https://github.com/Karasu-Lab/android-native/commit/080b5e964ec881678381f05d75a1613f94eb7245))
* fixed publish job ([b07e1bd](https://github.com/Karasu-Lab/android-native/commit/b07e1bd4e0dac1e0e15e85b8bac6d4f8edebc8af))
* fixed publish step ([fbe8d73](https://github.com/Karasu-Lab/android-native/commit/fbe8d731e695a13d8126b9029b2b167f67e1fde1))
* fixed release step ([17a3676](https://github.com/Karasu-Lab/android-native/commit/17a367617d3593ed99d2091359bb01b9e59c7492))
* fixed signed build step ([96d037c](https://github.com/Karasu-Lab/android-native/commit/96d037ca99940951296a52b125bee6c1780161f0))
* fixed signed build step ([409ea03](https://github.com/Karasu-Lab/android-native/commit/409ea036437b44b30e6461f5770484aa49bb2827))
* fixed signed build step ([840c43c](https://github.com/Karasu-Lab/android-native/commit/840c43cbd7ad1813b0961c58935a74e8184ce946))
* fixed signed build step ([fb18375](https://github.com/Karasu-Lab/android-native/commit/fb18375c89815587eceda35efc1f54ac2db26a00))
* fixed signed build step ([3b48c0a](https://github.com/Karasu-Lab/android-native/commit/3b48c0a7b4ac18b605d15af4806d5e8f26598d6b))
* fixed signing config ([8d7dd15](https://github.com/Karasu-Lab/android-native/commit/8d7dd15c0767fb1d857c3815f54410809ccac7cd))
* fixed signing config ([94541de](https://github.com/Karasu-Lab/android-native/commit/94541de660adcb8f19b4dceb8becef6758de308a))
* fixed Unknown system error in prepare keys step ([5c37270](https://github.com/Karasu-Lab/android-native/commit/5c372708543d96228efb992d1ad59721cd993fc3))
* fixed Unknown system error in prepare keys step ([ea86ab3](https://github.com/Karasu-Lab/android-native/commit/ea86ab38de40f7f06e2f72067b5a106c74c43353))
* fixed Unknown system error in prepare keys step ([742f0dc](https://github.com/Karasu-Lab/android-native/commit/742f0dc9040fd6608f30f373662568028bd632db))
* fixed Unknown system error in prepare keys step ([31a078e](https://github.com/Karasu-Lab/android-native/commit/31a078e3d266411dbbb99448979dc02fd9611c01))
* import Android GPG key from file ([41c6c64](https://github.com/Karasu-Lab/android-native/commit/41c6c6407ec3925af19ee4464b69b748ed43e4f8))
* invalid input in prepare keys step ([3d8f897](https://github.com/Karasu-Lab/android-native/commit/3d8f89717f791d77fb424fc0ad1f86c9e1945b54))
* log Credential Manager failures in the Android app ([a46bd0a](https://github.com/Karasu-Lab/android-native/commit/a46bd0aa9e6caef2fffdb08cc4ec82ad0a1e4e19))
* log unsuccessful HTTP responses in the Android app ([82d4209](https://github.com/Karasu-Lab/android-native/commit/82d42093a74bba38fb5bf4dba342942cefe8ae86))
* log why the Android connection probe failed ([6c0b776](https://github.com/Karasu-Lab/android-native/commit/6c0b776a06ee93b81dbd6609da8094e03f45a145))
* make the local API reachable through the tunnel and stop failed API key checks ([2bb0dc2](https://github.com/Karasu-Lab/android-native/commit/2bb0dc2cef207717d8d90e9e82941acfefe562ce))
* prepare Android signing key in build job ([68e686e](https://github.com/Karasu-Lab/android-native/commit/68e686e2779d0b57dfcdcb2c0d9add9c3c6147fd))
* read the Android signing passwords from local.properties ([f5d6b16](https://github.com/Karasu-Lab/android-native/commit/f5d6b16b726b86650ff35aa7e15d571fd097e40b))
* reserve the sign-in error line even when there is no error ([ef743d3](https://github.com/Karasu-Lab/android-native/commit/ef743d3cab03ba86fbe5b08a162303fe65da26b7))
* route signing through GitHub Environments ([d8ae39b](https://github.com/Karasu-Lab/android-native/commit/d8ae39ba544fda4edb740faa65bb8348476ff792))
* sign same-repository pull requests ([55d1c38](https://github.com/Karasu-Lab/android-native/commit/55d1c3834b09ad002df9076dcfaf95ee1b96bc4c))
* take the Google server client ID from google-services.json ([2477858](https://github.com/Karasu-Lab/android-native/commit/24778582adafe02dd098b0211c329c9cbe983d56))
* update only the affected provider after linking or unlinking ([07197a2](https://github.com/Karasu-Lab/android-native/commit/07197a2efd9c6455d18923965e4daf0af19ee6d9))
* use configured Android signing secrets ([c7d3807](https://github.com/Karasu-Lab/android-native/commit/c7d380765578ac9dbf9561833f96cf1c936d9ed1))

## [1.1.0](https://github.com/Karasu-Lab/android-native/compare/v1.0.0...v1.1.0) (2026-11-26)


### Features

* add account creation and connection info to the Android auth screen ([0644d23](https://github.com/Karasu-Lab/android-native/commit/0644d235deae1c6a66efd63320ead8ceaea787d2))
* add an animated splash and a step-based loading screen to the Android app ([8e2b361](https://github.com/Karasu-Lab/android-native/commit/8e2b3619a2e18f6ab3219dfaafe39c3990bc3e83))
* add Android signing environment variables ([b6218c3](https://github.com/Karasu-Lab/android-native/commit/b6218c313a9ff97eaf5c9efaaa492fdf4e7c3fb2))
* apply the Material Theme Builder theme to the Android app ([063c24d](https://github.com/Karasu-Lab/android-native/commit/063c24d16ec97ad0a29d852778f3932c0b5b8614))
* build signed and unsigned artifacts in parallel ([dca4f78](https://github.com/Karasu-Lab/android-native/commit/dca4f787d3fc5e79f2f7ff43034767a8500cfad4))
* create, list and delete passkeys in the Android app ([ec72026](https://github.com/Karasu-Lab/android-native/commit/ec720268f8da0d295d204642ef3e377fc62e7400))
* initialize Firebase Auth, Crashlytics and Analytics in the Android app ([ee1f4e4](https://github.com/Karasu-Lab/android-native/commit/ee1f4e4ac9e16ed38d276bc90dbfb1909ba6fd97))
* move the better-auth server to the api-auth worker ([fc8fa48](https://github.com/Karasu-Lab/android-native/commit/fc8fa4811d784fedbde50524270219f53044d873))
* rebuild the sign-in and account screens after auth and accounts ([cf2e5dc](https://github.com/Karasu-Lab/android-native/commit/cf2e5dc1d17d46eab88564539133a30cf4fb17cf))
* refine the account screens, transitions and error logging ([15ee3e8](https://github.com/Karasu-Lab/android-native/commit/15ee3e895449f8d0d49cccedabb808686f1a32d0))
* restore native OAuth and deep link support ([bafe154](https://github.com/Karasu-Lab/android-native/commit/bafe1544e0d58dd4d01aa3a007f486ed9d7dbf9e))
* select the Android app's remote hosts per build type ([69da633](https://github.com/Karasu-Lab/android-native/commit/69da633594c8708904bcd67293b7596f4fc90313))
* show per-host connection status in the Android auth footer ([4972c78](https://github.com/Karasu-Lab/android-native/commit/4972c7891bb61dee9362211729383018e4a52b2a))
* show per-host connection status in the Android auth footer ([1507961](https://github.com/Karasu-Lab/android-native/commit/150796176c72afec0d0ee064375f4b36e4d5c83b))
* sign Android builds with debug and release keys from one keystore ([4cec315](https://github.com/Karasu-Lab/android-native/commit/4cec3158489b91fd2f966356a8ee362e065b6e54))
* sign debug builds with a gpg-decrypted key from the direnv environment ([3c47c64](https://github.com/Karasu-Lab/android-native/commit/3c47c646158f6120ed55f5371d9e702921c19273))
* use icon buttons for Google and passkey sign-in in the Android app ([769f598](https://github.com/Karasu-Lab/android-native/commit/769f598eeb3ac96ae236eaae50f6b412b19c2bac))


### Bug Fixes

* clarify Android signing secrets ([579557c](https://github.com/Karasu-Lab/android-native/commit/579557c4f60e03a40bb3811737eb5baccb59733c))
* decoded a pgp key from base64 ([aef8c23](https://github.com/Karasu-Lab/android-native/commit/aef8c232b2bf4092a825773de631235cf88193f3))
* enable signing on manual main builds ([86ee1d7](https://github.com/Karasu-Lab/android-native/commit/86ee1d736aa4ad30daf9f6f91edf95d07a2b54a4))
* fixed actions variables ([569a5f1](https://github.com/Karasu-Lab/android-native/commit/569a5f13c8da8f8829bd4fa1b11a5569465e3868))
* fixed Build errors ([422a0df](https://github.com/Karasu-Lab/android-native/commit/422a0dfb18770f41b3db45de5689239d183fc913))
* fixed Build errors ([e80d3c9](https://github.com/Karasu-Lab/android-native/commit/e80d3c93caecd1bc2e362b40f6831d592a78e18d))
* fixed Build errors ([98f5e51](https://github.com/Karasu-Lab/android-native/commit/98f5e5155b782506755db80b793edbd09507b4e9))
* fixed Build errors ([6312aae](https://github.com/Karasu-Lab/android-native/commit/6312aae11ace28ea843ec688660e94d4675c3714))
* fixed Build errors ([7beb725](https://github.com/Karasu-Lab/android-native/commit/7beb7259ebfd1c7906fc0eed9682d45cc62cacbe))
* fixed CI build steps ([0134de3](https://github.com/Karasu-Lab/android-native/commit/0134de3e5bdac093bb2ec3481e374b48172400b3))
* fixed CI build steps ([e402917](https://github.com/Karasu-Lab/android-native/commit/e4029171b9d38af1818e826353cc5324c2eccf1c))
* fixed CI build steps ([baecb30](https://github.com/Karasu-Lab/android-native/commit/baecb3044f7be4a9809f448370ef4ca6a3c60ee2))
* fixed CI build steps ([08ca03c](https://github.com/Karasu-Lab/android-native/commit/08ca03cb4a9482ea31ea23ab7924a51677ba8329))
* fixed publish job ([ccd53b2](https://github.com/Karasu-Lab/android-native/commit/ccd53b27cf9518fd531fa45798a01ccf6c9b539c))
* fixed publish job ([080b5e9](https://github.com/Karasu-Lab/android-native/commit/080b5e964ec881678381f05d75a1613f94eb7245))
* fixed publish job ([b07e1bd](https://github.com/Karasu-Lab/android-native/commit/b07e1bd4e0dac1e0e15e85b8bac6d4f8edebc8af))
* fixed publish step ([fbe8d73](https://github.com/Karasu-Lab/android-native/commit/fbe8d731e695a13d8126b9029b2b167f67e1fde1))
* fixed release step ([17a3676](https://github.com/Karasu-Lab/android-native/commit/17a367617d3593ed99d2091359bb01b9e59c7492))
* fixed signed build step ([96d037c](https://github.com/Karasu-Lab/android-native/commit/96d037ca99940951296a52b125bee6c1780161f0))
* fixed signed build step ([409ea03](https://github.com/Karasu-Lab/android-native/commit/409ea036437b44b30e6461f5770484aa49bb2827))
* fixed signed build step ([840c43c](https://github.com/Karasu-Lab/android-native/commit/840c43cbd7ad1813b0961c58935a74e8184ce946))
* fixed signed build step ([fb18375](https://github.com/Karasu-Lab/android-native/commit/fb18375c89815587eceda35efc1f54ac2db26a00))
* fixed signed build step ([3b48c0a](https://github.com/Karasu-Lab/android-native/commit/3b48c0a7b4ac18b605d15af4806d5e8f26598d6b))
* fixed signing config ([8d7dd15](https://github.com/Karasu-Lab/android-native/commit/8d7dd15c0767fb1d857c3815f54410809ccac7cd))
* fixed signing config ([94541de](https://github.com/Karasu-Lab/android-native/commit/94541de660adcb8f19b4dceb8becef6758de308a))
* fixed Unknown system error in prepare keys step ([5c37270](https://github.com/Karasu-Lab/android-native/commit/5c372708543d96228efb992d1ad59721cd993fc3))
* fixed Unknown system error in prepare keys step ([ea86ab3](https://github.com/Karasu-Lab/android-native/commit/ea86ab38de40f7f06e2f72067b5a106c74c43353))
* fixed Unknown system error in prepare keys step ([742f0dc](https://github.com/Karasu-Lab/android-native/commit/742f0dc9040fd6608f30f373662568028bd632db))
* fixed Unknown system error in prepare keys step ([31a078e](https://github.com/Karasu-Lab/android-native/commit/31a078e3d266411dbbb99448979dc02fd9611c01))
* import Android GPG key from file ([41c6c64](https://github.com/Karasu-Lab/android-native/commit/41c6c6407ec3925af19ee4464b69b748ed43e4f8))
* invalid input in prepare keys step ([3d8f897](https://github.com/Karasu-Lab/android-native/commit/3d8f89717f791d77fb424fc0ad1f86c9e1945b54))
* log Credential Manager failures in the Android app ([a46bd0a](https://github.com/Karasu-Lab/android-native/commit/a46bd0aa9e6caef2fffdb08cc4ec82ad0a1e4e19))
* log unsuccessful HTTP responses in the Android app ([82d4209](https://github.com/Karasu-Lab/android-native/commit/82d42093a74bba38fb5bf4dba342942cefe8ae86))
* log why the Android connection probe failed ([6c0b776](https://github.com/Karasu-Lab/android-native/commit/6c0b776a06ee93b81dbd6609da8094e03f45a145))
* make the local API reachable through the tunnel and stop failed API key checks ([2bb0dc2](https://github.com/Karasu-Lab/android-native/commit/2bb0dc2cef207717d8d90e9e82941acfefe562ce))
* prepare Android signing key in build job ([68e686e](https://github.com/Karasu-Lab/android-native/commit/68e686e2779d0b57dfcdcb2c0d9add9c3c6147fd))
* read the Android signing passwords from local.properties ([f5d6b16](https://github.com/Karasu-Lab/android-native/commit/f5d6b16b726b86650ff35aa7e15d571fd097e40b))
* reserve the sign-in error line even when there is no error ([ef743d3](https://github.com/Karasu-Lab/android-native/commit/ef743d3cab03ba86fbe5b08a162303fe65da26b7))
* route signing through GitHub Environments ([d8ae39b](https://github.com/Karasu-Lab/android-native/commit/d8ae39ba544fda4edb740faa65bb8348476ff792))
* sign same-repository pull requests ([55d1c38](https://github.com/Karasu-Lab/android-native/commit/55d1c3834b09ad002df9076dcfaf95ee1b96bc4c))
* take the Google server client ID from google-services.json ([2477858](https://github.com/Karasu-Lab/android-native/commit/24778582adafe02dd098b0211c329c9cbe983d56))
* update only the affected provider after linking or unlinking ([07197a2](https://github.com/Karasu-Lab/android-native/commit/07197a2efd9c6455d18923965e4daf0af19ee6d9))
* use configured Android signing secrets ([c7d3807](https://github.com/Karasu-Lab/android-native/commit/c7d380765578ac9dbf9561833f96cf1c936d9ed1))
