# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Horizon is an Android library module within the canvas-android monorepo that provides a modern, Jetpack Compose-based learning experience for Canvas Career. It is part of the Instructure Canvas LMS mobile ecosystem and integrates with the Student app. The module is located at `libs/horizon` within the monorepo.

## Build Commands

All Gradle commands should be run from the repository root (`canvas-android/`), not from the `libs/horizon` directory.

### Building
```bash
./gradle/gradlew :libs:horizon:assembleDebug
./gradle/gradlew :libs:horizon:assembleRelease
```

### Testing
```bash
# Run unit tests (JUnit + Robolectric)
./gradle/gradlew :libs:horizon:testDebugUnitTest

# Run instrumented tests (Espresso + Compose UI tests)
./gradle/gradlew :libs:horizon:connectedDebugAndroidTest

# Run specific test class
./gradle/gradlew :libs:horizon:testDebugUnitTest --tests "com.instructure.horizon.features.dashboard.DashboardViewModelTest"
```

### Linting and Code Quality
```bash
./gradle/gradlew :libs:horizon:lintDebug
```

## Architecture

### Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose with Material3
- **Dependency Injection**: Dagger Hilt
- **Navigation**: Jetpack Navigation Compose with type-safe routes using Kotlin Serialization
- **Async**: Kotlin Coroutines and Flow
- **Testing**: JUnit, Mockk, Robolectric, Espresso, Compose UI Test

### Project Structure

```
src/main/java/com/instructure/horizon/
├── HorizonActivity.kt         # Main entry point, handles navigation and push notifications
├── di/                         # Hilt dependency injection modules
├── features/                   # Feature modules organized by domain
│   ├── account/
│   ├── aiassistant/
│   ├── dashboard/
│   ├── home/                   # Landing screen with navigation
│   ├── inbox/                  # Messaging functionality
│   ├── learn/                  # Course and program learning content
│   ├── moduleitemsequence/    # Sequential module item navigation
│   ├── notebook/               # Note-taking functionality
│   ├── notification/           # Notification center
│   └── skillspace/
├── horizonui/                  # Design system components
│   ├── HorizonTheme.kt        # Theme configuration (forced light mode)
│   ├── foundation/             # Colors, typography, spacing
│   ├── molecules/              # Small reusable components
│   ├── organisms/              # Complex composed components
│   └── animation/              # Navigation transitions
├── model/                      # Domain models specific to Horizon
└── navigation/
    └── HorizonNavigation.kt   # NavHost and routing configuration
```

### Design system

 - The `horizonui` package contains the major UI components
 - If possible use these components
 - If the design contains a reusable component extend the existing design system
 - Local Componenents from Figma design shouldn't be placed here, implement them in the feature's package


### Feature Architecture Pattern

Each feature follows MVVM pattern with a consistent structure:

```
features/[feature-name]/
├── [FeatureName]Screen.kt       # Composable UI
├── [FeatureName]ViewModel.kt    # Hilt-injected ViewModel with StateFlow
├── [FeatureName]Repository.kt   # Data layer, API calls
├── [FeatureName]UiState.kt      # UI state data class
└── navigation/                   # Feature-specific navigation graphs
```

**Example flow:**
- `HomeScreen` → `HomeViewModel` → `HomeRepository` → Canvas API
- ViewModels expose `StateFlow<UiState>` collected in Composables
- Repositories use suspend functions and return API models from `canvasapi2`

### Key Design Patterns

1. **Navigation**: Type-safe routes using `@Serializable` data classes in `MainNavigationRoute.kt`
2. **Deep Linking**: Deep links configured in NavHost composables map to Canvas LMS URLs
3. **Dependency Injection**: `@HiltViewModel` for ViewModels, constructor injection for repositories
4. **UI State Management**: Immutable data classes with sealed classes for state variants
5. **Testing**:
   - Unit tests use Mockk with `UnconfinedTestDispatcher` for coroutines
   - UI tests extend `HorizonTest` base class and use Page Object pattern

## Important Dependencies

- `:pandautils` - Core shared utilities, base classes, and common Canvas components
- Canvas API 2 - REST API client for Canvas LMS (`com.instructure.canvasapi2`)
- PSPDFKit - PDF annotation and viewing
- AndroidX WorkManager - Background task scheduling for submissions

## Testing Notes

- **Unit Tests**: Located in `src/test/`, use Robolectric for Android framework dependencies
- **Instrumented Tests**: Located in `src/androidTest/`, divided into:
  - `espresso/` - Test infrastructure and base classes
  - `ui/features/` - UI-only tests using Compose Test
  - `interaction/features/` - Full integration tests with data seeding
  - Every application screen has a test page which contains all of the important assertions and actions. Create and use these Page classes for every new screen.
- **Test Authentication**: Use `tokenLogin()` method from `HorizonTest` base class for instrumented tests
- **Data Seeding**: Available via `:dataseedingapi` module for instrumented tests
- **Mocked Data**: MockCanvas is an object which can contain all the mock data which will be returned by the mock endpoints during testing

## Horizon-Specific Considerations

1. **Theme**: Horizon forces light mode in `HorizonActivity.onCreate()` to ensure consistent branding
2. **Navigation Deep Links**: Must include `ApiPrefs.fullDomain` in URI patterns for proper routing
3. **Module Item Sequence**: Central navigation pattern for course content (assignments, quizzes, pages)
4. **Canvas Career**: This module specifically supports Canvas Career View (`ApiPrefs.canvasCareerView = true`)

## Code Style

Follow the code style guidelines from the parent repository:
- No inline comments unless specifically requested - code should be self-documenting
- Use Kotlin idioms and best practices
- Prefer immutability and data classes
- Use descriptive naming for functions and variables
- Write tests that mirror existing test patterns in the project
- Match existing file structure and naming conventions when adding new features
- Alwways create a preview composable for the new composables