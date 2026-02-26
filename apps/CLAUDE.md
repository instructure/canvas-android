# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Skills

Operational commands are available as skills in `.claude/skills/`:
- `/build` - Build commands for all apps
- `/test` - Unit and instrumentation test commands
- `/deploy` - Device deployment and ADB commands
- `/pr` - Pull request creation guidelines

Use these skills when you need specific command references.

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
- `horizon/` - Career experience features
- `pandares/` - Shared resources

**Testing Libraries (in `../automation/`):**
- `espresso/` - UI testing framework built on Espresso
- `dataseedingapi/` - gRPC wrapper for Canvas data seeding in tests

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
- **Companion objects**: Place companion objects at the bottom of the class, following Kotlin style guides. For simple private constants used only within a file, consider using top-level constants instead
- Always use imports instead of fully qualified names in code

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

### Router Pattern
The project uses a Router pattern for navigation between features:
- **Interface Definition**: Router interfaces defined in `pandautils` (e.g., `DiscussionRouter`, `CalendarRouter`)
- **App Implementation**: Each app implements the router interface (e.g., `StudentDiscussionRouter`, `TeacherDiscussionRouter`)
- **Dependency Injection**: Routers are injected via Hilt into Fragments/ViewModels
- **Usage**: ViewModels emit actions that are handled by Fragments, which delegate to app-specific routers
- **Routing Methods**: Routers use `RouteMatcher.route()` to navigate to other fragments using `Route` objects
- **Example Flow**: ViewModel → Action → Fragment.handleAction() → Router.routeTo*() → RouteMatcher.route()
- 
### Licenses

- Files belonging to top-level projects (i.e. shippable apps) should all use the GNU General Public License v3.0.
  These modules are: student, parent, teacher. Exceptions are UI and unit tests in those modules. Those should use Apache License Version 2.0.
- Code in other modules should use the Apache License Version 2.0.
- Always add the correct year information into the header of the license based on the current day.

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