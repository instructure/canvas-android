/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.queuedsubmissions

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.HorizonSubmissionHelper
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.SubmissionState
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QueuedSubmissionsViewModelTest {
    private val createSubmissionDao: CreateSubmissionDao = mockk(relaxed = true)
    private val submissionHelper: HorizonSubmissionHelper = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val flow = MutableStateFlow<List<CreateSubmissionEntity>>(emptyList())
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { createSubmissionDao.findScheduledSubmissionsFlow(any()) } returns flow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun entity(id: Long, name: String, state: SubmissionState) = CreateSubmissionEntity(
        id = id,
        assignmentId = id,
        canvasContext = CanvasContext.emptyCourseContext(1L),
        userId = 1L,
        assignmentName = name,
        submissionType = "online_text_entry",
        submissionState = state,
    )

    @Test
    fun `Maps queued submissions to item states`() = runTest {
        val viewModel = QueuedSubmissionsViewModel(createSubmissionDao, submissionHelper, apiPrefs)

        flow.value = listOf(
            entity(1L, "Essay", SubmissionState.QUEUED),
            entity(2L, "Lab", SubmissionState.FAILED),
        )
        testScheduler.advanceUntilIdle()

        val items = viewModel.uiState.value.items
        assertEquals(false, viewModel.uiState.value.loading)
        assertEquals(2, items.size)
        assertEquals("Essay", items[0].assignmentName)
        assertEquals(SubmissionState.QUEUED, items[0].state)
        assertEquals(SubmissionState.FAILED, items[1].state)
    }

    @Test
    fun `onRetry delegates to submission helper`() = runTest {
        val viewModel = QueuedSubmissionsViewModel(createSubmissionDao, submissionHelper, apiPrefs)

        flow.value = listOf(entity(5L, "Lab", SubmissionState.FAILED))
        testScheduler.advanceUntilIdle()

        viewModel.uiState.value.items.first().onRetry()
        testScheduler.advanceUntilIdle()

        coVerify { submissionHelper.retrySubmission(match { it.id == 5L }) }
    }

    @Test
    fun `onCancel delegates to submission helper`() = runTest {
        val viewModel = QueuedSubmissionsViewModel(createSubmissionDao, submissionHelper, apiPrefs)

        flow.value = listOf(entity(7L, "Essay", SubmissionState.QUEUED))
        testScheduler.advanceUntilIdle()

        viewModel.uiState.value.items.first().onCancel()
        testScheduler.advanceUntilIdle()

        coVerify { submissionHelper.cancelSubmission(match { it.id == 7L }) }
    }

    @Test
    fun `List updates live when the flow re-emits`() = runTest {
        val viewModel = QueuedSubmissionsViewModel(createSubmissionDao, submissionHelper, apiPrefs)

        flow.value = listOf(entity(1L, "Essay", SubmissionState.QUEUED))
        testScheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.items.size)

        flow.value = emptyList()
        testScheduler.advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.items.size)
    }
}
