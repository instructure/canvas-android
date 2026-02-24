# Canvas Android — Technical Documentation
> Reverse-engineered architecture reference · Last updated: 2026-02-24

---

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Repository Layout](#2-repository-layout)
3. [Technology Stack](#3-technology-stack)
4. [Build System](#4-build-system)
5. [Application Modules](#5-application-modules)
6. [Library Modules](#6-library-modules)
7. [Architecture Patterns](#7-architecture-patterns)
8. [Dependency Injection (Hilt)](#8-dependency-injection-hilt)
9. [Network Layer](#9-network-layer)
10. [Data Layer (Room)](#10-data-layer-room)
11. [Authentication & Session Management](#11-authentication--session-management)
12. [Theming & Design System](#12-theming--design-system)
13. [Navigation](#13-navigation)
14. [UI Layer — Compose vs XML](#14-ui-layer--compose-vs-xml)
15. [State Management (Mobius / MVI)](#15-state-management-mobius--mvi)
16. [Push Notifications & Firebase](#16-push-notifications--firebase)
17. [Analytics & Observability](#17-analytics--observability)
18. [PDF & Media Handling](#18-pdf--media-handling)
19. [Offline Support & WorkManager](#19-offline-support--workmanager)
20. [Testing Strategy](#20-testing-strategy)
21. [Automation & CI](#21-automation--ci)
22. [Build Variants & Flavors](#22-build-variants--flavors)
23. [Key Entry Points](#23-key-entry-points)
24. [Data Flow Diagram](#24-data-flow-diagram)

---

## 1. Project Overview

**Canvas** is an open-source Learning Management System (LMS) mobile suite built by **Instructure, Inc.** This repository contains three separate Android applications that connect to any self-hosted or Instructure-hosted Canvas LMS instance via its public REST API and GraphQL endpoint.

| App | Package ID | Version | Description |
|-----|-----------|---------|-------------|
| **Student** | `com.instructure.candroid` | 8.5.0 (build 287) | Student-facing LMS app |
| **Teacher** | `com.instructure.teacher` | — | Teacher/instructor app |
| **Parent** | `com.instructure.parentapp` | 4.9.0 (build 65) | Parent observer app |

- **License**: GNU General Public License v3 (Student/Teacher) / Apache 2.0 (libs)
- **Min SDK**: 28 (Android 9 Pie)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 36

---

## 2. Repository Layout

```
canvas-android/
├── apps/                          # Runnable Android applications
│   ├── student/                   # Student app
│   ├── teacher/                   # Teacher app
│   ├── parent/                    # Parent app
│   ├── build.gradle               # Root apps build file
│   ├── settings.gradle            # Module inclusion + root settings
│   ├── gradlew                    # Gradle wrapper (entry point for all builds)
│   └── buildSrc/                  # Shared build logic
│       └── src/main/java/
│           ├── GlobalDependencies.kt   # ALL library versions (Versions + Libs objects)
│           ├── LocaleScanner.kt        # Scans available locale translation files
│           └── MergePrivateData.kt     # Merges private API keys from private-data/
├── libs/                          # Shared library modules
│   ├── canvas-api-2/              # Core Canvas REST + GraphQL API client
│   ├── login-api-2/               # OAuth2 login flows
│   ├── pandautils/                # Shared UI utilities, base classes, composables
│   ├── pandares/                  # Shared resources (colors, themes, drawables)
│   ├── interactions/              # Interaction/animation library
│   ├── rceditor/                  # Rich Content Editor (WebView-based)
│   ├── annotations/               # Custom annotation processors
│   ├── panda_annotations/         # Panda-specific annotations
│   ├── panda_processor/           # Annotation processors (KSP)
│   └── recyclerview/              # Custom RecyclerView helpers
├── automation/                    # CI/CD & testing tooling
│   ├── espresso/                  # Shared Espresso test utilities
│   ├── dataseedingapi/            # Test data seeding API client
│   ├── soseedycli/                # Kotlin CLI for test data seeding
│   ├── cloud_build_api/           # Bitrise CI integration utilities
│   ├── cloud_build_metrics/       # Build metrics reporting CLI
│   ├── api_config/                # Protobuf schema parsing utility
│   └── cluster_create/            # GCP cluster management CLI
├── docs/                          # Documentation (this file lives here)
├── translations/                  # Crowdin translation source files
├── private-data/                  # ⚠️ Gitignored — API keys, keystore, secrets
├── scripts/                       # Shell utility scripts
└── open_source_data/              # Open-source disclosure data
```

---

## 3. Technology Stack

### Core Languages & Runtime
| Component | Version |
|-----------|---------|
| Kotlin | 2.2.21 |
| Java (compatibility target) | 17 |
| Kotlin Coroutines | 1.10.2 |
| KSP (Kotlin Symbol Processing) | 2.2.21-2.0.4 |

### UI
| Component | Library | Version |
|-----------|---------|---------|
| Compose BOM | `androidx.compose:compose-bom` | 2025.09.01 |
| Material 3 | `androidx.compose.material3` | 1.4.0 |
| Material Design (XML) | `com.google.android.material` | 1.13.0 |
| DataBinding | Built-in AGP | — |
| ViewBinding | Built-in AGP | — |
| Navigation (Fragments) | `androidx.navigation` | 2.9.6 |
| Navigation (Compose) | `androidx.navigation:navigation-compose` | 2.9.6 |
| Glance (App Widgets) | `androidx.glance:glance-appwidget` | 1.1.1 |
| Lottie Animations | `com.airbnb.android:lottie` | 6.7.0 |

### Networking
| Component | Library | Version |
|-----------|---------|---------|
| HTTP Client | OkHttp | 4.12.0 |
| REST Client | Retrofit | 3.0.0 |
| JSON Parser | Gson | 2.10.1 |
| GraphQL | Apollo | 4.3.3 |

### Persistence
| Component | Library | Version |
|-----------|---------|---------|
| Room (SQLite ORM) | `androidx.room` | 2.8.4 |
| DataStore (Preferences) | `androidx.datastore` | 1.2.0 |
| Encrypted SharedPreferences | `androidx.security:security-crypto` | 1.1.0 |

### Dependency Injection
| Component | Library | Version |
|-----------|---------|---------|
| Hilt | `com.google.dagger:hilt-android` | 2.57.2 |
| Hilt AndroidX | `androidx.hilt` | 1.3.0 |

### Architecture & State
| Component | Library | Version |
|-----------|---------|---------|
| ViewModel | `androidx.lifecycle:lifecycle-viewmodel-ktx` | 2.10.0 |
| LiveData | `androidx.lifecycle:lifecycle-livedata-ktx` | 2.10.0 |
| Mobius (MVI loop) | `com.spotify.mobius` | 1.2.1 |
| WorkManager | `androidx.work` | 2.11.0 |

### Firebase / Analytics
| Component | Library |
|-----------|---------|
| Firebase BOM | 34.6.0 |
| Crashlytics (NDK) | `firebase-crashlytics-ndk` |
| FCM Push Notifications | `firebase-messaging` |
| Remote Config | `firebase-config` |
| Pendo Analytics | `sdk.pendo.io:pendoIO:3.9.+` |

### Media / Content
| Component | Library | Version |
|-----------|---------|---------|
| PDF / Annotations | Nutrient (PSPDFKit) | 10.7.0 |
| ExoPlayer | Media3 ExoPlayer | 1.8.0 |
| Image Loading | Glide | 5.0.5 |
| Photo Zoom | PhotoView | 2.3.0 |
| QR Code | ZXing | 4.3.0 / 3.5.2 |
| Camera | CameraView | 2.7.2 |

### Testing
| Component | Library | Version |
|-----------|---------|---------|
| Unit Tests | JUnit 4 | 4.13.2 |
| Mocking | MockK | 1.14.7 |
| Android Unit Tests | Robolectric | 4.16 |
| UI Tests | Espresso + UIAutomator | — |
| Hilt Testing | `hilt-android-testing` | 2.57.2 |
| Coroutines Testing | `kotlinx-coroutines-test` | 1.10.2 |

---

## 4. Build System

### Gradle Configuration
- **Wrapper location**: `apps/gradlew`
- **AGP version**: 8.13.2
- **Build tools**: 35.0.0
- **Kotlin Gradle plugin**: 2.2.21

### Dependency Catalog (`buildSrc/GlobalDependencies.kt`)
All versions are centrally defined in two Kotlin objects:
```kotlin
object Versions { /* SDK versions, library versions */ }
object Libs     { /* Full dependency coordinates */ }
object Plugins  { /* Gradle plugin coordinates */ }
```

### Private Data Injection (`MergePrivateData.kt`)
Sensitive values (API keys, PSPDFKit license, OAuth credentials) are stored in `private-data/` (gitignored) and merged at build time via `PrivateData.merge(project, "student")`. This injects values as `BuildConfig` fields:
```kotlin
buildConfigField "String", "PSPDFKIT_LICENSE_KEY", "\"$pspdfkitLicenseKey\""
buildConfigField "String", "PENDO_TOKEN",           "\"$pendoAccessToken\""
```

### Signing Configuration
Release builds use environment variables for keystore credentials:
```groovy
storeFile     file('../candroid.keystore')
storePassword System.getenv('KEYSTORE_PASSWORD')
keyAlias      System.getenv('KEY_ALIAS')
keyPassword   System.getenv('KEY_PASSWORD')
```

---

## 5. Application Modules

### Student App (`apps/student`)
- **Application ID**: `com.instructure.candroid`
- **Namespace**: `com.instructure.student`
- **Entry activity**: `NavigationActivity`
- **Dependencies**: `login-api-2`, `annotations`, `rceditor`, `interactions`, `horizon`
- **Key features**: Assignments, grades, courses, calendar, discussions, modules, inbox, To-Do, file browser, QR login, app widgets, offline mode, speed grader, LTI tools, smart search

### Teacher App (`apps/teacher`)
- **Entry activity**: `NavigationActivity` (teacher variant)
- **Key features**: Course management, grading, speed grader, announcements, attendance

### Parent App (`apps/parent`)
- **Application ID**: `com.instructure.parentapp`
- **Version**: 4.9.0 (build 65)
- **Key features**: Observe child progress, alerts, calendar, inbox

---

## 6. Library Modules

### `canvas-api-2`
Core networking module. Contains:
- `CanvasRestAdapter` — abstract base for all Retrofit service builders
- `RestBuilder` / `RestParams` — builder pattern for constructing API calls
- `ApiPrefs` — singleton `PrefManager` storing domain, access token, refresh token, user object, theme
- `TokenRefresher` — handles token refresh via `Authenticator`
- `RequestInterceptor` / `ResponseInterceptor` — OkHttp interceptors for auth headers and response processing
- `managers/` — high-level API manager classes (one per Canvas API domain)
- `apis/` — Retrofit service interfaces
- `models/` — Gson-serializable data models (`User`, `Course`, `Assignment`, `CanvasContext`, etc.)
- `utils/` — `Logger`, `DateHelper`, `NumberHelper`, `CanvasType`, `FileUtils`
- GraphQL: `QLClientConfig`, `DomainServicesGraphQLClientConfig` (Apollo)

### `login-api-2`
OAuth2 login flows:
- School domain lookup (`FindSchoolActivity`)
- WebView-based OAuth token exchange
- Multi-user account switching
- QR code login

### `pandautils`
The main shared UI toolkit:
- `base/` — Base ViewModel, Base Fragment/Activity with common lifecycle helpers
- `blueprint/` — Presenter/View contract pattern (legacy MVC-like)
- `compose/` — Compose `CanvasTheme`, reusable composables (calendar, file details, RCE, to-do)
- `utils/ThemePrefs` — Runtime theming preferences
- `utils/ColorKeeper` — Maps course contexts to dynamic colours
- `data/repository/` — Repository implementations for all Canvas entities (courses, assignments, announcements, etc.)
- `analytics/` — Page view analytics
- `binding/` — DataBinding adapters

### `pandares`
Pure resource module (no Kotlin code):
- `res/values/colors.xml` — Full **InstUI** color system (light)
- `res/values-night/colors.xml` — Dark mode InstUI overrides
- `res/values/themes.xml` — App-wide Material themes
- `res/drawable/` — Shared vector drawables and icons
- Provides color tokens: `backgroundLightest`, `textDarkest`, `borderMedium`, course colors (12 presets), app theme accent colors (student/teacher/parent)

### `interactions`
Animation and interaction utilities.

### `rceditor`
Rich Content Editor — a WebView wrapper around a custom HTML editor used in discussions, announcements, and assignment submissions.

### `annotations` / `panda_annotations` / `panda_processor`
Custom KSP annotation processors for code generation (page view tracking, route registration).

---

## 7. Architecture Patterns

### Overall: Clean Architecture + MVVM
```
┌─────────────────────────────────────────────┐
│  UI Layer                                    │
│  Fragment / Composable  ←→  ViewModel        │
│  (observe StateFlow / LiveData)              │
├─────────────────────────────────────────────┤
│  Domain Layer                                │
│  UseCase (optional) / Repository Interface   │
├─────────────────────────────────────────────┤
│  Data Layer                                  │
│  RepositoryImpl → [LocalDataSource (Room)]   │
│                 → [RemoteDataSource (API)]   │
└─────────────────────────────────────────────┘
```

### Per-feature Module Pattern
Each feature (e.g., assignments) has:
```
features/assignments/details/
├── AssignmentDetailsFragment.kt       ← UI (XML + ViewBinding)
├── AssignmentDetailsViewModel.kt      ← ViewModel
├── AssignmentDetailsViewData.kt       ← UI state data class
├── datasource/
│   ├── AssignmentDetailsDataSource.kt         ← interface
│   ├── AssignmentDetailsLocalDataSource.kt    ← Room
│   └── AssignmentDetailsNetworkDataSource.kt  ← Retrofit
└── di/
    └── AssignmentDetailsModule.kt     ← Hilt bindings
```

### Repository Pattern
```kotlin
interface AssignmentDetailsRepository {
    suspend fun getAssignment(courseId: Long, assignmentId: Long): Assignment
}

class AssignmentDetailsRepositoryImpl @Inject constructor(
    private val localDataSource: AssignmentDetailsLocalDataSource,
    private val networkDataSource: AssignmentDetailsNetworkDataSource,
    private val isOnline: Boolean
) : AssignmentDetailsRepository {
    override suspend fun getAssignment(...) =
        if (isOnline) networkDataSource.getAssignment(...).also { localDataSource.save(it) }
        else localDataSource.getAssignment(...)
}
```

---

## 8. Dependency Injection (Hilt)

- **Application-level DI**: Each app has a `@HiltAndroidApp` Application class
- **Module count (Student app)**: 50+ `@Module`/`@InstallIn` classes
- **Scoping**: `@Singleton` (app-wide), `@ActivityScoped`, `@ViewModelScoped`
- **Fragment injection**: All fragments use `@AndroidEntryPoint`
- **ViewModel injection**: All ViewModels use `@HiltViewModel` + `@Inject constructor`
- **WorkManager integration**: `HiltWorkerFactory` via `@HiltAndroidApp`
- **Testing**: `HiltAndroidTest` + `@UninstallModules` + test `@Module` replacements

### Key DI Modules (Student)
| Module | Purpose |
|--------|---------|
| `ApplicationModule` | App-wide singletons (context, feature flags) |
| `DatabaseModule` | Room database + DAO providers |
| `NetworkModule` (implicit in canvas-api-2) | OkHttp, Retrofit, Apollo |
| `LoginModule` | Login router + auth dependencies |
| `OfflineModule` | Offline capability flags |
| `DashboardModule` | Dashboard repository bindings |
| `feature/*Module` | 30+ feature-specific repository/datasource bindings |

---

## 9. Network Layer

### Stack
```
ViewModel
  └── Repository
        └── NetworkDataSource
              └── Retrofit Service Interface
                    └── CanvasRestAdapter
                          └── OkHttpClient
                                ├── RequestInterceptor    (adds Authorization: Bearer <token>)
                                ├── ResponseInterceptor   (handles pagination Link headers)
                                ├── RollCallInterceptor   (attendance tracking)
                                └── TokenRefresher        (OkHttp Authenticator — 401 refresh)
```

### `ApiPrefs` (Session Storage)
```kotlin
object ApiPrefs : PrefManager("canvas-kit-sp") {
    var accessToken: String       // OAuth2 Bearer token
    var refreshToken: String      // For token refresh
    var protocol: String          // "https" default
    var domain: String            // e.g. "myschool.instructure.com"
    var user: User?               // Serialized current user
    var clientId: String          // OAuth client ID
    var clientSecret: String      // OAuth client secret
    var theme: CanvasTheme?       // Server-side theme overrides
    var canvasRegion: String?     // Data region
}
```

### Base URL Construction
```kotlin
val baseUrl = "${ApiPrefs.protocol}://${ApiPrefs.domain}/api/v1/"
```
Domain is **user-provided at login** — the user types their school's Canvas URL. There is no hardcoded server.

### Pagination
Canvas REST API uses `Link` header pagination. `ResponseInterceptor` parses `next`, `prev`, `first`, `last` URLs and `StatusCallback` exposes `hasMore()` for consumers.

### GraphQL (Apollo)
Used for domain services (`DomainServicesGraphQLClientConfig`). Apollo client is configured with `ApolloHttpCache` and custom `DomainServicesRequestInterceptor`.

---

## 10. Data Layer (Room)

### Database location
Each app has its own Room database. The schema is stored in `apps/*/schemas/`.

### Common entities (via pandautils)
- `CourseEntity`, `EnrollmentEntity`, `AssignmentEntity`
- `DiscussionTopicEntity`, `AnnouncementEntity`
- `FileEntity`, `ModuleEntity`, `QuizEntity`
- `CalendarEventEntity`, `ScheduleItemEntity`
- `InboxMessageEntity`, `AlertEntity`
- `PageViewAnalyticsEntity`

### DAOs
Each entity has a corresponding DAO interface. DAOs are provided via Hilt `@DatabaseModule`.

### Offline Strategy
1. **Network-first**: Try API, cache result in Room on success
2. **Cache-fallback**: If offline (`isOnline == false` from Hilt), serve from Room
3. **WorkManager sync**: Background periodic sync workers re-fetch data when connectivity resumes

---

## 11. Authentication & Session Management

### Flow
```
1. User opens app
2. LoginLandingPageActivity — school domain entry
3. FindSchoolActivity — Canvas auto-complete school search
4. OAuth2 WebView flow → Canvas LMS returns access_token
5. Token stored in ApiPrefs (EncryptedSharedPreferences)
6. User object fetched and stored
7. NavigationActivity launched
```

### Token Refresh
`TokenRefresher` implements OkHttp `Authenticator`. On 401 responses it:
1. Calls Canvas token refresh endpoint
2. Updates `ApiPrefs.accessToken`
3. Retries the original request

### Multi-Account
Multiple Canvas accounts (different institutions) are supported. Each account is stored as a separate `CanvasAccount` object.

### Masquerade (Act As)
Teachers/admins can masquerade as students. `ApiPrefs` supports a `masqueradeId` that is appended to API requests as `?as_user_id=`.

---

## 12. Theming & Design System

### InstUI Color System
Defined in `libs/pandares/src/main/res/values/colors.xml` (light) and `values-night/colors.xml` (dark):

**Semantic color tokens:**
```
backgroundLightest, backgroundLight, backgroundMedium, backgroundDark, backgroundDarkest
textDarkest, textDark, textPlaceholder, textInfo, textSuccess, textWarning, textDanger
borderLightest, borderLight, borderMedium, borderDark, borderDarkest
```

**App accent colors (light mode):**
- Student: `#E66000` (orange)
- Teacher: `#A855A1` (purple)
- Parent: `#1770AB` (blue)

**Dark mode accent colors:**
- Student: `#FF6B72`
- Teacher: `#CE7BE7`
- Parent: `#8094FF`

**Course colors (12 presets):**
`courseColor1` through `courseColor12` — used for differentiating courses in the UI.

### ThemePrefs
Runtime theming:
```kotlin
object ThemePrefs : PrefManager("theme-prefs") {
    var brandColor: Int         // Primary brand color (from server CanvasTheme)
    var buttonColor: Int
    var buttonTextColor: Int
    var primaryTextColor: Int
    var primaryColor: Int
    var accentColor: Int
    var darkMode: Boolean
}
```

### ColorKeeper
Maps `CanvasContext` (course) IDs to dynamic colors for course-specific theming.

### Compose Theme
`libs/pandautils/src/main/java/com/instructure/pandautils/compose/CanvasTheme.kt` wraps the InstUI token system into a Compose `MaterialTheme` provider.

### Server-side Theming
`CanvasTheme` model is fetched from the Canvas API (`/api/v1/brand_variables`) and stored in `ApiPrefs.theme`. This allows institutions to override brand colors.

---

## 13. Navigation

### Fragment-based (Legacy + Primary)
- `NavigationActivity` hosts a `NavHostFragment`
- Navigation graphs are defined in `res/navigation/*.xml`
- Deep links handled by `BaseRouterActivity` / `InterwebsToApplication`
- URL routing: Canvas URL patterns (e.g. `/courses/:id/assignments/:id`) are matched and dispatched to appropriate fragments

### Compose Navigation
- Newer features use `NavHostController` with Compose `NavHost`
- Hilt Compose Navigation: `hiltViewModel()` in composables

### Deep Link / URL Routing
`InterwebsToApplication` parses incoming Canvas web URLs and routes them to the correct in-app screen. `LoginRouter` handles Canvas-domain URLs before authentication.

---

## 14. UI Layer — Compose vs XML

### Current State (Hybrid)
The codebase is actively migrating from XML DataBinding/ViewBinding to Jetpack Compose:

| UI Technology | Used For |
|--------------|----------|
| **Jetpack Compose + Material 3** | New features, calendar, to-do, file details, RCE composables |
| **XML + DataBinding** | Legacy screens (courses list, grades, submission details) |
| **XML + ViewBinding** | Many fragments, recycler views |
| **WebView** | RCE (Rich Content Editor), LTI tool launches |

### Key Compose entry points
- `pandautils/compose/composables/` — shared composables
- `pandautils/compose/CanvasTheme.kt` — theme wrapper
- Feature-specific composable files under `apps/*/features/*/compose/`

---

## 15. State Management (Mobius / MVI)

Spotify's **Mobius** library is used for an MVI-like unidirectional data flow in complex screens:

```
Event → Update (pure function) → Model (immutable state)
                               ↓
                         Effect Handler → Side effects (API calls, navigation)
                               ↓
                            Effect → triggers next Event
```

**Mobius components:**
- `MobiusLoop` — the core state machine
- `Update<Model, Event, Effect>` — pure state transition function
- `Connectable<Effect, Event>` — side effect handlers
- `MobiusAndroid.loopFrom(loopBuilder, modelObservable)` — Android integration

Used alongside ViewModels: the ViewModel owns the Mobius loop.

---

## 16. Push Notifications & Firebase

- **FCM** (`firebase-messaging`) — push notifications for new assignments, grades, messages
- **Crashlytics** (`firebase-crashlytics-ndk`) — crash reporting including NDK crashes
- **Remote Config** (`firebase-config`) — feature flags and remote configuration
- FCM token registration happens at login; token is sent to Canvas API
- Notification channels are registered per notification type

---

## 17. Analytics & Observability

### Pendo (`sdk.pendo.io:pendoIO:3.9.+`)
- Product analytics and in-app guidance
- Initialized with `PENDO_TOKEN` (injected at build time from private data)
- Tracks screen views and user interactions

### Page View Analytics
- Custom internal page view tracking in `pandautils/analytics/pageview/`
- Persists page view events to Room (`PageViewAnalyticsEntity`)
- Periodically flushes to Canvas API

### Logging
- `Logger` class in `canvas-api-2/utils/` — wraps `android.util.Log` with build-type guards

---

## 18. PDF & Media Handling

### Nutrient (formerly PSPDFKit) — `io.nutrient:nutrient:10.7.0`
- PDF rendering, annotation, and signing
- Licensed via `PSPDFKIT_LICENSE_KEY` build config field
- Custom `CandroidPSPDFActivity` in student app wraps Nutrient viewer

### ExoPlayer (Media3) — `androidx.media3:media3-exoplayer:1.8.0`
- Video and audio playback (MP4, HLS, DASH, SmoothStreaming)
- Embedded in submission viewers and course media

### Glide — `com.github.bumptech.glide:glide:5.0.5`
- Image loading and caching
- OkHttp integration (`glide-okhttp3-integration`) for authenticated image requests

---

## 19. Offline Support & WorkManager

- **WorkManager** (`androidx.work:2.11.0`) — background sync and upload tasks
- **Offline flag**: Injected via Hilt's `@Named("isOnline") Boolean` — switches repositories between network and Room sources
- **Sync workers**: Registered to run on network reconnect
- **Hilt Worker integration**: `HiltWorkerFactory` + `@HiltWorker` on worker classes

---

## 20. Testing Strategy

### Unit Tests (`test/`)
- Framework: **JUnit 4** + **MockK** (Kotlin-friendly mocking)
- Android-specific: **Robolectric** 4.16 (runs Android code on JVM)
- Coroutines: `kotlinx-coroutines-test` + `TestCoroutineDispatcher`
- Architecture: `androidx.arch.core:core-testing` for `LiveData` / `StateFlow` testing

### Instrumented UI Tests (`androidTest/`)
- Framework: **Espresso** + **UIAutomator**
- Page Object Model in `automation/espresso/`
- Test Runner: `StudentHiltTestRunner` (custom Hilt test runner per app)
- Data seeding: `automation/dataseedingapi` creates test users/courses via Canvas API
- Test flavors: `qa` flavor enables `IS_TESTING = true` and exposes test credentials

### Coverage
- JaCoCo via `./gradlew -Pcoverage firebaseJacoco`
- Firebase Test Lab integration for CI

---

## 21. Automation & CI

| Tool | Purpose |
|------|---------|
| `automation/espresso/` | Shared Espresso page objects and helpers |
| `automation/dataseedingapi/` | Retrofit client library for Canvas test data seeding |
| `automation/soseedycli/` | CLI wrapper (`java -jar soseedycli.jar`) for data seeding commands |
| `automation/cloud_build_api/` | Bitrise CI API + Google Sheets/Cloud Datastore integration |
| `automation/cloud_build_metrics/` | Build and test metrics reports (sent to Data Studio) |
| `automation/api_config/` | Protobuf schema parser using Square Wire |
| `automation/cluster_create/` | GCP Firebase Test Lab cluster management |

**CI Platform**: Bitrise (referenced throughout `cloud_build_api/cloud_build_metrics`)

---

## 22. Build Variants & Flavors

### Flavor Dimensions
```groovy
flavorDimensions 'default'
productFlavors {
    dev  { /* Development — loose checks */ }
    qa   { IS_TESTING=true; test credentials injected }
    prod { /* Production release */ }
}
```

### Build Types
| Type | Debug | Minify | Signing | Notes |
|------|-------|--------|---------|-------|
| `debug` | ✅ | ❌ | Debug | pseudoLocalesEnabled |
| `debugMinify` | ❌ | ✅ | Debug | Test minification |
| `release` | ❌ | ✅ | Release keystore | Production build |

### Full variant matrix
`devDebug`, `devRelease`, `qaDebug`, `qaRelease`, `prodDebug`, `prodRelease`

---

## 23. Key Entry Points

| File | Role |
|------|------|
| `apps/student/src/main/java/.../activity/NavigationActivity.kt` | Main host activity |
| `apps/student/src/main/java/.../activity/LoginActivity.kt` | Login entry point |
| `apps/student/src/main/java/.../activity/LoginLandingPageActivity.kt` | School domain input |
| `apps/student/src/main/java/.../activity/BaseRouterActivity.kt` | Deep link router |
| `apps/student/src/main/java/.../activity/InterwebsToApplication.kt` | URL → in-app routing |
| `libs/canvas-api-2/.../CanvasRestAdapter.kt` | Base Retrofit adapter |
| `libs/canvas-api-2/.../utils/ApiPrefs.kt` | Auth/session storage |
| `libs/pandautils/.../utils/ThemePrefs.kt` | Runtime theming |
| `libs/pandautils/.../utils/ColorKeeper.kt` | Course color mapping |
| `libs/pandautils/.../compose/CanvasTheme.kt` | Compose theme root |
| `apps/buildSrc/.../GlobalDependencies.kt` | All library versions |

---

## 24. Data Flow Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│  CANVAS LMS SERVER (institution-hosted)                          │
│  REST API: https://<domain>/api/v1/                              │
│  GraphQL:  https://<domain>/api/graphql                          │
└────────────────────────┬─────────────────────────────────────────┘
                         │  HTTPS + OAuth2 Bearer token
                         ▼
┌──────────────────────────────────────────────────────────────────┐
│  NETWORK LAYER (canvas-api-2)                                    │
│  OkHttpClient → RequestInterceptor (auth) → Retrofit / Apollo   │
│                → ResponseInterceptor (pagination)                │
│                → TokenRefresher (401 auto-refresh)               │
└────────────────────────┬─────────────────────────────────────────┘
                         │
                         ▼
┌──────────────────────────────────────────────────────────────────┐
│  DATA LAYER                                                       │
│  RemoteDataSource (Retrofit/Apollo calls)                        │
│        ↕  sync                                                   │
│  LocalDataSource  (Room DAOs)                                    │
│  Repository combines both based on connectivity                  │
└────────────────────────┬─────────────────────────────────────────┘
                         │ suspend fun / Flow<>
                         ▼
┌──────────────────────────────────────────────────────────────────┐
│  VIEWMODEL (Hilt-injected)                                       │
│  StateFlow<UiState> / LiveData<ViewData>                         │
│  Mobius loop for complex screens                                 │
└────────────────────────┬─────────────────────────────────────────┘
                         │ observe / collect
                         ▼
┌──────────────────────────────────────────────────────────────────┐
│  UI LAYER                                                         │
│  Fragment (XML DataBinding / ViewBinding)                        │
│  Composable (Jetpack Compose + Material 3)                       │
└──────────────────────────────────────────────────────────────────┘
```

---

*Generated by reverse-engineering the canvas-android open-source repository.*
