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
package com.instructure.horizon.offline.sync

import com.google.gson.Gson
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.Failure
import com.instructure.horizon.data.datasource.ModuleItemNetworkDataSource
import com.instructure.horizon.database.dao.PendingActionDao
import com.instructure.horizon.database.entity.PendingActionEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

class MarkAsDoneActionSyncerTest {
    private val pendingActionDao: PendingActionDao = mockk(relaxed = true)
    private val networkDataSource: ModuleItemNetworkDataSource = mockk(relaxed = true)

    private val gson = Gson()
    private val syncer = MarkAsDoneActionSyncer(pendingActionDao, networkDataSource)

    private val success: DataResult<okhttp3.ResponseBody> = DataResult.Success("".toResponseBody(null))

    @Test
    fun `drains a done action and deletes the row on success`() = runTest {
        coEvery { pendingActionDao.getReadyToDrain(any()) } returns listOf(action(id = 1L, done = true))
        coEvery { networkDataSource.markAsDone(10L, 20L, 30L, any()) } returns success

        syncer.drain()

        coVerify(exactly = 1) { networkDataSource.markAsDone(10L, 20L, 30L, any()) }
        coVerify(exactly = 1) { pendingActionDao.delete(1L) }
    }

    @Test
    fun `drains a not-done action via the delete endpoint`() = runTest {
        coEvery { pendingActionDao.getReadyToDrain(any()) } returns listOf(action(id = 1L, done = false))
        coEvery { networkDataSource.markAsNotDone(10L, 20L, 30L, any()) } returns success

        syncer.drain()

        coVerify(exactly = 1) { networkDataSource.markAsNotDone(10L, 20L, 30L, any()) }
        coVerify(exactly = 1) { pendingActionDao.delete(1L) }
    }

    @Test
    fun `ignores actions of other types`() = runTest {
        coEvery { pendingActionDao.getReadyToDrain(any()) } returns listOf(
            action(id = 1L, done = true).copy(actionType = "SOMETHING_ELSE")
        )

        syncer.drain()

        coVerify(exactly = 0) { networkDataSource.markAsDone(any(), any(), any(), any()) }
        coVerify(exactly = 0) { pendingActionDao.delete(any()) }
    }

    @Test
    fun `treats a 409 conflict as idempotent success`() = runTest {
        coEvery { pendingActionDao.getReadyToDrain(any()) } returns listOf(action(id = 1L, done = true))
        coEvery { networkDataSource.markAsDone(10L, 20L, 30L, any()) } returns
            DataResult.Fail(Failure.Network("conflict", 409))

        syncer.drain()

        coVerify(exactly = 1) { pendingActionDao.delete(1L) }
        coVerify(exactly = 0) { pendingActionDao.updateRetry(any(), any(), any()) }
    }

    @Test
    fun `bumps retry and rethrows on a 401 so the worker retries`() = runTest {
        coEvery { pendingActionDao.getReadyToDrain(any()) } returns listOf(action(id = 1L, done = true, retryCount = 2))
        coEvery { networkDataSource.markAsDone(10L, 20L, 30L, any()) } returns
            DataResult.Fail(Failure.Authorization("expired"))

        val threw = drainThrows()

        assertTrue(threw)
        coVerify(exactly = 1) { pendingActionDao.updateRetry(1L, 3, "expired") }
        coVerify(exactly = 0) { pendingActionDao.delete(any()) }
    }

    @Test
    fun `bumps retry and rethrows on a 5xx failure`() = runTest {
        coEvery { pendingActionDao.getReadyToDrain(any()) } returns listOf(action(id = 1L, done = true, retryCount = 0))
        coEvery { networkDataSource.markAsDone(10L, 20L, 30L, any()) } returns
            DataResult.Fail(Failure.Network("server error", 500))

        val threw = drainThrows()

        assertTrue(threw)
        coVerify(exactly = 1) { pendingActionDao.updateRetry(1L, 1, "server error") }
    }

    @Test
    fun `marks a permanent 4xx failure exhausted and keeps the row without rethrowing`() = runTest {
        coEvery { pendingActionDao.getReadyToDrain(any()) } returns listOf(action(id = 1L, done = true))
        coEvery { networkDataSource.markAsDone(10L, 20L, 30L, any()) } returns
            DataResult.Fail(Failure.Network("not found", 404))

        val threw = drainThrows()

        assertFalse(threw)
        coVerify(exactly = 1) { pendingActionDao.updateRetry(1L, PendingActionSyncer.MAX_RETRIES, "not found") }
        coVerify(exactly = 0) { pendingActionDao.delete(any()) }
    }

    @Test
    fun `replays with token-safe rest params`() = runTest {
        coEvery { pendingActionDao.getReadyToDrain(any()) } returns listOf(action(id = 1L, done = true))
        val paramsSlot = slot<RestParams>()
        coEvery { networkDataSource.markAsDone(10L, 20L, 30L, capture(paramsSlot)) } returns success

        syncer.drain()

        assertTrue(paramsSlot.captured.shouldRefreshToken)
        assertFalse(paramsSlot.captured.shouldLoginOnTokenError)
    }

    private suspend fun drainThrows(): Boolean {
        return try {
            syncer.drain()
            false
        } catch (e: Exception) {
            true
        }
    }

    private fun action(id: Long, done: Boolean, retryCount: Int = 0) = PendingActionEntity(
        id = id,
        actionType = MARK_AS_DONE_ACTION,
        courseId = 10L,
        payload = gson.toJson(MarkAsDonePayload(courseId = 10L, moduleId = 20L, itemId = 30L, done = done)),
        retryCount = retryCount,
        createdAt = id,
    )
}
