# Build Skill

Build the Canvas Android apps (Student, Teacher, Parent).

## Instructions

All build commands must be run from the repository root (`canvas-android/`), not the `apps/` directory.

### Available Build Commands

**Build Individual Apps (dev debug variant):**
```bash
# Build Student app
./gradle/gradlew -p apps :student:assembleDevDebug

# Build Teacher app
./gradle/gradlew -p apps :teacher:assembleDevDebug

# Build Parent app
./gradle/gradlew -p apps :parent:assembleDevDebug
```

**Build All Apps:**
```bash
./gradle/gradlew -p apps assembleAllApps
```

**Clean Build:**
```bash
./gradle/gradlew -p apps clean
```

### Build Variants
- **Flavors**: `dev`, `qa`, `prod`
- **Types**: `debug`, `debugMinify`, `release`
- **Common**: `devDebug` (development), `qaDebug` (testing)

## Context

When the user asks to "build" or "compile" the app(s), use the appropriate command from above. If they don't specify which app, ask which one (Student, Teacher, or Parent) or offer to build all apps.