/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.moduleitemsequence.content.assignment

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.domain.usecase.GetAssignmentDetailsUseCase
import com.instructure.horizon.domain.usecase.GetSubmissionHistoryUseCase
import com.instructure.horizon.domain.usecase.GetUnreadCommentsCountUseCase
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
import com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.HorizonSubmissionHelper
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.SubmissionState
import com.instructure.pandautils.room.studentdb.entities.daos.CreateFileSubmissionDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AssignmentDetailsViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val getAssignmentDetailsUseCase: GetAssignmentDetailsUseCase = mockk(relaxed = true)
    private val getSubmissionHistoryUseCase: GetSubmissionHistoryUseCase = mockk(relaxed = true)
    private val getUnreadCommentsCountUseCase: GetUnreadCommentsCountUseCase = mockk(relaxed = true)
    private val htmlContentFormatter: HtmlContentFormatter = mockk(relaxed = true)
    private val oAuthApi: OAuthAPI.OAuthInterface = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val aiAssistContextProvider: AiAssistContextProvider = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val createSubmissionDao: CreateSubmissionDao = mockk(relaxed = true)
    private val createFileSubmissionDao: CreateFileSubmissionDao = mockk(relaxed = true)
    private val submissionHelper: HorizonSubmissionHelper = mockk(relaxed = true)
    private val textSubmissionFlow = MutableStateFlow<CreateSubmissionEntity?>(null)
    private val fileSubmissionFlow = MutableStateFlow<CreateSubmissionEntity?>(null)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val courseId = 1L
    private val assignmentId = 100L

    private val testAssignment = Assignment(
        id = assignmentId,
        name = "Test Assignment",
        pointsPossible = 100.0,
        description = "Test description",
        allowedAttempts = 3L
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { getAssignmentDetailsUseCase(any()) } returns testAssignment
        coEvery { getSubmissionHistoryUseCase(any()) } returns emptyList()
        coEvery { getUnreadCommentsCountUseCase(any()) } returns 0
        coEvery { htmlContentFormatter.formatHtmlWithIframes(any(), any()) } returns "Formatted content"
        coEvery { aiAssistContextProvider.aiAssistContext } returns mockk(relaxed = true)
        coEvery { aiAssistContextProvider.aiAssistContext = any() } returns Unit
        coEvery { networkStateProvider.isOnline() } returns true
        every {
            createSubmissionDao.findSubmissionByAssignmentIdAndTypeFlow(
                any(), any(), Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString
            )
        } returns textSubmissionFlow
        every {
            createSubmissionDao.findSubmissionByAssignmentIdAndTypeFlow(
                any(), any(), Assignment.SubmissionType.ONLINE_UPLOAD.apiString
            )
        } returns fileSubmissionFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test data loads with assignmentId and courseId`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))

        val viewModel = getViewModel(savedStateHandle)

        assertFalse(viewModel.uiState.value.loadingState.isLoading)
        coVerify { getAssignmentDetailsUseCase(GetAssignmentDetailsUseCase.Params(courseId, assignmentId, false)) }
    }

    @Test
    fun `Test assignment is loaded successfully`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))

        val viewModel = getViewModel(savedStateHandle)

        assertNotNull(viewModel.assignmentFlow.value)
        assertEquals(testAssignment, viewModel.assignmentFlow.value)
    }

    @Test
    fun `Test failed data load sets error state`() = runTest {
        coEvery { getAssignmentDetailsUseCase(any()) } throws Exception("Network error")

        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))

        val viewModel = getViewModel(savedStateHandle)

        assertFalse(viewModel.uiState.value.loadingState.isLoading)
    }

    @Test
    fun `Test unread comments check is performed`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))

        val viewModel = getViewModel(savedStateHandle)

        coVerify { getUnreadCommentsCountUseCase(any()) }
    }

    @Test
    fun `Test unread comments flag is set correctly`() = runTest {
        coEvery { getUnreadCommentsCountUseCase(any()) } returns 1

        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))

        val viewModel = getViewModel(savedStateHandle)

        assertTrue(viewModel.uiState.value.toolsBottomSheetUiState.hasUnreadComments)
    }

    @Test
    fun `Test assignment with no submission shows add submission`() = runTest {
        val assignmentWithoutSubmission = testAssignment.copy(submission = null)
        coEvery { getAssignmentDetailsUseCase(any()) } returns assignmentWithoutSubmission

        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))

        val viewModel = getViewModel(savedStateHandle)

        assertTrue(viewModel.uiState.value.showAddSubmission)
        assertFalse(viewModel.uiState.value.showSubmissionDetails)
    }

    @Test
    fun `Test assignment with submission shows submission details`() = runTest {
        val submission = Submission(attempt = 1L, workflowState = "submitted")
        coEvery { getSubmissionHistoryUseCase(any()) } returns listOf(submission)

        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))

        val viewModel = getViewModel(savedStateHandle)

        assertTrue(viewModel.uiState.value.showSubmissionDetails)
    }

    @Test
    fun `Test HTML content formatting is applied to description`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))

        val viewModel = getViewModel(savedStateHandle)

        coVerify { htmlContentFormatter.formatHtmlWithIframes(testAssignment.description.orEmpty(), courseId) }
        assertEquals("Formatted content", viewModel.uiState.value.instructions)
    }

    @Test
    fun `Test attempt selector visibility for multiple attempts`() = runTest {
        val assignmentWithMultipleAttempts = testAssignment.copy(allowedAttempts = 3L)
        coEvery { getAssignmentDetailsUseCase(any()) } returns assignmentWithMultipleAttempts

        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))

        val viewModel = getViewModel(savedStateHandle)

        assertTrue(viewModel.uiState.value.toolsBottomSheetUiState.showAttemptSelector)
    }

    @Test
    fun `Test attempt selector is hidden for single attempt assignment`() = runTest {
        val assignmentWithSingleAttempt = testAssignment.copy(allowedAttempts = 1L)
        coEvery { getAssignmentDetailsUseCase(any()) } returns assignmentWithSingleAttempt

        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))

        val viewModel = getViewModel(savedStateHandle)

        assertFalse(viewModel.uiState.value.toolsBottomSheetUiState.showAttemptSelector)
    }

    @Test
    fun `Test opening assignment tools updates UI state`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))

        val viewModel = getViewModel(savedStateHandle)

        viewModel.openAssignmentTools()

        assertTrue(viewModel.uiState.value.toolsBottomSheetUiState.show)
    }

    @Test
    fun `Pending submission reflects an active scheduled submission`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))
        val viewModel = getViewModel(savedStateHandle)

        textSubmissionFlow.value = CreateSubmissionEntity(
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(courseId),
            userId = 1L,
            isDraft = false,
            submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
            submissionState = SubmissionState.QUEUED,
        )
        testScheduler.advanceUntilIdle()

        assertEquals(SubmissionState.QUEUED, viewModel.uiState.value.pendingSubmission?.state)
    }

    @Test
    fun `A draft submission is not treated as pending`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))
        val viewModel = getViewModel(savedStateHandle)

        textSubmissionFlow.value = CreateSubmissionEntity(
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(courseId),
            userId = 1L,
            isDraft = true,
            submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
            submissionState = SubmissionState.QUEUED,
        )
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.pendingSubmission)
    }

    @Test
    fun `Pending submission clears when the submission row is removed`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))
        val viewModel = getViewModel(savedStateHandle)

        textSubmissionFlow.value = CreateSubmissionEntity(
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(courseId),
            userId = 1L,
            isDraft = false,
            submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
            submissionState = SubmissionState.SUBMITTING,
        )
        testScheduler.advanceUntilIdle()
        assertEquals(SubmissionState.SUBMITTING, viewModel.uiState.value.pendingSubmission?.state)

        textSubmissionFlow.value = null
        testScheduler.advanceUntilIdle()
        assertNull(viewModel.uiState.value.pendingSubmission)
    }

    @Test
    fun `Offline scheduled submission is shown as a queued attempt`() = runTest {
        coEvery { networkStateProvider.isOnline() } returns false
        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))
        val viewModel = getViewModel(savedStateHandle)

        textSubmissionFlow.value = CreateSubmissionEntity(
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(courseId),
            userId = 1L,
            isDraft = false,
            submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
            submissionState = SubmissionState.QUEUED,
        )
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1L, state.submissionDetailsUiState.pendingAttemptNumber)
        assertEquals(1L, state.submissionDetailsUiState.currentSubmissionAttempt)
        assertTrue(state.showSubmissionDetails)
        assertFalse(state.showAddSubmission)
        assertEquals(SubmissionState.QUEUED, state.pendingSubmission?.state)
        val queuedCard = state.attemptSelectorUiState.attempts.first()
        assertEquals(1L, queuedCard.attemptNumber)
        assertTrue(queuedCard.selected)
        assertNotNull(queuedCard.pillText)
    }

    @Test
    fun `Offline queued attempt is numbered after the last real attempt`() = runTest {
        coEvery { networkStateProvider.isOnline() } returns false
        coEvery { getSubmissionHistoryUseCase(any()) } returns listOf(Submission(attempt = 1L, workflowState = "submitted"))
        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))
        val viewModel = getViewModel(savedStateHandle)

        textSubmissionFlow.value = CreateSubmissionEntity(
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(courseId),
            userId = 1L,
            isDraft = false,
            submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
            submissionState = SubmissionState.QUEUED,
        )
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2L, state.submissionDetailsUiState.pendingAttemptNumber)
        assertEquals(2L, state.submissionDetailsUiState.currentSubmissionAttempt)
        // Queued attempt on top, followed by the real attempt.
        assertEquals(2L, state.attemptSelectorUiState.attempts[0].attemptNumber)
        assertEquals(1L, state.attemptSelectorUiState.attempts[1].attemptNumber)
    }

    @Test
    fun `Cancelling an offline queued attempt with no real submission returns to add submission`() = runTest {
        coEvery { networkStateProvider.isOnline() } returns false
        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))
        val viewModel = getViewModel(savedStateHandle)

        textSubmissionFlow.value = CreateSubmissionEntity(
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(courseId),
            userId = 1L,
            isDraft = false,
            submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
            submissionState = SubmissionState.QUEUED,
        )
        testScheduler.advanceUntilIdle()
        assertEquals(1L, viewModel.uiState.value.submissionDetailsUiState.pendingAttemptNumber)

        // Cancel reverts the entity to a draft, so it drops out of the scheduled flow.
        textSubmissionFlow.value = null
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.pendingSubmission)
        assertEquals(-1L, state.submissionDetailsUiState.pendingAttemptNumber)
        assertTrue(state.showAddSubmission)
        assertFalse(state.showSubmissionDetails)
    }

    @Test
    fun `Completed offline queued attempt silently refreshes without a confirmation modal`() = runTest {
        coEvery { networkStateProvider.isOnline() } returns false
        coEvery { getSubmissionHistoryUseCase(match { !it.forceRefresh }) } returns emptyList()
        coEvery { getSubmissionHistoryUseCase(match { it.forceRefresh }) } returns listOf(Submission(attempt = 1L, workflowState = "submitted"))
        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))
        val viewModel = getViewModel(savedStateHandle)

        val entity = CreateSubmissionEntity(
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(courseId),
            userId = 1L,
            isDraft = false,
            submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
            submissionState = SubmissionState.QUEUED,
        )
        textSubmissionFlow.value = entity
        testScheduler.advanceUntilIdle()
        assertEquals(1L, viewModel.uiState.value.submissionDetailsUiState.pendingAttemptNumber)

        textSubmissionFlow.value = entity.copy(submissionState = SubmissionState.COMPLETED)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.submissionConfirmationUiState.show)
        assertNull(state.pendingSubmission)
        assertEquals(-1L, state.submissionDetailsUiState.pendingAttemptNumber)
        assertTrue(state.showSubmissionDetails)
        coVerify { getSubmissionHistoryUseCase(match { it.forceRefresh }) }
    }

    @Test
    fun `Selecting an older real attempt while an offline attempt is queued shows the real attempt`() = runTest {
        coEvery { networkStateProvider.isOnline() } returns false
        coEvery { getSubmissionHistoryUseCase(any()) } returns listOf(Submission(attempt = 1L, workflowState = "submitted"))
        val savedStateHandle = SavedStateHandle(mapOf(
            ModuleItemContent.Assignment.ASSIGNMENT_ID to assignmentId,
            Const.COURSE_ID to courseId
        ))
        val viewModel = getViewModel(savedStateHandle)

        textSubmissionFlow.value = CreateSubmissionEntity(
            assignmentId = assignmentId,
            canvasContext = CanvasContext.emptyCourseContext(courseId),
            userId = 1L,
            isDraft = false,
            submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY.apiString,
            submissionState = SubmissionState.QUEUED,
        )
        testScheduler.advanceUntilIdle()
        assertEquals(2L, viewModel.uiState.value.submissionDetailsUiState.currentSubmissionAttempt)

        val realCard = viewModel.uiState.value.attemptSelectorUiState.attempts.first { it.attemptNumber == 1L }
        realCard.onClick?.invoke()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1L, state.submissionDetailsUiState.currentSubmissionAttempt)
        // The queued attempt is still pending, just not the one being viewed.
        assertNotNull(state.pendingSubmission)
        assertEquals(2L, state.submissionDetailsUiState.pendingAttemptNumber)
    }

    private fun getViewModel(savedStateHandle: SavedStateHandle): AssignmentDetailsViewModel {
        return AssignmentDetailsViewModel(
            context,
            getAssignmentDetailsUseCase,
            getSubmissionHistoryUseCase,
            getUnreadCommentsCountUseCase,
            htmlContentFormatter,
            oAuthApi,
            apiPrefs,
            aiAssistContextProvider,
            networkStateProvider,
            createSubmissionDao,
            createFileSubmissionDao,
            submissionHelper,
            savedStateHandle
        )
    }
}