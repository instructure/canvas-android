/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.student.features.assignmentdetail

import android.app.Application
import android.content.res.Resources
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.managers.AssignmentManager
import com.instructure.canvasapi2.managers.CourseManager
import com.instructure.canvasapi2.managers.QuizManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.student.db.StudentDb
import com.instructure.student.features.assignmentdetails.AssignmentDetailAction
import com.instructure.student.features.assignmentdetails.AssignmentDetailsViewModel
import com.instructure.student.features.assignmentdetails.gradecellview.GradeCellViewData
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
class AssignmentDetailsViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = TestCoroutineDispatcher()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val courseManager: CourseManager = mockk(relaxed = true)
    private val assignmentManager: AssignmentManager = mockk(relaxed = true)
    private val quizManager: QuizManager = mockk(relaxed = true)
    private val submissionManager: SubmissionManager = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val htmlContentFormatter: HtmlContentFormatter = mockk(relaxed = true)
    private val colorKeeper: ColorKeeper = mockk(relaxed = true)
    private val application: Application = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val database: StudentDb = mockk(relaxed = true)

    @Before
    fun setUp() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Dispatchers.setMain(testDispatcher)

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        ContextKeeper.appContext = mockk(relaxed = true)

        mockkStatic("kotlinx.coroutines.AwaitKt")

        every { savedStateHandle.get<Course>(Const.CANVAS_CONTEXT) } returns Course()
        every { savedStateHandle.get<Long>(Const.ASSIGNMENT_ID) } returns 0L
    }

    fun tearDown() {
        unmockkAll()
    }

    private fun getViewModel() = AssignmentDetailsViewModel(
        savedStateHandle,
        courseManager,
        assignmentManager,
        quizManager,
        submissionManager,
        resources,
        htmlContentFormatter,
        colorKeeper,
        application,
        apiPrefs,
        database
    )

    private fun getDbSubmission() = com.instructure.student.Submission(
        id = 0,
        submissionEntry = "",
        lastActivityDate = null,
        assignmentName = null,
        assignmentId = 0,
        canvasContext = CanvasContext.emptyCourseContext(0),
        submissionType = "",
        errorFlag = false,
        assignmentGroupCategoryId = null,
        userId = 0,
        currentFile = 0,
        fileCount = 0,
        progress = null,
        annotatableAttachmentId = null,
        isDraft = false
    )

    @Test
    fun `Load error`() {
        val expected = "There was a problem loading this assignment. Please check your connection and try again."

        every { resources.getString(R.string.errorLoadingAssignment) } returns expected

        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Fail()
        }

        val viewModel = getViewModel()

        Assert.assertEquals(ViewState.Error(expected), viewModel.state.value)
        Assert.assertEquals(expected, (viewModel.state.value as? ViewState.Error)?.errorMessage)
    }

    @Test
    fun `Load fully locked assignment`() {
        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(
                Assignment(
                    lockInfo = LockInfo(
                        unlockAt = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.time.toApiString()
                    )
                )
            )
        }

        val viewModel = getViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(true, viewModel.data.value?.fullLocked)
    }

    @Test
    fun `Load partially locked assignment`() {
        val lockedExplanation = "locked"

        every { resources.getString(R.string.errorLoadingAssignment) } returns lockedExplanation

        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(
                Assignment(
                    lockExplanation = lockedExplanation,
                    lockAt = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.time.toApiString()
                )
            )
        }

        val viewModel = getViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(false, viewModel.data.value?.fullLocked)
        Assert.assertEquals(lockedExplanation, viewModel.data.value?.lockedMessage)
    }

    @Test
    fun `Load assignment as Student`() {
        val expected = "Test name"

        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment(name = expected))
        }

        val viewModel = getViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(expected, viewModel.data.value?.assignmentName)
    }

    @Test
    fun `Load assignment as Parent`() {
        val expected = "Test name"

        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Observer))))
        }

        every { assignmentManager.getAssignmentIncludeObserveesAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(
                ObserveeAssignment(
                    submissionList = listOf(Submission()),
                    name = expected
                )
            )
        }

        val viewModel = getViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(expected, viewModel.data.value?.assignmentName)
    }

    @Test
    fun `Load assignment with draft`() {
        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment())
        }

        every {
            database.submissionQueries.getSubmissionsByAssignmentId(any(), any()).executeAsList()
        } returns listOf(getDbSubmission().copy(isDraft = true))

        val viewModel = getViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(true, viewModel.data.value?.hasDraft)
    }

    @Test
    fun `Map attempts`() {
        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(
                Assignment(
                    submission = Submission(
                        submissionHistory = listOf(
                            Submission(submittedAt = Date()),
                            Submission(submittedAt = Date()),
                            Submission(submittedAt = Date())
                        )
                    )
                )
            )
        }

        val viewModel = getViewModel()

        Assert.assertEquals(3, viewModel.data.value?.attempts?.size)
    }

    @Test
    fun `Map grade cell`() {
        val expectedGradeCell = GradeCellViewData.fromSubmission(
            resources,
            colorKeeper.getOrGenerateColor(Course()),
            Assignment(),
            Submission()
        )

        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment(submission = Submission()))
        }

        val viewModel = getViewModel()

        Assert.assertEquals(expectedGradeCell, viewModel.data.value?.selectedGradeCellViewData)
        Assert.assertEquals(GradeCellViewData.State.EMPTY, viewModel.data.value?.selectedGradeCellViewData?.state)
    }

    @Test
    fun `Missing submission`() {
        val expectedLabelText = "Missing"
        val expectedTint = R.color.backgroundDanger
        val expectedIcon = R.drawable.ic_no

        every { resources.getString(R.string.missingAssignment) } returns expectedLabelText

        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(
                Assignment(
                    submission = Submission(missing = true),
                    submissionTypesRaw = listOf("media_recording")
                )
            )
        }

        val viewModel = getViewModel()

        Assert.assertEquals(expectedLabelText, viewModel.data.value?.submissionStatusText)
        Assert.assertEquals(expectedTint, viewModel.data.value?.submissionStatusTint)
        Assert.assertEquals(expectedIcon, viewModel.data.value?.submissionStatusIcon)
        Assert.assertEquals(true, viewModel.data.value?.submissionStatusVisible)
    }

    @Test
    fun `Not submitted submission`() {
        val expectedLabelText = "Not submitted"
        val expectedTint = R.color.backgroundDark
        val expectedIcon = R.drawable.ic_no

        every { resources.getString(R.string.notSubmitted) } returns expectedLabelText

        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment(submissionTypesRaw = listOf("media_recording")))
        }

        val viewModel = getViewModel()

        Assert.assertEquals(expectedLabelText, viewModel.data.value?.submissionStatusText)
        Assert.assertEquals(expectedTint, viewModel.data.value?.submissionStatusTint)
        Assert.assertEquals(expectedIcon, viewModel.data.value?.submissionStatusIcon)
        Assert.assertEquals(true, viewModel.data.value?.submissionStatusVisible)
    }

    @Test
    fun `Graded submission`() {
        val expectedLabelText = "Graded"
        val expectedTint = R.color.backgroundSuccess
        val expectedIcon = R.drawable.ic_complete_solid

        every { resources.getString(R.string.gradedSubmissionLabel) } returns expectedLabelText

        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(
                Assignment(
                    submission = Submission(
                        submittedAt = Date(),
                        grade = "A",
                        postedAt = Date()
                    )
                )
            )
        }

        val viewModel = getViewModel()

        Assert.assertEquals(expectedLabelText, viewModel.data.value?.submissionStatusText)
        Assert.assertEquals(expectedTint, viewModel.data.value?.submissionStatusTint)
        Assert.assertEquals(expectedIcon, viewModel.data.value?.submissionStatusIcon)
        Assert.assertEquals(true, viewModel.data.value?.submissionStatusVisible)
    }

    @Test
    fun `Submitted submission`() {
        val expectedLabelText = "Submitted"
        val expectedTint = R.color.backgroundSuccess
        val expectedIcon = R.drawable.ic_complete_solid

        every { resources.getString(R.string.submitted) } returns expectedLabelText

        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(
                Assignment(
                    submissionTypesRaw = listOf("media_recording"),
                    submission = Submission(submittedAt = Date())
                )
            )
        }

        val viewModel = getViewModel()

        Assert.assertEquals(expectedLabelText, viewModel.data.value?.submissionStatusText)
        Assert.assertEquals(expectedTint, viewModel.data.value?.submissionStatusTint)
        Assert.assertEquals(expectedIcon, viewModel.data.value?.submissionStatusIcon)
        Assert.assertEquals(true, viewModel.data.value?.submissionStatusVisible)
    }

    @Test
    fun `Select submission attempt`() {
        val firstSubmission = Submission(submittedAt = Date(), grade = "A", postedAt = Date())
        val assignment = Assignment(
            submission = Submission(
                submissionHistory = listOf(
                    firstSubmission,
                    Submission(submittedAt = Date(), grade = "B", postedAt = Date()),
                    Submission(submittedAt = Date(), grade = "C", postedAt = Date()),
                    Submission(grade = "D"),
                )
            )
        )

        val expectedGradeCellViewData = GradeCellViewData.fromSubmission(
            resources,
            colorKeeper.getOrGenerateColor(Course()),
            assignment,
            firstSubmission
        )

        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(assignment)
        }

        val viewModel = getViewModel()
        viewModel.onAttemptSelected(2)

        Assert.assertEquals(3, viewModel.data.value?.attempts?.size)
        Assert.assertEquals(expectedGradeCellViewData, viewModel.data.value?.selectedGradeCellViewData)
    }

    @Test
    fun `LTI button click`() {
        val expected = "test"

        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment())
        }

        val viewModel = getViewModel()
        viewModel.onLtiButtonPressed(expected)

        Assert.assertEquals(expected, (viewModel.events.value?.peekContent() as AssignmentDetailAction.NavigateToLtiScreen).url)
    }

    @Test
    fun `Grade cell click`() {
        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment())
        }

        val viewModel = getViewModel()
        viewModel.onGradeCellClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.NavigateToSubmissionScreen)
    }

    @Test
    fun `Grade cell click while uploading text`() {
        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment())
        }

        every {
            database.submissionQueries.getSubmissionsByAssignmentId(any(), any()).executeAsList()
        } returns listOf(getDbSubmission().copy(submissionType = "online_text_entry"))

        val viewModel = getViewModel()
        viewModel.queryResultsChanged()
        viewModel.onGradeCellClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.NavigateToTextEntryScreen)
    }

    @Test
    fun `Grade cell click while uploading file`() {
        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment())
        }

        every {
            database.submissionQueries.getSubmissionsByAssignmentId(any(), any()).executeAsList()
        } returns listOf(getDbSubmission().copy(submissionType = "online_upload"))

        val viewModel = getViewModel()
        viewModel.queryResultsChanged()
        viewModel.onGradeCellClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.NavigateToUploadStatusScreen)
    }

    @Test
    fun `Grade cell click while uploading url`() {
        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment())
        }

        every {
            database.submissionQueries.getSubmissionsByAssignmentId(any(), any()).executeAsList()
        } returns listOf(getDbSubmission().copy(submissionType = "online_url"))

        val viewModel = getViewModel()
        viewModel.queryResultsChanged()
        viewModel.onGradeCellClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.NavigateToUrlSubmissionScreen)
    }

    @Test
    fun `Submit button click - quiz`() {
        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment(submissionTypesRaw = listOf("online_quiz"), quizId = 1))
        }

        every { quizManager.getQuizAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Quiz())
        }

        val viewModel = getViewModel()
        viewModel.onSubmitButtonClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.NavigateToQuizScreen)
    }

    @Test
    fun `Submit button click - discussion`() {
        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment(submissionTypesRaw = listOf("discussion_topic")))
        }

        val viewModel = getViewModel()
        viewModel.onSubmitButtonClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.NavigateToDiscussionScreen)
    }

    @Test
    fun `Submit button click - multiple submission types`() {
        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(
                Assignment(
                    submissionTypesRaw = listOf(
                        "online_text_entry",
                        "online_url",
                        "media_recording"
                    )
                )
            )
        }

        val viewModel = getViewModel()
        viewModel.onSubmitButtonClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.ShowSubmitDialog)
    }

    @Test
    fun `Submit button click - text`() {
        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment(submissionTypesRaw = listOf("online_text_entry")))
        }

        val viewModel = getViewModel()
        viewModel.onSubmitButtonClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.NavigateToTextEntryScreen)
    }

    @Test
    fun `Submit button click - url`() {
        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment(submissionTypesRaw = listOf("online_url")))
        }

        val viewModel = getViewModel()
        viewModel.onSubmitButtonClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.NavigateToUrlSubmissionScreen)
    }

    @Test
    fun `Submit button click - annotation`() {
        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment(submissionTypesRaw = listOf("student_annotation")))
        }

        val viewModel = getViewModel()
        viewModel.onSubmitButtonClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.NavigateToAnnotationSubmissionScreen)
    }

    @Test
    fun `Submit button click - media recoding`() {
        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment(submissionTypesRaw = listOf("media_recording")))
        }

        val viewModel = getViewModel()
        viewModel.onSubmitButtonClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.ShowMediaDialog)
    }

    @Test
    fun `Submit button click - external tool`() {
        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment(submissionTypesRaw = listOf("external_tool")))
        }

        val viewModel = getViewModel()
        viewModel.onSubmitButtonClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.NavigateToLtiLaunchScreen)
    }

    @Test
    fun `Upload fail`() {
        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment())
        }

        every {
            database.submissionQueries.getSubmissionsByAssignmentId(any(), any()).executeAsList()
        } returns listOf(getDbSubmission())

        val viewModel = getViewModel()
        viewModel.queryResultsChanged()

        Assert.assertTrue(viewModel.data.value?.attempts?.first()?.data?.isUploading!!)

        every {
            database.submissionQueries.getSubmissionsByAssignmentId(any(), any()).executeAsList()
        } returns listOf(getDbSubmission().copy(errorFlag = true))

        viewModel.queryResultsChanged()

        Assert.assertTrue(viewModel.data.value?.attempts?.first()?.data?.isFailed!!)
    }

    @Test
    fun `Upload success`() {
        val expected = Submission(submittedAt = Date())

        every { courseManager.getCourseWithGradeAsync(any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))))
        }

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment())
        }

        every {
            database.submissionQueries.getSubmissionsByAssignmentId(any(), any()).executeAsList()
        } returns listOf(getDbSubmission())

        val viewModel = getViewModel()
        viewModel.queryResultsChanged()

        Assert.assertTrue(viewModel.data.value?.attempts?.first()?.data?.isUploading!!)

        every {
            database.submissionQueries.getSubmissionsByAssignmentId(any(), any()).executeAsList()
        } returns listOf()

        every { assignmentManager.getAssignmentAsync(any(), any(), any()) } returns mockk {
            coEvery { await() } returns DataResult.Success(Assignment(submission = Submission(submissionHistory = listOf(expected))))
        }

        viewModel.queryResultsChanged()

        Assert.assertEquals(expected, viewModel.data.value?.attempts?.last()?.data?.submission)
    }
}
