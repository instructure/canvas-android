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
import com.instructure.canvasapi2.models.SubmissionComment
import com.instructure.canvasapi2.type.SubmissionType
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.DiscussionCheckpointUiState
import com.instructure.pandautils.compose.composables.SubmissionStateLabel
import com.instructure.pandautils.features.assignments.list.filter.AssignmentFilter
import com.instructure.pandautils.features.assignments.list.filter.AssignmentGroupByOption
import com.instructure.pandautils.features.grades.gradepreferences.GradePreferencesUiState
import com.instructure.pandautils.features.grades.gradepreferences.SortBy
import com.instructure.pandautils.features.grades.strategy.AcademicGradesStrategy
import com.instructure.pandautils.features.grades.strategy.NGCGradesStrategy
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.DisplayGrade
import com.instructure.pandautils.utils.orDefault
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
    private val gradesRepository = mockk<GradesRepository>(relaxed = true)
    private val gradeFormatter = mockk<GradeFormatter>(relaxed = true)
    private val gradeCalculator = mockk<GradeCalculator>(relaxed = true)
    private val gradesViewModelBehavior = mockk<GradesViewModelBehavior>(relaxed = true)
    private val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)

    private lateinit var viewModel: GradesViewModel

    @Before
    fun setup() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        every { savedStateHandle.get<Long>(COURSE_ID_KEY) } returns 1
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
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns assignmentGroups
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()
        coEvery { gradeFormatter.getGradeString(any(), any(), any()) } returns "100% A"

        createViewModel()

        val expected = GradesUiState(
            isLoading = false,
            gradePreferencesUiState = GradePreferencesUiState(
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
                            displayGrade = DisplayGrade(""),
                            score = null,
                            maxScore = 0.0,
                            whatIfScore = null
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
            gradePreferencesUiState = GradePreferencesUiState(),
            isError = true
        )

        Assert.assertEquals(expected, viewModel.uiState.value)
    }

    @Test
    fun `Load grades empty`() {
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()

        createViewModel()

        val expected = GradesUiState(
            isLoading = false,
            gradePreferencesUiState = GradePreferencesUiState(
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
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns assignmentGroups
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()

        createViewModel()

        val expected = GradesUiState(
            isLoading = false,
            gradePreferencesUiState = GradePreferencesUiState(
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
                            displayGrade = DisplayGrade(""),
                            score = 0.0,
                            maxScore = 0.0,
                            whatIfScore = null
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
                            displayGrade = DisplayGrade(""),
                            score = null,
                            maxScore = 0.0,
                            whatIfScore = null
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
                            displayGrade = DisplayGrade(""),
                            score = null,
                            maxScore = 0.0,
                            whatIfScore = null
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
                            displayGrade = DisplayGrade("A"),
                            score = 0.0,
                            maxScore = 0.0,
                            whatIfScore = null
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
                            displayGrade = DisplayGrade(""),
                            score = 0.0,
                            maxScore = 0.0,
                            whatIfScore = null
                        ),
                        AssignmentUiState(
                            id = 6,
                            iconRes = R.drawable.ic_discussion,
                            name = "Assignment 6",
                            dueDate = "No due date",
                            submissionStateLabel = SubmissionStateLabel.Graded,
                            displayGrade = DisplayGrade("A"),
                            score = 0.0,
                            maxScore = 15.0,
                            whatIfScore = null,
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
                                    submissionStateLabel = SubmissionStateLabel.NotSubmitted,
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
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns assignmentGroups
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()

        createViewModel()

        val expected = GradesUiState(
            isLoading = false,
            gradePreferencesUiState = GradePreferencesUiState(
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
                            displayGrade = DisplayGrade(""),
                            score = null,
                            maxScore = 0.0,
                            whatIfScore = null
                        ),
                        AssignmentUiState(
                            id = 2,
                            iconRes = R.drawable.ic_discussion,
                            name = "Assignment 2",
                            dueDate = "No due date",
                            submissionStateLabel = SubmissionStateLabel.NotSubmitted,
                            displayGrade = DisplayGrade(""),
                            score = null,
                            maxScore = 0.0,
                            whatIfScore = null
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
                            displayGrade = DisplayGrade("A"),
                            score = 0.0,
                            maxScore = 0.0,
                            whatIfScore = null
                        ),
                        AssignmentUiState(
                            id = 4,
                            iconRes = R.drawable.ic_assignment,
                            name = "Assignment 4",
                            dueDate = "No due date",
                            submissionStateLabel = SubmissionStateLabel.Submitted,
                            displayGrade = DisplayGrade(""),
                            score = 0.0,
                            maxScore = 0.0,
                            whatIfScore = null
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
                            displayGrade = DisplayGrade(""),
                            score = 0.0,
                            maxScore = 0.0,
                            whatIfScore = null
                        ),
                        AssignmentUiState(
                            id = 6,
                            iconRes = R.drawable.ic_discussion,
                            name = "Assignment 6",
                            dueDate = "No due date",
                            submissionStateLabel = SubmissionStateLabel.Graded,
                            displayGrade = DisplayGrade("A"),
                            score = 0.0,
                            maxScore = 15.0,
                            whatIfScore = null,
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
                                    submissionStateLabel = SubmissionStateLabel.NotSubmitted,
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
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()
        coEvery { gradeFormatter.getGradeString(any(), any(), any()) } returns "N/A"

        createViewModel()

        val expected = GradesUiState(
            isLoading = false,
            gradePreferencesUiState = GradePreferencesUiState(
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
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()
        coEvery { gradesRepository.getCourseGrade(any(), any(), any(), any()) } returns CourseGrade(isLocked = true)

        createViewModel()

        val expected = GradesUiState(
            isLoading = false,
            gradePreferencesUiState = GradePreferencesUiState(
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
        coVerify { gradesRepository.loadAssignmentGroups(1, any(), true, any()) }
        coVerify { gradesRepository.loadEnrollments(1, any(), true) }
    }

    @Test
    fun `Group header click closes group`() {
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns listOf(
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
            gradePreferencesUiState = GradePreferencesUiState(
                courseName = "Course 1"
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
                            displayGrade = DisplayGrade(""),
                            score = null,
                            maxScore = 0.0,
                            whatIfScore = null
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
            gradePreferencesUiState = GradePreferencesUiState(
                show = true
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
            gradePreferencesUiState = GradePreferencesUiState(),
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
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns listOf(
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
            gradePreferencesUiState = GradePreferencesUiState(
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
                            displayGrade = DisplayGrade(""),
                            score = null,
                            maxScore = 0.0,
                            whatIfScore = null
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
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns listOf(
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
            gradePreferencesUiState = GradePreferencesUiState(
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
                            displayGrade = DisplayGrade(""),
                            score = null,
                            maxScore = 0.0,
                            whatIfScore = null
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
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns listOf(
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

    @Test
    fun `Show what-if scores switch checked`() {
        createViewModel()

        viewModel.handleAction(GradesAction.ShowWhatIfScoreSwitchCheckedChange(true))

        Assert.assertTrue(viewModel.uiState.value.showWhatIfScore)
    }

    @Test
    fun `Hide what-if scores switch unchecked`() {
        createViewModel()

        viewModel.handleAction(GradesAction.ShowWhatIfScoreSwitchCheckedChange(true))
        viewModel.handleAction(GradesAction.ShowWhatIfScoreSwitchCheckedChange(false))

        Assert.assertFalse(viewModel.uiState.value.showWhatIfScore)
    }

    @Test
    fun `Toggle what-if switch with existing scores calls calculator`() {
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 1,
                        name = "Assignment 1",
                        pointsPossible = 10.0,
                        submissionTypesRaw = listOf(
                            SubmissionType.online_text_entry.rawValue
                        ),
                        submission = Submission(
                            score = 5.0
                        )
                    )
                )
            )
        )

        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns assignmentGroups
        every { gradeCalculator.calculateGrade(any(), any(), any(), any()) } returns 85.0

        createViewModel()

        viewModel.handleAction(GradesAction.UpdateWhatIfScore(1, 9.0))

        viewModel.handleAction(GradesAction.ShowWhatIfScoreSwitchCheckedChange(true))

        verify {
            gradeCalculator.calculateGrade(
                groups = assignmentGroups,
                whatIfScores = mapOf(1L to 9.0),
                applyGroupWeights = false,
                onlyGraded = true
            )
        }
    }

    @Test
    fun `Update what-if score for assignment`() {
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 1,
                        name = "Assignment 1",
                        pointsPossible = 10.0,
                        submissionTypesRaw = listOf(
                            SubmissionType.online_text_entry.rawValue
                        ),
                        submission = Submission(
                            score = 5.0
                        )
                    )
                )
            )
        )

        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns assignmentGroups
        every { gradeCalculator.calculateGrade(any(), any(), any(), any()) } returns 80.0

        createViewModel()

        viewModel.handleAction(GradesAction.ShowWhatIfScoreSwitchCheckedChange(true))

        viewModel.handleAction(GradesAction.UpdateWhatIfScore(1, 8.0))

        val assignment = viewModel.uiState.value.items.first().assignments.first()
        Assert.assertEquals(8.0, assignment.whatIfScore.orDefault(), 0.01)

        verify {
            gradeCalculator.calculateGrade(
                groups = assignmentGroups,
                whatIfScores = mapOf(1L to 8.0),
                applyGroupWeights = false,
                onlyGraded = true
            )
        }
    }

    @Test
    fun `Show what-if score dialog for assignment`() {
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 1,
                        name = "Assignment 1",
                        pointsPossible = 10.0,
                        submissionTypesRaw = listOf(
                            SubmissionType.online_text_entry.rawValue
                        ),
                        submission = Submission(
                            score = 5.0
                        )
                    )
                )
            )
        )

        createViewModel()

        viewModel.handleAction(GradesAction.ShowWhatIfScoreDialog(1L))

        Assert.assertNotNull(viewModel.uiState.value.whatIfScoreDialogData)
        Assert.assertEquals(1L, viewModel.uiState.value.whatIfScoreDialogData?.assignmentId)
    }

    @Test
    fun `Clear what-if score for assignment`() {
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 1,
                        name = "Assignment 1",
                        pointsPossible = 10.0,
                        submissionTypesRaw = listOf(
                            SubmissionType.online_text_entry.rawValue
                        ),
                        submission = Submission(
                            score = 5.0
                        )
                    )
                )
            )
        )

        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns assignmentGroups
        every { gradeCalculator.calculateGrade(any(), any(), any(), any()) } returns 80.0
        coEvery { gradeFormatter.getGradeString(any(), any(), any()) } returns "85% B"

        createViewModel()

        viewModel.handleAction(GradesAction.ShowWhatIfScoreSwitchCheckedChange(true))

        viewModel.handleAction(GradesAction.UpdateWhatIfScore(1, 8.0))

        viewModel.handleAction(GradesAction.UpdateWhatIfScore(1, null))

        val assignment = viewModel.uiState.value.items.first().assignments.first()
        Assert.assertNull(assignment.whatIfScore)

        verify { gradeFormatter.getGradeString(any(), any(), any()) }
    }

    @Test
    fun `What-if scores preserved when sorting changes`() {
        val today = LocalDateTime.now()
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 1,
                        name = "Assignment 1",
                        pointsPossible = 10.0,
                        dueAt = today.plusDays(1).toApiString(),
                        submissionTypesRaw = listOf(
                            SubmissionType.online_text_entry.rawValue
                        ),
                        submission = Submission(
                            score = 5.0
                        )
                    ),
                    Assignment(
                        id = 2,
                        name = "Assignment 2",
                        pointsPossible = 20.0,
                        dueAt = today.plusDays(2).toApiString(),
                        submissionTypesRaw = listOf(
                            SubmissionType.online_text_entry.rawValue
                        ),
                        submission = Submission(
                            score = 15.0
                        )
                    )
                )
            )
        )

        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns assignmentGroups
        every { gradeCalculator.calculateGrade(any(), any(), any(), any()) } returns 85.0

        createViewModel()

        viewModel.handleAction(GradesAction.ShowWhatIfScoreSwitchCheckedChange(true))
        viewModel.handleAction(GradesAction.UpdateWhatIfScore(1, 9.0))
        viewModel.handleAction(GradesAction.UpdateWhatIfScore(2, 18.0))

        val assignmentBefore = viewModel.uiState.value.items.flatMap { it.assignments }.find { it.id == 1L }
        Assert.assertEquals(9.0, assignmentBefore?.whatIfScore.orDefault(), 0.01)

        viewModel.handleAction(GradesAction.GradePreferencesUpdated(null, SortBy.GROUP))

        val assignment1After = viewModel.uiState.value.items.flatMap { it.assignments }.find { it.id == 1L }
        val assignment2After = viewModel.uiState.value.items.flatMap { it.assignments }.find { it.id == 2L }
        Assert.assertEquals(9.0, assignment1After?.whatIfScore.orDefault(), 0.01)
        Assert.assertEquals(18.0, assignment2After?.whatIfScore.orDefault(), 0.01)

        verify {
            gradeCalculator.calculateGrade(
                groups = assignmentGroups,
                whatIfScores = mapOf(1L to 9.0, 2L to 18.0),
                applyGroupWeights = false,
                onlyGraded = true
            )
        }
    }

    @Test
    fun `NGC experience populates filter state via strategy`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.NGC.name
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        val startDate = OffsetDateTime.now().minusDays(30).withNano(0)
        val endDate = OffsetDateTime.now().plusDays(20).withNano(0)
        val gradingPeriods = listOf(GradingPeriod(id = 1, startDate = startDate.toString(), endDate = endDate.toString()))
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns gradingPeriods
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns emptyList()

        createViewModel()

        val filter = viewModel.uiState.value.filter
        Assert.assertNotNull(filter)
        Assert.assertEquals("Course 1", filter!!.courseName)
        Assert.assertEquals(gradingPeriods.first(), filter.selectedFilters.selectedGradingPeriodFilter)
    }

    @Test
    fun `Academic experience leaves filter null`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.Academic.name
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns emptyList()

        createViewModel()

        Assert.assertNull(viewModel.uiState.value.filter)
    }

    @Test
    fun `onFilterUpdated reloads grades when grading period changes`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.NGC.name
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        val period1 = GradingPeriod(id = 1, title = "Q1")
        val period2 = GradingPeriod(id = 2, title = "Q2")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns listOf(period1, period2)
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns emptyList()

        createViewModel()
        val initialFilter = viewModel.uiState.value.filter!!.selectedFilters

        viewModel.onFilterUpdated(initialFilter.copy(selectedGradingPeriodFilter = period2))

        coVerify(atLeast = 2) { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) }
        Assert.assertEquals(period2, viewModel.uiState.value.gradePreferencesUiState.selectedGradingPeriod)
    }

    @Test
    fun `onFilterUpdated does not reload when grading period unchanged`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.NGC.name
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        val period = GradingPeriod(id = 1, title = "Q1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns listOf(period)
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns emptyList()

        createViewModel()
        val initialFilter = viewModel.uiState.value.filter!!.selectedFilters

        viewModel.onFilterUpdated(
            initialFilter.copy(selectedAssignmentFilters = listOf(AssignmentFilter.Graded))
        )

        coVerify(exactly = 1) { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) }
    }

    @Test
    fun `onFilterUpdated syncs sortBy from selectedGroupByOption`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.NGC.name
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns emptyList()

        createViewModel()
        val initialFilter = viewModel.uiState.value.filter!!.selectedFilters

        viewModel.onFilterUpdated(
            initialFilter.copy(selectedGroupByOption = AssignmentGroupByOption.DueDate)
        )

        Assert.assertEquals(SortBy.DUE_DATE, viewModel.uiState.value.gradePreferencesUiState.sortBy)
    }

    @Test
    fun `onFilterScreenOpenChanged delegates to strategy`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.NGC.name
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns emptyList()

        createViewModel()

        viewModel.onFilterScreenOpenChanged(open = true)
        Assert.assertEquals(true, viewModel.uiState.value.filter?.isFilterScreenOpen)

        viewModel.onFilterScreenOpenChanged(open = false)
        Assert.assertEquals(false, viewModel.uiState.value.filter?.isFilterScreenOpen)
    }

    @Test
    fun `NGC hides no-weight groups when course applies assignment group weights`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.NGC.name
        every { gradesRepository.getSortBy() } returns SortBy.GROUP
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(
            id = 1,
            name = "Course 1",
            isApplyAssignmentGroupWeights = true,
        )
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns listOf(
            AssignmentGroup(
                id = 1,
                name = "Weighted",
                groupWeight = 50.0,
                assignments = listOf(
                    Assignment(
                        id = 1,
                        name = "A1",
                        pointsPossible = 10.0,
                        submissionTypesRaw = listOf(SubmissionType.online_text_entry.rawValue),
                        submission = Submission(score = 9.0)
                    )
                )
            ),
            AssignmentGroup(
                id = 2,
                name = "No weight",
                groupWeight = 0.0,
                assignments = listOf(
                    Assignment(
                        id = 2,
                        name = "A2",
                        pointsPossible = 10.0,
                        submissionTypesRaw = listOf(SubmissionType.online_text_entry.rawValue),
                    )
                )
            )
        )
        every { gradeCalculator.calculateGrade(any(), any(), any(), any()) } returns 90.0

        createViewModel()

        val items = viewModel.uiState.value.items
        Assert.assertEquals(1, items.size)
        Assert.assertEquals(1L, items.first().id)
    }

    @Test
    fun `NGC keeps no-weight groups when course does not apply group weights`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.NGC.name
        every { gradesRepository.getSortBy() } returns SortBy.GROUP
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(
            id = 1,
            name = "Course 1",
            isApplyAssignmentGroupWeights = false,
        )
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns listOf(
            AssignmentGroup(
                id = 2,
                name = "No weight",
                groupWeight = 0.0,
                assignments = listOf(
                    Assignment(
                        id = 2,
                        name = "A2",
                        pointsPossible = 10.0,
                        submissionTypesRaw = listOf(SubmissionType.online_text_entry.rawValue),
                        submission = Submission(score = 8.0)
                    )
                )
            )
        )
        every { gradeCalculator.calculateGrade(any(), any(), any(), any()) } returns 80.0

        createViewModel()

        val items = viewModel.uiState.value.items
        Assert.assertEquals(1, items.size)
        Assert.assertEquals(2L, items.first().id)
    }

    @Test
    fun `NGC initial sortBy syncs with strategy's selectedGroupByOption when no saved pref`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.NGC.name
        every { gradesRepository.getSortBy() } returns null
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()

        createViewModel()

        // NGCGradesStrategy.initialFilterState defaults selectedGroupByOption to AssignmentGroup,
        // so sortBy must also land on GROUP to keep the filter chip and the actual sort aligned.
        Assert.assertEquals(SortBy.GROUP, viewModel.uiState.value.gradePreferencesUiState.sortBy)
    }

    @Test
    fun `Academic initial sortBy stays at DUE_DATE when no saved pref`() {
        every { gradesRepository.getSortBy() } returns null
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()

        createViewModel()

        Assert.assertEquals(SortBy.DUE_DATE, viewModel.uiState.value.gradePreferencesUiState.sortBy)
    }

    @Test
    fun `Saved sortBy preference wins over strategy filter default`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.NGC.name
        every { gradesRepository.getSortBy() } returns SortBy.DUE_DATE
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()

        createViewModel()

        Assert.assertEquals(SortBy.DUE_DATE, viewModel.uiState.value.gradePreferencesUiState.sortBy)
    }

    @Test
    fun `NGC filter's selectedGroupByOption is rewritten to match saved sortBy`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.NGC.name
        every { gradesRepository.getSortBy() } returns SortBy.DUE_DATE
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()

        createViewModel()

        val filter = viewModel.uiState.value.filter
        Assert.assertNotNull(filter)
        Assert.assertEquals(
            AssignmentGroupByOption.DueDate,
            filter!!.selectedFilters.selectedGroupByOption
        )
    }

    @Test
    fun `onFilterUpdated persists newSortBy when it changes`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.NGC.name
        every { gradesRepository.getSortBy() } returns SortBy.DUE_DATE
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()

        createViewModel()
        val initialFilters = viewModel.uiState.value.filter!!.selectedFilters

        viewModel.onFilterUpdated(
            initialFilters.copy(selectedGroupByOption = AssignmentGroupByOption.AssignmentGroup)
        )

        verify { gradesRepository.setSortBy(SortBy.GROUP) }
    }

    @Test
    fun `onFilterUpdated does not persist sortBy when unchanged`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.NGC.name
        every { gradesRepository.getSortBy() } returns SortBy.DUE_DATE
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()

        createViewModel()
        val initialFilters = viewModel.uiState.value.filter!!.selectedFilters

        viewModel.onFilterUpdated(
            initialFilters.copy(selectedAssignmentFilters = listOf(AssignmentFilter.Graded))
        )

        verify(exactly = 0) { gradesRepository.setSortBy(any()) }
    }

    @Test
    fun `NGC filter's selectedGroupByOption stays AssignmentGroup when no saved sortBy`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.NGC.name
        every { gradesRepository.getSortBy() } returns null
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()

        createViewModel()

        val filter = viewModel.uiState.value.filter
        Assert.assertNotNull(filter)
        Assert.assertEquals(
            AssignmentGroupByOption.AssignmentGroup,
            filter!!.selectedFilters.selectedGroupByOption
        )
    }

    @Test
    fun `Maps comment count and unread state for NGC experience`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.NGC.name
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()
        coEvery { gradeFormatter.getGradeString(any(), any(), any()) } returns ""

        // Relaxed gradesRepository.studentId returns 0, so authorId = 0 represents the student's own
        // comment and a non-zero authorId represents teacher feedback.
        val teacherId = 999L
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 10,
                        name = "Unread feedback",
                        submission = Submission(
                            id = 100,
                            unreadCommentCount = 2,
                            submissionComments = listOf(
                                SubmissionComment(authorId = teacherId),
                                SubmissionComment(authorId = teacherId),
                                SubmissionComment(authorId = teacherId)
                            )
                        ),
                        submissionTypesRaw = listOf(SubmissionType.online_text_entry.rawValue)
                    ),
                    Assignment(
                        id = 20,
                        name = "Read feedback",
                        submission = Submission(
                            id = 200,
                            submissionComments = listOf(SubmissionComment(authorId = teacherId))
                        ),
                        submissionTypesRaw = listOf(SubmissionType.online_text_entry.rawValue)
                    ),
                    Assignment(
                        id = 30,
                        name = "No comments",
                        submission = Submission(id = 300),
                        submissionTypesRaw = listOf(SubmissionType.online_text_entry.rawValue)
                    ),
                    Assignment(
                        id = 40,
                        name = "Hidden feedback (only student's own comment visible)",
                        submission = Submission(
                            id = 400,
                            unreadCommentCount = 2,
                            submissionComments = listOf(SubmissionComment(authorId = 0))
                        ),
                        submissionTypesRaw = listOf(SubmissionType.online_text_entry.rawValue)
                    )
                )
            )
        )
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns assignmentGroups

        createViewModel()

        val assignments = viewModel.uiState.value.items.flatMap { it.assignments }
        val unread = assignments.first { it.id == 10L }
        val read = assignments.first { it.id == 20L }
        val none = assignments.first { it.id == 30L }
        val hiddenFeedback = assignments.first { it.id == 40L }

        Assert.assertEquals(3, unread.commentCount)
        Assert.assertTrue(unread.hasUnreadComments)
        Assert.assertEquals(1, read.commentCount)
        Assert.assertFalse(read.hasUnreadComments)
        Assert.assertEquals(0, none.commentCount)
        Assert.assertFalse(none.hasUnreadComments)
        // unreadCommentCount > 0 but the only visible comment is the student's own → no dot.
        Assert.assertEquals(1, hiddenFeedback.commentCount)
        Assert.assertFalse(hiddenFeedback.hasUnreadComments)
    }

    @Test
    fun `Does not map comment count or unread state for Academic experience`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.Academic.name
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()
        coEvery { gradeFormatter.getGradeString(any(), any(), any()) } returns ""

        // The submission carries unread teacher feedback, but the comment pill is NGC-only: in the
        // Academic experience the comment list is never read, so no count and no unread dot are shown.
        val teacherId = 999L
        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    Assignment(
                        id = 10,
                        name = "Unread feedback",
                        submission = Submission(
                            id = 100,
                            unreadCommentCount = 2,
                            submissionComments = listOf(
                                SubmissionComment(authorId = teacherId),
                                SubmissionComment(authorId = teacherId)
                            )
                        ),
                        submissionTypesRaw = listOf(SubmissionType.online_text_entry.rawValue)
                    )
                )
            )
        )
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns assignmentGroups

        createViewModel()

        val assignment = viewModel.uiState.value.items.flatMap { it.assignments }.first { it.id == 10L }
        Assert.assertEquals(0, assignment.commentCount)
        Assert.assertFalse(assignment.hasUnreadComments)
    }

    @Test
    fun `Comment count only includes the latest attempt`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.NGC.name
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()
        coEvery { gradeFormatter.getGradeString(any(), any(), any()) } returns ""

        val assignmentGroups = listOf(
            AssignmentGroup(
                id = 1,
                name = "Group 1",
                assignments = listOf(
                    // Latest attempt is 2 → only the two attempt-2 comments count, older/null ones are excluded.
                    Assignment(
                        id = 10,
                        name = "Multiple attempts",
                        submission = Submission(
                            id = 100,
                            attempt = 2,
                            submissionComments = listOf(
                                SubmissionComment(id = 1, attempt = 2),
                                SubmissionComment(id = 2, attempt = 2),
                                SubmissionComment(id = 3, attempt = 1),
                                SubmissionComment(id = 4, attempt = null)
                            )
                        ),
                        submissionTypesRaw = listOf(SubmissionType.online_text_entry.rawValue)
                    ),
                    // Latest attempt <= 1 → attempts null/0/1 are grouped together, attempt 2 excluded.
                    Assignment(
                        id = 20,
                        name = "First attempt grouping",
                        submission = Submission(
                            id = 200,
                            attempt = 1,
                            submissionComments = listOf(
                                SubmissionComment(id = 5, attempt = null),
                                SubmissionComment(id = 6, attempt = 0),
                                SubmissionComment(id = 7, attempt = 1),
                                SubmissionComment(id = 8, attempt = 2)
                            )
                        ),
                        submissionTypesRaw = listOf(SubmissionType.online_text_entry.rawValue)
                    )
                )
            )
        )
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns assignmentGroups

        createViewModel()

        val assignments = viewModel.uiState.value.items.flatMap { it.assignments }
        Assert.assertEquals(2, assignments.first { it.id == 10L }.commentCount)
        Assert.assertEquals(3, assignments.first { it.id == 20L }.commentCount)
    }

    @Test
    fun `Does not paginate comments for Academic experience`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.Academic.name
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()

        createViewModel()

        coVerify { gradesRepository.loadAssignmentGroups(1, any(), any(), paginateComments = false) }
    }

    @Test
    fun `Paginates comments for NGC experience`() {
        every { savedStateHandle.get<String>(EXPERIENCE_KEY) } returns Experience.NGC.name
        coEvery { gradesRepository.loadCourse(1, any()) } returns Course(id = 1, name = "Course 1")
        coEvery { gradesRepository.loadGradingPeriods(1, any()) } returns emptyList()
        coEvery { gradesRepository.loadAssignmentGroups(1, any(), any(), any()) } returns emptyList()
        coEvery { gradesRepository.loadEnrollments(1, any(), any()) } returns listOf()

        createViewModel()

        coVerify { gradesRepository.loadAssignmentGroups(1, any(), any(), paginateComments = true) }
    }

    private fun createViewModel() {
        val strategies = mapOf(
            Experience.Academic to AcademicGradesStrategy(),
            Experience.NGC to NGCGradesStrategy(),
        )
        viewModel = GradesViewModel(
            context,
            gradesRepository,
            gradeFormatter, gradeCalculator,
            gradesViewModelBehavior,
            strategies,
            testDispatcher,
            savedStateHandle
        )
    }

    private fun getFormattedDate(localDateTime: LocalDateTime): String {
        val date = Date(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        val dateText = DateHelper.monthDayYearDateFormatUniversalShort.format(date)
        val timeText = DateHelper.getFormattedTime(context, date)
        return "Due $dateText $timeText"
    }
}
