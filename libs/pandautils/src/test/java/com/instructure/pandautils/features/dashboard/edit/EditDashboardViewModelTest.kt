/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

package com.instructure.pandautils.features.dashboard.edit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Favorite
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.edit.itemviewmodels.*
import com.instructure.pandautils.mvvm.ViewState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId

@ExperimentalCoroutinesApi
class EditDashboardViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val courseManager: CourseManager = mockk(relaxed = true)
    private val groupManager: GroupManager = mockk(relaxed = true)
    private val repository: EditDashboardRepository = mockk(relaxed = true)

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private lateinit var viewModel: EditDashboardViewModel

    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `Show error state if fetching courses fails`() {
        //Given
        val groups = listOf(createGroup(id = 1L, name = "Group1"))

        coEvery { repository.getCurses() } throws IllegalStateException()

        coEvery { repository.getGroups() } returns groups

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}

        //Then
        assertTrue(viewModel.state.value is ViewState.Error)
    }

    @Test
    fun `Show error state if fetching groups fails`() {
        //Given
        val courses = listOf(Course(id = 1L, name = "Course"))

        coEvery { repository.getCurses() } returns listOf(courses, emptyList(), emptyList())

        coEvery { repository.getGroups() } throws IllegalStateException()

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}

        //Then
        assertTrue(viewModel.state.value is ViewState.Error)
    }

    @Test
    fun `Correct headers for courses`() {
        //Given
        val courses = listOf(createCourse(1L, "Current Course"))

        coEvery { repository.getCurses() } returns listOf(courses, emptyList(), emptyList())

        coEvery { repository.getGroups() } returns emptyList()

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)

        val data = viewModel.data.value?.items ?: emptyList()
        assertEquals(4, data.size)
        assertTrue(data[0] is EditDashboardHeaderViewModel)
        assertTrue(data[1] is EditDashboardDescriptionItemViewModel)
        assertTrue(data[2] is EditDashboardEnrollmentItemViewModel)
        assertTrue(data[3] is EditDashboardCourseItemViewModel)
    }

    @Test
    fun `Correct headers for groups`() {
        //Given
        coEvery { repository.getCurses() } returns listOf(emptyList(), emptyList(), emptyList())

        val groups = listOf(Group(id = 1L, name = "Group1"))

        coEvery { repository.getGroups() } returns groups

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)

        val data = viewModel.data.value?.items ?: emptyList()
        assertEquals(3, data.size)
        assertTrue(data[0] is EditDashboardHeaderViewModel)
        assertTrue(data[1] is EditDashboardDescriptionItemViewModel)
        assertTrue(data[2] is EditDashboardGroupItemViewModel)
    }

    @Test
    fun `Add course to favorites`() {
        //Given
        val courses = listOf(createCourse(1L, "Current course"))

        coEvery { repository.getCurses() } returns listOf(courses, emptyList(), emptyList())

        every { repository.isFavoriteable(any()) } returns true

        coEvery { repository.getGroups() } returns emptyList()

        every { courseManager.addCourseToFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}

        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(4, data.size)
        assertTrue(data[3] is EditDashboardCourseItemViewModel)

        val itemViewModel = (data[3] as EditDashboardCourseItemViewModel)
        itemViewModel.onFavoriteClick()
        assertTrue(itemViewModel.isFavorite)
    }

    @Test
    fun `Remove course from favorites`() {
        //Given
        val courses = listOf(createCourse(1L, "Current course", true))

        coEvery { repository.getCurses() } returns listOf(courses, emptyList(), emptyList())

        every { repository.isFavoriteable(any()) } returns true

        coEvery { repository.getGroups() } returns emptyList()

        every { courseManager.removeCourseFromFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}

        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(4, data.size)
        assertTrue(data[3] is EditDashboardCourseItemViewModel)

        val itemViewModel = (data[3] as EditDashboardCourseItemViewModel)
        itemViewModel.onFavoriteClick()
        assertFalse(itemViewModel.isFavorite)
    }

    @Test
    fun `Add group to favorites`() {
        //Given
        coEvery { repository.getCurses() } returns listOf(emptyList(), emptyList(), emptyList())

        val groups = listOf(createGroup(1L, "Group"))
        coEvery { repository.getGroups() } returns groups

        every { groupManager.addGroupToFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}

        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(3, data.size)
        assertTrue(data[2] is EditDashboardGroupItemViewModel)

        val itemViewModel = (data[2] as EditDashboardGroupItemViewModel)
        itemViewModel.onFavoriteClick()
        assertTrue(itemViewModel.isFavorite)
    }

    @Test
    fun `Remove group from favorites`() {
        //Given
        coEvery { repository.getCurses() } returns listOf(emptyList(), emptyList(), emptyList())

        val groups = listOf(createGroup(1L, "Group", true))
        coEvery { repository.getGroups() } returns groups

        every { groupManager.removeGroupFromFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}

        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(3, data.size)
        assertTrue(data[2] is EditDashboardGroupItemViewModel)

        val itemViewModel = (data[2] as EditDashboardGroupItemViewModel)
        itemViewModel.onFavoriteClick()
        assertFalse(itemViewModel.isFavorite)
    }

    @Test
    fun `Add all courses to favorites`() {
        val courses = listOf(
                createCourse(1L, "Current course"),
                createCourse(2L, "Current course 2")
        )

        coEvery { repository.getCurses() } returns listOf(courses, emptyList(), emptyList())

        every { repository.isFavoriteable(any()) } returns true

        coEvery { repository.getGroups() } returns emptyList()

        every { courseManager.addCourseToFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}

        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)

        val headerViewModel = (data[0] as EditDashboardHeaderViewModel)
        assertFalse(headerViewModel.hasItemSelected)
        headerViewModel.onActionClick()
        assertTrue(headerViewModel.hasItemSelected)
        data.forEach {
            if (it is EditDashboardCourseItemViewModel) {
                assertTrue(it.isFavorite)
            }
        }
    }

    @Test
    fun `Remove all courses from favorites`() {
        val courses = listOf(
                createCourse(1L, "Current course", true),
                createCourse(2L, "Current course 2", false)
        )

        coEvery { repository.getCurses() } returns listOf(courses, emptyList(), emptyList())

        coEvery { repository.getGroups() } returns emptyList()

        every { courseManager.removeCourseFromFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.events.observe(lifecycleOwner) {}

        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)

        val headerViewModel = (data[0] as EditDashboardHeaderViewModel)
        assertTrue(headerViewModel.hasItemSelected)
        headerViewModel.onActionClick()
        assertFalse(headerViewModel.hasItemSelected)
        data.forEach {
            if (it is EditDashboardCourseItemViewModel) {
                assertFalse(it.isFavorite)
            }
        }
    }

    @Test
    fun `Add all groups to favorites`() {
        //Given
        coEvery { repository.getCurses() } returns listOf(emptyList(), emptyList(), emptyList())

        val groups = listOf(
                createGroup(1L, "Group"),
                createGroup(2L, "Group 2")
        )
        coEvery { repository.getGroups() } returns groups

        every { groupManager.addGroupToFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}

        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)

        val headerViewModel = (data[0] as EditDashboardHeaderViewModel)
        assertFalse(headerViewModel.hasItemSelected)
        headerViewModel.onActionClick()
        assertTrue(headerViewModel.hasItemSelected)
        data.forEach {
            if (it is EditDashboardGroupItemViewModel) {
                assertTrue(it.isFavorite)
            }
        }
    }

    @Test
    fun `Remove all groups from favorites`() {
        //Given
        coEvery { repository.getCurses() } returns listOf(emptyList(), emptyList(), emptyList())

        val groups = listOf(
                createGroup(1L, "Group", true),
                createGroup(2L, "Group 2")
        )
        coEvery { repository.getGroups() } returns groups

        every { groupManager.removeGroupFromFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}

        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)

        val headerViewModel = (data[0] as EditDashboardHeaderViewModel)
        assertTrue(headerViewModel.hasItemSelected)
        headerViewModel.onActionClick()
        assertFalse(headerViewModel.hasItemSelected)
        data.forEach {
            if (it is EditDashboardGroupItemViewModel) {
                assertFalse(it.isFavorite)
            }
        }
    }

    @Test
    fun `Query course`() {
        val courses = listOf(
                createCourse(1L, "Course", true),
        )

        val groups = listOf(
                createGroup(2L, "Group", true),
        )

        coEvery { repository.getCurses() } returns listOf(courses, emptyList(), emptyList())

        coEvery { repository.getGroups() } returns groups

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}

        viewModel.queryItems("course")
        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(4, data.size)
        assertTrue(data[3] is EditDashboardCourseItemViewModel)
        val courseItemViewModel = data[3] as EditDashboardCourseItemViewModel
        assertEquals("Course", courseItemViewModel.name)
    }

    @Test
    fun `Query group`() {
        val courses = listOf(
                createCourse(1L, "Course", true),
        )

        val groups = listOf(
                createGroup(2L, "Group", true),
        )

        coEvery { repository.getCurses() } returns listOf(courses, emptyList(), emptyList())

        coEvery { repository.getGroups() } returns groups

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}

        viewModel.queryItems("group")
        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(3, data.size)
        assertTrue(data[2] is EditDashboardGroupItemViewModel)
        val groupItemViewModel = data[2] as EditDashboardGroupItemViewModel
        assertEquals("Group", groupItemViewModel.name)
    }

    @Test
    fun `No match for query`() {
        val courses = listOf(
                createCourse(1L, "Course", true),
        )

        val groups = listOf(
                createGroup(2L, "Group", true),
        )

        coEvery { repository.getCurses() } returns listOf(courses, emptyList(), emptyList())

        coEvery { repository.getGroups() } returns groups

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}

        viewModel.queryItems("No match")
        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Empty)
        assertEquals(0, data.size)
    }

    @Test
    fun `Correct headers for all items`() {
        val pastCourse = createCourse(1L, "Past course", false, OffsetDateTime.now().withYear(OffsetDateTime.now().year - 1))
        val futureCourse = createCourse(2L, "Future course", false, OffsetDateTime.now().withYear(OffsetDateTime.now().year + 1))
        val currentCourse = createCourse(3L, "Current course", false)

        coEvery { repository.getCurses() } returns listOf(listOf(currentCourse), listOf(pastCourse), listOf(futureCourse))

        val groups = listOf(
                createGroup(4L, "Group", true),
        )

        coEvery { repository.getGroups() } returns groups

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}

        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(11, data.size)
        assertTrue(data[0] is EditDashboardHeaderViewModel)
        assertTrue(data[1] is EditDashboardDescriptionItemViewModel)

        assertTrue(data[2] is EditDashboardEnrollmentItemViewModel)
        val currentHeader = data[2] as EditDashboardEnrollmentItemViewModel
        assertEquals(R.string.current_enrollments, currentHeader.title)

        assertTrue(data[3] is EditDashboardCourseItemViewModel)
        val currentCourseItemViewModel = data[3] as EditDashboardCourseItemViewModel
        assertEquals("Current course", currentCourseItemViewModel.name)

        assertTrue(data[4] is EditDashboardEnrollmentItemViewModel)
        val pastHeader = data[4] as EditDashboardEnrollmentItemViewModel
        assertEquals(R.string.past_enrollments, pastHeader.title)

        assertTrue(data[5] is EditDashboardCourseItemViewModel)
        val pastCourseItemViewModel = data[5] as EditDashboardCourseItemViewModel
        assertEquals("Past course", pastCourseItemViewModel.name)

        assertTrue(data[6] is EditDashboardEnrollmentItemViewModel)
        val futureHeader = data[6] as EditDashboardEnrollmentItemViewModel
        assertEquals(R.string.future_enrollments, futureHeader.title)

        assertTrue(data[7] is EditDashboardCourseItemViewModel)
        val futureCourseItemViewModel = data[7] as EditDashboardCourseItemViewModel
        assertEquals("Future course", futureCourseItemViewModel.name)

        assertTrue(data[8] is EditDashboardHeaderViewModel)
        assertTrue(data[9] is EditDashboardDescriptionItemViewModel)

        assertTrue(data[10] is EditDashboardGroupItemViewModel)
        val groupItemViewModel = data[10] as EditDashboardGroupItemViewModel
        assertEquals("Group", groupItemViewModel.name)
    }

    @Test
    fun `Past courses cannot be favorited`() {
        //Given
        val courses = listOf(createCourse(1L, "Past course"))

        coEvery { repository.getCurses() } returns listOf(emptyList(), courses, emptyList())

        every { repository.isFavoriteable(any()) } returns false

        coEvery { repository.getGroups() } returns emptyList()

        every { courseManager.addCourseToFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.events.observe(lifecycleOwner) {}

        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(4, data.size)
        assertTrue(data[3] is EditDashboardCourseItemViewModel)

        val itemViewModel = (data[3] as EditDashboardCourseItemViewModel)
        itemViewModel.onFavoriteClick()
        assert(!itemViewModel.isFavorite)

        val failedEvent = viewModel.events.value?.getContentIfNotHandled()
        assert(failedEvent is EditDashboardItemAction.ShowSnackBar)
        assertEquals(R.string.inactive_courses_cant_be_added_to_dashboard, (failedEvent as EditDashboardItemAction.ShowSnackBar).res)
    }

    @Test
    fun `Future courses can only be favorited if they are published`() {
        val courses = listOf(
                createCourse(1L, "Published future course"),
                createCourse(2L, "Unpublished future course", workflowState = Course.WorkflowState.UNPUBLISHED)
        )

        coEvery { repository.getCurses() } returns listOf(emptyList(), emptyList(), courses)

        every { repository.isFavoriteable(courses.first()) } returns true

        every { repository.isFavoriteable(courses.last()) } returns false

        coEvery { repository.getGroups() } returns emptyList()

        every { courseManager.addCourseToFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.events.observe(lifecycleOwner) {}

        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(5, data.size)
        assertTrue(data[3] is EditDashboardCourseItemViewModel)

        val publishedItem = (data[3] as EditDashboardCourseItemViewModel)
        publishedItem.onFavoriteClick()
        assert(publishedItem.isFavorite)

        val successfulEvent = viewModel.events.value?.getContentIfNotHandled()
        assert(successfulEvent is EditDashboardItemAction.ShowSnackBar)
        assertEquals(R.string.added_to_dashboard, (successfulEvent as EditDashboardItemAction.ShowSnackBar).res)

        val unpublishedItem = (data[4] as EditDashboardCourseItemViewModel)
        unpublishedItem.onFavoriteClick()
        assert(!unpublishedItem.isFavorite)

        val failedEvent = viewModel.events.value?.getContentIfNotHandled()
        assert(failedEvent is EditDashboardItemAction.ShowSnackBar)
        assertEquals(R.string.inactive_courses_cant_be_added_to_dashboard, (failedEvent as EditDashboardItemAction.ShowSnackBar).res)
    }

    @Test
    fun `Open course`() {
        val courses = listOf(createCourse(1L, "Current course"))

        coEvery { repository.getCurses() } returns listOf(courses, emptyList(), emptyList())

        every { repository.isOpenable(any()) } returns true

        coEvery { repository.getGroups() } returns emptyList()

        every { courseManager.addCourseToFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.events.observe(lifecycleOwner) {}

        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(4, data.size)
        assertTrue(data[3] is EditDashboardCourseItemViewModel)

        val itemViewModel = (data[3] as EditDashboardCourseItemViewModel)
        itemViewModel.onClick()

        val openEvent = viewModel.events.value?.getContentIfNotHandled()
        assert(openEvent is EditDashboardItemAction.OpenItem)
        assertEquals(1L, (openEvent as EditDashboardItemAction.OpenItem).canvasContext?.id)
    }

    @Test
    fun `Open group`() {
        coEvery { repository.getCurses() } returns listOf(emptyList(), emptyList(), emptyList())

        val groups = listOf(createGroup(1L, "Group"))
        coEvery { repository.getGroups() } returns groups

        every { groupManager.addGroupToFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.events.observe(lifecycleOwner) {}

        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(3, data.size)
        assertTrue(data[2] is EditDashboardGroupItemViewModel)

        val itemViewModel = (data[2] as EditDashboardGroupItemViewModel)
        itemViewModel.onClick()

        val openEvent = viewModel.events.value?.getContentIfNotHandled()
        assert(openEvent is EditDashboardItemAction.OpenItem)
        assertEquals(1L, (openEvent as EditDashboardItemAction.OpenItem).canvasContext?.id)
    }

    @Test
    fun `Not isOpenable courses cannot be opened`() {
        val courses = listOf(createCourse(1L, "Unpublished course", workflowState = Course.WorkflowState.UNPUBLISHED))

        coEvery { repository.getCurses() } returns listOf(emptyList(), emptyList(), courses)

        every { repository.isOpenable(any()) } returns false

        coEvery { repository.getGroups() } returns emptyList()

        every { courseManager.addCourseToFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager, repository)
        viewModel.state.observe(lifecycleOwner) {}
        viewModel.data.observe(lifecycleOwner) {}
        viewModel.events.observe(lifecycleOwner) {}

        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Success)
        assertEquals(4, data.size)
        assertTrue(data[3] is EditDashboardCourseItemViewModel)

        val itemViewModel = (data[3] as EditDashboardCourseItemViewModel)
        itemViewModel.onClick()

        val openEvent = viewModel.events.value?.getContentIfNotHandled()
        assert(openEvent is EditDashboardItemAction.ShowSnackBar)
        assertEquals(R.string.unauthorized, (openEvent as EditDashboardItemAction.ShowSnackBar).res)
    }

    private fun createCourse(
            id: Long,
            name: String,
            isFavorite: Boolean = false,
            startOffsetDate: OffsetDateTime? = null,
            endOffsetDate: OffsetDateTime? = null,
            workflowState: Course.WorkflowState = Course.WorkflowState.AVAILABLE,
    ): Course {

        val startDate = startOffsetDate ?: OffsetDateTime.now()
        val endDate = endOffsetDate ?: startDate.plusDays(1)

        val enrollment = Enrollment(
                type = Enrollment.EnrollmentType.Student,
                enrollmentState = EnrollmentAPI.STATE_ACTIVE,
                currentGradingPeriodId = 27,
                multipleGradingPeriodsEnabled = true)
        val enrollments = arrayListOf(enrollment)
        return Course(id = id,
                name = name,
                endAt = DateTimeUtils.toDate(endDate.atZoneSimilarLocal(ZoneId.systemDefault()).toInstant()).toApiString(),
                startAt = DateTimeUtils.toDate(startDate.atZoneSimilarLocal(ZoneId.systemDefault()).toInstant()).toApiString(),
                isFavorite = isFavorite,
                enrollments = enrollments,
                workflowState = workflowState)
    }

    private fun createGroup(id: Long, name: String, isFavorite: Boolean = false, courseId: Long = 0): Group {
        return Group(id = id, name = name, courseId = courseId, isFavorite = isFavorite)
    }
}