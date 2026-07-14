/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.ngc.features.submission.domain

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
class DeleteDraftUseCaseTest {

    private val createSubmissionDao: CreateSubmissionDao = mockk()
    private val apiPrefs: ApiPrefs = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val useCase = DeleteDraftUseCase(createSubmissionDao, apiPrefs)

    private val userId = 7L
    private val assignmentId = 42L

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { apiPrefs.user } returns User(id = userId)
        coJustRun {
            createSubmissionDao.deleteDraftByAssignmentIdAndType(any(), any(), any())
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke - deletes the draft for the requested submission type`() = runTest {
        val types = listOf(
            Assignment.SubmissionType.ONLINE_TEXT_ENTRY,
            Assignment.SubmissionType.ONLINE_URL,
            Assignment.SubmissionType.ONLINE_UPLOAD,
        )

        types.forEach { type ->
            useCase(DeleteDraftUseCase.Params(assignmentId, type))

            coVerify {
                createSubmissionDao.deleteDraftByAssignmentIdAndType(
                    assignmentId,
                    userId,
                    type.apiString
                )
            }
        }
    }

    @Test
    fun `invoke - does nothing when user is null`() = runTest {
        every { apiPrefs.user } returns null

        useCase(DeleteDraftUseCase.Params(assignmentId, Assignment.SubmissionType.ONLINE_URL))

        coVerify(exactly = 0) { createSubmissionDao.deleteDraftByAssignmentIdAndType(any(), any(), any()) }
    }
}
