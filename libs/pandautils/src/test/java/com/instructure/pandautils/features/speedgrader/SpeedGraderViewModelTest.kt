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
package com.instructure.pandautils.features.speedgrader

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.AssignmentDetailsQuery
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandares.R
import com.instructure.pandautils.utils.Const
import com.instructure.testutils.ViewModelTestRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SpeedGraderViewModelTest {

    @get:Rule
    val viewModelTestRule = ViewModelTestRule()

    private lateinit var viewModel: SpeedGraderViewModel
    private lateinit var repository: SpeedGraderRepository
    private lateinit var assignmentSubmissionRepository: AssignmentSubmissionRepository
    private lateinit var speedGraderPostPolicyRouter: SpeedGraderPostPolicyRouter
    private lateinit var savedStateHandle: SavedStateHandle
    private val resources: Resources = mockk(relaxed = true)
    private val errorHandler: SpeedGraderErrorHolder = mockk(relaxed = true)

    private fun createViewModel() {
        viewModel = SpeedGraderViewModel(
            savedStateHandle,
            repository,
            assignmentSubmissionRepository,
            speedGraderPostPolicyRouter,
            errorHandler,
            resources
        )
    }

    @Before
    fun setUp() = runTest {
        ContextKeeper.appContext = mockk(relaxed = true)
        repository = mockk(relaxed = true)
        assignmentSubmissionRepository = mockk(relaxed = true)
        speedGraderPostPolicyRouter = mockk(relaxed = true)
        assignmentSubmissionRepository = mockk()
        savedStateHandle = SavedStateHandle(
            mapOf(
                Const.COURSE_ID to 1L,
                Const.ASSIGNMENT_ID to 1L,
                SpeedGraderFragment.FILTERED_SUBMISSION_IDS to longArrayOf(1L)
            )
        )

        coEvery {
            assignmentSubmissionRepository.getAssignment(
                any(),
                any(),
                any()
            )
        } returns Assignment()

        coEvery { resources.getString(R.string.generalUnexpectedError) } returns "Error"
    }

    @Test
    fun `fetchData updates uiState correctly`() = runTest(testDispatcher) {
        val course = AssignmentDetailsQuery.Course(name = "Test Course", _id = "1")
        val assignment =
            AssignmentDetailsQuery.Assignment(title = "Test Assignment", course = course)
        val assignmentDetails = AssignmentDetailsQuery.Data(assignment = assignment)
        coEvery { repository.getAssignmentDetails(1L) } returns assignmentDetails

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val uiState = viewModel.uiState.first()
        assertEquals("Test Assignment", uiState.assignmentName)
        assertEquals("Test Course", uiState.courseName)
        assertEquals(1L, uiState.courseId)
    }

    @Test
    fun `init throws exception when assignmentId is missing`() {
        savedStateHandle = SavedStateHandle()

        assertThrows(IllegalStateException::class.java) {
            createViewModel()
        }
    }

    @Test
    fun `init throws exception when submissionIds are missing`() {
        savedStateHandle = SavedStateHandle(mapOf(Const.ASSIGNMENT_ID to 1L))

        assertThrows(IllegalStateException::class.java) {
            createViewModel()
        }
    }

    @Test
    fun `init sets selectedItem to 0 when it is missing`() {
        val course = AssignmentDetailsQuery.Course(name = "Test Course", _id = "1")
        val assignment =
            AssignmentDetailsQuery.Assignment(title = "Test Assignment", course = course)
        val assignmentDetails = AssignmentDetailsQuery.Data(assignment = assignment)
        coEvery { repository.getAssignmentDetails(1L) } returns assignmentDetails
        savedStateHandle = SavedStateHandle(
            mapOf(
                Const.COURSE_ID to 1L,
                Const.ASSIGNMENT_ID to 1L,
                SpeedGraderFragment.FILTERED_SUBMISSION_IDS to longArrayOf(1L)
            )
        )

        createViewModel()

        // Assert
        val uiState = viewModel.uiState.value
        assertEquals(0, uiState.selectedItem)
    }

    @Test
    fun `init sets selectedItem to provided value`() {
        val course = AssignmentDetailsQuery.Course(name = "Test Course", _id = "1")
        val assignment =
            AssignmentDetailsQuery.Assignment(title = "Test Assignment", course = course)
        val assignmentDetails = AssignmentDetailsQuery.Data(assignment = assignment)
        coEvery { repository.getAssignmentDetails(1L) } returns assignmentDetails
        savedStateHandle = SavedStateHandle(
            mapOf(
                Const.COURSE_ID to 1L,
                Const.ASSIGNMENT_ID to 1L,
                SpeedGraderFragment.FILTERED_SUBMISSION_IDS to longArrayOf(1L),
                Const.SELECTED_ITEM to 2
            )
        )

        createViewModel()

        // Assert
        val uiState = viewModel.uiState.value
        assertEquals(2, uiState.selectedItem)
    }

    @Test
    fun `Navigating to the post policy screen calls the router`() = runTest {
        createViewModel()

        val uiState = viewModel.uiState.first()

        uiState.navigateToPostPolicy(mockk())

        verify { speedGraderPostPolicyRouter.navigateToPostPolicies(any(), any(), any()) }
    }

    @Test
    fun `Error posted when fetching data fails`() = runTest {
        val exception = Exception("Network error")
        coEvery { repository.getAssignmentDetails(1L) } throws exception

        createViewModel()

        coVerify {
            errorHandler.postError("Error", any())
        }
    }
}
