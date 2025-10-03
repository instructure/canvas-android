# Testing ViewModels with Navigation Arguments

## Problem
The `ModuleItemSequenceViewModel` uses Jetpack Navigation's type-safe navigation with `savedStateHandle.toRoute<MainNavigationRoute.ModuleItemSequence>()` to extract navigation arguments. Testing this requires properly mocking the SavedStateHandle and the extension function.

## Solution

### 1. Mock the SavedStateHandle
Instead of trying to create a real SavedStateHandle with serialized data (which requires Android framework classes), we mock it:

```kotlin
val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
```

### 2. Create the Route Object
Create an actual instance of the navigation route with test data:

```kotlin
val route = MainNavigationRoute.ModuleItemSequence(
    courseId = courseId,
    moduleItemId = moduleItemId,
    moduleItemAssetType = moduleItemAssetType,
    moduleItemAssetId = moduleItemAssetId
)
```

### 3. Mock the toRoute() Extension Function
Mock the `toRoute()` extension function to return our route object:

```kotlin
every { savedStateHandle.toRoute<MainNavigationRoute.ModuleItemSequence>() } returns route
```

### 4. Mock Static Navigation Extensions
Add this in your `@Before` setup:

```kotlin
mockkStatic("androidx.navigation.NavBackStackEntryKt")
```

## Complete Example

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class ModuleItemSequenceViewModelTest {
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("androidx.navigation.NavBackStackEntryKt") // Mock static extensions
        // ... other setup
    }

    @Test
    fun `Test data loads with moduleItemId`() = runTest {
        val savedStateHandle = createSavedStateHandle(courseId, moduleItemId)
        val viewModel = getViewModel(savedStateHandle)

        assertFalse(viewModel.uiState.value.loadingState.isLoading)
        coVerify { repository.getModulesWithItems(courseId, any()) }
    }

    private fun createSavedStateHandle(
        courseId: Long,
        moduleItemId: Long?,
        moduleItemAssetType: String? = null,
        moduleItemAssetId: String? = null
    ): SavedStateHandle {
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
        val route = MainNavigationRoute.ModuleItemSequence(
            courseId = courseId,
            moduleItemId = moduleItemId,
            moduleItemAssetType = moduleItemAssetType,
            moduleItemAssetId = moduleItemAssetId
        )
        every { savedStateHandle.toRoute<MainNavigationRoute.ModuleItemSequence>() } returns route
        return savedStateHandle
    }
}
```

## Why This Works

1. **Avoids Android Framework Dependencies**: No need for Bundle serialization or Android classes
2. **Mocks Extension Functions**: The `toRoute()` extension is properly mocked to return our test data
3. **Type-Safe**: We use the actual route data class, maintaining type safety
4. **Clean Test Code**: Helper functions keep tests readable and maintainable

## What Doesn't Work

❌ **Trying to serialize into SavedStateHandle**:
```kotlin
// This requires Android framework classes and won't work in unit tests
val bundle = bundleOf("courseId" to courseId)
val savedStateHandle = SavedStateHandle.createHandle(bundle, null)
```

❌ **Putting route directly in SavedStateHandle**:
```kotlin
// toRoute() deserializes, it doesn't just read the object
val savedStateHandle = SavedStateHandle()
savedStateHandle.set("route", route) // Won't work with toRoute()
```

## Key Imports

```kotlin
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
```
