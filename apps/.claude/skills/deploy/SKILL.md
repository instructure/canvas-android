---
name: deploy
description: Install and deploy Canvas Android apps to connected devices or emulators using adb and Gradle. Use when user mentions installing, deploying, running on device, launching app, or working with emulators.
allowed-tools: Bash
---

# Deploy Canvas Android Apps

Install and deploy Canvas Android apps to connected devices or emulators.

## Deploy Location

All deployment commands must be run from the repository root (`canvas-android/`), not the `apps/` directory.

## Check Connected Devices

Before deploying, always check for connected devices or emulators:

```bash
adb devices -l
```

If no devices are connected, start an emulator first.

## Install to Device

Install an app to a connected device or emulator:

```bash
# Install Student app
./gradle/gradlew -p apps :student:installDevDebug

# Install Teacher app
./gradle/gradlew -p apps :teacher:installDevDebug

# Install Parent app
./gradle/gradlew -p apps :parent:installDevDebug
```

## Launch App

After installation, launch the app using monkey:

```bash
# Launch Student app
adb shell monkey -p com.instructure.candroid -c android.intent.category.LAUNCHER 1

# Launch Teacher app
adb shell monkey -p com.instructure.teacher -c android.intent.category.LAUNCHER 1

# Launch Parent app
adb shell monkey -p com.instructure.parentapp -c android.intent.category.LAUNCHER 1
```

## Package Names

- **Student**: `com.instructure.candroid`
- **Teacher**: `com.instructure.teacher`
- **Parent**: `com.instructure.parentapp`

## Target Specific Device

If multiple devices are connected, target a specific device:

```bash
# Install to specific device using Gradle
./gradle/gradlew -p apps :student:installDevDebug -Pandroid.injected.device.serial=emulator-5554

# Or use adb with -s flag
adb -s emulator-5554 shell monkey -p com.instructure.candroid -c android.intent.category.LAUNCHER 1
```

## Common ADB Commands

### View Logs

```bash
# View logs for Student app
adb logcat | grep "candroid"

# View logs for Teacher app
adb logcat | grep "teacher"
```

### Clear App Data

```bash
# Clear Student app data
adb shell pm clear com.instructure.candroid

# Clear Teacher app data
adb shell pm clear com.instructure.teacher

# Clear Parent app data
adb shell pm clear com.instructure.parentapp
```

### Uninstall Apps

```bash
# Uninstall Student app
adb uninstall com.instructure.candroid

# Uninstall Teacher app
adb uninstall com.instructure.teacher

# Uninstall Parent app
adb uninstall com.instructure.parentapp
```

## Examples

Install and launch Student app:
```bash
./gradle/gradlew -p apps :student:installDevDebug
adb shell monkey -p com.instructure.candroid -c android.intent.category.LAUNCHER 1
```

Reinstall Teacher app (clear data first):
```bash
adb shell pm clear com.instructure.teacher
./gradle/gradlew -p apps :teacher:installDevDebug
adb shell monkey -p com.instructure.teacher -c android.intent.category.LAUNCHER 1
```