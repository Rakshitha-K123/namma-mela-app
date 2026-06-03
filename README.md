# Namma Mela Android App

Native Kotlin Android app for rural drama theaters. The project uses Jetpack Compose, Material 3, and an MVVM-style state holder.

## Open In Android Studio

1. Open this folder in Android Studio:
   `C:\Users\91807\Documents\Codex\2026-05-04\files-mentioned-by-the-user-readme`
2. Let Gradle sync.
3. Run the `app` configuration on an emulator or Android phone.

## Build From Terminal

```powershell
.\gradlew.bat assembleDebug
```

If Android Studio creates the Gradle wrapper, use that wrapper command. Without a wrapper, run the same task from Android Studio.

## Current Features

- Home dashboard for upcoming village drama shows
- Show selection with venue, troupe, date, and price
- Seat booking grid with live selected-seat total
- Applause wall for audience comments
- Compose Material 3 theme and Android launcher configuration
