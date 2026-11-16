# Glassy Launcher for Android TV

A lightweight Android TV home launcher with a frosted-glass look, live clock/date, app search, app grid, and D-pad focus glow.

## Build an APK using only your Android phone

This project includes a GitHub Actions workflow. You do not need Android Studio on your phone.

1. Sign in to GitHub in your phone browser and create a new **public or private repository**.
2. Upload the contents of this project folder to the repository (keep the folder structure).
3. Open the repository's **Actions** tab and enable Actions if GitHub asks.
4. The workflow named **Build Glassy Launcher APK** runs automatically after upload. You can also open it and tap **Run workflow**.
5. When it finishes, open the completed workflow run, scroll to **Artifacts**, and download `GlassyLauncher-debug-apk`.
6. Extract the downloaded ZIP. The APK is `app-debug.apk`.

## Install on Xiaomi / Android TV

1. Send `app-debug.apk` from your phone to the TV using LocalSend, or copy it to a USB drive.
2. On the TV, open it with a file manager and install it. Allow installation from that file manager if Android asks.
3. Open **Glassy Launcher** once.
4. To make it your home screen, open TV Settings → Apps → Default apps → Home app (wording varies by TV) → Glassy Launcher.

If your TV does not show a Home app selector, press the remote's Home button and choose Glassy Launcher if Android offers a choice.

## Notes

- The launcher lists apps that expose a standard Android or Android TV launch activity.
- Some TV manufacturers hide or restrict the Home app setting.
- This is an original basic launcher implementation, not a copy of a commercial launcher.
