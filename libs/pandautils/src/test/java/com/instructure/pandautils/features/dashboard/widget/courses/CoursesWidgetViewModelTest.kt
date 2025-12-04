/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.courses

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.domain.models.courses.GradeDisplay
import com.instructure.pandautils.domain.usecase.courses.LoadFavoriteCoursesParams
import com.instructure.pandautils.domain.usecase.courses.LoadFavoriteCoursesUseCase
import com.instructure.pandautils.domain.usecase.courses.LoadGroupsParams
import com.instructure.pandautils.domain.usecase.courses.LoadGroupsUseCase
import com.instructure.pandautils.room.offline.daos.CourseSyncSettingsDao
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.ThemedColor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CoursesWidgetViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val loadFavoriteCoursesUseCase: LoadFavoriteCoursesUseCase = mockk()
    private val loadGroupsUseCase: LoadGroupsUseCase = mockk()
    private val sectionExpandedStateDataStore: SectionExpandedStateDataStore = mockk(relaxed = true)
    private val coursesWidgetBehavior: CoursesWidgetBehavior = mockk(relaxed = true)
    private val courseSyncSettingsDao: CourseSyncSettingsDao = mockk()
    private val networkStateProvider: NetworkStateProvider = mockk()
    private val featureFlagProvider: FeatureFlagProvider = mockk()
    private val courseRepository: CourseRepository = mockk()

    private lateinit var viewModel: CoursesWidgetViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(ColorKeeper)
        every { ColorKeeper.getOrGenerateColor(any<Course>()) } returns ThemedColor(0xFF0000, 0xFF0000)
        every { ColorKeeper.getOrGenerateColor(any<Group>()) } returns ThemedColor(0x00FF00, 0x00FF00)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun setupDefaultMocks() {
        coEvery { loadFavoriteCoursesUseCase(any()) } returns emptyList()
        coEvery { loadGroupsUseCase(any()) } returns emptyList()
        every { sectionExpandedStateDataStore.observeCoursesExpanded() } returns flowOf(true)
        every { sectionExpandedStateDataStore.observeGroupsExpanded() } returns flowOf(true)
        every { coursesWidgetBehavior.observeGradeVisibility() } returns flowOf(false)
        every { coursesWidgetBehavior.observeColorOverlay() } returns flowOf(false)
        coEvery { featureFlagProvider.offlineEnabled() } returns false
        every { networkStateProvider.isOnline() } returns true
    }

    @Test
    fun `init loads courses and groups successfully`() {
        setupDefaultMocks()
        val courses = listOf(
            Course(id = 1, name = "Course 1", isFavorite = true),
            Course(id = 2, name = "Course 2", isFavorite = true)
        )
        val groups = listOf(
            Group(id = 1, name = "Group 1", isFavorite = true)
        )
        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses
        coEvery { loadGroupsUseCase(any()) } returns groups

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isError)
        assertEquals(2, state.courses.size)
        assertEquals(1, state.groups.size)
    }

    @Test
    fun `init sets error state when loading fails`() {
        setupDefaultMocks()
        coEvery { loadFavoriteCoursesUseCase(any()) } throws RuntimeException("Network error")

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isError)
    }

    @Test
    fun `refresh reloads data with forceRefresh`() {
        setupDefaultMocks()
        coEvery { loadFavoriteCoursesUseCase(any()) } returns emptyList()
        coEvery { loadGroupsUseCase(any()) } returns emptyList()

        viewModel = createViewModel()
        viewModel.refresh()

        coVerify { loadFavoriteCoursesUseCase(LoadFavoriteCoursesParams(forceRefresh = true)) }
        coVerify { loadGroupsUseCase(LoadGroupsParams(forceRefresh = true)) }
    }

    @Test
    fun `toggleCoursesExpanded updates expanded state`() {
        setupDefaultMocks()

        viewModel = createViewModel()
        viewModel.toggleCoursesExpanded()

        coVerify { sectionExpandedStateDataStore.setCoursesExpanded(false) }
    }

    @Test
    fun `toggleGroupsExpanded updates expanded state`() {
        setupDefaultMocks()

        viewModel = createViewModel()
        viewModel.toggleGroupsExpanded()

        coVerify { sectionExpandedStateDataStore.setGroupsExpanded(false) }
    }

    @Test
    fun `observeExpandedStates updates ui state`() {
        setupDefaultMocks()
        every { sectionExpandedStateDataStore.observeCoursesExpanded() } returns flowOf(false)
        every { sectionExpandedStateDataStore.observeGroupsExpanded() } returns flowOf(false)

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.isCoursesExpanded)
        assertFalse(state.isGroupsExpanded)
    }

    @Test
    fun `observeGradeVisibility updates showGrades in ui state`() {
        setupDefaultMocks()
        every { coursesWidgetBehavior.observeGradeVisibility() } returns flowOf(true)

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.showGrades)
    }

    @Test
    fun `observeColorOverlay updates showColorOverlay in ui state`() {
        setupDefaultMocks()
        every { coursesWidgetBehavior.observeColorOverlay() } returns flowOf(true)

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.showColorOverlay)
    }

    @Test
    fun `courses are mapped to CourseCardItems correctly`() {
        setupDefaultMocks()
        val enrollment = Enrollment(
            computedCurrentGrade = "A",
            computedCurrentScore = 95.0
        )
        val courses = listOf(
            Course(
                id = 1,
                name = "Test Course",
                courseCode = "TC101",
                imageUrl = "https://example.com/image.jpg",
                isFavorite = true,
                enrollments = mutableListOf(enrollment)
            )
        )
        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(1, state.courses.size)
        val courseCard = state.courses[0]
        assertEquals(1L, courseCard.id)
        assertEquals("Test Course", courseCard.name)
        assertEquals("TC101", courseCard.courseCode)
        assertEquals("https://example.com/image.jpg", courseCard.imageUrl)
        assertTrue(courseCard.isClickable)
    }

    @Test
    fun `grade is mapped as Letter when computedCurrentGrade is available`() {
        setupDefaultMocks()
        val enrollment = Enrollment(computedCurrentGrade = "B+", computedCurrentScore = 88.0)
        val course = Course(id = 1, name = "Course", isFavorite = true, enrollments = mutableListOf(enrollment))
        coEvery { loadFavoriteCoursesUseCase(any()) } returns listOf(course)

        viewModel = createViewModel()

        val grade = viewModel.uiState.value.courses[0].grade
        assertTrue(grade is GradeDisplay.Letter)
        assertEquals("B+", (grade as GradeDisplay.Letter).grade)
    }

    @Test
    fun `grade is mapped as Percentage when only score is available`() {
        setupDefaultMocks()
        val enrollment = Enrollment(computedCurrentScore = 75.0)
        val course = Course(id = 1, name = "Course", isFavorite = true, enrollments = mutableListOf(enrollment))
        coEvery { loadFavoriteCoursesUseCase(any()) } returns listOf(course)

        viewModel = createViewModel()

        val grade = viewModel.uiState.value.courses[0].grade
        assertTrue(grade is GradeDisplay.Percentage)
        assertEquals("75%", (grade as GradeDisplay.Percentage).value)
    }

    @Test
    fun `grade is mapped as Locked when hideFinalGrades is true and no grade available`() {
        setupDefaultMocks()
        val enrollment = Enrollment()
        val course = Course(
            id = 1,
            name = "Course",
            isFavorite = true,
            hideFinalGrades = true,
            enrollments = mutableListOf(enrollment)
        )
        coEvery { loadFavoriteCoursesUseCase(any()) } returns listOf(course)

        viewModel = createViewModel()

        val grade = viewModel.uiState.value.courses[0].grade
        assertTrue(grade is GradeDisplay.Locked)
    }

    @Test
    fun `grade is mapped as NotAvailable when no enrollment`() {
        setupDefaultMocks()
        val course = Course(id = 1, name = "Course", isFavorite = true)
        coEvery { loadFavoriteCoursesUseCase(any()) } returns listOf(course)

        viewModel = createViewModel()

        val grade = viewModel.uiState.value.courses[0].grade
        assertTrue(grade is GradeDisplay.NotAvailable)
    }

    @Test
    fun `courses are marked as not clickable when offline and not synced`() {
        setupDefaultMocks()
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { courseSyncSettingsDao.findAll() } returns emptyList()
        every { networkStateProvider.isOnline() } returns false

        val courses = listOf(Course(id = 1, name = "Course", isFavorite = true))
        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertFalse(state.courses[0].isClickable)
    }

    @Test
    fun `courses are marked as clickable when offline but synced`() {
        setupDefaultMocks()
        coEvery { featureFlagProvider.offlineEnabled() } returns true
        coEvery { courseSyncSettingsDao.findAll() } returns listOf(
            CourseSyncSettingsEntity(courseId = 1, courseName = "Course", fullContentSync = true)
        )
        every { networkStateProvider.isOnline() } returns false

        val courses = listOf(Course(id = 1, name = "Course", isFavorite = true))
        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.courses[0].isClickable)
        assertTrue(state.courses[0].isSynced)
    }

    @Test
    fun `courses are always clickable when online`() {
        setupDefaultMocks()
        every { networkStateProvider.isOnline() } returns true

        val courses = listOf(Course(id = 1, name = "Course", isFavorite = true))
        coEvery { loadFavoriteCoursesUseCase(any()) } returns courses

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state.courses[0].isClickable)
    }

    @Test
    fun `groups are mapped to GroupCardItems correctly`() {
        setupDefaultMocks()
        val parentCourse = Course(id = 100, name = "Parent Course")
        val groups = listOf(
            Group(id = 1, name = "Study Group", courseId = 100, membersCount = 5, isFavorite = true)
        )
        coEvery { loadGroupsUseCase(any()) } returns groups
        coEvery { courseRepository.getCourse(100, false) } returns DataResult.Success(parentCourse)

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(1, state.groups.size)
        val groupCard = state.groups[0]
        assertEquals(1L, groupCard.id)
        assertEquals("Study Group", groupCard.name)
        assertEquals("Parent Course", groupCard.parentCourseName)
        assertEquals(100L, groupCard.parentCourseId)
        assertEquals(5, groupCard.memberCount)
    }

    @Test
    fun `groups without parent course have null parentCourseName`() {
        setupDefaultMocks()
        val groups = listOf(
            Group(id = 1, name = "Standalone Group", courseId = 0, isFavorite = true)
        )
        coEvery { loadGroupsUseCase(any()) } returns groups

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(1, state.groups.size)
        assertEquals(null, state.groups[0].parentCourseName)
    }

    @Test
    fun `onCourseClick delegates to behavior`() {
        setupDefaultMocks()
        val course = Course(id = 1, name = "Course", isFavorite = true)
        coEvery { loadFavoriteCoursesUseCase(any()) } returns listOf(course)

        viewModel = createViewModel()
        val activity: FragmentActivity = mockk()
        viewModel.uiState.value.onCourseClick(activity, 1)

        verify { coursesWidgetBehavior.onCourseClick(activity, course) }
    }

    @Test
    fun `onGroupClick delegates to behavior`() {
        setupDefaultMocks()
        val group = Group(id = 1, name = "Group", isFavorite = true)
        coEvery { loadGroupsUseCase(any()) } returns listOf(group)

        viewModel = createViewModel()
        val activity: FragmentActivity = mockk()
        viewModel.uiState.value.onGroupClick(activity, 1)

        verify { coursesWidgetBehavior.onGroupClick(activity, group) }
    }

    @Test
    fun `onManageOfflineContent delegates to behavior`() {
        setupDefaultMocks()
        val course = Course(id = 1, name = "Course", isFavorite = true)
        coEvery { loadFavoriteCoursesUseCase(any()) } returns listOf(course)

        viewModel = createViewModel()
        val activity: FragmentActivity = mockk()
        viewModel.uiState.value.onManageOfflineContent(activity, 1)

        verify { coursesWidgetBehavior.onManageOfflineContent(activity, course) }
    }

    @Test
    fun `onCustomizeCourse delegates to behavior`() {
        setupDefaultMocks()
        val course = Course(id = 1, name = "Course", isFavorite = true)
        coEvery { loadFavoriteCoursesUseCase(any()) } returns listOf(course)

        viewModel = createViewModel()
        val activity: FragmentActivity = mockk()
        viewModel.uiState.value.onCustomizeCourse(activity, 1)

        verify { coursesWidgetBehavior.onCustomizeCourse(activity, course) }
    }

    @Test
    fun `onAllCourses delegates to behavior`() {
        setupDefaultMocks()

        viewModel = createViewModel()
        val activity: FragmentActivity = mockk()
        viewModel.uiState.value.onAllCourses(activity)

        verify { coursesWidgetBehavior.onAllCoursesClicked(activity) }
    }

    private fun createViewModel(): CoursesWidgetViewModel {
        return CoursesWidgetViewModel(
            loadFavoriteCoursesUseCase = loadFavoriteCoursesUseCase,
            loadGroupsUseCase = loadGroupsUseCase,
            sectionExpandedStateDataStore = sectionExpandedStateDataStore,
            coursesWidgetBehavior = coursesWidgetBehavior,
            courseSyncSettingsDao = courseSyncSettingsDao,
            networkStateProvider = networkStateProvider,
            featureFlagProvider = featureFlagProvider,
            courseRepository = courseRepository
        )
    }
}