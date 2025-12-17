# Forecast Widget Implementation Plan

## Overview

Implementation of the Forecast Simple Widget (MBL-19459) for the Student app, providing students with at-a-glance visibility of their workload, overdue assignments, upcoming work, and recent grades.

**Status:** Implementation Complete (Phases 0-4) - Testing Pending
**Dependency:** MBL-19453 (Widget Foundation & Layout System) ✅ COMPLETE
**Target:** Single PR implementation
**Location:** Student app only

## Feature Requirements

### Core Functionality

1. **Week Navigation**
   - Display current week period (e.g., "Dec 9 - Dec 15")
   - Navigation arrows to move between weeks (previous/next)
   - Week definition based on user's locale settings
     - Default: Monday-Sunday for most locales
     - US locale: Sunday-Saturday
   - Selected period persists when navigating away and returning

2. **Missing Section (Overdue Assignments)**
   - Shows all missing assignments from past until today
   - Sorted by due date (oldest first - most urgent)
   - Collapsible/expandable with count badge (e.g., "Missing (5)")
   - Hidden when no overdue assignments

3. **Due Section (Upcoming Assignments)**
   - Shows assignments due in the selected week period
   - Sorted by due date (earliest first)
   - Collapsible/expandable with count badge (e.g., "Due (8)")
   - Hidden when no assignments due

4. **Recent Grades Section**
   - Shows assignments graded in last 7 days
   - Sorted by grading date (newest first)
   - Collapsible/expandable with count badge (e.g., "Recent Grades (3)")
   - Hidden when no recent grades
   - Note: No "new" badge tracking needed, just item count

### Assignment Item Display

Each assignment item shows:
- Course name and color
- Assignment name
- Due date/time (or grading date for Recent Grades)
- Points possible
- Assignment weight (if applicable)

### Interactions

- All assignment items are tappable
- Tapping navigates to assignment detail screen
- Smooth animations for accordion expand/collapse
- Smooth transitions when navigating between weeks

### State Persistence

