/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.parentapp.features.courses.list

import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.studentColor
import com.instructure.parentapp.features.dashboard.TestSelectStudentHolder
import com.instructure.testutils.ViewModelTestRule
import com.instructure.testutils.LifecycleTestOwner
import com.instructure.testutils.collectForTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CoursesViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private val lifecycleTestOwner = LifecycleTestOwner()

    private val repository: CoursesRepository = mockk(relaxed = true)
    private val selectedStudentFlow = MutableStateFlow<User?>(null)
    private val selectedStudentHolder = TestSelectStudentHolder(selectedStudentFlow)
    private val courseGradeFormatter: CourseGradeFormatter = mockk(relaxed = true)

    private lateinit var viewModel: CoursesViewModel

    @Before
    fun setup() {
        mockkStatic(User::studentColor)
    }

    @Test
    fun `Load courses when student changes`() = runTest {
        val student = User(1L)
        coEvery { repository.getCourses(student.id, any()) } returns listOf(Course(id = 1L, name = "Course 1", courseCode = "code-1"))
        every { courseGradeFormatter.getGradeText(any(), any()) } returns "A+"
        every { student.studentColor } returns 1

        createViewModel()
        selectedStudentFlow.emit(student)

        val expectedState = CoursesUiState(
            isLoading = false,
            isError = false,
            courseListItems = listOf(
                CourseListItemUiState(
                    courseId = 1L,
                    courseName = "Course 1",
                    courseCode = "code-1",
                    grade = "A+"
                )
            ),
            studentColor = 1
        )

        assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Courses map correctly`() = runTest {
        val student = User(1L)
        val courses = listOf(
            Course(id = 1L, name = "Course 1", courseCode = "code-1"),
            Course(id = 2L, name = "Course 2", courseCode = "code-2"),
            Course(id = 3L, name = "Course 3", courseCode = "code-3")
        )
        coEvery { repository.getCourses(any(), any()) } returns courses
        every { courseGradeFormatter.getGradeText(any(), any()) } returns "A+"
        every { student.studentColor } returns 1

        createViewModel()
        selectedStudentFlow.emit(student)

        val expectedState = CoursesUiState(
            isLoading = false,
            isError = false,
            courseListItems = courses.map {
                CourseListItemUiState(
                    courseId = it.id,
                    courseName = it.name,
                    courseCode = it.courseCode,
                    grade = "A+"
                )
            },
            studentColor = 1
        )

        assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Error load courses`() = runTest {
        val student = User(1L)
        coEvery { repository.getCourses(student.id, any()) } throws Exception()
        every { student.studentColor } returns 1

        createViewModel()
        selectedStudentFlow.emit(student)

        val expectedState = CoursesUiState(
            isLoading = false,
            isError = true,
            studentColor = 1
        )

        assertEquals(expectedState, viewModel.uiState.value)
    }

    @Test
    fun `Refresh reloads courses`() = runTest {
        createViewModel()
        val student = User(1L)
        selectedStudentHolder.updateSelectedStudent(student)
        every { student.studentColor } returns 1

        viewModel.handleAction(CoursesAction.Refresh)

        coVerify { repository.getCourses(any(), true) }
    }

    @Test
    fun `Navigate to course details`() = runTest {
        createViewModel()

        val events = viewModel.events.collectForTest(viewModelTestRule.testDispatcher, backgroundScope)

        viewModel.handleAction(CoursesAction.CourseTapped(1L))

        val expected = CoursesViewModelAction.NavigateToCourseDetails(1L)
        assertEquals(expected, events.last())
    }

    @Test
    fun `Change color when student color is changed`() = runTest {
        val student = User(1L)
        mockkStatic(User::studentColor)
        every { student.studentColor } returns 1
        createViewModel()
        selectedStudentFlow.emit(student)

        assertEquals(1, viewModel.uiState.value.studentColor)

        every { student.studentColor } returns 2
        selectedStudentHolder.selectedStudentColorChanged()

        assertEquals(2, viewModel.uiState.value.studentColor)
        unmockkAll()
    }

    private fun createViewModel() {
        viewModel = CoursesViewModel(repository, selectedStudentHolder, courseGradeFormatter)
    }
}
