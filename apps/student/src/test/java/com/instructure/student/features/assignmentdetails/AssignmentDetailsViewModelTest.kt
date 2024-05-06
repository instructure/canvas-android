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

package com.instructure.student.features.assignmentdetails

import android.app.Application
import android.content.res.Resources
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.CourseSettings
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.room.appdatabase.entities.ReminderEntity
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.student.db.StudentDb
import com.instructure.student.features.assignments.details.AssignmentDetailAction
import com.instructure.student.features.assignments.details.AssignmentDetailsRepository
import com.instructure.student.features.assignments.details.AssignmentDetailsViewModel
import com.instructure.student.features.assignments.details.ReminderChoice
import com.instructure.student.features.assignments.details.ReminderViewData
import com.instructure.student.features.assignments.details.gradecellview.GradeCellViewData
import com.instructure.student.features.assignments.reminder.AlarmScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import java.util.Date

@ExperimentalCoroutinesApi
class AssignmentDetailsViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val lifecycleOwner: LifecycleOwner = mockk(relaxed = true)
    private val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)

    private val testDispatcher = TestCoroutineDispatcher()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val assignmentDetailsRepository: AssignmentDetailsRepository = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val htmlContentFormatter: HtmlContentFormatter = mockk(relaxed = true)
    private val colorKeeper: ColorKeeper = mockk(relaxed = true)
    private val application: Application = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val database: StudentDb = mockk(relaxed = true)
    private val alarmScheduler: AlarmScheduler = mockk(relaxed = true)

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

        every { assignmentDetailsRepository.getRemindersByAssignmentIdLiveData(any(), any()) } returns MutableLiveData()
        every { apiPrefs.user } returns User(id = 1)
    }

    fun tearDown() {
        unmockkAll()
    }

    private fun getViewModel() = AssignmentDetailsViewModel(
        savedStateHandle,
        assignmentDetailsRepository,
        resources,
        htmlContentFormatter,
        colorKeeper,
        application,
        apiPrefs,
        alarmScheduler,
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

        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } throws IllegalStateException()

        val viewModel = getViewModel()

        Assert.assertEquals(ViewState.Error(expected), viewModel.state.value)
        Assert.assertEquals(expected, (viewModel.state.value as? ViewState.Error)?.errorMessage)
    }

    @Test
    fun `Authentication error`() {
        val generalError = "There was a problem loading this assignment. Please check your connection and try again."
        val authError = "This assignment is no longer available."

        every { resources.getString(R.string.errorLoadingAssignment) } returns generalError
        every { resources.getString(R.string.assignmentNoLongerAvailable) } returns authError

        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } throws IllegalAccessException()

        val viewModel = getViewModel()

        Assert.assertEquals(ViewState.Error(authError), viewModel.state.value)
        Assert.assertEquals(authError, (viewModel.state.value as? ViewState.Error)?.errorMessage)
    }

    @Test
    fun `Load fully locked assignment`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(lockInfo = LockInfo(unlockAt = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.time.toApiString()))
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(true, viewModel.data.value?.fullLocked)
    }

    @Test
    fun `Load partially locked assignment`() {
        val lockedExplanation = "locked"

        every { resources.getString(R.string.errorLoadingAssignment) } returns lockedExplanation

        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(
            lockExplanation = lockedExplanation,
            lockAt = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.time.toApiString()
        )
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(false, viewModel.data.value?.fullLocked)
        Assert.assertEquals(lockedExplanation, viewModel.data.value?.lockedMessage)
    }

    @Test
    fun `Load assignment as Student`() {
        val expected = "Test name"

        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(name = expected)
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(expected, viewModel.data.value?.assignmentName)
    }

    @Test
    fun `Load assignment as Parent`() {
        val expected = "Test name"

        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Observer)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(name = expected)
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(expected, viewModel.data.value?.assignmentName)
    }

    @Test
    fun `Load assignment with draft`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns Assignment()

        every {
            database.submissionQueries.getSubmissionsByAssignmentId(any(), any()).executeAsList()
        } returns listOf(getDbSubmission().copy(isDraft = true))

        val viewModel = getViewModel()

        Assert.assertEquals(ViewState.Success, viewModel.state.value)
        Assert.assertEquals(true, viewModel.data.value?.hasDraft)
    }

    @Test
    fun `Map attempts`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(
            submission = Submission(
                submissionHistory = listOf(
                    Submission(submittedAt = Date()),
                    Submission(submittedAt = Date()),
                    Submission(submittedAt = Date())
                )
            )
        )
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        Assert.assertEquals(3, viewModel.data.value?.attempts?.size)
    }

    @Test
    fun `Map grade cell`() {
        val expectedGradeCell = GradeCellViewData.fromSubmission(
            resources,
            colorKeeper.getOrGenerateColor(Course()),
            Assignment(),
            Submission(),
            false
        )

        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(submission = Submission())
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        Assert.assertEquals(expectedGradeCell, viewModel.data.value?.selectedGradeCellViewData)
        Assert.assertEquals(GradeCellViewData.State.EMPTY, viewModel.data.value?.selectedGradeCellViewData?.state)
    }

    @Test
    fun `Missing submission`() {
        val expectedLabelText = "Missing"
        val expectedTint = R.color.textDanger
        val expectedIcon = R.drawable.ic_no

        every { resources.getString(R.string.missingAssignment) } returns expectedLabelText

        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(
            submission = Submission(missing = true),
            submissionTypesRaw = listOf("media_recording")
        )
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        Assert.assertEquals(expectedLabelText, viewModel.data.value?.submissionStatusText)
        Assert.assertEquals(expectedTint, viewModel.data.value?.submissionStatusTint)
        Assert.assertEquals(expectedIcon, viewModel.data.value?.submissionStatusIcon)
        Assert.assertEquals(true, viewModel.data.value?.submissionStatusVisible)
    }

    @Test
    fun `Not submitted submission`() {
        val expectedLabelText = "Not submitted"
        val expectedTint = R.color.textDark
        val expectedIcon = R.drawable.ic_no

        every { resources.getString(R.string.notSubmitted) } returns expectedLabelText

        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(submissionTypesRaw = listOf("media_recording"))
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        Assert.assertEquals(expectedLabelText, viewModel.data.value?.submissionStatusText)
        Assert.assertEquals(expectedTint, viewModel.data.value?.submissionStatusTint)
        Assert.assertEquals(expectedIcon, viewModel.data.value?.submissionStatusIcon)
        Assert.assertEquals(true, viewModel.data.value?.submissionStatusVisible)
    }

    @Test
    fun `Graded submission`() {
        val expectedLabelText = "Graded"
        val expectedTint = R.color.textSuccess
        val expectedIcon = R.drawable.ic_complete_solid

        every { resources.getString(R.string.gradedSubmissionLabel) } returns expectedLabelText

        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(
            submission = Submission(
                submittedAt = Date(),
                grade = "A",
                postedAt = Date()
            )
        )
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        Assert.assertEquals(expectedLabelText, viewModel.data.value?.submissionStatusText)
        Assert.assertEquals(expectedTint, viewModel.data.value?.submissionStatusTint)
        Assert.assertEquals(expectedIcon, viewModel.data.value?.submissionStatusIcon)
        Assert.assertEquals(true, viewModel.data.value?.submissionStatusVisible)
    }

    @Test
    fun `Submitted submission`() {
        val expectedLabelText = "Submitted"
        val expectedTint = R.color.textSuccess
        val expectedIcon = R.drawable.ic_complete_solid

        every { resources.getString(R.string.submitted) } returns expectedLabelText

        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(
            submissionTypesRaw = listOf("media_recording"),
            submission = Submission(submittedAt = Date())
        )
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

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
            firstSubmission,
            false
        )

        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()
        viewModel.onAttemptSelected(2)

        Assert.assertEquals(3, viewModel.data.value?.attempts?.size)
        Assert.assertEquals(expectedGradeCellViewData, viewModel.data.value?.selectedGradeCellViewData)
    }

    @Test
    fun `LTI button click`() {
        val expected = "test"

        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns Assignment()

        val viewModel = getViewModel()
        viewModel.onLtiButtonPressed(expected)

        Assert.assertEquals(expected, (viewModel.events.value?.peekContent() as AssignmentDetailAction.NavigateToLtiScreen).url)
    }

    @Test
    fun `Grade cell click`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns Assignment()

        val viewModel = getViewModel()
        viewModel.onGradeCellClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.NavigateToSubmissionScreen)
    }

    @Test
    fun `Grade cell click while uploading text`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns Assignment()

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
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns Assignment()

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
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns Assignment()

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
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(submissionTypesRaw = listOf("online_quiz"), quizId = 1)
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        coEvery { assignmentDetailsRepository.getQuiz(any(), any(), any()) } returns Quiz()

        val viewModel = getViewModel()
        viewModel.onSubmitButtonClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.NavigateToQuizScreen)
    }

    @Test
    fun `Submit button click - discussion`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(submissionTypesRaw = listOf("discussion_topic"))
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()
        viewModel.onSubmitButtonClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.NavigateToDiscussionScreen)
    }

    @Test
    fun `Submit button click - multiple submission types`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(
            submissionTypesRaw = listOf(
                "online_text_entry",
                "online_url",
                "media_recording"
            )
        )
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()
        viewModel.onSubmitButtonClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.ShowSubmitDialog)
    }

    @Test
    fun `Submit button click - text`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(submissionTypesRaw = listOf("online_text_entry"))
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()
        viewModel.onSubmitButtonClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.NavigateToTextEntryScreen)
    }

    @Test
    fun `Submit button click - url`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(submissionTypesRaw = listOf("online_url"))
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()
        viewModel.onSubmitButtonClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.NavigateToUrlSubmissionScreen)
    }

    @Test
    fun `Submit button click - annotation`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(submissionTypesRaw = listOf("student_annotation"))
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()
        viewModel.onSubmitButtonClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.NavigateToAnnotationSubmissionScreen)
    }

    @Test
    fun `Submit button click - media recoding`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(submissionTypesRaw = listOf("media_recording"))
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()
        viewModel.onSubmitButtonClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.ShowMediaDialog)
    }

    @Test
    fun `Submit button click - external tool`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(submissionTypesRaw = listOf("external_tool"))
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()
        viewModel.onSubmitButtonClicked()

        Assert.assertTrue(viewModel.events.value?.peekContent() is AssignmentDetailAction.NavigateToLtiLaunchScreen)
    }

    @Test
    fun `Upload fail`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns Assignment()

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

        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns Assignment()

        every {
            database.submissionQueries.getSubmissionsByAssignmentId(any(), any()).executeAsList()
        } returns listOf(getDbSubmission())

        val viewModel = getViewModel()
        viewModel.queryResultsChanged()

        Assert.assertTrue(viewModel.data.value?.attempts?.first()?.data?.isUploading!!)

        every {
            database.submissionQueries.getSubmissionsByAssignmentId(any(), any()).executeAsList()
        } returns listOf()

        val assignment = Assignment(submission = Submission(submissionHistory = listOf(expected)))
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        viewModel.queryResultsChanged()

        Assert.assertEquals(expected, viewModel.data.value?.attempts?.last()?.data?.submission)
    }

    @Test
    fun `Create viewData with points when quantitative data is not restricted`() {
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns Course(
            enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student))
        )

        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns Assignment(submission = Submission(), pointsPossible = 20.0)

        every { resources.getQuantityString(any(), any(), any()) } returns "20 pts"

        val viewModel = getViewModel()

        Assert.assertEquals("20 pts", viewModel.data.value?.points)
    }

    @Test
    fun `Create viewData without points when quantitative data is restricted`() {
        val courseSettings = CourseSettings(restrictQuantitativeData = true)
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns Course(
            enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)), settings = courseSettings
        )

        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns Assignment(submission = Submission(), pointsPossible = 20.0)

        every { resources.getQuantityString(any(), any(), any()) } returns "20 pts"

        val viewModel = getViewModel()

        Assert.assertEquals("", viewModel.data.value?.points)
    }

    @Test
    fun `Do not show content if assignment is null`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } throws IllegalStateException()

        val viewModel = getViewModel()

        Assert.assertFalse(viewModel.showContent(viewModel.state.value))
    }

    @Test
    fun `Show content on assignment success`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns Assignment(submission = Submission(), pointsPossible = 20.0)

        val viewModel = getViewModel()

        Assert.assertTrue(viewModel.showContent(viewModel.state.value))
    }

    @Test
    fun `Submit button is not visible when loaded as Observer`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Observer)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(name = "Test", submissionTypesRaw = listOf("online_text_entry"))
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        Assert.assertFalse(viewModel.data.value?.submitVisible!!)
    }

    @Test
    fun `Submit button is not visible when not between valid date range`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)), accessRestrictedByDate = true)
        every { savedStateHandle.get<Course>(Const.CANVAS_CONTEXT) } returns course
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(name = "Test", submissionTypesRaw = listOf("online_text_entry"))
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        Assert.assertFalse(viewModel.data.value?.submitVisible!!)
    }

    @Test
    fun `Submit button is not visible when excused`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(name = "Test", submissionTypesRaw = listOf("online_text_entry"), submission = Submission(excused = true))
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        Assert.assertFalse(viewModel.data.value?.submitVisible!!)
    }

    @Test
    fun `Reminder section is not visible if there's no future deadline`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(name = "Test", submissionTypesRaw = listOf("online_text_entry"))
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        Assert.assertFalse(viewModel.data.value?.showReminders!!)
    }

    @Test
    fun `Reminder section visible if there's a future deadline`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(
            name = "Test",
            submissionTypesRaw = listOf("online_text_entry"),
            dueAt = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.time.toApiString()
        )
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        Assert.assertTrue(viewModel.data.value?.showReminders!!)
    }

    @Test
    fun `Reminders map correctly`() {
        val reminderEntities = listOf(
            ReminderEntity(1, 1, 1, "htmlUrl1", "Assignment 1", "1 day", 1000),
            ReminderEntity(2, 1, 1, "htmlUrl2", "Assignment 2", "2 days", 2000),
            ReminderEntity(3, 1, 1, "htmlUrl3", "Assignment 3", "3 days", 3000)
        )
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course
        every { assignmentDetailsRepository.getRemindersByAssignmentIdLiveData(any(), any()) } returns MutableLiveData(reminderEntities)
        every { resources.getString(eq(R.string.reminderBefore), any()) } answers { call -> "${(call.invocation.args[1] as Array<*>)[0]} Before" }

        val assignment = Assignment(
            name = "Test",
            submissionTypesRaw = listOf("online_text_entry"),
            dueAt = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.time.toApiString()
        )
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        Assert.assertEquals(
            reminderEntities.map { ReminderViewData(it.id, "${it.text} Before") },
            viewModel.data.value?.reminders?.map { it.data }
        )
    }

    @Test
    fun `Reminders update correctly`() {
        val remindersLiveData = MutableLiveData<List<ReminderEntity>>()
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course
        every { assignmentDetailsRepository.getRemindersByAssignmentIdLiveData(any(), any()) } returns remindersLiveData
        every { resources.getString(eq(R.string.reminderBefore), any()) } answers { call -> "${(call.invocation.args[1] as Array<*>)[0]} Before" }

        val assignment = Assignment(
            name = "Test",
            submissionTypesRaw = listOf("online_text_entry"),
            dueAt = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.time.toApiString()
        )
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        Assert.assertEquals(0, viewModel.data.value?.reminders?.size)

        remindersLiveData.value = listOf(ReminderEntity(1, 1, 1, "htmlUrl1", "Assignment 1", "1 day", 1000))

        Assert.assertEquals(ReminderViewData(1, "1 day Before"), viewModel.data.value?.reminders?.first()?.data)
    }

    @Test
    fun `Add reminder posts action`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(
            name = "Test",
            submissionTypesRaw = listOf("online_text_entry"),
            dueAt = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.time.toApiString()
        )
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        viewModel.onAddReminderClicked()

        Assert.assertEquals(AssignmentDetailAction.ShowReminderDialog, viewModel.events.value?.peekContent())
    }

    @Test
    fun `Selected reminder choice`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course
        every { savedStateHandle.get<Long>(Const.ASSIGNMENT_ID) } returns 1
        every { resources.getQuantityString(R.plurals.reminderDay, 3, 3) } returns "3 days"

        val assignment = Assignment(
            name = "Test",
            submissionTypesRaw = listOf("online_text_entry"),
            dueAt = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 4) }.time.toApiString()
        )
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        viewModel.onReminderSelected(ReminderChoice.Day(3))

        val time = assignment.dueDate?.time?.minus(3 * 24 * 60 * 60 * 1000L)

        coVerify(exactly = 1) {
            assignmentDetailsRepository.addReminder(1, assignment, "3 days", time!!)
        }
    }

    @Test
    fun `Selected reminder choice custom`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course

        val assignment = Assignment(
            name = "Test",
            submissionTypesRaw = listOf("online_text_entry"),
            dueAt = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }.time.toApiString()
        )
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        viewModel.onReminderSelected(ReminderChoice.Custom)

        Assert.assertEquals(AssignmentDetailAction.ShowCustomReminderDialog, viewModel.events.value?.peekContent())
    }

    @Test
    fun `Selected past reminder choice`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course
        every { savedStateHandle.get<Long>(Const.ASSIGNMENT_ID) } returns 1
        every { resources.getQuantityString(R.plurals.reminderDay, 3, 3) } returns "3 days"
        every { resources.getString(R.string.reminderInPast) } returns "Reminder in past"

        val assignment = Assignment(
            name = "Test",
            submissionTypesRaw = listOf("online_text_entry"),
            dueAt = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 2) }.time.toApiString()
        )
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment

        val viewModel = getViewModel()

        viewModel.onReminderSelected(ReminderChoice.Day(3))

        Assert.assertEquals(AssignmentDetailAction.ShowToast("Reminder in past"), viewModel.events.value?.peekContent())
    }

    @Test
    fun `Selected reminder already set up`() {
        val course = Course(enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))
        coEvery { assignmentDetailsRepository.getCourseWithGrade(any(), any()) } returns course
        every { savedStateHandle.get<Long>(Const.ASSIGNMENT_ID) } returns 1
        every { resources.getQuantityString(R.plurals.reminderDay, 3, 3) } returns "3 days"
        every { resources.getString(R.string.reminderAlreadySet) } returns "Reminder in past"
        val assignment = Assignment(
            name = "Test",
            submissionTypesRaw = listOf("online_text_entry"),
            dueAt = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 4) }.time.toApiString()
        )
        coEvery { assignmentDetailsRepository.getAssignment(any(), any(), any(), any()) } returns assignment
        val time = assignment.dueDate?.time?.minus(3 * 24 * 60 * 60 * 1000L)
        val reminderEntities = listOf(
            ReminderEntity(1, 1, 1, "htmlUrl1", "Assignment 1", "1 day", time!!)
        )
        every { assignmentDetailsRepository.getRemindersByAssignmentIdLiveData(any(), any()) } returns MutableLiveData(reminderEntities)

        val viewModel = getViewModel()

        viewModel.onReminderSelected(ReminderChoice.Day(3))

        Assert.assertEquals(AssignmentDetailAction.ShowToast("Reminder in past"), viewModel.events.value?.peekContent())
    }
}
