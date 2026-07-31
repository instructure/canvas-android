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
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.room.studentdb.entities.CreateFileSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.CreateSubmissionEntity
import com.instructure.pandautils.room.studentdb.entities.daos.CreateFileSubmissionDao
import com.instructure.pandautils.room.studentdb.entities.daos.CreateSubmissionDao
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class GetMediaDraftUseCaseTest {

    private val createSubmissionDao: CreateSubmissionDao = mockk()
    private val createFileSubmissionDao: CreateFileSubmissionDao = mockk()
    private val apiPrefs: ApiPrefs = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val useCase = GetMediaDraftUseCase(createSubmissionDao, createFileSubmissionDao, apiPrefs)

    private val userId = 7L
    private val assignmentId = 42L
    private val savedAt = Date()

    private val draftSubmission = CreateSubmissionEntity(
        id = 5L,
        assignmentId = assignmentId,
        canvasContext = CanvasContext.emptyCourseContext(id = 1L),
        submissionType = Assignment.SubmissionType.MEDIA_RECORDING.apiString,
        userId = userId,
        isDraft = true,
        lastActivityDate = savedAt,
    )

    private val clipFile = CreateFileSubmissionEntity(
        id = 1L,
        dbSubmissionId = 5L,
        name = "clip.mp4",
        size = 4096L,
        contentType = "video/mp4",
        fullPath = "/tmp/clip.mp4",
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { apiPrefs.user } returns User(id = userId)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke - returns MediaDraftData when a draft and its file row exist`() = runTest {
        coEvery {
            createSubmissionDao.findDraftSubmissionByAssignmentIdAndType(
                assignmentId,
                userId,
                Assignment.SubmissionType.MEDIA_RECORDING.apiString,
            )
        } returns draftSubmission
        coEvery { createFileSubmissionDao.findFilesForSubmissionId(draftSubmission.id) } returns listOf(clipFile)

        val result = useCase(GetMediaDraftUseCase.Params(assignmentId))

        assertEquals(
            MediaDraftData(
                path = "/tmp/clip.mp4",
                name = "clip.mp4",
                contentType = "video/mp4",
                savedAt = savedAt,
            ),
            result,
        )
    }

    @Test
    fun `invoke - returns null when no draft submission exists`() = runTest {
        coEvery {
            createSubmissionDao.findDraftSubmissionByAssignmentIdAndType(
                assignmentId,
                userId,
                Assignment.SubmissionType.MEDIA_RECORDING.apiString,
            )
        } returns null

        val result = useCase(GetMediaDraftUseCase.Params(assignmentId))

        assertNull(result)
    }

    @Test
    fun `invoke - returns null when the draft submission has no file rows`() = runTest {
        coEvery {
            createSubmissionDao.findDraftSubmissionByAssignmentIdAndType(
                assignmentId,
                userId,
                Assignment.SubmissionType.MEDIA_RECORDING.apiString,
            )
        } returns draftSubmission
        coEvery { createFileSubmissionDao.findFilesForSubmissionId(draftSubmission.id) } returns emptyList()

        val result = useCase(GetMediaDraftUseCase.Params(assignmentId))

        assertNull(result)
    }

    @Test
    fun `invoke - returns null when there is no logged in user`() = runTest {
        every { apiPrefs.user } returns null

        val result = useCase(GetMediaDraftUseCase.Params(assignmentId))

        assertNull(result)
    }
}
