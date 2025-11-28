---
name: test
description: Run unit tests and instrumentation tests for Canvas Android apps. Use when user mentions testing, running tests, JUnit, Espresso, or checking test results. Includes commands for Student, Teacher, and Parent apps.
allowed-tools: Bash, Read
---

# Test Canvas Android Apps

Run unit tests, instrumentation tests, and Espresso tests for Canvas Android apps.

## Test Location

All test commands must be run from the repository root (`canvas-android/`), not the `apps/` directory.

## Unit Tests

Unit tests verify business logic in isolation using Mockk for mocking.

### Prerequisites

- Set Build Variant to `qaDebug` in Android Studio, or
- Use command line with `testQaDebugUnitTest` as shown below

### Run Unit Tests for Apps

```bash
# Student app - all unit tests
./gradle/gradlew -p apps :student:testQaDebugUnitTest

# Teacher app - all unit tests
./gradle/gradlew -p apps :teacher:testQaDebugUnitTest

# Parent app - all unit tests
./gradle/gradlew -p apps :parent:testQaDebugUnitTest
```

### Run Unit Tests for Shared Libraries

```bash
# Test a specific module (e.g., pandautils)
./gradle/gradlew -p apps :pandautils:testDebugUnitTest

# Test specific class or package
./gradle/gradlew -p apps :pandautils:testDebugUnitTest --tests "com.instructure.pandautils.features.discussion.router.*"

# Force re-run tests (ignore cache)
./gradle/gradlew -p apps :pandautils:testDebugUnitTest --rerun-tasks
```

### Run Specific Unit Tests

Use the `--tests` flag to run specific test classes or methods:

```bash
# Run single test class
./gradle/gradlew -p apps :student:testQaDebugUnitTest --tests "com.instructure.student.features.dashboard.DashboardViewModelTest"

# Run tests matching a pattern
./gradle/gradlew -p apps :student:testQaDebugUnitTest --tests "com.instructure.student.features.dashboard.widget.*"
```

### Test Structure

- **Unit tests**: `src/test/java/com/instructure/{app}/features/{feature}/`
- **Instrumentation tests**: `src/androidTest/java/com/instructure/{app}/ui/{feature}/`

## Instrumentation/Espresso Tests

Instrumentation tests run on a device or emulator to test UI interactions.

### Prerequisites

Before running UI tests, check for connected devices:

```bash
adb devices -l
```

If multiple devices are connected:
- Prefer running on an emulator if available
- Use `ANDROID_SERIAL` environment variable or `-s` flag to specify a device

### Run All Instrumentation Tests

```bash
# Student app
./gradle/gradlew -p apps :student:connectedQaDebugAndroidTest

# Teacher app
./gradle/gradlew -p apps :teacher:connectedQaDebugAndroidTest
```

### Run Specific Instrumentation Test

```bash
# Run single test class
./gradle/gradlew -p apps :student:connectedQaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.instructure.student.ui.dashboard.DashboardPageTest
```

### Target Specific Device

If multiple devices are connected:

```bash
# Use environment variable
ANDROID_SERIAL=emulator-5554 ./gradle/gradlew -p apps :student:connectedQaDebugAndroidTest

# Or use adb flag
adb -s emulator-5554 shell ...
```

## Testing Patterns

- Write unit tests in the same style as existing tests (check `student/src/test/`)
- Write instrumentation tests following existing patterns (check `student/src/androidTest/`)
- Mock dependencies with Mockk
- Use test doubles for repositories in ViewModel tests
- Espresso tests should use page object pattern from `:espresso` module
- Ensure tests are isolated and repeatable

## Examples

Run all widget tests:
```bash
./gradle/gradlew -p apps :student:testQaDebugUnitTest --tests "com.instructure.student.features.dashboard.widget.*"
```

Run tests and view report:
```bash
./gradle/gradlew -p apps :student:testQaDebugUnitTest
open apps/student/build/reports/tests/testQaDebugUnitTest/index.html
```