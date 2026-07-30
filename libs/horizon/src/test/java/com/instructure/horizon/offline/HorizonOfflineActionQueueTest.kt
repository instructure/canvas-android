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
package com.instructure.horizon.offline

import android.net.Uri
import com.instructure.horizon.database.dao.PendingActionDao
import com.instructure.horizon.database.entity.PendingActionEntity
import com.instructure.horizon.offline.sync.HorizonOfflineSyncHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

class HorizonOfflineActionQueueTest {

    private val pendingActionDao: PendingActionDao = mockk(relaxed = true)
    private val fileStaging: HorizonPendingFileStaging = mockk(relaxed = true)
    private val syncHelper: HorizonOfflineSyncHelper = mockk(relaxed = true)

    private val queue = HorizonOfflineActionQueue(pendingActionDao, fileStaging, syncHelper)

    @Before
    fun setUp() {
        coEvery { pendingActionDao.insert(any()) } returns 42L
    }

    @Test
    fun `persists the action, stages attachments and schedules a drain`() = runTest {
        val uri: Uri = mockk()
        val entitySlot = slot<PendingActionEntity>()
        coEvery { pendingActionDao.insert(capture(entitySlot)) } returns 42L
        every { fileStaging.stage(any(), any(), any()) } returns mockk()

        val id = queue.enqueue(
            actionType = "submission",
            payload = "{}",
            courseId = 7L,
            attachments = listOf(PendingActionAttachment(uri, "doc.txt")),
        )

        assertEquals(42L, id)
        assertEquals("submission", entitySlot.captured.actionType)
        assertEquals(7L, entitySlot.captured.courseId)
        coVerify { fileStaging.stage(42L, uri, "doc.txt") }
        coVerify { syncHelper.enqueueActionDrain() }
    }

    @Test
    fun `schedules a drain even without attachments`() = runTest {
        queue.enqueue(actionType = "markAsRead", payload = "{}")

        coVerify(exactly = 0) { fileStaging.stage(any(), any(), any()) }
        coVerify { syncHelper.enqueueActionDrain() }
    }

    @Test
    fun `rolls back and does not schedule a drain when staging fails`() = runTest {
        val uri: Uri = mockk()
        every { fileStaging.stage(any(), any(), any()) } throws IOException("provider gone")

        try {
            queue.enqueue(
                actionType = "submission",
                payload = "{}",
                attachments = listOf(PendingActionAttachment(uri, "doc.txt")),
            )
            throw AssertionError("Expected IOException to propagate")
        } catch (_: IOException) {
        }

        coVerify { pendingActionDao.delete(42L) }
        coVerify { fileStaging.delete(42L) }
        coVerify(exactly = 0) { syncHelper.enqueueActionDrain() }
    }
}
