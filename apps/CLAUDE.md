# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Canvas Android is a multi-app learning management system project with three main applications (Student, Teacher, Parent) sharing common libraries. The apps are built with Kotlin, Jetpack Compose (modern UI) and XML layouts (legacy), following MVVM architecture with Dagger Hilt for dependency injection.

**Main Applications:**
- `student/` - Canvas Student app for students
- `teacher/` - Canvas Teacher app for teachers
- `parent/` - Canvas Parent app for parents (native version; there's also a Flutter version at `flutter_parent/`)

**Shared Libraries (in `../libs/`):**
- `pandautils/` - Core shared library with common code, features, compose components, and utilities
- `canvas-api-2/` - Canvas LMS API client using Retrofit/OkHttp
- `login-api-2/` - Authentication and login flows
- `annotations/` - PSPDFKit wrapper for PDF annotation handling
- `rceditor/` - Rich content editor wrapper
- `interactions/` - Navigation interactions
- `horizon/` - Additional utilities
- `pandares/` - Shared resources

**Testing Libraries (in `../automation/`):**
- `espresso/` - UI testing framework built on Espresso
- `dataseedingapi/` - gRPC wrapper for Canvas data seeding in tests

## Build Commands

Run from repository root (`canvas-android/`), not the `apps/` directory:

```bash
# Build Student app (dev debug variant)
./gradle/gradlew -p apps :student:assembleDevDebug

# Build Teacher app (dev debug variant)
./gradle/gradlew -p apps :teacher:assembleDevDebug

# Build Parent app (dev debug variant)
./gradle/gradlew -p apps :parent:assembleDevDebug

# Build all apps
./gradle/gradlew -p apps assembleAllApps

# Clean build
./gradle/gradlew -p apps clean
```

## Running Tests

**Unit Tests:**
1. Set Build Variant to `qaDebug` in Android Studio
2. Run tests by clicking the play button next to test cases/classes
3. Or via command line:
```bash
./gradle/gradlew -p apps :student:testQaDebugUnitTest
./gradle/gradlew -p apps :teacher:testQaDebugUnitTest
./gradle/gradlew -p apps :parent:testQaDebugUnitTest
```

**Instrumentation/Espresso Tests:**
```bash
./gradle/gradlew -p apps :student:connectedQaDebugAndroidTest
./gradle/gradlew -p apps :teacher:connectedQaDebugAndroidTest
```

**Single Test:**
```bash
# Unit test
./gradle/gradlew -p apps :student:testQaDebugUnitTest --tests "com.instructure.student.SpecificTest"

# Instrumentation test
./gradle/gradlew -p apps :student:connectedQaDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.instructure.student.ui.SpecificTest
```

## Architecture

### Feature Organization

Features follow a modular structure within each app under `src/main/java/com/instructure/{app}/features/{feature}/`:

```
features/
  └── featurename/
      ├── FeatureScreen.kt          # Compose UI (modern)
      ├── FeatureFragment.kt         # Fragment (legacy)
      ├── FeatureViewModel.kt        # Business logic
      ├── FeatureRepository.kt       # Data layer
      └── FeatureUiState.kt         # UI state models
```

**Test Structure:**
- Unit tests: `src/test/java/com/instructure/{app}/features/{feature}/`
- Instrumentation tests: `src/androidTest/java/com/instructure/{app}/ui/{feature}/`

### MVVM Pattern

- **ViewModels** handle business logic and state management using Kotlin Flows/LiveData
- **Repositories** abstract data sources (API, local database, etc.)
- **UI Layer** - Jetpack Compose for new features, XML + Data Binding for legacy
- **Dependency Injection** - Dagger Hilt modules in `di/` directories

### Key Technologies

- **UI**: Jetpack Compose (modern), XML layouts + Data Binding (legacy), Material Design 3
- **Networking**: Retrofit 3.0, OkHttp 5.1, Apollo GraphQL 4.3
- **Database**: Room 2.8 with Coroutines
- **DI**: Dagger Hilt 2.57
- **Async**: Kotlin Coroutines 1.9, Flow, LiveData
- **Testing**: JUnit 4, Mockk, Robolectric, Espresso, Compose UI Testing
- **Other**: Mobius (for some features), WorkManager, Firebase (Crashlytics, Messaging)

### Build Configuration

- **Build Tools**: AGP 8.13, Kotlin 2.2, KSP 2.0
- **SDK**: compileSdk 35, minSdk 28, targetSdk 35
- **Java**: Version 17
- **Build Variants**:
  - Flavors: `dev`, `qa`, `prod`
  - Types: `debug`, `debugMinify`, `release`
  - Common: `devDebug` (development), `qaDebug` (testing)

Dependencies are centralized in `buildSrc/src/main/java/GlobalDependencies.kt` with `Versions`, `Libs`, and `Plugins` objects.

## Development Guidelines

### Code Style
- Use Kotlin idioms and best practices
- Prefer immutability where possible
- Follow existing project patterns and conventions
- Self-documenting code without inline comments unless specifically requested
- Use descriptive variable and function names

### Component Patterns
- Use existing utility functions and shared components from `pandautils`
- Follow project's component structure and naming conventions
- Prefer Repository pattern for data access
- Use Hilt for dependency injection
- New UI features should use Jetpack Compose
- Legacy features may use XML + Data Binding

### Testing Patterns
- Write unit tests in the same manner as existing tests (e.g., check `student/src/test/`)
- Write instrumentation tests in the same manner as existing tests (e.g., check `student/src/androidTest/`)
- Mock dependencies with Mockk
- Use test doubles for repositories in ViewModel tests
- Espresso tests should use page object pattern from `:espresso` module
- Ensure tests are isolated and repeatable

### Module Dependencies
- Apps depend on shared libraries (`:pandautils`, `:canvas-api-2`, etc.)
- Shared libraries are in `../libs/` relative to `apps/`
- Canvas API models and endpoints are in `:canvas-api-2`
- Common utilities, dialogs, and base classes are in `:pandautils`

## Additional Context

### Initial Setup
Before first build, run from repository root:
```bash
./open_source.sh
```

This sets up Flutter SDK (if working with Flutter Parent) and other initial configuration.

### ProGuard
Each app has ProGuard rules in `{app}/proguard-rules.txt`

### Private Data
The project uses `PrivateData.merge()` to inject private configuration (API keys, tokens) from `android-vault/private-data/`. These are not in version control.

### Localization
Apps support multiple languages. Translation tags are scanned at build time via `LocaleScanner.getAvailableLanguageTags()`.