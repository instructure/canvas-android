/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.parentapp.features.inbox.coursepicker

import android.content.Context
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.utils.ScreenState
import com.instructure.parentapp.R
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentInboxCoursePickerViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: ParentInboxCoursePickerRepository = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Selecting a course navigates to the compose screen with proper attributes`() = runTest {
        coEvery { repository.getCourses() } returns DataResult.Success(emptyList())
        coEvery { repository.getEnrollments() } returns DataResult.Success(emptyList())
        every { apiPrefs.fullDomain } returns "https://canvas.instructure.com"
        val studentContextItem = StudentContextItem(Course(1, "Course 1"), User(1, "User 1"))
        val viewModel = getViewModel()
        val courseId = 123L
        val expectedHiddenMessage = "Regarding: User 1, https://canvas.instructure.com/courses/$courseId"
        every { context.getString(R.string.regardingHiddenMessage, any(), any()) } returns expectedHiddenMessage

        val events = mutableListOf<ParentInboxCoursePickerBottomSheetAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.actionHandler(ParentInboxCoursePickerAction.StudentContextSelected(studentContextItem))
        val options = (events.last() as ParentInboxCoursePickerBottomSheetAction.NavigateToCompose).options
        assertEquals(expectedHiddenMessage, options.hiddenBodyMessage)
        assertEquals("Course 1", options.defaultValues.contextName)
        assertEquals("course_1", options.defaultValues.contextCode)
        assertEquals("Course 1", options.defaultValues.subject)
        assertEquals(true, options.disabledFields.isContextDisabled)
        assertEquals(listOf(EnrollmentType.TeacherEnrollment), options.autoSelectRecipientsFromRoles)
    }

    @Test
    fun `loadCoursePickerItems should update uiState with error when enrollments or courses fail`() {
        coEvery { repository.getCourses() } returns DataResult.Fail()
        coEvery { repository.getEnrollments() } returns DataResult.Fail()

        val viewModel = getViewModel()
        assertEquals(ScreenState.Error, viewModel.uiState.value.screenState)
    }

    @Test
    fun `loadCoursePickerItems should update uiState with data when enrollments and courses are successful`() {
        val courses = listOf(Course(1, "Course 1"), Course(2, "Course 2"))
        val users = listOf(User(1, "User 1"), User(2, "User 2"))
        val enrollments = listOf(
            Enrollment(1, courseId = 1, enrollmentState = "active", observedUser = users[0]),
            Enrollment(2, courseId = 2, enrollmentState = "active", observedUser = users[1])
        )
        coEvery { repository.getCourses() } returns DataResult.Success(courses)
        coEvery { repository.getEnrollments() } returns DataResult.Success(enrollments)

        val viewModel = getViewModel()
        assertEquals(ScreenState.Content, viewModel.uiState.value.screenState)
        assertEquals(2, viewModel.uiState.value.studentContextItems.size)
        assertEquals(courses[0], viewModel.uiState.value.studentContextItems[0].course)
        assertEquals(users[0], viewModel.uiState.value.studentContextItems[0].user)
        assertEquals(courses[1], viewModel.uiState.value.studentContextItems[1].course)
        assertEquals(users[1], viewModel.uiState.value.studentContextItems[1].user)
    }

    @Test
    fun `loadCoursePickerItems should filter out completed and inactive enrollments`() {
        val courses = listOf(
            Course(1, "Active Course"),
            Course(2, "Completed Course"),
            Course(3, "Inactive Course")
        )
        val users = listOf(User(1, "User 1"), User(2, "User 2"), User(3, "User 3"))
        val enrollments = listOf(
            Enrollment(1, courseId = 1, enrollmentState = "active", observedUser = users[0]),
            Enrollment(2, courseId = 2, enrollmentState = "completed", observedUser = users[1]),
            Enrollment(3, courseId = 3, enrollmentState = "inactive", observedUser = users[2])
        )
        coEvery { repository.getCourses() } returns DataResult.Success(courses)
        coEvery { repository.getEnrollments() } returns DataResult.Success(enrollments)

        val viewModel = getViewModel()
        assertEquals(ScreenState.Content, viewModel.uiState.value.screenState)
        assertEquals(1, viewModel.uiState.value.studentContextItems.size)
        assertEquals(courses[0], viewModel.uiState.value.studentContextItems[0].course)
        assertEquals(users[0], viewModel.uiState.value.studentContextItems[0].user)
    }

    @Test
    fun `loadCoursePickerItems should include active, invited, and creation_pending enrollments`() {
        val courses = listOf(
            Course(1, "Active Course"),
            Course(2, "Invited Course"),
            Course(3, "Creation Pending Course")
        )
        val users = listOf(User(1, "User 1"), User(2, "User 2"), User(3, "User 3"))
        val enrollments = listOf(
            Enrollment(1, courseId = 1, enrollmentState = "active", observedUser = users[0]),
            Enrollment(2, courseId = 2, enrollmentState = "invited", observedUser = users[1]),
            Enrollment(3, courseId = 3, enrollmentState = "creation_pending", observedUser = users[2])
        )
        coEvery { repository.getCourses() } returns DataResult.Success(courses)
        coEvery { repository.getEnrollments() } returns DataResult.Success(enrollments)

        val viewModel = getViewModel()
        assertEquals(ScreenState.Content, viewModel.uiState.value.screenState)
        assertEquals(3, viewModel.uiState.value.studentContextItems.size)
        assertEquals(courses[0], viewModel.uiState.value.studentContextItems[0].course)
        assertEquals(users[0], viewModel.uiState.value.studentContextItems[0].user)
        assertEquals(courses[1], viewModel.uiState.value.studentContextItems[1].course)
        assertEquals(users[1], viewModel.uiState.value.studentContextItems[1].user)
        assertEquals(courses[2], viewModel.uiState.value.studentContextItems[2].course)
        assertEquals(users[2], viewModel.uiState.value.studentContextItems[2].user)
    }

    @Test
    fun `loadCoursePickerItems should filter out soft concluded courses with past term dates`() {
        val pastTermEndDate = "2024-01-01T00:00:00Z"
        val futureTermEndDate = "2026-12-31T23:59:59Z"
        val courses = listOf(
            Course(1, "Current Course", term = com.instructure.canvasapi2.models.Term(endAt = futureTermEndDate)),
            Course(2, "Soft Concluded Course", term = com.instructure.canvasapi2.models.Term(endAt = pastTermEndDate))
        )
        val users = listOf(User(1, "User 1"), User(2, "User 2"))
        val enrollments = listOf(
            Enrollment(1, courseId = 1, enrollmentState = "active", observedUser = users[0]),
            Enrollment(2, courseId = 2, enrollmentState = "active", observedUser = users[1])
        )
        coEvery { repository.getCourses() } returns DataResult.Success(courses)
        coEvery { repository.getEnrollments() } returns DataResult.Success(enrollments)

        val viewModel = getViewModel()
        assertEquals(ScreenState.Content, viewModel.uiState.value.screenState)
        assertEquals(1, viewModel.uiState.value.studentContextItems.size)
        assertEquals(courses[0], viewModel.uiState.value.studentContextItems[0].course)
        assertEquals(users[0], viewModel.uiState.value.studentContextItems[0].user)
    }

    @Test
    fun `loadCoursePickerItems should filter out soft concluded courses with past course end dates`() {
        val pastCourseEndDate = "2024-01-01T00:00:00Z"
        val futureCourseEndDate = "2026-12-31T23:59:59Z"
        val courses = listOf(
            Course(1, "Current Course", endAt = futureCourseEndDate, restrictEnrollmentsToCourseDate = true),
            Course(2, "Soft Concluded Course", endAt = pastCourseEndDate, restrictEnrollmentsToCourseDate = true)
        )
        val users = listOf(User(1, "User 1"), User(2, "User 2"))
        val enrollments = listOf(
            Enrollment(1, courseId = 1, enrollmentState = "active", observedUser = users[0]),
            Enrollment(2, courseId = 2, enrollmentState = "active", observedUser = users[1])
        )
        coEvery { repository.getCourses() } returns DataResult.Success(courses)
        coEvery { repository.getEnrollments() } returns DataResult.Success(enrollments)

        val viewModel = getViewModel()
        assertEquals(ScreenState.Content, viewModel.uiState.value.screenState)
        assertEquals(1, viewModel.uiState.value.studentContextItems.size)
        assertEquals(courses[0], viewModel.uiState.value.studentContextItems[0].course)
        assertEquals(users[0], viewModel.uiState.value.studentContextItems[0].user)
    }

    @Test
    fun `loadCoursePickerItems should filter out hard concluded courses with completed workflow state`() {
        val courses = listOf(
            Course(1, "Active Course", workflowState = Course.WorkflowState.AVAILABLE),
            Course(2, "Hard Concluded Course", workflowState = Course.WorkflowState.COMPLETED)
        )
        val users = listOf(User(1, "User 1"), User(2, "User 2"))
        val enrollments = listOf(
            Enrollment(1, courseId = 1, enrollmentState = "active", observedUser = users[0]),
            Enrollment(2, courseId = 2, enrollmentState = "active", observedUser = users[1])
        )
        coEvery { repository.getCourses() } returns DataResult.Success(courses)
        coEvery { repository.getEnrollments() } returns DataResult.Success(enrollments)

        val viewModel = getViewModel()
        assertEquals(ScreenState.Content, viewModel.uiState.value.screenState)
        assertEquals(1, viewModel.uiState.value.studentContextItems.size)
        assertEquals(courses[0], viewModel.uiState.value.studentContextItems[0].course)
        assertEquals(users[0], viewModel.uiState.value.studentContextItems[0].user)
    }

    private fun getViewModel(): ParentInboxCoursePickerViewModel {
        return ParentInboxCoursePickerViewModel(context, repository, apiPrefs)
    }
}