- Week offset persists across sessions
- Section collapsed/expanded states persist
- Background color customization (via PR #3441)

### Accessibility

- Full TalkBack support
- Proper content descriptions for all interactive elements

## Architecture

### Pattern

Following the **domain-based repository** pattern used in the codebase:

```
Jetpack Compose UI
      ↓
  ViewModel (Hilt)
      ↓
   Use Cases
      ↓
Domain Repositories (interface + impl)
      ↓
  Canvas API Interfaces
```

**Key Architectural Principles:**
- **Domain Repositories**: Located in `pandautils/data/repository/{domain}/`
  - Interface + Implementation pattern (e.g., `AssignmentRepository` + `AssignmentRepositoryImpl`)
  - Methods return `DataResult<T>`
  - Provided in `RepositoryModule` as Singletons
- **Use Cases**: Located in `pandautils/domain/usecase/{domain}/`
  - Extend `BaseUseCase<Params, Result>`
  - Inject repositories (not APIs directly)
  - Invoked with operator pattern: `val result = useCase(params)`
- **ViewModels**: Inject use cases (not repositories directly)
- **Feature Repositories**: For widget-specific logic only, in feature package

### Required Infrastructure Components

**New Canvas API Components (in `canvas-api-2`):** ✅ COMPLETE
1. **RecentGradedSubmissionsQuery.graphql** - GraphQL query for recent graded submissions
   - Uses `allCourses` and `submissionsConnection` with `gradedSince` filter
   - Implements pagination with `pageInfo`, `hasNextPage`, `endCursor`
2. **RecentGradedSubmissionsManager** - GraphQL manager with depaging support
   - `suspend fun getRecentGradedSubmissions(studentId: Long, gradedSince: String, pageSize: Int, forceNetwork: Boolean)`
3. **PlannerAPI.kt** - Already has suspend methods ✅
   - `getPlannerItems(startDate, endDate, contextCodes, filter)` - used for Due section
4. **Models** - New or extended:
   - `GradedSubmission.kt` (new) - Data model for graded submissions
   - Ensure `Assignment`, `PlannerItem` models support needed fields

**New Domain Repositories (in `pandautils/data/repository/`):** ✅ COMPLETE
1. **assignment/AssignmentRepository.kt** (interface) ✅
   - `suspend fun getMissingAssignments(forceRefresh: Boolean): DataResult<List<Assignment>>`
2. **assignment/AssignmentRepositoryImpl.kt** ✅
3. **planner/PlannerRepository.kt** (interface) ✅
   - `suspend fun getPlannerItems(startDate: String, endDate: String, forceRefresh: Boolean): DataResult<List<PlannerItem>>`
4. **planner/PlannerRepositoryImpl.kt** ✅
5. **submission/SubmissionRepository.kt** (interface) ✅
   - `suspend fun getRecentGradedSubmissions(studentId: Long, gradedSince: String, forceRefresh: Boolean): DataResult<List<GradedSubmission>>`
6. **submission/SubmissionRepositoryImpl.kt** ✅

**New Use Cases (in `pandautils/domain/usecase/`):** ✅ COMPLETE
1. **assignment/LoadMissingAssignmentsUseCase.kt** ✅
2. **assignment/LoadUpcomingAssignmentsUseCase.kt** ✅ (uses PlannerRepository)
3. **audit/LoadRecentGradeChangesUseCase.kt** ✅ (uses SubmissionRepository, filters by endTime)

**Hilt Module Updates:** ✅ COMPLETE
- Updated `GraphQlApiModule.kt` to provide `RecentGradedSubmissionsManager`
- Updated `RepositoryModule.kt` to provide new repositories

### Widget-Specific Components

1. **ForecastWidget.kt** - Main Compose UI entry point
2. **ForecastWidgetViewModel.kt** - Business logic, injects use cases
3. **ForecastWidgetRepository.kt** - Widget-specific data transformation only (optional)
4. **ForecastWidgetDataStore.kt** - Preferences persistence
5. **ForecastWidgetRouter.kt** - Navigation to assignment details

### Reusable Components

- Assignment item patterns - Reference existing implementations
- Date utilities - Use existing pandautils date helpers
- Course color utilities - Use existing pandautils helpers

### Custom Components to Build

- **Tabbed Section Interface** - Horizontal tabs with count badges (different from CoursesWidget's CollapsibleSection)
- Tab selection with single expanded section at a time
- Animated transitions between tabs

## File Structure

### Canvas API Layer (`libs/canvas-api-2`)
```
canvas-api-2/src/main/
├── graphql/com/instructure/canvasapi2/
│   └── RecentGradedSubmissionsQuery.graphql    # NEW ✅ - GraphQL query with pagination
└── java/com/instructure/canvasapi2/
    ├── managers/graphql/
    │   ├── RecentGradedSubmissionsManager.kt        # NEW ✅ - Interface
    │   └── RecentGradedSubmissionsManagerImpl.kt    # NEW ✅ - GraphQL manager with depaging
    └── di/
        └── GraphQlApiModule.kt                      # UPDATE ✅ - Provide manager
```

### Domain Layer (`libs/pandautils`)
```
pandautils/src/main/java/com/instructure/pandautils/
├── data/
│   ├── model/
│   │   └── GradedSubmission.kt         # NEW ✅ - Graded submission model
│   └── repository/
│       ├── assignment/
│       │   ├── AssignmentRepository.kt     # NEW ✅ - Interface
│       │   └── AssignmentRepositoryImpl.kt # NEW ✅ - Implementation
│       ├── planner/
│       │   ├── PlannerRepository.kt        # NEW ✅ - Interface
│       │   └── PlannerRepositoryImpl.kt    # NEW ✅ - Implementation
│       └── submission/
│           ├── SubmissionRepository.kt         # NEW ✅ - Interface
│           └── SubmissionRepositoryImpl.kt     # NEW ✅ - Implementation
├── domain/usecase/
│   ├── assignment/
│   │   ├── LoadMissingAssignmentsUseCase.kt    # NEW ✅
│   │   └── LoadUpcomingAssignmentsUseCase.kt   # NEW ✅ (uses PlannerRepository)
│   └── audit/
│       └── LoadRecentGradeChangesUseCase.kt    # NEW ✅ (uses SubmissionRepository)
└── di/
    └── RepositoryModule.kt             # UPDATE ✅ - Add new repository providers
```

### Widget Feature Layer (`apps/student`)
```
student/src/main/java/com/instructure/student/
├── features/dashboard/widget/forecast/
│   ├── ForecastWidget.kt               # Main Compose UI
│   ├── ForecastWidgetViewModel.kt      # Business logic (injects use cases)
│   ├── ForecastWidgetUiState.kt        # UI state model
│   ├── ForecastWidgetRouter.kt         # Navigation
│   ├── ForecastWidgetDataStore.kt      # Preferences persistence
│   └── components/
│       ├── WeekNavigationHeader.kt     # Week period + navigation
│       ├── TabbedSectionInterface.kt   # Custom tabbed section
│       └── AssignmentListItem.kt       # Assignment item UI
└── di/feature/
    └── ForecastWidgetModule.kt         # Hilt DI for widget

student/src/test/java/com/instructure/student/
└── features/dashboard/widget/forecast/
    └── ForecastWidgetViewModelTest.kt  # ViewModel unit tests

student/src/androidTest/java/com/instructure/student/
└── features/dashboard/widget/forecast/
    └── ForecastWidgetTest.kt           # UI integration tests
```

## Implementation Phases

### Phase 0: Infrastructure Layer (Canvas API & Domain) ✅ COMPLETE

**Location:** `libs/canvas-api-2` and `libs/pandautils`

**Actual Implementation:**
- `canvas-api-2/.../graphql/RecentGradedSubmissionsQuery.graphql` - NEW ✅ GraphQL query
- `canvas-api-2/.../managers/graphql/RecentGradedSubmissionsManager.kt` - NEW ✅ Interface
- `canvas-api-2/.../managers/graphql/RecentGradedSubmissionsManagerImpl.kt` - NEW ✅ Implementation with depaging
- `canvas-api-2/.../di/GraphQlApiModule.kt` - UPDATE ✅ Provide manager
- `pandautils/.../data/model/GradedSubmission.kt` - NEW ✅ Data model
- `pandautils/.../data/repository/assignment/AssignmentRepository.kt` - NEW ✅
- `pandautils/.../data/repository/assignment/AssignmentRepositoryImpl.kt` - NEW ✅
- `pandautils/.../data/repository/planner/PlannerRepository.kt` - NEW ✅
- `pandautils/.../data/repository/planner/PlannerRepositoryImpl.kt` - NEW ✅
- `pandautils/.../data/repository/submission/SubmissionRepository.kt` - NEW ✅
- `pandautils/.../data/repository/submission/SubmissionRepositoryImpl.kt` - NEW ✅
- `pandautils/.../domain/usecase/assignment/LoadMissingAssignmentsUseCase.kt` - NEW ✅
- `pandautils/.../domain/usecase/assignment/LoadUpcomingAssignmentsUseCase.kt` - NEW ✅
- `pandautils/.../domain/usecase/audit/LoadRecentGradeChangesUseCase.kt` - NEW ✅
- `pandautils/.../di/RepositoryModule.kt` - UPDATE ✅

**Key Implementation Notes:**
- Used GraphQL `submissionsConnection` instead of REST Audit API (students lack permission for audit endpoints)
- Implemented pagination/depaging in `RecentGradedSubmissionsManagerImpl` following existing patterns
- Client-side filtering by endTime in `LoadRecentGradeChangesUseCase`
- Used existing `toDate()` extension from `canvasapi2.utils`

### Phase 1: Widget Data Layer ✅ COMPLETE

**Location:** `apps/student`

**Files:**
- `ForecastWidgetUiState.kt` - UI state model ✅
- `ForecastWidgetDataStore.kt` - Preferences persistence ✅

**Completed:**
1. ✅ Defined `ForecastSection` enum (MISSING, DUE, RECENT_GRADES)
2. ✅ Defined `ForecastWidgetUiState` data class with all required state
3. ✅ Created `ForecastWidgetDataStore` for persisting:
   - Week offset (Int)
   - Selected section (ForecastSection? nullable)
4. ✅ Defined helper models:
   - `WeekPeriod` data class for week calculations
   - `AssignmentItem` data class for unified assignment display

### Phase 2: ViewModel & State ✅ COMPLETE

**Files:**
- `ForecastWidgetUiState.kt` ✅
- `ForecastWidgetViewModel.kt` ✅

**Completed:**
1. ✅ Defined UI state structure with all sections
2. ✅ Implemented ViewModel with:
   - Data loading logic for all three sections
   - Week navigation (previous/next) with DataStore persistence
   - Section toggle handlers
   - Assignment click handler with router integration
   - Refresh capability
3. ✅ Combined DataStore flows with data flows
4. ✅ Handled loading/error/empty states

### Phase 3: UI Components ✅ COMPLETE

**Files:**
- `ForecastWidget.kt` ✅
- Components built inline within ForecastWidget.kt

**Completed:**
1. ✅ Created main widget composable with ViewModel integration
2. ✅ Built week navigation header:
   - Week period display
   - Previous/next arrows with proper styling
3. ✅ Implemented tabbed section interface:
   - Horizontal tabs with count badges
   - Single expanded section at a time
   - Animated transitions
   - Selected tab indication
4. ✅ Created assignment list item with:
   - Course color indicator
   - Course name
   - Assignment name
   - Date/time
   - Points display
5. ✅ Added empty state messages for each section
6. ✅ Implemented loading states

### Phase 4: Navigation & Integration ✅ COMPLETE

**Files:**
- `ForecastWidgetRouter.kt` ✅
- Integration into `DashboardScreen.kt` ✅
- `WidgetMetadata.kt` - Added WIDGET_ID_FORECAST ✅
- `EnsureDefaultWidgetsUseCase.kt` - Added default widget entry ✅

**Completed:**
1. ✅ Created router for assignment detail navigation using `RouteMatcher.route()`
2. ✅ Hilt dependency injection (ViewModel auto-injected)
3. ✅ Integrated widget into dashboard with refresh signal
4. ✅ Added widget to default widgets list (position 4, not full width)
5. ✅ Navigation flows implemented

### Phase 5: Testing

**Files:**
- Unit tests
- Integration tests

**Tasks:**
1. Write ViewModel tests:
   - Week navigation logic
   - Section toggling
   - Data loading and mapping
   - Error handling
   - Empty states
2. Write Repository tests:
   - API data transformation
   - Week calculation logic
3. Write UI tests:
   - Section expand/collapse
   - Assignment click navigation
   - Week navigation
   - Accessibility (TalkBack)

## Data Models

### AssignmentItem

```kotlin
data class AssignmentItem(
    val id: Long,
    val courseId: Long,
    val courseName: String,
    val courseColor: Int,
    val assignmentName: String,
    val dueDate: Date?,
    val gradedDate: Date?,
    val pointsPossible: Double,
    val weight: Double?,
    val iconRes: Int,
    val url: String
)
```

### WeekPeriod

```kotlin
data class WeekPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val displayText: String,  // e.g., "Dec 9 - Dec 15"
    val weekNumber: Int       // Week number of year
)
```

### ForecastWidgetUiState

```kotlin
enum class ForecastSection {
    MISSING,
    DUE,
    RECENT_GRADES
}

data class ForecastWidgetUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,

    // Week navigation
    val weekPeriod: WeekPeriod,
    val canNavigatePrevious: Boolean = true,
    val canNavigateNext: Boolean = true,

    // Section data
    val missingAssignments: List<AssignmentItem> = emptyList(),
    val dueAssignments: List<AssignmentItem> = emptyList(),
    val recentGrades: List<AssignmentItem> = emptyList(),

    // Currently selected/expanded section (tab-based UI)
    val selectedSection: ForecastSection = ForecastSection.DUE,

    // Event handlers
    val onNavigatePrevious: () -> Unit = {},
    val onNavigateNext: () -> Unit = {},
    val onSectionSelected: (ForecastSection) -> Unit = {},
    val onAssignmentClick: (Long, Long) -> Unit = { _, _ -> },
    val onRetry: () -> Unit = {}
)
```

## Key Implementation Details

### Week Calculation

```kotlin
// Pseudo-code for week calculation
fun calculateWeekPeriod(offsetWeeks: Int, locale: Locale): WeekPeriod {
    val today = LocalDate.now()
    val firstDayOfWeek = if (locale == Locale.US) DayOfWeek.SUNDAY else DayOfWeek.MONDAY

    val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
    val targetWeekStart = currentWeekStart.plusWeeks(offsetWeeks.toLong())
    val targetWeekEnd = targetWeekStart.plusDays(6)

    val displayText = "${targetWeekStart.format(formatter)} - ${targetWeekEnd.format(formatter)}"

    return WeekPeriod(
        startDate = targetWeekStart,
        endDate = targetWeekEnd,
        displayText = displayText,
        weekNumber = targetWeekStart.get(WeekFields.of(locale).weekOfYear())
    )
}
```

### Section Visibility Rules

- **Missing Section**: Hidden when `missingAssignments.isEmpty()`
- **Due Section**: Hidden when `dueAssignments.isEmpty()`
- **Recent Grades Section**: Hidden when `recentGrades.isEmpty()`

### Sorting Rules

- **Missing**: `sortedBy { it.dueDate }` (oldest first)
- **Due**: `sortedBy { it.dueDate }` (earliest first)
- **Recent Grades**: `sortedByDescending { it.gradedDate }` (newest first)

### Empty State Messages

- **Missing**: "All caught up!"
- **Due**: "No assignments due this week"
- **Recent Grades**: "No new grades yet"

### Assignment Icons

Map assignment types to existing icon resources:
- Assignment: `R.drawable.ic_assignment`
- Quiz: `R.drawable.ic_quiz`
- Discussion: `R.drawable.ic_discussion`
- etc.

## Technical Considerations

### Performance

- Use `LazyColumn` for assignment lists if > 10 items
- Cache week calculations to avoid repeated computation
- Debounce week navigation to prevent rapid API calls
- Use Flow collection with proper lifecycle awareness

### State Management

- All state changes flow through ViewModel
- DataStore provides persistent storage
- Use `StateFlow` for UI state
- Combine multiple Flow sources efficiently

### Error Handling

- Show retry button on API failures
- Log errors to Crashlytics
- Graceful degradation (show cached data if available)
- Clear error messages for users

### Accessibility

- Content descriptions for all interactive elements
- TalkBack announcements for section expand/collapse
- Navigate by heading support
- Proper focus management

### Testing Strategy

1. **Unit Tests**: ViewModel logic, Repository mapping, Week calculations
2. **Integration Tests**: Full UI flows, Navigation, Section interactions
3. **Accessibility Tests**: TalkBack navigation, Content descriptions
4. **Performance Tests**: Large dataset handling, Rapid navigation

## Dependencies

### Existing Components to Reuse

- Date formatting utilities - pandautils
- Course color utilities - pandautils
- Assignment icon mapping - student app

### New Dependencies

None - using existing project dependencies

## Design Reference

- [Figma Design - Dashboard MVP](https://www.figma.com/design/d0XYwwGe37hEWdmArVVBhr/%E2%8F%B3-Mobile-Design-2024-2025?node-id=14030-48564)
- Collapsed widget: node-id=15621-25241
- Expanded widget: node-id=16208-122589

## Open Questions

1. ~~**Grade Change Log API**: Need to verify exact endpoint and response format~~ ✅ RESOLVED
   - Used GraphQL `submissionsConnection` with `gradedSince` filter instead of REST Audit API
   - Students lack permission for audit endpoints, GraphQL approach works correctly
2. **Background Color Integration**: Wait for PR #3441 to merge, then integrate color picker
3. **Assignment Weight Display**: Confirm format (e.g., "10% of final grade")
4. **Date Format**: Confirm locale-specific date formatting requirements
5. **Recent Grades Week Change**: Recent grades list currently doesn't update when week offset changes - may need to reload or filter based on week period

## Success Criteria

- [ ] Week navigation works correctly based on locale
- [ ] Missing section shows all overdue assignments
- [ ] Due section shows correct assignments for selected week
- [ ] Recent Grades section shows assignments graded in last 7 days
- [ ] All sections are collapsible/expandable
- [ ] Section states persist across sessions
- [ ] Week selection persists across sessions
- [ ] Tapping assignments navigates to correct detail screen
- [ ] All assignment data displays accurately
- [ ] Empty states display appropriately
- [ ] Smooth animations throughout
- [ ] Full TalkBack accessibility
- [ ] Loading states show properly
- [ ] Error states have retry functionality
- [ ] No layout jumping during orientation changes
- [ ] Unit test coverage > 80%
- [ ] All UI tests pass

## Timeline Estimate

- Phase 0 (Infrastructure - APIs, Repositories, Use Cases): 3-4 days
- Phase 1 (Widget Data Layer): 1 day
- Phase 2 (ViewModel): 1-2 days
- Phase 3 (UI Components): 3-4 days
- Phase 4 (Integration): 1 day
- Phase 5 (Testing): 2-3 days
- **Total**: ~11-15 days

**Note:** Phase 0 is foundational infrastructure that benefits the entire codebase, not just this widget.

## Related Tickets

- MBL-19453: Widget Foundation & Layout System ✅ COMPLETE
- MBL-19459: Forecast Simple Widget (this ticket)
- MBL-19460: Forecast Simple Widget (Part 2) - Future enhancement
- PR #3441: Background color picker (pending review)