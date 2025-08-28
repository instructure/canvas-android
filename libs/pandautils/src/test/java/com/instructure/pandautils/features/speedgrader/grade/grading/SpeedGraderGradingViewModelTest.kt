package com.instructure.pandautils.features.speedgrader.grade.grading

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.SubmissionGradeQuery
import com.instructure.canvasapi2.UpdateSubmissionStatusMutation
import com.instructure.canvasapi2.models.GradingSchemeRow
import com.instructure.canvasapi2.type.CourseGradeStatus
import com.instructure.canvasapi2.type.GradingType
import com.instructure.canvasapi2.type.LatePolicyStatusType
import com.instructure.canvasapi2.type.SubmissionGradingStatus
import com.instructure.pandautils.R
import com.instructure.pandautils.features.speedgrader.SpeedGraderErrorHolder
import com.instructure.pandautils.features.speedgrader.grade.GradingEvent
import com.instructure.pandautils.features.speedgrader.grade.SpeedGraderGradingEventHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Date

@ExperimentalCoroutinesApi
class SpeedGraderGradingViewModelTest {

    private lateinit var viewModel: SpeedGraderGradingViewModel
    private lateinit var savedStateHandle: SavedStateHandle
    private val repository: SpeedGraderGradingRepository = mockk(relaxed = true)
    private val gradingEventHandler = SpeedGraderGradingEventHandler()
    private val resources: Resources = mockk(relaxed = true)
    private val errorHandler: SpeedGraderErrorHolder = mockk(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()

    private val assignmentId = 123L
    private val studentId = 456L
    private val courseId = 789L

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        savedStateHandle = SavedStateHandle().apply {
            set("assignmentId", assignmentId)
            set("submissionId", studentId)
            set("courseId", courseId)
        }

        coEvery { resources.getString(R.string.gradingStatus_late) } returns "Late"
        coEvery { resources.getString(R.string.gradingStatus_missing) } returns "Missing"
        coEvery { resources.getString(R.string.gradingStatus_extended) } returns "Extended"
        coEvery { resources.getString(R.string.gradingStatus_excused) } returns "Excused"
        coEvery { resources.getString(R.string.gradingStatus_none) } returns "None"
        coEvery { resources.getString(R.string.notGraded) } returns "Not Graded"
    }

