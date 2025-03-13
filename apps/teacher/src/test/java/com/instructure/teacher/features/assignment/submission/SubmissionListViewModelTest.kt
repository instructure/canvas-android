/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
 */
package com.instructure.teacher.features.assignment.submission

import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.GradeableStudentSubmission
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.StudentAssignee
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.teacher.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class SubmissionListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val submissionListRepository: AssignmentSubmissionRepository = mockk(relaxed = true)

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val submissions = listOf(
        GradeableStudentSubmission(
            assignee = StudentAssignee(
                student = User(1L, name = "Late Student"),
            ),
            submission = Submission(
                id = 1L,
                late = true,
                attempt = 1L
            ),
        ),
        GradeableStudentSubmission(
            assignee = StudentAssignee(
                student = User(2L, name = "On Time Student"),
            ),
            submission = Submission(
                id = 2L,
                late = false,
                attempt = 1L
            ),
        ),
        GradeableStudentSubmission(
            assignee = StudentAssignee(
                student = User(3L, name = "Missing Student"),
            ),
            submission = Submission(
                id = 3L,
                missing = true
            )
        ),
        GradeableStudentSubmission(
            assignee = StudentAssignee(
                student = User(4L, name = "Good Graded Student"),
            ),
            submission = Submission(
                id = 4L,
                grade = "10",
                score = 10.0,
                attempt = 1L,
                isGradeMatchesCurrentSubmission = true
            )
        ),
        GradeableStudentSubmission(
            assignee = StudentAssignee(
                student = User(5L, name = "Bad Graded Student"),
            ),
            submission = Submission(
                id = 5L,
                grade = "0",
                score = 0.0,
                attempt = 1L,
                isGradeMatchesCurrentSubmission = true
            )
        ),
        GradeableStudentSubmission(
            assignee = StudentAssignee(
                student = User(6L, name = "Excused Student"),
            ),
            submission = Submission(
                id = 6L,
                excused = true,
                attempt = 1L,
                isGradeMatchesCurrentSubmission = true,
                grade = "Excused"
            )
        ),
        GradeableStudentSubmission(
            assignee = StudentAssignee(
                student = User(7L, name = "Updated Grade Student"),
            ),
            submission = Submission(
                id = 7L,
                attempt = 1L,
                isGradeMatchesCurrentSubmission = false,
                grade = "10",
                score = 10.0
            )
        ),
        GradeableStudentSubmission(
            assignee = StudentAssignee(
                student = User(8L, name = "Not Submitted Student"),
            ),
            submission = null
        )
    )

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        ContextKeeper.appContext = mockk(relaxed = true)

        every { savedStateHandle.get<Course>(SubmissionListFragment.COURSE) } returns Course(
            1L,
            name = "Course 1"
        )
        every { savedStateHandle.get<Assignment>(SubmissionListFragment.ASSIGNMENT) } returns Assignment(
            1L,
            name = "Assignment 1",
            submissionTypesRaw = listOf("online_text_entry")
        )
        every { savedStateHandle.get<SubmissionListFilter>(SubmissionListFragment.FILTER_TYPE) } returns SubmissionListFilter.ALL

        coEvery {
            submissionListRepository.getGradeableStudentSubmissions(
                any(),
                any(),
                any()
            )
        } returns submissions

        setupString()
    }

    @Test
    fun `Empty state`() = runTest {
        coEvery {
            submissionListRepository.getGradeableStudentSubmissions(
                any(),
                any(),
                any()
            )
        } returns emptyList()
        coEvery { submissionListRepository.getSections(any(), any()) } returns emptyList()

        val viewModel = createViewModel()

        assert(viewModel.uiState.value.submissions.isEmpty())
    }

    @Test
    fun `Error state when submissions fail`() = runTest {
        coEvery {
            submissionListRepository.getGradeableStudentSubmissions(
                any(),
                any(),
                any()
            )
        } throws Exception()
        coEvery { submissionListRepository.getSections(any(), any()) } returns emptyList()

        val viewModel = createViewModel()

        assert(viewModel.uiState.value.error)
    }

    @Test
    fun `Items map correctly`() {
        val viewModel = createViewModel()

        val expected = listOf(
            SubmissionUiState(
                1L,
                1L,
                "Late Student",
                null,
                listOf(SubmissionTag.LATE, SubmissionTag.NEEDS_GRADING),
                "-",
                true
            ),
            SubmissionUiState(
                2L,
                2L,
                "On Time Student",
                null,
                listOf(SubmissionTag.SUBMITTED, SubmissionTag.NEEDS_GRADING),
                "-",
                true
            ),
            SubmissionUiState(
                3L,
                3L,
                "Missing Student",
                null,
                listOf(SubmissionTag.MISSING),
                "-",
                true
            ),
            SubmissionUiState(
                4L,
                4L,
                "Good Graded Student",
                null,
                listOf(SubmissionTag.GRADED),
                "10",
                true
            ),
            SubmissionUiState(
                5L,
                5L,
                "Bad Graded Student",
                null,
                listOf(SubmissionTag.GRADED),
                "0",
                true
            ),
            SubmissionUiState(
                6L,
                6L,
                "Excused Student",
                null,
                listOf(SubmissionTag.EXCUSED),
                "",
                true
            ),
            SubmissionUiState(
                7L,
                7L,
                "Updated Grade Student",
                null,
                listOf(SubmissionTag.GRADED),
                "10",
                true
            ),
            SubmissionUiState(
                8L,
                8L,
                "Not Submitted Student",
                null,
                listOf(SubmissionTag.NOT_SUBMITTED),
                "-",
                false
            )
        )

        assertEquals(expected, viewModel.uiState.value.submissions)
    }

    @Test
    fun `Filter late submissions`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.value.actionHandler(
            SubmissionListAction.SetFilters(
                SubmissionListFilter.LATE,
                null,
                emptyList()
            )
        )

        val expectedData = listOf(
            SubmissionUiState(
                1L,
                1L,
                "Late Student",
                null,
                listOf(SubmissionTag.LATE, SubmissionTag.NEEDS_GRADING),
                "-",
                true
            )
        )

        assertEquals(expectedData, viewModel.uiState.value.submissions)
    }

    @Test
    fun `Filter graded submissions`() {
        val viewModel = createViewModel()

        viewModel.uiState.value.actionHandler(
            SubmissionListAction.SetFilters(
                SubmissionListFilter.GRADED,
                null,
                emptyList()
            )
        )

        val expectedData = listOf(
            SubmissionUiState(
                4L,
                4L,
                "Good Graded Student",
                null,
                listOf(SubmissionTag.GRADED),
                "10",
                true
            ),
            SubmissionUiState(
                5L,
                5L,
                "Bad Graded Student",
                null,
                listOf(SubmissionTag.GRADED),
                "0",
                true
            ),
            SubmissionUiState(
                6L,
                6L,
                "Excused Student",
                null,
                listOf(SubmissionTag.EXCUSED),
                "",
                true
            )
        )

        assertEquals(expectedData, viewModel.uiState.value.submissions)
    }

    @Test
    fun `Filter not graded submissions`() {
        val viewModel = createViewModel()

        viewModel.uiState.value.actionHandler(
            SubmissionListAction.SetFilters(
                SubmissionListFilter.NOT_GRADED,
                null,
                emptyList()
            )
        )

        val expected = listOf(
            SubmissionUiState(
                1L,
                1L,
                "Late Student",
                null,
                listOf(SubmissionTag.LATE, SubmissionTag.NEEDS_GRADING),
                "-",
                true
            ),
            SubmissionUiState(
                2L,
                2L,
                "On Time Student",
                null,
                listOf(SubmissionTag.SUBMITTED, SubmissionTag.NEEDS_GRADING),
                "-",
                true
            ),
            SubmissionUiState(
                3L,
                3L,
                "Missing Student",
                null,
                listOf(SubmissionTag.MISSING),
                "-",
                true
            ),
            SubmissionUiState(
                7L,
                7L,
                "Updated Grade Student",
                null,
                listOf(SubmissionTag.GRADED),
                "10",
                true
            )
        )

        assertEquals(expected, viewModel.uiState.value.submissions)
    }

    @Test
    fun `Filter not submitted`() {
        val viewModel = createViewModel()

        viewModel.uiState.value.actionHandler(
            SubmissionListAction.SetFilters(
                SubmissionListFilter.MISSING,
                null,
                emptyList()
            )
        )

        val expected = listOf(
            SubmissionUiState(
                8L,
                8L,
                "Not Submitted Student",
                null,
                listOf(SubmissionTag.NOT_SUBMITTED),
                "-",
                false
            )
        )

        assertEquals(expected, viewModel.uiState.value.submissions)
    }

    @Test
    fun `Filter scored more than`() {
        val viewModel = createViewModel()

        viewModel.uiState.value.actionHandler(
            SubmissionListAction.SetFilters(
                SubmissionListFilter.ABOVE_VALUE,
                5.0,
                emptyList()
            )
        )

        val expected = listOf(
            SubmissionUiState(
                4L,
                4L,
                "Good Graded Student",
                null,
                listOf(SubmissionTag.GRADED),
                "10",
                true
            ),
            SubmissionUiState(
                7L,
                7L,
                "Updated Grade Student",
                null,
                listOf(SubmissionTag.GRADED),
                "10",
                true
            )
        )

        assertEquals(expected, viewModel.uiState.value.submissions)
    }

    @Test
    fun `Filter scored less than`() {
        val viewModel = createViewModel()

        viewModel.uiState.value.actionHandler(
            SubmissionListAction.SetFilters(
                SubmissionListFilter.BELOW_VALUE,
                5.0,
                emptyList()
            )
        )

        val expected = listOf(
            SubmissionUiState(
                5L,
                5L,
                "Bad Graded Student",
                null,
                listOf(SubmissionTag.GRADED),
                "0",
                true
            )
        )

        assertEquals(expected, viewModel.uiState.value.submissions)
    }

    @Test
    fun `Filter by section`() = runTest {
        coEvery {
            submissionListRepository.getGradeableStudentSubmissions(
                any(),
                any(),
                any()
            )
        } returns listOf(
            GradeableStudentSubmission(
                assignee = StudentAssignee(
                    student = User(
                        1L,
                        name = "Student 1",
                        enrollments = listOf(Enrollment(id = 1L, courseSectionId = 1L))
                    ),
                ),
                submission = Submission(
                    id = 1L,
                    late = true,
                    attempt = 1L
                ),
            ),
            GradeableStudentSubmission(
                assignee = StudentAssignee(
                    student = User(
                        2L,
                        name = "Student 2",
                        enrollments = listOf(Enrollment(id = 2L, courseSectionId = 2L))
                    ),
                ),
                submission = Submission(
                    id = 2L,
                    late = false,
                    attempt = 1L
                ),
            ),
        )

        coEvery { submissionListRepository.getSections(any(), any()) } returns listOf(
            Section(1L, "Section 1"),
            Section(2L, "Section 2")
        )

        val viewModel = createViewModel()

        viewModel.uiState.value.actionHandler(
            SubmissionListAction.SetFilters(
                SubmissionListFilter.ALL,
                null,
                listOf(1L)
            )
        )

        val expected = listOf(
            SubmissionUiState(
                1L,
                1L,
                "Student 1",
                null,
                listOf(SubmissionTag.LATE, SubmissionTag.NEEDS_GRADING),
                "-",
                true
            )
        )

        assertEquals(expected, viewModel.uiState.value.submissions)
    }

    @Test
    fun `Hidden flag set if submission has no postedAt`() = runTest {
        coEvery {
            submissionListRepository.getGradeableStudentSubmissions(
                any(),
                any(),
                any()
            )
        } returns listOf(
            GradeableStudentSubmission(
                assignee = StudentAssignee(
                    student = User(1L, name = "Student 1"),
                ),
                submission = Submission(
                    id = 1L,
                    late = true,
                    attempt = 1L
                ),
            ),
            GradeableStudentSubmission(
                assignee = StudentAssignee(
                    student = User(2L, name = "Student 2"),
                ),
                submission = Submission(
                    id = 2L,
                    late = false,
                    attempt = 1L,
                    postedAt = Date()
                ),
            ),
        )

        val viewModel = createViewModel()

        val expected = listOf(
            SubmissionUiState(
                1L,
                1L,
                "Student 1",
                null,
                listOf(SubmissionTag.LATE, SubmissionTag.NEEDS_GRADING),
                "-",
                true
            ),
            SubmissionUiState(
                2L,
                2L,
                "Student 2",
                null,
                listOf(SubmissionTag.SUBMITTED, SubmissionTag.NEEDS_GRADING),
                "-",
                false
            )
        )

        assertEquals(expected, viewModel.uiState.value.submissions)
    }

    @Test
    fun `Route to submission`() = runTest {
        coEvery {
            submissionListRepository.getGradeableStudentSubmissions(
                any(),
                any(),
                any()
            )
        } returns listOf(
            GradeableStudentSubmission(
                assignee = StudentAssignee(
                    student = User(1L, name = "Late Student"),
                ),
                submission = Submission(
                    id = 1L,
                    late = true,
                    attempt = 1L
                ),
            )
        )

        val viewModel = createViewModel()

        viewModel.uiState.value.actionHandler(SubmissionListAction.SubmissionClicked(1L))

        val events = mutableListOf<SubmissionListViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
            assertEquals(
                SubmissionListViewModelAction.RouteToSubmission(
                    courseId = 1L,
                    assignmentId = 1L,
                    selectedIdx = 0,
                    anonymousGrading = false,
                    filteredSubmissionIds = longArrayOf(1L),
                    SubmissionListFilter.ALL,
                    0.0
                ), events.last()
            )
        }
    }

    @Test
    fun `Refresh action`() = runTest {
        coEvery {
            submissionListRepository.getGradeableStudentSubmissions(
                any(),
                any(),
                any()
            )
        } returns emptyList()

        val viewModel = createViewModel()

        coEvery {
            submissionListRepository.getGradeableStudentSubmissions(
                any(),
                any(),
                any()
            )
        } returns submissions
        viewModel.uiState.value.actionHandler(SubmissionListAction.Refresh)

        coVerify {
            submissionListRepository.getGradeableStudentSubmissions(any(), any(), true)
        }

        assertEquals(submissions.size, viewModel.uiState.value.submissions.size)
    }

    @Test
    fun `Route to user profile`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.value.actionHandler(SubmissionListAction.AvatarClicked(1L))

        val events = mutableListOf<SubmissionListViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
            assertEquals(SubmissionListViewModelAction.RouteToUser(1L, 1L), events.last())
        }
    }

    @Test
    fun `Search action`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.value.actionHandler(SubmissionListAction.Search("On Time Student"))

        val expected = listOf(
            SubmissionUiState(
                2L,
                2L,
                "On Time Student",
                null,
                listOf(SubmissionTag.SUBMITTED, SubmissionTag.NEEDS_GRADING),
                "-",
                true
            )
        )

        assertEquals(expected, viewModel.uiState.value.submissions)
        assertEquals("On Time Student", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `Set filters update uiState`() {
        val viewModel = createViewModel()
        viewModel.uiState.value.actionHandler(
            SubmissionListAction.SetFilters(
                SubmissionListFilter.ABOVE_VALUE,
                5.0,
                listOf(1L)
            )
        )

        assertEquals(SubmissionListFilter.ABOVE_VALUE, viewModel.uiState.value.filter)
        assertEquals(5.0, viewModel.uiState.value.filterValue)
        assertEquals(listOf(1L), viewModel.uiState.value.selectedSections)
    }

    @Test
    fun `Open post policy`() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.value.actionHandler(SubmissionListAction.ShowPostPolicy)

        val events = mutableListOf<SubmissionListViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
            assertEquals(
                SubmissionListViewModelAction.ShowPostPolicy(
                    course = Course(1L),
                    assignment = Assignment(1L)
                ), events.last()
            )
        }
    }

    @Test
    fun `Send message`() = runTest {
        val course = Course(1L, name = "Course 1")
        val assignment = Assignment(1L, name = "Assignment 1")
        every { savedStateHandle.get<Course>(SubmissionListFragment.COURSE) } returns course
        every { savedStateHandle.get<Assignment>(SubmissionListFragment.ASSIGNMENT) } returns assignment

        val viewModel = createViewModel()

        viewModel.uiState.value.actionHandler(
            SubmissionListAction.SetFilters(
                SubmissionListFilter.NOT_GRADED,
                null,
                emptyList()
            )
        )

        viewModel.uiState.value.actionHandler(SubmissionListAction.SendMessage)

        val expectedRecipients =
            listOf(submissions[0], submissions[1], submissions[2], submissions[6]).map {
                Recipient.from((it.assignee as StudentAssignee).student)
            }

        val events = mutableListOf<SubmissionListViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
            assertEquals(
                SubmissionListViewModelAction.SendMessage(
                    course.contextId,
                    course.name,
                    expectedRecipients,
                    assignment.name.orEmpty()
                ), events.last()
            )
        }
    }

    @Test
    fun `Complete incomplete grades`() = runTest {
        coEvery {
            submissionListRepository.getGradeableStudentSubmissions(
                any(),
                any(),
                any()
            )
        } returns listOf(
            GradeableStudentSubmission(
                assignee = StudentAssignee(
                    student = User(1L, name = "Student 1"),
                ),
                submission = Submission(
                    id = 1L,
                    attempt = 1L,
                    grade = "complete"
                ),
            ),
            GradeableStudentSubmission(
                assignee = StudentAssignee(
                    student = User(2L, name = "Student 2"),
                ),
                submission = Submission(
                    id = 2L,
                    attempt = 1L,
                    grade = "incomplete"
                ),
            )
        )

        val viewModel = createViewModel()

        val expected = listOf(
            SubmissionUiState(
                1L,
                1L,
                "Student 1",
                null,
                listOf(SubmissionTag.GRADED),
                "Complete",
                true
            ),
            SubmissionUiState(
                2L,
                2L,
                "Student 2",
                null,
                listOf(SubmissionTag.GRADED),
                "Incomplete",
                true
            )
        )

        assertEquals(expected, viewModel.uiState.value.submissions)

    }

    @Test
    fun `Percentage grade`() = runTest {
        every { savedStateHandle.get<Assignment>(SubmissionListFragment.ASSIGNMENT) } returns Assignment(
            1L,
            name = "Assignment 1",
            gradingType = "percent",
            submissionTypesRaw = listOf("online_text_entry")
        )
        coEvery {
            submissionListRepository.getGradeableStudentSubmissions(
                any(),
                any(),
                any()
            )
        } returns listOf(
            GradeableStudentSubmission(
                assignee = StudentAssignee(
                    student = User(1L, name = "Student 1"),
                ),
                submission = Submission(
                    id = 1L,
                    attempt = 1L,
                    grade = "85.123%",
                    score = 85.123
                ),
            ),
            GradeableStudentSubmission(
                assignee = StudentAssignee(
                    student = User(2L, name = "Student 2"),
                ),
                submission = Submission(
                    id = 2L,
                    attempt = 1L,
                    grade = "10%",
                    score = 10.0
                ),
            )
        )

        val viewModel = createViewModel()

        val expected = listOf(
            SubmissionUiState(
                1L,
                1L,
                "Student 1",
                null,
                listOf(SubmissionTag.GRADED),
                "85.12%",
                true
            ),
            SubmissionUiState(
                2L,
                2L,
                "Student 2",
                null,
                listOf(SubmissionTag.GRADED),
                "10%",
                true
            )
        )

        assertEquals(expected, viewModel.uiState.value.submissions)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Throw exception if no course`() {
        every { savedStateHandle.get<Course>(SubmissionListFragment.COURSE) } returns null
        createViewModel()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Throw exception if no assignment`() {
        every { savedStateHandle.get<Assignment>(SubmissionListFragment.ASSIGNMENT) } returns null
        createViewModel()
    }

    @Test
    fun `Set filter to 'All' if nothing is set`() {
        every { savedStateHandle.get<SubmissionListFilter>(SubmissionListFragment.FILTER_TYPE) } returns null
        val viewModel = createViewModel()
        assertEquals(SubmissionListFilter.ALL, viewModel.uiState.value.filter)
    }

    @Test
    fun `Anonymous grading`() = runTest {
        coEvery { submissionListRepository.getGradeableStudentSubmissions(any(), any(), any()) } returns listOf(
            GradeableStudentSubmission(
                assignee = StudentAssignee(
                    student = User(1L, name = "Student 1"),
                ),
                submission = Submission(
                    id = 1L,
                    attempt = 1L,
                    grade = "85.123%",
                    score = 85.123
                ),
            ),
            GradeableStudentSubmission(
                assignee = StudentAssignee(
                    student = User(2L, name = "Student 2"),
                ),
                submission = Submission(
                    id = 2L,
                    attempt = 1L,
                    grade = "10%",
                    score = 10.0
                ),
            ),
            GradeableStudentSubmission(
                assignee = StudentAssignee(
                    student = User(3L, name = "Student 3"),
                ),
                submission = Submission(
                    id = 3L,
                    attempt = 1L,
                    grade = "10%",
                    score = 10.0
                ),
            )
        )

        every { savedStateHandle.get<Assignment>(SubmissionListFragment.ASSIGNMENT) } returns Assignment(
            1L,
            name = "Assignment 1",
            anonymousGrading = true,
            submissionTypesRaw = listOf("online_text_entry")
        )

        val viewModel = createViewModel()

        val excepted = listOf(
            SubmissionUiState(
                1L,
                1L,
                "Student 1",
                null,
                listOf(SubmissionTag.GRADED),
                "85.123%",
                true
            ),
            SubmissionUiState(
                2L,
                2L,
                "Student 2",
                null,
                listOf(SubmissionTag.GRADED),
                "10%",
                true
            ),
            SubmissionUiState(
                3L,
                3L,
                "Student 3",
                null,
                listOf(SubmissionTag.GRADED),
                "10%",
                true
            )
        )

        excepted.forEach {
            assert(viewModel.uiState.value.submissions.contains(it))
        }
        assertEquals(true, viewModel.uiState.value.anonymousGrading)
    }

    private fun createViewModel(): SubmissionListViewModel {
        return SubmissionListViewModel(savedStateHandle, resources, submissionListRepository)
    }

    private fun setupString() {
        every { resources.getString(R.string.complete_grade) } returns "Complete"
        every { resources.getString(R.string.incomplete_grade) } returns "Incomplete"
    }
}