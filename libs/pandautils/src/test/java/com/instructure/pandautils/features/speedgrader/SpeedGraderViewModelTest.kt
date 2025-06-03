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

import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.AssignmentDetailsQuery
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.utils.Const
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SpeedGraderViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: SpeedGraderViewModel
    private lateinit var repository: SpeedGraderRepository
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setUp() {
        ContextKeeper.appContext = mockk(relaxed = true)
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        savedStateHandle = SavedStateHandle(mapOf(Const.ASSIGNMENT_ID to 1L, SpeedGraderFragment.FILTERED_SUBMISSION_IDS to longArrayOf(1L)))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchData updates uiState correctly`() = runTest(testDispatcher) {
        val course = AssignmentDetailsQuery.Course(name = "Test Course", _id = "1")
        val assignment = AssignmentDetailsQuery.Assignment(title = "Test Assignment", course = course)
        val assignmentDetails = AssignmentDetailsQuery.Data(assignment = assignment)
        coEvery { repository.getAssignmentDetails(1L) } returns assignmentDetails

        viewModel = SpeedGraderViewModel(savedStateHandle, repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val uiState = viewModel.uiState.first()
        assertEquals("Test Assignment", uiState.assignmentName)
        assertEquals("Test Course", uiState.courseName)
    }

    @Test
    fun `init throws exception when assignmentId is missing`() {
        savedStateHandle = SavedStateHandle()

        assertThrows(IllegalStateException::class.java) {
            SpeedGraderViewModel(savedStateHandle, repository)
        }
    }

    @Test
    fun `init throws exception when submissionIds are missing`() {
        savedStateHandle = SavedStateHandle(mapOf(Const.ASSIGNMENT_ID to 1L))

        assertThrows(IllegalStateException::class.java) {
            SpeedGraderViewModel(savedStateHandle, repository)
        }
    }

    @Test
    fun `init sets selectedItem to 0 when it is missing`() {
        val course = AssignmentDetailsQuery.Course(name = "Test Course", _id = "1")
        val assignment = AssignmentDetailsQuery.Assignment(title = "Test Assignment", course = course)
        val assignmentDetails = AssignmentDetailsQuery.Data(assignment = assignment)
        coEvery { repository.getAssignmentDetails(1L) } returns assignmentDetails
        savedStateHandle = SavedStateHandle(mapOf(Const.ASSIGNMENT_ID to 1L, SpeedGraderFragment.FILTERED_SUBMISSION_IDS to longArrayOf(1L)))

        viewModel = SpeedGraderViewModel(savedStateHandle, repository)

        // Assert
        val uiState = viewModel.uiState.value
        assertEquals(0, uiState.selectedItem)
    }

    @Test
    fun `init sets selectedItem to provided value`() {
        val course = AssignmentDetailsQuery.Course(name = "Test Course", _id = "1")
        val assignment = AssignmentDetailsQuery.Assignment(title = "Test Assignment", course = course)
        val assignmentDetails = AssignmentDetailsQuery.Data(assignment = assignment)
        coEvery { repository.getAssignmentDetails(1L) } returns assignmentDetails
        savedStateHandle = SavedStateHandle(mapOf(Const.ASSIGNMENT_ID to 1L, SpeedGraderFragment.FILTERED_SUBMISSION_IDS to longArrayOf(1L), Const.SELECTED_ITEM to 2))

        viewModel = SpeedGraderViewModel(savedStateHandle, repository)

        // Assert
        val uiState = viewModel.uiState.value
        assertEquals(2, uiState.selectedItem)
    }
}