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
package com.instructure.ngc.features.comments.domain

import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker
import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import com.instructure.pandautils.room.appdatabase.entities.FileUploadInputEntity
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private const val WORKER_ID = "44444444-4444-4444-4444-444444444444"

@OptIn(ExperimentalCoroutinesApi::class)
class InsertFileUploadInputUseCaseTest {

    private val fileUploadInputDao: FileUploadInputDao = mockk(relaxed = true)
    private val useCase = InsertFileUploadInputUseCase(fileUploadInputDao)

    @Test
    fun `inserts a file upload input entity for the restart worker`() = runTest {
        val slot = slot<FileUploadInputEntity>()
        coEvery { fileUploadInputDao.insert(capture(slot)) } returns Unit

        useCase(
            InsertFileUploadInputUseCase.Params(
                workerId = WORKER_ID,
                filePaths = listOf("/tmp/a.png"),
                courseId = 10L,
                assignmentId = 20L,
                studentId = 30L,
                attemptId = 5L
            )
        )

        assertEquals(WORKER_ID, slot.captured.workerId)
        assertEquals(listOf("/tmp/a.png"), slot.captured.filePaths)
        assertEquals(10L, slot.captured.courseId)
        assertEquals(20L, slot.captured.assignmentId)
        assertEquals(30L, slot.captured.userId)
        assertEquals(5L, slot.captured.attemptId)
        assertEquals(FileUploadWorker.ACTION_TEACHER_SUBMISSION_COMMENT, slot.captured.action)
    }
}
