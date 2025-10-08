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
package com.instructure.horizon.features.moduleitemsequence.content.assessment

import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.Assignment
import com.instructure.horizon.features.moduleitemsequence.ModuleItemContent
import com.instructure.pandautils.utils.Const
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AssessmentViewModelTest {
    private val repository: AssessmentRepository = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val assignmentId = 1L
    private val courseId = 100L
    private val testAssignment = Assignment(
        id = assignmentId,
        name = "Test Quiz",
        url = "https://example.com/quiz/1"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { savedStateHandle.get<Long>(ModuleItemContent.Assignment.ASSIGNMENT_ID) } returns assignmentId
        every { savedStateHandle.get<Long>(Const.COURSE_ID) } returns courseId
        coEvery { repository.getAssignment(any(), any(), any()) } returns testAssignment
        coEvery { repository.authenticateUrl(any()) } returns "https://authenticated.url"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test ViewModel loads assignment data`() = runTest {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.loadingState.isLoading)
        assertEquals("Test Quiz", viewModel.uiState.value.assessmentName)
        coVerify { repository.getAssignment(assignmentId, courseId, false) }
    }

    @Test
    fun `Test start quiz clicked shows dialog and loads URL`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onStartQuizClicked()

        assertTrue(viewModel.uiState.value.showAssessmentDialog)
        assertEquals("https://authenticated.url", viewModel.uiState.value.urlToLoad)
        viewModel.uiState.value.onAssessmentLoaded()
        assertFalse(viewModel.uiState.value.assessmentLoading)
        coVerify { repository.authenticateUrl("https://example.com/quiz/1") }
    }

    @Test
    fun `Test start quiz with authentication error`() = runTest {
        coEvery { repository.authenticateUrl(any()) } throws Exception("Auth error")

        val viewModel = getViewModel()

        viewModel.uiState.value.onStartQuizClicked()

        assertTrue(viewModel.uiState.value.showAssessmentDialog)
        assertFalse(viewModel.uiState.value.assessmentLoading)
    }

    @Test
    fun `Test assessment closed clears URL and dialog`() = runTest {
        val viewModel = getViewModel()
        viewModel.uiState.value.onStartQuizClicked()

        viewModel.uiState.value.onAssessmentClosed()

        assertNull(viewModel.uiState.value.urlToLoad)
        assertFalse(viewModel.uiState.value.showAssessmentDialog)
    }

    @Test
    fun `Test assessment loaded clears loading state`() = runTest {
        val viewModel = getViewModel()
        viewModel.uiState.value.onStartQuizClicked()

        viewModel.uiState.value.onAssessmentLoaded()

        assertFalse(viewModel.uiState.value.assessmentLoading)
    }

    @Test
    fun `Test assessment completion starts loading`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onAssessmentCompletion()

        assertTrue(viewModel.uiState.value.assessmentCompletionLoading)
    }

    @Test
    fun `Test assessment completion finishes after delay`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onAssessmentCompletion()
        assertTrue(viewModel.uiState.value.assessmentCompletionLoading)

        advanceTimeBy(15100)

        assertFalse(viewModel.uiState.value.assessmentCompletionLoading)
    }

    @Test
    fun `Test load error sets error state`() = runTest {
        coEvery { repository.getAssignment(any(), any(), any()) } throws Exception("Error")

        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.loadingState.isError)
    }

    @Test
    fun `Test start quiz with null assessment URL`() = runTest {
        coEvery { repository.getAssignment(any(), any(), any()) } returns testAssignment.copy(url = null)

        val viewModel = getViewModel()

        viewModel.uiState.value.onStartQuizClicked()

        assertTrue(viewModel.uiState.value.showAssessmentDialog)
        assertNull(viewModel.uiState.value.urlToLoad)
        assertFalse(viewModel.uiState.value.assessmentLoading)
    }

    @Test
    fun `Test UI state contains all callbacks`() = runTest {
        val viewModel = getViewModel()

        assertNotNull(viewModel.uiState.value.onAssessmentClosed)
        assertNotNull(viewModel.uiState.value.onStartQuizClicked)
        assertNotNull(viewModel.uiState.value.onAssessmentCompletion)
        assertNotNull(viewModel.uiState.value.onAssessmentLoaded)
    }

    private fun getViewModel(): AssessmentViewModel {
        return AssessmentViewModel(repository, savedStateHandle)
    }
}
