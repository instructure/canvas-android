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

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentInboxCoursePickerViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository: ParentInboxCoursePickerRepository = mockk(relaxed = true)

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
    fun `getContextURL should return correct URL`() {
        mockkObject(ApiPrefs)
        every { ApiPrefs.fullDomain } returns "https://canvas.instructure.com"
        val viewModel = getViewModel()
        val courseId = 123L
        val expected = "https://canvas.instructure.com/courses/$courseId"
        assertEquals(expected, viewModel.getContextURL(courseId))
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
        val enrollments = listOf(Enrollment(1, courseId = 1, observedUser = users[0]), Enrollment(2, courseId = 2, observedUser = users[1]))
        coEvery { repository.getCourses() } returns DataResult.Success(courses)
        coEvery { repository.getEnrollments() } returns DataResult.Success(enrollments)

        val viewModel = getViewModel()
        assertEquals(ScreenState.Data, viewModel.uiState.value.screenState)
        assertEquals(2, viewModel.uiState.value.studentContextItems.size)
        assertEquals(courses[0], viewModel.uiState.value.studentContextItems[0].course)
        assertEquals(users[0], viewModel.uiState.value.studentContextItems[0].user)
        assertEquals(courses[1], viewModel.uiState.value.studentContextItems[1].course)
        assertEquals(users[1], viewModel.uiState.value.studentContextItems[1].user)
    }

    private fun getViewModel(): ParentInboxCoursePickerViewModel {
        return ParentInboxCoursePickerViewModel(repository)
    }
}