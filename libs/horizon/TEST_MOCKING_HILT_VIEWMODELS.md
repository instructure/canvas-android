# Mocking Hilt ViewModels in Compose UI Tests

## Problem
When testing Compose screens that use `hiltViewModel<T>()`, you need to provide ViewModels through Hilt's dependency injection. You have two options:
1. **Use real ViewModels** (integration testing)
2. **Mock ViewModels** (isolated UI testing)

## Solution 1: Using Real ViewModels (Integration Testing)

This approach uses the real Hilt modules and ViewModels. Good for integration tests.

```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HorizonHomeUiTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testHomeNavigationDisplaysBottomBar() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val parentNavController = rememberNavController()
            // This will use real ViewModels injected by Hilt
            HomeNavigation(navController, parentNavController)
        }

        composeTestRule.onNodeWithContentDescription("Home").assertIsDisplayed()
    }
}
```

### Required: HiltTestActivity
Create this activity in your androidTest directory:

```kotlin
package com.instructure.horizon.ui

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
```

## Solution 2: Mocking ViewModels (Isolated Testing)

If you want to mock the ViewModels to control their behavior, you need to:

### Step 1: Create Test Modules

Create fake/mock ViewModels and provide them via test modules:

```kotlin
// In androidTest directory
package com.instructure.horizon.ui.features.home

import androidx.lifecycle.SavedStateHandle
import com.instructure.horizon.features.dashboard.DashboardRepository
import com.instructure.horizon.features.dashboard.DashboardViewModel
import com.instructure.horizon.features.learn.LearnRepository
import com.instructure.horizon.features.learn.LearnViewModel
import com.instructure.horizon.features.skillspace.SkillspaceRepository
import com.instructure.horizon.features.skillspace.SkillspaceViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [] // Add your production modules here if needed
)
object TestRepositoryModule {
    @Provides
    @Singleton
    fun provideDashboardRepository(): DashboardRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideLearnRepository(): LearnRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideSkillspaceRepository(): SkillspaceRepository = mockk(relaxed = true)
}
```

### Step 2: Configure Mocks in Tests

```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HorizonHomeUiTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Inject
    lateinit var dashboardRepository: DashboardRepository

    @Inject
    lateinit var learnRepository: LearnRepository

    @Before
    fun init() {
        hiltRule.inject()

        // Configure mock behavior
        coEvery { dashboardRepository.getCourses() } returns emptyList()
        coEvery { learnRepository.getPrograms(any()) } returns emptyList()
    }

    @Test
    fun testHomeNavigationWithMockedData() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val parentNavController = rememberNavController()
            HomeNavigation(navController, parentNavController)
        }

        // Test with controlled mock data
        composeTestRule.onNodeWithContentDescription("Home").assertIsDisplayed()
    }
}
```

## Solution 3: Using CompositionLocalProvider (Alternative)

If you don't want to use Hilt testing, you can manually provide ViewModels:

```kotlin
@RunWith(AndroidJUnit4::class)
class HorizonHomeUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testWithProvidedViewModels() {
        val mockDashboardViewModel = mockk<DashboardViewModel>(relaxed = true)
        val mockLearnViewModel = mockk<LearnViewModel>(relaxed = true)

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides FakeViewModelStoreOwner(
                    dashboardViewModel = mockDashboardViewModel,
                    learnViewModel = mockLearnViewModel
                )
            ) {
                val navController = rememberNavController()
                val parentNavController = rememberNavController()
                HomeNavigation(navController, parentNavController)
            }
        }
    }
}
```

**Note**: This approach is more complex and requires creating a custom ViewModelStoreOwner.

## Recommended Approach

For your case with `HomeNavigation`:

1. **For integration tests**: Use Solution 1 (real ViewModels)
2. **For isolated UI tests**: Use Solution 2 (mock repositories, real ViewModels)
3. **Avoid**: Solution 3 unless you have specific reasons to bypass Hilt

## Current Implementation

The `HorizonHomeUiTest.kt` is now set up with Solution 1 (integration testing with real ViewModels).

To switch to Solution 2:
1. Create the `TestRepositoryModule` (see above)
2. Inject repositories in your test
3. Configure mock behavior with `coEvery`

## Key Files

- **HiltTestActivity**: `/src/androidTest/java/com/instructure/horizon/ui/HiltTestActivity.kt`
- **Updated Test**: `/src/androidTest/java/com/instructure/horizon/ui/features/home/HorizonHomeUiTest.kt`
