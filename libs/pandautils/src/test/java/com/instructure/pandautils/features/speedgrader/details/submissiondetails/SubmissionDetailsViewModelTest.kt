/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.pandautils.features.speedgrader.details.submissiondetails

import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.SubmissionDetailsQuery
import com.instructure.canvasapi2.type.SubmissionType
import com.instructure.pandautils.features.speedgrader.SpeedGraderSelectedAttemptHolder
import com.instructure.pandautils.features.speedgrader.content.SpeedGraderContentViewModel.Companion.ASSIGNMENT_ID_KEY
import com.instructure.pandautils.features.speedgrader.content.SpeedGraderContentViewModel.Companion.STUDENT_ID_KEY
import com.instructure.pandautils.utils.ScreenState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class SubmissionDetailsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val repository: SubmissionDetailsRepository = mockk(relaxed = true)
    private val selectedAttemptHolder = SpeedGraderSelectedAttemptHolder()

    private lateinit var viewModel: SubmissionDetailsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { savedStateHandle.get<Long>(ASSIGNMENT_ID_KEY) } returns 1L
        every { savedStateHandle.get<Long>(STUDENT_ID_KEY) } returns 1L
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun createViewModel(): SubmissionDetailsViewModel {
        return SubmissionDetailsViewModel(savedStateHandle, repository, selectedAttemptHolder)
    }

    @Test
    fun `Loads submission details`() {
        val submissionData1 = SubmissionDetailsData(
            attempt = 1,
            wordCount = null,
            submissionType = SubmissionType.online_upload
        )
        val submissionData2 = SubmissionDetailsData(
            attempt = 2,
            wordCount = 123.0,
            submissionType = SubmissionType.online_text_entry
        )
        coEvery { repository.getSubmission(1, 1) } returns mockk<SubmissionDetailsQuery.Data>(relaxed = true).apply {
            coEvery { this@apply.submission?.submissionHistoriesConnection } returns mockSubmissionHistory(submissionData1, submissionData2)
        }

        viewModel = createViewModel()

        selectedAttemptHolder.setSelectedAttemptId(1, 2)

        assertEquals(ScreenState.Content, viewModel.uiState.value.state)
        assertEquals(123, viewModel.uiState.value.wordCount)
    }

    @Test
    fun `Loads submission details with not online text type`() {
        val submissionData = SubmissionDetailsData(
            attempt = 1,
            wordCount = null,
            submissionType = SubmissionType.online_upload
        )
        coEvery { repository.getSubmission(1, 1) } returns mockk<SubmissionDetailsQuery.Data>(relaxed = true).apply {
            coEvery { this@apply.submission?.submissionHistoriesConnection } returns mockSubmissionHistory(submissionData)
        }

        viewModel = createViewModel()

        selectedAttemptHolder.setSelectedAttemptId(1, 1)

        assertEquals(ScreenState.Empty, viewModel.uiState.value.state)
    }

    @Test
    fun `Error loading submission details`() {
        coEvery { repository.getSubmission(1, 1) } throws Exception("Error loading submission")

        viewModel = createViewModel()

        selectedAttemptHolder.setSelectedAttemptId(1, 1)

        assertEquals(ScreenState.Empty, viewModel.uiState.value.state)
    }

    private data class SubmissionDetailsData(
        val attempt: Int,
        val wordCount: Double?,
        val submissionType: SubmissionType
    )

    private fun mockSubmissionHistory(
        vararg fields: SubmissionDetailsData
    ): SubmissionDetailsQuery.SubmissionHistoriesConnection {
        val edges = fields.map { field ->
            val node = mockk<SubmissionDetailsQuery.Node>(relaxed = true).apply {
                coEvery { attempt } returns field.attempt
                coEvery { wordCount } returns field.wordCount
                coEvery { submissionType } returns field.submissionType
            }
            mockk<SubmissionDetailsQuery.Edge>(relaxed = true).apply {
                coEvery { this@apply.node } returns node
            }
        }
        return mockk<SubmissionDetailsQuery.SubmissionHistoriesConnection>(relaxed = true).apply {
            coEvery { this@apply.edges } returns edges
        }
    }
}
