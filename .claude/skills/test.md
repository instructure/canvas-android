# Test Skill

Run unit tests, instrumentation tests, and Espresso tests for Canvas Android apps.

## Instructions

All test commands must be run from the repository root (`canvas-android/`), not the `apps/` directory.

### Unit Tests

**Prerequisites:**
- Set Build Variant to `qaDebug` in Android Studio
- Or use command line as shown below

**Run Unit Tests for Apps:**
```bash
# Student app
./gradle/gradlew -p apps :student:testQaDebugUnitTest

# Teacher app
./gradle/gradlew -p apps :teacher:testQaDebugUnitTest

# Parent app
./gradle/gradlew -p apps :parent:testQaDebugUnitTest
```

**Run Unit Tests for Shared Libraries:**
```bash
# Test a specific module (e.g., pandautils)
./gradle/gradlew -p apps :pandautils:testDebugUnitTest

# Test specific class
./gradle/gradlew -p apps :pandautils:testDebugUnitTest --tests "com.instructure.pandautils.features.discussion.router.DiscussionRouterViewModelTest.*"

# Force re-run tests (ignore cache)
./gradle/gradlew -p apps :pandautils:testDebugUnitTest --rerun-tasks
```

**Run Single Unit Test:**
```bash
./gradle/gradlew -p apps :student:testQaDebugUnitTest --tests "com.instructure.student.SpecificTest"
```

### Instrumentation/Espresso Tests

**Prerequisites:**
- Before running UI tests, check for connected devices:
```bash
adb devices -l
```
- If multiple devices are connected, prefer running on an emulator
- Use `ANDROID_SERIAL` environment variable or `-s` flag to specify a device if needed:
```bash
# Target specific device
ANDROID_SERIAL=emulator-5554 ./gradle/gradlew -p apps :student:connectedQaDebugAndroidTest

# Or using adb flag
adb -s emulator-5554 shell ...
```

**Run All Instrumentation Tests:**
```bash
# Student app
./gradle/gradlew -p apps :student:connectedQaDebugAndroidTest

# Teacher app
./gradle/gradlew -p apps :teacher:connectedQaDebugAndroidTest
```

**Run Single Instrumentation Test:**
```bash
./gradle/gradlew -p apps :student:connectedQaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.instructure.student.ui.SpecificTest
```

## Test Structure

- **Unit tests**: `src/test/java/com/instructure/{app}/features/{feature}/`
- **Instrumentation tests**: `src/androidTest/java/com/instructure/{app}/ui/{feature}/`

## Testing Patterns

- Write unit tests in the same manner as existing tests (e.g., check `student/src/test/`)
- Write instrumentation tests in the same manner as existing tests (e.g., check `student/src/androidTest/`)
- Mock dependencies with Mockk
- Use test doubles for repositories in ViewModel tests
- Espresso tests should use page object pattern from `:espresso` module
- Ensure tests are isolated and repeatable

## Context

When the user asks to "run tests" or "test", determine which type of test they need (unit or instrumentation) and which app/module. If unclear, ask for clarification.

For UI/instrumentation tests, always check for connected devices first using `adb devices -l`. If multiple devices are connected, prefer running on an emulator if available.