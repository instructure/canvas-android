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
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.domain.usecase.GetAssignmentDetailsUseCase
import com.instructure.horizon.domain.usecase.GetSubmissionHistoryUseCase
import com.instructure.horizon.domain.usecase.GetUnreadCommentsCountUseCase
import com.instructure.horizon.features.aiassistant.common.AiAssistContextProvider
import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
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
        val assignmentWithSubmission = testAssignment.copy(
            submission = submission
        )
        coEvery { getAssignmentDetailsUseCase(any()) } returns assignmentWithSubmission

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
            savedStateHandle
        )
    }
}