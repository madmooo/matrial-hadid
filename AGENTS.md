# Matrial project workflow

This repository is the Android offline app for Matrial. Keep user-facing UI in Finglish. Preserve on-device data and the existing application ID and signing identity for updates.

The user requested ongoing GitHub synchronization. After completing a user-requested app change, run relevant checks, increment the patch version once using `python tools/bump-version.py` unless a different version was requested, commit only the intended files, and push the current branch to origin. Report any authentication or push failure. Do not force push or discard other changes. Use tags `vX.Y.Z` for built and verified APK versions; do not tag unbuilt code as an installable release. Initial repository import does not require a version bump.

Never commit signing keys, credentials, local SDK paths, databases, backups, build caches, or generated APKs. Keep signing keys local. The historical ZIP in the repository is a snapshot; edit the extracted source files, not the ZIP.

The canonical frontend is `app/src/main/assets/`. After changing root `offline-store.js`, copy it to `app/src/main/assets/offline-store.js` and ensure they match. Run `node test-offline.cjs` and syntax checks on changed JavaScript. Android build: `gradle :app:assembleDebug`, using JDK 17, Gradle 8.9, SDK 35 and a local `.signing/debug.keystore`.
