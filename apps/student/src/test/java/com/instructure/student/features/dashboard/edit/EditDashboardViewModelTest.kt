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

package com.instructure.student.features.dashboard.edit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.GroupManager
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.Favorite
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.student.features.dashboard.edit.itemViewModel.*
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
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        val groups = listOf(createGroup(id = 1L, name = "Group1"))

        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(groups)
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager)
        viewModel.state.observe(lifecycleOwner, Observer {})

        //Then
        assertTrue(viewModel.state.value is ViewState.Error)
    }

    @Test
    fun `Show error state if fetching groups fails`() {
        //Given
        val courses = listOf(Course(id = 1L, name = "Course"))
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager)
        viewModel.state.observe(lifecycleOwner, Observer {})

        //Then
        assertTrue(viewModel.state.value is ViewState.Error)
    }

    @Test
    fun `Correct headers for courses`() {
        //Given
        val courses = listOf(createCourse(1L, "Current Course"))

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager)
        viewModel.state.observe(lifecycleOwner, Observer {})
        viewModel.data.observe(lifecycleOwner, Observer {})

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
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        val groups = listOf(Group(id = 1L, name = "Group1"))

        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(groups)
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager)
        viewModel.state.observe(lifecycleOwner, Observer {})
        viewModel.data.observe(lifecycleOwner, Observer {})

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

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { courseManager.addCourseToFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager)
        viewModel.state.observe(lifecycleOwner, Observer {})
        viewModel.data.observe(lifecycleOwner, Observer {})

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

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { courseManager.removeCourseFromFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager)
        viewModel.state.observe(lifecycleOwner, Observer {})
        viewModel.data.observe(lifecycleOwner, Observer {})

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
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        val groups = listOf(createGroup(1L, "Group"))
        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(groups)
        }

        every { groupManager.addGroupToFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager)
        viewModel.state.observe(lifecycleOwner, Observer {})
        viewModel.data.observe(lifecycleOwner, Observer {})

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
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        val groups = listOf(createGroup(1L, "Group", true))
        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(groups)
        }

        every { groupManager.removeGroupFromFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager)
        viewModel.state.observe(lifecycleOwner, Observer {})
        viewModel.data.observe(lifecycleOwner, Observer {})

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

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { courseManager.addCourseToFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager)
        viewModel.state.observe(lifecycleOwner, Observer {})
        viewModel.data.observe(lifecycleOwner, Observer {})

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

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        every { courseManager.removeCourseFromFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager)
        viewModel.state.observe(lifecycleOwner, Observer {})
        viewModel.data.observe(lifecycleOwner, Observer {})
        viewModel.events.observe(lifecycleOwner, Observer {})

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
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        val groups = listOf(
                createGroup(1L, "Group"),
                createGroup(2L, "Group 2")
        )
        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(groups)
        }

        every { groupManager.addGroupToFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager)
        viewModel.state.observe(lifecycleOwner, Observer {})
        viewModel.data.observe(lifecycleOwner, Observer {})

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
        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(emptyList())
        }

        val groups = listOf(
                createGroup(1L, "Group", true),
                createGroup(2L, "Group 2")
        )
        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(groups)
        }

        every { groupManager.removeGroupFromFavoritesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Favorite(1L))
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager)
        viewModel.state.observe(lifecycleOwner, Observer {})
        viewModel.data.observe(lifecycleOwner, Observer {})

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

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(groups)
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager)
        viewModel.state.observe(lifecycleOwner, Observer {})
        viewModel.data.observe(lifecycleOwner, Observer {})

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

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(groups)
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager)
        viewModel.state.observe(lifecycleOwner, Observer {})
        viewModel.data.observe(lifecycleOwner, Observer {})

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

        every { courseManager.getCoursesAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(courses)
        }

        every { groupManager.getAllGroupsAsync(any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(groups)
        }

        //When
        viewModel = EditDashboardViewModel(courseManager, groupManager)
        viewModel.state.observe(lifecycleOwner, Observer {})
        viewModel.data.observe(lifecycleOwner, Observer {})

        viewModel.queryItems("No match")
        val data = viewModel.data.value?.items ?: emptyList()

        //Then
        assertTrue(viewModel.state.value is ViewState.Empty)
        assertEquals(0, data.size)
    }

    private fun createCourse(id: Long, name: String, isFavorite: Boolean = false): Course {
        val startDate = OffsetDateTime.now()
        val endDate = startDate.withDayOfMonth(startDate.dayOfMonth + 1)

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
                enrollments = enrollments)
    }

    private fun createGroup(id: Long, name: String, isFavorite: Boolean = false, courseId: Long = 0): Group {
        return Group(id = id, name = name, courseId = courseId, isFavorite = isFavorite)
    }
}