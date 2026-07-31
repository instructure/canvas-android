# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Skills

Operational commands are available as skills in `.claude/skills/`:
- `/build` - Build commands for all apps
- `/test` - Unit and instrumentation test commands
- `/deploy` - Device deployment and ADB commands
- `/pr` - Pull request creation guidelines
- `/release` - Start a release or hotfix (branch, version bump, release PR)

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
- **UI actions = callbacks in the UiState, NOT an Action sealed class.** Expose user actions as lambda
  properties nested in an `actions` sub-object on the UiState (e.g. `data class FeatureActions(val onRefresh: () -> Unit = {}, ...)`
  with a `val actions: FeatureActions = FeatureActions()` field on the UiState). Wire them in the
  ViewModel via a private `withCallbacks()` extension that `copy(actions = FeatureActions(onRefresh = ::refresh, ...))`
  using `::method` references, and initialize `MutableStateFlow(FeatureUiState().withCallbacks())`.
  Composables invoke `uiState.actions.onX(...)` directly. Do **NOT** use a `sealed class FeatureAction` +
  `handleAction(action)` dispatch — that is the obsolete pattern. Canonical example:
  `AssignmentDetailsUiState` / `AssignmentDetailsViewModel` (`libs/pandautils/.../features/assignments/details/`).
  For one-off events (toast/navigation), use a nullable state field + a consumption callback
  (`val toastMessage: String? = null`, `val onToastShown: () -> Unit = {}`), consumed in a `LaunchedEffect`.

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
- **No reflexive module-name prefixes** on classes (e.g. avoid an `NGC`-prefixed class name inside the `ngc` module); rely on package namespacing and only prefix a name to resolve an actual cross-module collision
- **Don't adopt already-deprecated libraries/APIs for new work**, even if still functional; propose an actively-maintained alternative and flag the deprecation up front

### Git Workflow
- **Branch naming**: `MBL-<number>-<short-kebab-case-slug>` derived from the Jira summary, e.g. `MBL-20020-video-keeps-playing-in-background`

### Component Patterns
- Use existing utility functions and shared components from `pandautils`
- Follow project's component structure and naming conventions
- Prefer Repository pattern for data access
- Use Hilt for dependency injection
- New UI features should use Jetpack Compose
- Legacy features may use XML + Data Binding
- **ViewModels depend on UseCases only** — never inject Managers or Repositories directly into a ViewModel; add or extend a UseCase for new data paths. New UseCases extend `BaseUseCase<Params, Result>` (`com.instructure.pandautils.domain.usecase.BaseUseCase`) with a nested `Params` data class and an overridden `suspend fun execute(params: Params): Result`, rather than a hand-rolled `operator fun invoke`
- **GraphQL (Apollo) access belongs in a Manager**, not wrapped directly by a Repository; build reusable REST repositories in `:pandautils` rather than one-off, per-screen repositories
- **REST data access uses Repositories, not Manager objects** — Manager singletons wrapping APIs with `StatusCallback`/`awaitApi` are obsolete for new REST work. Define an `XRepository` interface + `XRepositoryImpl(api)` under `com.instructure.pandautils.data.repository.<feature>` that wraps the API interface's `suspend` functions and returns `DataResult<T>`; provide it `@ActivityRetainedScoped` in `com.instructure.pandautils.di.RepositoryModule` (mirror `EnrollmentRepository`/`SubmissionRepository`)
- **One-shot UI events live in `UiState`** — model as a nullable field plus a consume lambda (see Horizon's `AddSubmissionUiState` idiom), not a `Channel`/`SharedFlow`/`collectOneOffEvents`

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
- **Never hardcode user-facing strings** — always use string resources (`stringResource(R.string.x)` in Compose, `resources.getString(...)` elsewhere) so copy stays localizable. Exempt: logs, exceptions, API/enum value comparisons, HTML wrappers, delimiters, empty defaults, and KDoc/`@Preview` sample text
- **New user-facing strings go in `pandares`** (`libs/pandares/src/main/res/values/strings.xml`, non-translatable strings in the not-translatable file); import the `R` class of the module the string belongs to in code, never a fully-qualified reference
- **No cross-module aliased `R` imports** — never `import com.instructure.canvasapi2.R as CanvasApiR` (or any other module's `R`) to borrow a string; import the module's own `R` and add new user-facing strings to `pandares`

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

### InstUI / NGC Conventions
- Use **InstUI components directly** (`com.instructure.instui.compose.*`) in NGC screens rather than the `ngc`/`designsystem` `DS*` wrappers
- **No Material Icons in `instui`** — don't use `Libs.COMPOSE_MATERIAL_ICONS` or `Icons.Filled.*`/`Icons.Default.*`; use an icon slot or defer icon-bearing work until the InstUI icon system exists
- When **no Android design exists** for a component, build it from InstUI design tokens (typography, `InstUISemanticColors`, `InstUILayoutSizes.Spacing`) rather than stalling; lean iOS-faithful when an iOS reference implementation exists

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

### InstUI Design System (in progress, as of 2026-04)
This is a snapshot of an evolving effort, not a permanent spec — check for updates before relying on specifics long-term.
- **Architecture direction ("Option B")**: a standalone design-token repo plus separate Android/iOS design-system repos, with no web dependency. `libs/horizon` and `libs/ngc` are the current home for this work; the plan is to move it into a separate repo, consumed first as a git submodule and later via Maven.
- **Token generation status**: in `libs/instui`, the primitive and semantic token layers are generated; the component-token layer is still pending, so new components currently follow a dummy-component strategy until that layer lands.
- **Icons**: the InstUI icon system has not been designed yet — components that need icons should wait for it or use a placeholder slot (see InstUI / NGC Conventions above).