    private fun createViewModel() {
        viewModel = SpeedGraderGradingViewModel(savedStateHandle, repository, resources, gradingEventHandler, errorHandler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `init throws IllegalArgumentException if assignmentId is missing`() {
        savedStateHandle = SavedStateHandle().apply {
            set("submissionId", studentId)
            set("courseId", courseId)
        }
        createViewModel()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `init throws IllegalArgumentException if studentId is missing from SavedStateHandle`() {
        savedStateHandle = SavedStateHandle().apply {
            set("assignmentId", assignmentId)
            set("courseId", courseId)
        }
        createViewModel()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `init throws IllegalArgumentException if courseId is missing`() {
        savedStateHandle = SavedStateHandle().apply {
            set("assignmentId", assignmentId)
            set("submissionId", studentId)
        }
        createViewModel()
    }

    @Test
    fun `submission maps correctly`() = runTest {
        val dueDate = Date()
        coEvery {
            repository.getSubmissionGrade(
                any(),
                any(),
                any()
            )
        } returns createMockSubmission(dueDate)

        val expected = SpeedGraderGradingUiState(
            pointsPossible = 100.0,
            score = 95.0,
            grade = "A",
            excused = false,
            enteredGrade = "A",
            enteredScore = 95.0f,
            pointsDeducted = 0.0,
            gradingType = GradingType.points,
            loading = false,
            daysLate = 1f,
            submittedAt = dueDate,
            letterGrades = listOf(
                GradingSchemeRow(
                    name = "A",
                    value = 0.90,
                ),
                GradingSchemeRow(
                    name = "B",
                    value = 0.80
                )
            ),
            gradingStatuses = CourseGradeStatus.entries.filter { it != CourseGradeStatus.UNKNOWN__ }
                .map {
                    GradeStatus(
                        null,
                        it.rawValue,
                        it.rawValue.replaceFirstChar { it.uppercase() })
                } + listOf(
                GradeStatus(123L, null, "Custom Status"),
                GradeStatus(456L, null, "Another Custom Status")
            ),
            gradingStatus = "graded",
            onScoreChange = {},
            onExcuse = {},
            onStatusChange = {},
            onPercentageChange = {},
            onLateDaysChange = {}
        )

        createViewModel()
        val uiState = viewModel.uiState.first()

        assertEquals(expected.pointsPossible, uiState.pointsPossible)
        assertEquals(expected.score, uiState.score)
        assertEquals(expected.grade, uiState.grade)
        assertEquals(expected.excused, uiState.excused)
        assertEquals(expected.enteredGrade, uiState.enteredGrade)
        assertEquals(expected.enteredScore, uiState.enteredScore)
        assertEquals(expected.pointsDeducted, uiState.pointsDeducted)
        assertEquals(expected.gradingType, uiState.gradingType)
        assertEquals(expected.loading, uiState.loading)
        assertEquals(expected.daysLate, uiState.daysLate)
        assertEquals(expected.submittedAt, uiState.submittedAt)
        assertEquals(expected.gradingStatuses, uiState.gradingStatuses)
        assertEquals(expected.gradingStatus, uiState.gradingStatus)
    }

    @Test
    fun `score changed`() = runTest {
        val submission = createMockSubmission()

        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns submission
        createViewModel()

        val uiState = viewModel.uiState.first()

        assertEquals(95.0, uiState.score)

        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns submission.copy(
            submission = submission.submission?.copy(score = 90.0)
        )

        coEvery {
            repository.updateSubmissionGrade(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns mockk()

        uiState.onScoreChange(90f)
        testDispatcher.scheduler.advanceTimeBy(600)
        coVerify {
            repository.updateSubmissionGrade(
                "90.0",
                userId = studentId,
                assignmentId = assignmentId,
                courseId = courseId,
                excused = false
            )
        }
    }

    @Test
    fun `percentage changed`() = runTest {
        val submission = createMockSubmission()

        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns submission
        createViewModel()

        val uiState = viewModel.uiState.first()

        assertEquals(95.0, uiState.score)

        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns submission.copy(
            submission = submission.submission?.copy(score = 90.0)
        )

        coEvery {
            repository.updateSubmissionGrade(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns mockk()

        uiState.onPercentageChange(89.0f)
        testDispatcher.scheduler.advanceTimeBy(600)
        coVerify {
            repository.updateSubmissionGrade(
                "89.0",
                userId = studentId,
                assignmentId = assignmentId,
                courseId = courseId,
                excused = false
            )
        }
    }

    @Test
    fun `excuse submission`() = runTest {
        val submission = createMockSubmission()

        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns submission
        createViewModel()

        val uiState = viewModel.uiState.first()

        assertEquals(false, uiState.excused)

        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns submission.copy(
            submission = submission.submission?.copy(excused = true)
        )

        coEvery { repository.excuseSubmission(any(), any(), any()) } returns mockk()

        uiState.onExcuse()
        coVerify {
            repository.excuseSubmission(studentId, assignmentId, courseId)
        }

        val updatedUiState = viewModel.uiState.first()
        assertEquals(true, updatedUiState.excused)
    }

    @Test
    fun `change custom grading status`() = runTest {
        val submission = createMockSubmission()

        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns submission
        createViewModel()

        val uiState = viewModel.uiState.first()

        assertEquals("graded", uiState.gradingStatus)

        val newStatus = GradeStatus(123L, null, "Custom Status")
        coEvery {
            repository.updateSubmissionStatus(
                any(),
                any(),
                any()
            )
        } returns UpdateSubmissionStatusMutation.Data(
            updateSubmissionGradeStatus = UpdateSubmissionStatusMutation.UpdateSubmissionGradeStatus(
                submission = UpdateSubmissionStatusMutation.Submission(
                    status = "Custom Status",
                    score = 95.0,
                    grade = "A",
                    enteredGrade = "A",
                    enteredScore = 95.0,
                    secondsLate = 0.0,
                    deductedPoints = 0.0,
                    excused = false,
                    late = false
                )
            )
        )


        uiState.onStatusChange(newStatus)
        coVerify {
            repository.updateSubmissionStatus(
                submissionId = submission.submission?._id?.toLong() ?: 0L,
                customStatusId = newStatus.id?.toString(),
                latePolicyStatus = null
            )
        }

        val updatedUiState = viewModel.uiState.first()
        assertEquals("Custom Status", updatedUiState.gradingStatus)
    }

    @Test
    fun `change late policy status`() = runTest {
        val submission = createMockSubmission()
        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns submission

        createViewModel()
        val uiState = viewModel.uiState.first()

        assertEquals("graded", uiState.gradingStatus)

        val newStatus = GradeStatus(null, "missing", "Missing")
        coEvery {
            repository.updateSubmissionStatus(
                any(),
                any(),
                any()
            )
        } returns UpdateSubmissionStatusMutation.Data(
            updateSubmissionGradeStatus = UpdateSubmissionStatusMutation.UpdateSubmissionGradeStatus(
                submission = UpdateSubmissionStatusMutation.Submission(
                    status = "Missing",
                    score = 95.0,
                    grade = "A",
                    enteredGrade = "A",
                    enteredScore = 95.0,
                    secondsLate = 0.0,
                    deductedPoints = 0.0,
                    excused = false,
                    late = false
                )
            )
        )


        uiState.onStatusChange(newStatus)
        coVerify {
            repository.updateSubmissionStatus(
                submissionId = submission.submission?._id?.toLong() ?: 0L,
                customStatusId = null,
                latePolicyStatus = newStatus.statusId
            )
        }

        val updatedUiState = viewModel.uiState.first()
        assertEquals("Missing", updatedUiState.gradingStatus)

    }

    private fun createMockSubmission(
        dueDate: Date = Date(),
        status: String = "graded"
    ): SubmissionGradeQuery.Data {
        return SubmissionGradeQuery.Data(
            submission = SubmissionGradeQuery.Submission(
                gradingStatus = SubmissionGradingStatus.graded,
                grade = "A",
                gradeHidden = false,
                _id = "123",
                submissionStatus = "submitted",
                status = status,
                latePolicyStatus = LatePolicyStatusType.late,
                late = true,
                secondsLate = 3600.0,
                deductedPoints = 0.0,
                score = 95.0,
                excused = false,
                enteredGrade = "A",
                enteredScore = 95.0,
                hideGradeFromStudent = false,
                submittedAt = dueDate,
                assignment = SubmissionGradeQuery.Assignment(
                    dueAt = dueDate,
                    gradingType = GradingType.points,
                    pointsPossible = 100.0,
                    gradingStandard = null,
                    course = SubmissionGradeQuery.Course(
                        customGradeStatusesConnection = SubmissionGradeQuery.CustomGradeStatusesConnection(
                            edges = listOf(
                                SubmissionGradeQuery.Edge(
                                    node = SubmissionGradeQuery.Node(
                                        _id = "123",
                                        id = "customStatusId",
                                        color = "#FF0000",
                                        name = "Custom Status"
                                    )
                                ),
                                SubmissionGradeQuery.Edge(
                                    node = SubmissionGradeQuery.Node(
                                        _id = "456",
                                        id = "anotherCustomStatusId",
                                        color = "#00FF00",
                                        name = "Another Custom Status"
                                    )
                                )
                            ),
                            pageInfo = SubmissionGradeQuery.PageInfo(
                                hasNextPage = false,
                                endCursor = null
                            )
                        ),
                        gradingStandard = SubmissionGradeQuery.GradingStandard1(
                            data = listOf(
                                SubmissionGradeQuery.Data2(
                                    baseValue = 0.90,
                                    letterGrade = "A"
                                ),
                                SubmissionGradeQuery.Data2(
                                    baseValue = 0.80,
                                    letterGrade = "B"
                                )
                            ),
                        ),
                        gradeStatuses = CourseGradeStatus.entries.filter { it != CourseGradeStatus.UNKNOWN__ }
                    )
                )
            )
        )
    }

    @Test
    fun `init loading error`() = runTest {
        coEvery { repository.getSubmissionGrade(any(), any(), any()) } throws Exception("Network error")

        createViewModel()

        val uiState = viewModel.uiState.first()

        assertEquals(true, uiState.error)
        assertEquals(false, uiState.loading)
        assertNotNull(uiState.retryAction)

        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns createMockSubmission()

        uiState.retryAction?.invoke()
        val updatedUiState = viewModel.uiState.first()

        assertEquals(false, updatedUiState.error)
        assertEquals(false, updatedUiState.loading)
        assertEquals(100.0, updatedUiState.pointsPossible)
        assertEquals(95.0, updatedUiState.score)
    }

    @Test
    fun `on score changed error`() = runTest {
        val submission = createMockSubmission()

        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns submission
        createViewModel()

        val uiState = viewModel.uiState.first()

        assertEquals(95.0, uiState.score)
        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns submission.copy(
            submission = submission.submission?.copy(enteredScore = 90.0)
        )
        coEvery {
            repository.updateSubmissionGrade(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws Exception("Network error")

        uiState.onScoreChange(90f)
        testDispatcher.scheduler.advanceTimeBy(600)
        coVerify {
            errorHandler.postError(any(), any())
            repository.updateSubmissionGrade(
                "90.0",
                userId = studentId,
                assignmentId = assignmentId,
                courseId = courseId,
                excused = false
            )
        }
        val updatedUiState = viewModel.uiState.first()

        coEvery { repository.updateSubmissionGrade(any(), any(), any(), any(), any()) } returns mockk()
        updatedUiState.retryAction?.invoke()
        testDispatcher.scheduler.advanceTimeBy(600)

        coVerify {
            repository.updateSubmissionGrade(
                "90.0",
                userId = studentId,
                assignmentId = assignmentId,
                courseId = courseId,
                excused = false
            )
        }
        val finalUiState = viewModel.uiState.first()
        assertEquals(90.0f, finalUiState.enteredScore)
    }

    @Test
    fun `on excuse error`() = runTest {
        val submission = createMockSubmission()

        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns submission
        createViewModel()

        val uiState = viewModel.uiState.first()

        assertEquals(false, uiState.excused)

        coEvery { repository.excuseSubmission(any(), any(), any()) } throws Exception("Network error")

        uiState.onExcuse()
        coVerify {
            repository.excuseSubmission(studentId, assignmentId, courseId)
        }

        val updatedUiState = viewModel.uiState.first()
        assertEquals(true, updatedUiState.error)
        assertNotNull(updatedUiState.retryAction)

        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns submission.copy(
            submission = submission.submission?.copy(excused = true)
        )
        coEvery { repository.excuseSubmission(any(), any(), any()) } returns mockk()
        updatedUiState.retryAction?.invoke()

        coVerify {
            repository.excuseSubmission(studentId, assignmentId, courseId)
        }

        val finalUiState = viewModel.uiState.first()
        assertEquals(false, finalUiState.error)
        assertEquals(true, finalUiState.excused)
    }

    @Test
    fun `on status change error`() = runTest {
        val submission = createMockSubmission()

        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns submission
        createViewModel()

        val uiState = viewModel.uiState.first()

        assertEquals("graded", uiState.gradingStatus)

        val newStatus = GradeStatus(123L, null, "Custom Status")
        coEvery {
            repository.updateSubmissionStatus(
                any(),
                any(),
                any()
            )
        } throws Exception("Network error")

        uiState.onStatusChange(newStatus)
        coVerify {
            repository.updateSubmissionStatus(
                submissionId = submission.submission?._id?.toLong() ?: 0L,
                customStatusId = newStatus.id?.toString(),
                latePolicyStatus = null
            )
        }

        val updatedUiState = viewModel.uiState.first()
        assertEquals(true, updatedUiState.error)
        assertNotNull(updatedUiState.retryAction)

        coEvery {
            repository.updateSubmissionStatus(
                any(),
                any(),
                any()
            )
        } returns UpdateSubmissionStatusMutation.Data(
            updateSubmissionGradeStatus = UpdateSubmissionStatusMutation.UpdateSubmissionGradeStatus(
                submission = UpdateSubmissionStatusMutation.Submission(
                    status = "Custom Status",
                    score = 95.0,
                    grade = "A",
                    enteredGrade = "A",
                    enteredScore = 95.0,
                    secondsLate = 0.0,
                    deductedPoints = 0.0,
                    excused = false,
                    late = false
                )
            )
        )

        updatedUiState.retryAction?.invoke()

        coVerify {
            repository.updateSubmissionStatus(
                submissionId = submission.submission?._id?.toLong() ?: 0L,
                customStatusId = newStatus.id?.toString(),
                latePolicyStatus = null
            )
        }

        val finalUiState = viewModel.uiState.first()
        assertEquals(false, finalUiState.error)
        assertEquals("Custom Status", finalUiState.gradingStatus)
    }

    @Test
    fun `update screen on rubric event`() = runTest {
        val submission = createMockSubmission()

        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns submission
        createViewModel()

        val uiState = viewModel.uiState.first()

        assertEquals("graded", uiState.gradingStatus)

        val updatedSubmission = submission.copy(
            submission = submission.submission?.copy(
                status = "missing"
            )
        )

        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns updatedSubmission
        gradingEventHandler.postEvent(GradingEvent.RubricUpdated)

        assertEquals("missing", viewModel.uiState.first().gradingStatus)
    }

    @Test
    fun `late days changed`() = runTest {
        val submission = createMockSubmission()

        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns submission.copy(
            submission = submission.submission?.copy(secondsLate = 86400.0)
        )
        createViewModel()

        val uiState = viewModel.uiState.first()

        assertEquals(1f, uiState.daysLate)

        coEvery {
            repository.updateLateSecondsOverride(
                any(),
                any(),
                any(),
                any()
            )
        } returns mockk()

        coEvery { repository.getSubmissionGrade(any(), any(), any()) } returns submission.copy(
            submission = submission.submission?.copy(secondsLate = 2 * 86400.0)
        )

        uiState.onLateDaysChange(2f)
        testDispatcher.scheduler.advanceTimeBy(600)
        coVerify {
            repository.updateLateSecondsOverride(
                userId = studentId,
                assignmentId = assignmentId,
                courseId = courseId,
                lateSeconds = 2 * 86400
            )
        }
    }
}
