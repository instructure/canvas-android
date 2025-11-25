---
name: build
description: Build Canvas Android apps (Student, Teacher, Parent) using Gradle. Use when user mentions building, compiling, assembling, or making the app. Provides commands for dev, qa, and prod build variants.
---

# Build Canvas Android Apps

Build the Canvas Android apps (Student, Teacher, Parent) using Gradle.

## Build Location

All build commands must be run from the repository root (`canvas-android/`), not the `apps/` directory.

## Build Commands

### Build Individual Apps

Build a specific app in the dev debug variant:

```bash
# Build Student app
./gradle/gradlew -p apps :student:assembleDevDebug

# Build Teacher app
./gradle/gradlew -p apps :teacher:assembleDevDebug

# Build Parent app
./gradle/gradlew -p apps :parent:assembleDevDebug
```

### Build All Apps

Build all three apps at once:

```bash
./gradle/gradlew -p apps assembleAllApps
```

### Clean Build

Remove build artifacts before building:

```bash
./gradle/gradlew -p apps clean
```

## Build Variants

- **Flavors**: `dev`, `qa`, `prod`
- **Types**: `debug`, `debugMinify`, `release`
- **Common variants**:
  - `devDebug` - for development
  - `qaDebug` - for testing

## Examples

Build Teacher app for QA testing:
```bash
./gradle/gradlew -p apps :teacher:assembleQaDebug
```

Clean and rebuild Student app:
```bash
./gradle/gradlew -p apps clean :student:assembleDevDebug
```