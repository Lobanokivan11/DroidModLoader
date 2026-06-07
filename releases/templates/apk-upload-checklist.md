# APK Upload Checklist

Use this checklist before uploading Droid Mod Loader APKs to Nexus Mods, GitHub Releases, Discord, or other public locations.

## Build Identity

- [ ] `versionName` is correct in `app/build.gradle.kts`
- [ ] `versionCode` is incremented
- [ ] APK filename includes version
- [ ] Release notes use the same version
- [ ] Changelog uses the same version

## Build

- [ ] `./gradlew testDebugUnitTest` passes
- [ ] `./gradlew assembleDebug` passes
- [ ] `./gradlew assembleRelease` passes
- [ ] Release APK installs
- [ ] App launches

## Safety

- [ ] Tested with safe target folder
- [ ] Deployment plan reviewed
- [ ] Unmanaged files are not blindly deleted
- [ ] Dangerous paths are blocked or warned
- [ ] Recovery warning behavior checked

## UI

- [ ] Main dashboard readable
- [ ] Portrait layout checked
- [ ] Landscape layout checked
- [ ] Developer Tools hidden unless dev mode is unlocked
- [ ] Recovery Tools visible when needed

## Documentation

- [ ] README current
- [ ] Changelog updated
- [ ] Known issues updated
- [ ] Release notes written
- [ ] Nexus/GitHub page description still accurate

## Upload

- [ ] APK scanned locally if desired
- [ ] APK uploaded
- [ ] Release notes pasted
- [ ] Screenshots current enough
- [ ] Discord/community post prepared if needed