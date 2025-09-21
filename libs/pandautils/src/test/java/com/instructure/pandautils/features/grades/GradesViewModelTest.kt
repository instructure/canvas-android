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

package com.instructure.pandautils.features.grades

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.CustomGradeStatusesQuery
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentGroup
import com.instructure.canvasapi2.models.Checkpoint
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseGrade
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.GradingPeriod
import com.instructure.canvasapi2.models.SubAssignmentSubmission
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.type.SubmissionType
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.DiscussionCheckpointUiState
import com.instructure.pandautils.features.grades.gradepreferences.GradePreferencesUiState
import com.instructure.pandautils.features.grades.gradepreferences.SortBy
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.DisplayGrade
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import java.time.OffsetDateTime
import java.util.Date


@ExperimentalCoroutinesApi
class GradesViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val context = mockk<Context>(relaxed = true)
    private val gradesBehaviour = mockk<GradesBehaviour>(relaxed = true)
    private val gradesRepository = mockk<GradesRepository>(relaxed = true)
    private val gradeFormatter = mockk<GradeFormatter>(relaxed = true)
    private val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)

    private lateinit var viewModel: GradesViewModel

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        every { savedStateHandle.get<Long>(COURSE_ID_KEY) } returns 1
        every { gradesBehaviour.canvasContextColor } returns 1
        coEvery { gradesRepository.getCourseGrade(any(), any(), any(), any()) } returns CourseGrade()
        every { gradesRepository.getSortBy() } returns null

        every { context.getString(R.string.gradesNoDueDate) } returns "No due date"
        every { context.getString(R.string.due, any()) } answers { "Due ${(call.invocation.args[1] as Array<*>)[0]}" }
        every { context.getString(R.string.overdueAssignments) } returns "Overdue Assignments"
        every { context.getString(R.string.upcomingAssignments) } returns "Upcoming Assignments"
        every { context.getString(R.string.undatedAssignments) } returns "Undated Assignments"
        every { context.getString(R.string.pastAssignments) } returns "Past Assignments"
        every { context.getString(R.string.gradesRefreshFailed) } returns "Grade refresh failed"
        every { context.getString(R.string.reply_to_topic) } returns "Reply to topic"
        every { context.getString(R.string.additional_replies, any()) } answers {
            val args = secondArg<Array<Any>>()
            "Additional replies (${args[0]})"
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Load grades`() {
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        val startDate = OffsetDateTime.now().minusDays(30).withNano(0)
        val endDate = OffsetDateTime.now().plusDays(20).withNano(0)
        val gradingPeriods = listOf(GradingPeriod(id = 1, startDate = startDate.toString(), endDate = endDate.toString()))
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns gradingPeriods
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 1,
                        name = "Assignment 1",
                        submissionTypesRaw = listOf(
                            SubmissionType.online_text_entry.rawValue
                        )
                    )
                )
            )
        )
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any()) } returns assignmentGroups
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()
        coEvery { gradeFormatter.getGradeString(any(), any(), any()) } returns "100% A"

        createViewModel()

        val expected = GradesUiState(
            isLoading = false,
            canvasContextColor = 1,
            gradePreferencesUiState = GradePreferencesUiState(
                canvasContextColor = 1,
                courseName = "Course 1",
                gradingPeriods = gradingPeriods,
                defaultGradingPeriod = gradingPeriods.first(),
                selectedGradingPeriod = gradingPeriods.first()
            ),
            items = listOf(
                AssignmentGroupUiState(
                    id = 2,
                    name = "Undated Assignments",
                    expanded = true,
                    assignments = listOf(
                        AssignmentUiState(
                            id = 1,
                            iconRes = R.drawable.ic_assignment,
                            name = "Assignment 1",
                            dueDate = "No due date",
                            submissionStateLabel = SubmissionStateLabel.NotSubmitted,
                            displayGrade = DisplayGrade("")
                        )
                    )
                )
            ),
            gradeText = "100% A"
        )

        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Load grades error`() {
        coEvery { gradesRepository.loadCourse(1, any()) } throws Exception()

        createViewModel()

        val expected = GradesUiState(
            isLoading = false,
            canvasContextColor = 1,
            gradePreferencesUiState = GradePreferencesUiState(
                canvasContextColor = 1
            ),
            isError = true
        )

        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Load grades empty`() {
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()

        createViewModel()

        val expected = GradesUiState(
            isLoading = false,
            canvasContextColor = 1,
            gradePreferencesUiState = GradePreferencesUiState(
                canvasContextColor = 1,
                courseName = "Course 1",
                gradingPeriods = emptyList()
            ),
            items = emptyList()
        )

        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Assignments map correctly sorted by due date`() {
        val today = LocalDateTime.now()
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.getCustomGradeStatuses(1, any()) } returns listOf(
            mockk<CustomGradeStatusesQuery.Node>(relaxed = true) {
                every { _id } returns "1"
                every { name } returns "Custom Status 1"
            }
        )
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 1,
                        name = "Assignment 1",
                        submissionTypesRaw = listOf(
                            SubmissionType.online_quiz.rawValue
                        )
                    ),
                    Assignment(
                        id = 2,
                        name = "Assignment 2",
                        dueAt = today.plusDays(1).toApiString(),
                        submissionTypesRaw = listOf(
                            SubmissionType.discussion_topic.rawValue
                        )
                    )
                )
            ),
            AssignmentGroup(
                id = 2,
                name = "Group 2",
                assignments = listOf(
                    Assignment(
                        id = 3,
                        name = "Assignment 3",
                        dueAt = today.minusDays(1).toApiString(),
                        submissionTypesRaw = listOf(
                            SubmissionType.online_text_entry.rawValue
                        ),
                        submission = Submission(
                            submittedAt = Date(),
                            grade = "A",
                            postedAt = Date()
                        )
                    ),
                    Assignment(
                        id = 4,
                        name = "Assignment 4",
                        dueAt = today.minusDays(1).toApiString(),
                        submissionTypesRaw = listOf(
                            SubmissionType.online_text_entry.rawValue
                        ),
                        submission = Submission(
                            submittedAt = Date()
                        )
                    ),
                    Assignment(
                        id = 5,
                        name = "Assignment 5",
                        dueAt = today.minusDays(1).toApiString(),
                        submissionTypesRaw = listOf(
                            SubmissionType.online_text_entry.rawValue
                        ),
                        submission = Submission(
                            submittedAt = Date(),
                            customGradeStatusId = 1
                        )
                    ),
                    Assignment(
                        id = 6,
                        name = "Assignment 6",
                        submissionTypesRaw = listOf(
                            SubmissionType.discussion_topic.rawValue
                        ),
                        submission = Submission(
                            submittedAt = Date(),
                            subAssignmentSubmissions = arrayListOf(
                                SubAssignmentSubmission(
                                    grade = "A",
                                    score = 10.0,
                                    late = true,
                                    excused = false,
                                    missing = false,
                                    latePolicyStatus = null,
                                    customGradeStatusId = null,
                                    subAssignmentTag = Const.REPLY_TO_TOPIC,
                                    enteredGrade = null,
                                    enteredScore = 0.0,
                                    userId = 1L,
                                    isGradeMatchesCurrentSubmission = true
                                ),
                                SubAssignmentSubmission(
                                    grade = null,
                                    score = 0.0,
                                    late = false,
                                    excused = false,
                                    missing = false,
                                    latePolicyStatus = null,
                                    customGradeStatusId = null,
                                    subAssignmentTag = Const.REPLY_TO_ENTRY,
                                    enteredGrade = null,
                                    enteredScore = 0.0,
                                    userId = 1L,
                                    isGradeMatchesCurrentSubmission = true
                                )
                            ),
                            grade = "A",
                            postedAt = Date()
                        ),
                        checkpoints = listOf(
                            Checkpoint(
                                name = "Reply to topic",
                                tag = Const.REPLY_TO_TOPIC,
                                pointsPossible = 10.0,
                                dueAt = today.minusDays(1).toApiString(),
                                overrides = null,
                                onlyVisibleToOverrides = false,
                                lockAt = null,
                                unlockAt = null
                            ),
                            Checkpoint(
                                name = "Reply to entry",
                                tag = Const.REPLY_TO_ENTRY,
                                pointsPossible = 5.0,
                                dueAt = today.plusDays(2).toApiString(),
                                overrides = null,
                                onlyVisibleToOverrides = false,
                                lockAt = null,
                                unlockAt = null
                            )
                        ),
                        pointsPossible = 15.0,
                        discussionTopicHeader = DiscussionTopicHeader(
                            replyRequiredCount = 3
                        )
                    )
                )
            )
        )
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any()) } returns assignmentGroups
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()

        createViewModel()

        val expected = GradesUiState(
            isLoading = false,
            canvasContextColor = 1,
            gradePreferencesUiState = GradePreferencesUiState(
                canvasContextColor = 1,
                courseName = "Course 1"
            ),
            items = listOf(
                AssignmentGroupUiState(
                    id = 0,
                    name = "Overdue Assignments",
                    expanded = true,
                    assignments = listOf(
                        AssignmentUiState(
                            id = 4,
                            iconRes = R.drawable.ic_assignment,
                            name = "Assignment 4",
                            dueDate = getFormattedDate(today.minusDays(1)),
                            submissionStateLabel = SubmissionStateLabel.Submitted,
                            displayGrade = DisplayGrade("")
                        )
                    )
                ),
                AssignmentGroupUiState(
                    id = 1,
                    name = "Upcoming Assignments",
                    expanded = true,
                    assignments = listOf(
                        AssignmentUiState(
                            id = 2,
                            iconRes = R.drawable.ic_discussion,
                            name = "Assignment 2",
                            dueDate = getFormattedDate(today.plusDays(1)),
                            submissionStateLabel = SubmissionStateLabel.NotSubmitted,
                            displayGrade = DisplayGrade("")
                        )
                    )
                ),
                AssignmentGroupUiState(
                    id = 2,
                    name = "Undated Assignments",
                    expanded = true,
                    assignments = listOf(
                        AssignmentUiState(
                            id = 1,
                            iconRes = R.drawable.ic_quiz,
                            name = "Assignment 1",
                            dueDate = "No due date",
                            submissionStateLabel = SubmissionStateLabel.NotSubmitted,
                            displayGrade = DisplayGrade("")
                        )
                    )
                ),
                AssignmentGroupUiState(
                    id = 3,
                    name = "Past Assignments",
                    expanded = true,
                    assignments = listOf(
                        AssignmentUiState(
                            id = 3,
                            iconRes = R.drawable.ic_assignment,
                            name = "Assignment 3",
                            dueDate = getFormattedDate(today.minusDays(1)),
                            submissionStateLabel = SubmissionStateLabel.Graded,
                            displayGrade = DisplayGrade("A")
                        ),
                        AssignmentUiState(
                            id = 5,
                            iconRes = R.drawable.ic_assignment,
                            name = "Assignment 5",
                            dueDate = getFormattedDate(today.minusDays(1)),
                            submissionStateLabel = SubmissionStateLabel.Custom(
                                R.drawable.ic_flag,
                                R.color.textInfo,
                                "Custom Status 1"
                            ),
                            displayGrade = DisplayGrade("")
                        ),
                        AssignmentUiState(
                            id = 6,
                            iconRes = R.drawable.ic_discussion,
                            name = "Assignment 6",
                            dueDate = "No due date",
                            submissionStateLabel = SubmissionStateLabel.Graded,
                            displayGrade = DisplayGrade("A"),
                            checkpoints = listOf(
                                DiscussionCheckpointUiState(
                                    name = "Reply to topic",
                                    dueDate = getFormattedDate(today.minusDays(1)),
                                    submissionStateLabel = SubmissionStateLabel.Late,
                                    displayGrade = DisplayGrade("A"),
                                    pointsPossible = 10
                                ),
                                DiscussionCheckpointUiState(
                                    name = "Additional replies (3)",
                                    dueDate = getFormattedDate(today.plusDays(2)),
                                    submissionStateLabel = SubmissionStateLabel.None,
                                    displayGrade = DisplayGrade(),
                                    pointsPossible = 5
                                )
                            )
                        )
                    )
                )
            )
        )

        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Assignments map correctly sorted by groups`() {
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.getCustomGradeStatuses(1, any()) } returns listOf(
            mockk<CustomGradeStatusesQuery.Node>(relaxed = true) {
                every { _id } returns "1"
                every { name } returns "Custom Status 1"
            }
        )
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 1,
                        name = "Assignment 1",
                        submissionTypesRaw = listOf(
                            SubmissionType.online_quiz.rawValue
                        )
                    ),
                    Assignment(
                        id = 2,
                        name = "Assignment 2",
                        submissionTypesRaw = listOf(
                            SubmissionType.discussion_topic.rawValue
                        )
                    )
                )
            ),
            AssignmentGroup(
                id = 2,
                name = "Group 2",
                assignments = listOf(
                    Assignment(
                        id = 3,
                        name = "Assignment 3",
                        submissionTypesRaw = listOf(
                            SubmissionType.online_text_entry.rawValue
                        ),
                        submission = Submission(
                            submittedAt = Date(),
                            grade = "A",
                            postedAt = Date()
                        )
                    ),
                    Assignment(
                        id = 4,
                        name = "Assignment 4",
                        submissionTypesRaw = listOf(
                            SubmissionType.online_text_entry.rawValue
                        ),
                        submission = Submission(
                            submittedAt = Date(),
                        )
                    ),
                    Assignment(
                        id = 5,
                        name = "Assignment 5",
                        submissionTypesRaw = listOf(
                            SubmissionType.online_text_entry.rawValue
                        ),
                        submission = Submission(
                            customGradeStatusId = 1
                        )
                    ),
                    Assignment(
                        id = 6,
                        name = "Assignment 6",
                        submissionTypesRaw = listOf(
                            SubmissionType.discussion_topic.rawValue
                        ),
                        submission = Submission(
                            submittedAt = Date(),
                            subAssignmentSubmissions = arrayListOf(
                                SubAssignmentSubmission(
                                    grade = "A",
                                    score = 10.0,
                                    late = true,
                                    excused = false,
                                    missing = false,
                                    latePolicyStatus = null,
                                    customGradeStatusId = null,
                                    subAssignmentTag = Const.REPLY_TO_TOPIC,
                                    enteredGrade = null,
                                    enteredScore = 0.0,
                                    userId = 1L,
                                    isGradeMatchesCurrentSubmission = true
                                ),
                                SubAssignmentSubmission(
                                    grade = null,
                                    score = 0.0,
                                    late = false,
                                    excused = false,
                                    missing = false,
                                    latePolicyStatus = null,
                                    customGradeStatusId = null,
                                    subAssignmentTag = Const.REPLY_TO_ENTRY,
                                    enteredGrade = null,
                                    enteredScore = 0.0,
                                    userId = 1L,
                                    isGradeMatchesCurrentSubmission = true
                                )
                            ),
                            grade = "A",
                            postedAt = Date()
                        ),
                        checkpoints = listOf(
                            Checkpoint(
                                name = "Reply to topic",
                                tag = Const.REPLY_TO_TOPIC,
                                pointsPossible = 10.0,
                                overrides = null,
                                onlyVisibleToOverrides = false,
                                lockAt = null,
                                unlockAt = null
                            ),
                            Checkpoint(
                                name = "Reply to entry",
                                tag = Const.REPLY_TO_ENTRY,
                                pointsPossible = 5.0,
                                overrides = null,
                                onlyVisibleToOverrides = false,
                                lockAt = null,
                                unlockAt = null
                            )
                        ),
                        pointsPossible = 15.0,
                        discussionTopicHeader = DiscussionTopicHeader(
                            replyRequiredCount = 3
                        )
                    )
                )
            )
        )
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any()) } returns assignmentGroups
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()

        createViewModel()

        val expected = GradesUiState(
            isLoading = false,
            canvasContextColor = 1,
            gradePreferencesUiState = GradePreferencesUiState(
                canvasContextColor = 1,
                courseName = "Course 1",
                sortBy = SortBy.GROUP
            ),
            items = listOf(
                AssignmentGroupUiState(
                    id = 1,
                    name = "Group 1",
                    expanded = true,
                    assignments = listOf(
                        AssignmentUiState(
                            id = 1,
                            iconRes = R.drawable.ic_quiz,
                            name = "Assignment 1",
                            dueDate = "No due date",
                            submissionStateLabel = SubmissionStateLabel.NotSubmitted,
                            displayGrade = DisplayGrade("")
                        ),
                        AssignmentUiState(
                            id = 2,
                            iconRes = R.drawable.ic_discussion,
                            name = "Assignment 2",
                            dueDate = "No due date",
                            submissionStateLabel = SubmissionStateLabel.NotSubmitted,
                            displayGrade = DisplayGrade("")
                        )

                    )
                ),
                AssignmentGroupUiState(
                    id = 2,
                    name = "Group 2",
                    expanded = true,
                    assignments = listOf(
                        AssignmentUiState(
                            id = 3,
                            iconRes = R.drawable.ic_assignment,
                            name = "Assignment 3",
                            dueDate = "No due date",
                            submissionStateLabel = SubmissionStateLabel.Graded,
                            displayGrade = DisplayGrade("A")
                        ),
                        AssignmentUiState(
                            id = 4,
                            iconRes = R.drawable.ic_assignment,
                            name = "Assignment 4",
                            dueDate = "No due date",
                            submissionStateLabel = SubmissionStateLabel.Submitted,
                            displayGrade = DisplayGrade("")
                        ),
                        AssignmentUiState(
                            id = 5,
                            iconRes = R.drawable.ic_assignment,
                            name = "Assignment 5",
                            dueDate = "No due date",
                            submissionStateLabel = SubmissionStateLabel.Custom(
                                R.drawable.ic_flag,
                                R.color.textInfo,
                                "Custom Status 1"
                            ),
                            displayGrade = DisplayGrade("")
                        ),
                        AssignmentUiState(
                            id = 6,
                            iconRes = R.drawable.ic_discussion,
                            name = "Assignment 6",
                            dueDate = "No due date",
                            submissionStateLabel = SubmissionStateLabel.Graded,
                            displayGrade = DisplayGrade("A"),
                            checkpoints = listOf(
                                DiscussionCheckpointUiState(
                                    name = "Reply to topic",
                                    dueDate = "No due date",
                                    submissionStateLabel = SubmissionStateLabel.Late,
                                    displayGrade = DisplayGrade("A"),
                                    pointsPossible = 10
                                ),
                                DiscussionCheckpointUiState(
                                    name = "Additional replies (3)",
                                    dueDate = "No due date",
                                    submissionStateLabel = SubmissionStateLabel.None,
                                    displayGrade = DisplayGrade(),
                                    pointsPossible = 5
                                )
                            )
                        )
                    )
                )
            )
        )

        viewModel.handleAction(GradesAction.GradePreferencesUpdated(null, SortBy.GROUP))

        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Format grade when no current grade`() {
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()
        coEvery { gradeFormatter.getGradeString(any(), any(), any()) } returns "N/A"

        createViewModel()

        val expected = GradesUiState(
            isLoading = false,
            canvasContextColor = 1,
            gradePreferencesUiState = GradePreferencesUiState(
                canvasContextColor = 1,
                courseName = "Course 1",
                gradingPeriods = emptyList()
            ),
            items = emptyList(),
            gradeText = "N/A"
        )

        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Show lock when grade is locked`() {
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()
        coEvery { gradesRepository.getCourseGrade(any(), any(), any(), any()) } returns CourseGrade(isLocked = true)

        createViewModel()

        val expected = GradesUiState(
            isLoading = false,
            canvasContextColor = 1,
            gradePreferencesUiState = GradePreferencesUiState(
                canvasContextColor = 1,
                courseName = "Course 1",
                gradingPeriods = emptyList()
            ),
            items = emptyList(),
            isGradeLocked = true
        )

        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Refresh reloads grades`() {
        createViewModel()

        viewModel.handleAction(GradesAction.Refresh())

        coVerify { gradesRepository.loadCourse(1, true) }
        coVerify { gradesRepository.loadGradingPeriods(1, true) }
        coVerify { gradesRepository.loadAssignmentGroups(1, any(), true) }
        coVerify { gradesRepository.loadEnrollments(1, any(), true) }
    }

    @Test
    fun `Group header click closes group`() {
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any()) } returns listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 1,
                        name = "Assignment 1",
                        submissionTypesRaw = listOf(
                            SubmissionType.online_text_entry.rawValue
                        )
                    )
                )
            )
        )
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()

        createViewModel()

        viewModel.handleAction(GradesAction.GroupHeaderClick(2))

        val expected = GradesUiState(
            isLoading = false,
            canvasContextColor = 1,
            gradePreferencesUiState = GradePreferencesUiState(
                courseName = "Course 1",
                canvasContextColor = 1
            ),
            items = listOf(
                AssignmentGroupUiState(
                    id = 2,
                    name = "Undated Assignments",
                    expanded = false,
                    assignments = listOf(
                        AssignmentUiState(
                            id = 1,
                            iconRes = R.drawable.ic_assignment,
                            name = "Assignment 1",
                            dueDate = "No due date",
                            submissionStateLabel = SubmissionStateLabel.NotSubmitted,
                            displayGrade = DisplayGrade("")
                        )
                    )
                )
            )
        )

        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Show hide grade preferences`() {
        createViewModel()

        viewModel.handleAction(GradesAction.ShowGradePreferences)

        val show = GradesUiState(
            isLoading = false,
            canvasContextColor = 1,
            gradePreferencesUiState = GradePreferencesUiState(
                show = true,
                canvasContextColor = 1
            )
        )

        Assert.assertEquals(show, viewModel.uiState.value)

        viewModel.handleAction(GradesAction.HideGradePreferences)

        val hide = show.copy(
            gradePreferencesUiState = show.gradePreferencesUiState.copy(show = false)
        )

        Assert.assertEquals(hide, viewModel.uiState.value)
    }

    @Test
    fun `Only graded assignments switch checked change`() {
        createViewModel()

        viewModel.handleAction(GradesAction.OnlyGradedAssignmentsSwitchCheckedChange(false))

        val expected = GradesUiState(
            isLoading = false,
            canvasContextColor = 1,
            gradePreferencesUiState = GradePreferencesUiState(
                canvasContextColor = 1
            ),
            onlyGradedAssignmentsSwitchEnabled = false
        )

        Assert.assertEquals(expected, viewModel.uiState.value)
        coVerify { gradeFormatter.getGradeString(any(), any(), true) }
    }

    @Test
    fun `Navigate to assignment details`() = runTest {
        createViewModel()

        val events = mutableListOf<GradesViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewModel.events.toList(events)
        }

        viewModel.handleAction(GradesAction.AssignmentClick(1L))

        val expected = GradesViewModelAction.NavigateToAssignmentDetails(1L, 1L)
        Assert.assertEquals(expected, events.last())
    }

    @Test
    fun `Show snackbar if load error and list is not empty`() {
        every { context.getString(R.string.gradesRefreshFailed) } returns "Grade refresh failed"
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any()) } returns listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 1,
                        name = "Assignment 1",
                        submissionTypesRaw = listOf(
                            SubmissionType.online_text_entry.rawValue
                        )
                    )
                )
            )
        )

        createViewModel()

        val loaded = GradesUiState(
            isLoading = false,
            canvasContextColor = 1,
            gradePreferencesUiState = GradePreferencesUiState(
                canvasContextColor = 1,
                courseName = "Course 1"
            ),
            items = listOf(
                AssignmentGroupUiState(
                    id = 2,
                    name = "Undated Assignments",
                    expanded = true,
                    assignments = listOf(
                        AssignmentUiState(
                            id = 1,
                            iconRes = R.drawable.ic_assignment,
                            name = "Assignment 1",
                            dueDate = "No due date",
                            submissionStateLabel = SubmissionStateLabel.NotSubmitted,
                            displayGrade = DisplayGrade("")
                        )
                    )
                )
            )
        )

        coEvery { gradesRepository.loadCourse(1, any()) } throws Exception()
        viewModel.handleAction(GradesAction.Refresh())

        val expectedWithSnackbar = loaded.copy(snackbarMessage = "Grade refresh failed")
        Assert.assertEquals(expectedWithSnackbar, viewModel.uiState.value)

        viewModel.handleAction(GradesAction.SnackbarDismissed)
        val expected = loaded.copy(snackbarMessage = null)
        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Filter hidden grades in Grade list`() {
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any()) } returns listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 1,
                        name = "Assignment 1",
                        submissionTypesRaw = listOf(
                            SubmissionType.online_text_entry.rawValue
                        )
                    ),
                    Assignment(
                        id = 2,
                        name = "Assignment 2",
                        submissionTypesRaw = listOf(
                            SubmissionType.online_text_entry.rawValue
                        ),
                        isHiddenInGradeBook = true
                    ),
                )
            )
        )

        createViewModel()

        val expected = GradesUiState(
            isLoading = false,
            canvasContextColor = 1,
            gradePreferencesUiState = GradePreferencesUiState(
                canvasContextColor = 1,
                courseName = "Course 1"
            ),
            items = listOf(
                AssignmentGroupUiState(
                    id = 2,
                    name = "Undated Assignments",
                    expanded = true,
                    assignments = listOf(
                        AssignmentUiState(
                            id = 1,
                            iconRes = R.drawable.ic_assignment,
                            name = "Assignment 1",
                            dueDate = "No due date",
                            submissionStateLabel = SubmissionStateLabel.NotSubmitted,
                            displayGrade = DisplayGrade("")
                        )
                    )
                )
            )
        )

        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Get saved sorting preference, save when updating preferences`() {
        coEvery { gradesRepository.getSortBy() } returns SortBy.GROUP

        createViewModel()

        Assert.assertEquals(SortBy.GROUP, viewModel.uiState.value.gradePreferencesUiState.sortBy)

        viewModel.handleAction(GradesAction.GradePreferencesUpdated(null, SortBy.DUE_DATE))

        verify { gradesRepository.setSortBy(SortBy.DUE_DATE) }
    }

    @Test
    fun `Toggle checkpoints expanded`() {
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any()) } returns listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 1,
                        name = "Assignment 1",
                        submissionTypesRaw = listOf(
                            SubmissionType.discussion_topic.rawValue
                        ),
                        checkpoints = listOf(
                            Checkpoint(
                                name = "Reply to topic",
                                tag = Const.REPLY_TO_TOPIC,
                                pointsPossible = 10.0,
                                overrides = null,
                                onlyVisibleToOverrides = false,
                                lockAt = null,
                                unlockAt = null
                            ),
                            Checkpoint(
                                name = "Reply to entry",
                                tag = Const.REPLY_TO_ENTRY,
                                pointsPossible = 5.0,
                                overrides = null,
                                onlyVisibleToOverrides = false,
                                lockAt = null,
                                unlockAt = null
                            )
                        )
                    )
                )
            )
        )

        createViewModel()

        Assert.assertFalse(viewModel.uiState.value.items.first().assignments.first().checkpointsExpanded)

        viewModel.handleAction(GradesAction.ToggleCheckpointsExpanded(1))

        Assert.assertTrue(viewModel.uiState.value.items.first().assignments.first().checkpointsExpanded)
    }

    private fun createViewModel() {
        viewModel = GradesViewModel(context, gradesBehaviour, gradesRepository, gradeFormatter, savedStateHandle)
    }

    private fun getFormattedDate(localDateTime: LocalDateTime): String {
        val date = Date(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        val dateText = DateHelper.monthDayYearDateFormatUniversalShort.format(date)
        val timeText = DateHelper.getFormattedTime(context, date)
        return "Due $dateText $timeText"
    }
}
