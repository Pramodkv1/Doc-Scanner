# How to get the APK (step by step)

1. Create a free account at github.com and log in.
2. Click "+" (top right) -> "New repository". Name it DocScanner. Keep it Public or Private -> "Create repository".
3. Unzip this zip on your computer. Open the unzipped folder: you must see
   settings.gradle.kts, build.gradle.kts, app/ and a hidden folder .github/ directly inside.
   (If hidden folders are not visible: Windows -> View -> Show hidden items; Mac -> Cmd+Shift+. )
4. Easiest upload: install GitHub Desktop (desktop.github.com), sign in, File -> Add local repository
   (choose the unzipped folder) -> Publish repository.
   Browser upload also works: in the repo click "uploading an existing file", drag ALL files
   AND the .github folder, then "Commit changes". Then confirm .github/workflows/build.yml exists on GitHub.
5. Open the "Actions" tab. If asked, click "I understand my workflows, enable them".
6. Left side: click "Build Android" -> "Run workflow" -> green "Run workflow" button.
7. Wait 5-10 minutes until the run shows a green tick.
8. Open the run, scroll to the bottom -> "Artifacts" -> click "app-debug-apk" to download.
9. Unzip it -> app-debug.apk -> copy to phone -> tap to install (allow "Install unknown apps").
10. If the run is red: open it, click the failed step, copy the last ~30 lines of the log and send them for a fix.
