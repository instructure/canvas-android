# Deploy Skill

Install and deploy Canvas Android apps to connected devices or emulators.

## Instructions

All deployment commands must be run from the repository root (`canvas-android/`), not the `apps/` directory.

### Check Connected Devices

Before deploying, check for connected devices:
```bash
adb devices -l
```

### Install to Connected Device

```bash
# Install Student app
./gradle/gradlew -p apps :student:installDevDebug

# Install Teacher app
./gradle/gradlew -p apps :teacher:installDevDebug

# Install Parent app
./gradle/gradlew -p apps :parent:installDevDebug
```

### Launch App

After installation, launch the app using monkey:
```bash
# Launch Student app
adb shell monkey -p com.instructure.candroid -c android.intent.category.LAUNCHER 1

# Launch Teacher app
adb shell monkey -p com.instructure.teacher -c android.intent.category.LAUNCHER 1

# Launch Parent app
adb shell monkey -p com.instructure.parentapp -c android.intent.category.LAUNCHER 1
```

### Common ADB Commands

```bash
# View logs for Student app
adb logcat | grep "candroid"

# Clear app data (Student)
adb shell pm clear com.instructure.candroid

# Clear app data (Teacher)
adb shell pm clear com.instructure.teacher

# Clear app data (Parent)
adb shell pm clear com.instructure.parentapp

# Uninstall Student app
adb uninstall com.instructure.candroid

# Uninstall Teacher app
adb uninstall com.instructure.teacher

# Uninstall Parent app
adb uninstall com.instructure.parentapp
```

### Target Specific Device

If multiple devices are connected, target a specific device:
```bash
# Install to specific device
./gradle/gradlew -p apps :student:installDevDebug -Pandroid.injected.device.serial=emulator-5554

# Or use adb with -s flag
adb -s emulator-5554 shell monkey -p com.instructure.candroid -c android.intent.category.LAUNCHER 1
```

## Package Names

- **Student**: `com.instructure.candroid`
- **Teacher**: `com.instructure.teacher`
- **Parent**: `com.instructure.parentapp`

## Context

When the user asks to "install", "deploy", or "run on device", use the appropriate commands from above. Always check for connected devices first. If multiple devices are connected, ask which device to target or prefer an emulator if available.