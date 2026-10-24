# Doc Scanner

Android document scanner (Kotlin + Compose + ML Kit Document Scanner).
Features: auto edge-detect, crop, rotate, filters, shadow/stain removal, multi-page, gallery import, PDF export, open/share/rename/delete.

## Build with GitHub
1. Push this repo to GitHub. Actions builds `app-debug.apk` on every push (Actions tab -> run -> Artifacts).
2. For Play Store, create a keystore once:
   keytool -genkeypair -v -keystore release.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
   base64 -w0 release.jks   # (macOS: base64 -i release.jks)
3. Add repo secrets (Settings -> Secrets -> Actions): KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD.
4. Re-run the workflow -> download `release-signed` -> upload the .aab to Play Console.
Back up release.jks and its passwords somewhere safe; never commit them.

## Before publishing
- Change applicationId/namespace in app/build.gradle.kts (and the package folder/name).
- Replace the placeholder launcher icon.
- Add a privacy policy URL (Play requires it).
