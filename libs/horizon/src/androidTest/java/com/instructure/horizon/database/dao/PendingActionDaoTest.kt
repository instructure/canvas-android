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
package com.instructure.horizon.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.horizon.database.HorizonDatabase
import com.instructure.horizon.database.entity.PendingActionEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PendingActionDaoTest {
    private lateinit var db: HorizonDatabase
    private lateinit var dao: PendingActionDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HorizonDatabase::class.java).build()
        dao = db.pendingActionDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertReturnsGeneratedIdAndCanBeReadBack() = runTest {
        val id = dao.insert(action(actionType = "submit", createdAt = 100))

        assertTrue(id > 0)
        val stored = dao.getById(id)
        assertEquals("submit", stored?.actionType)
        assertEquals(100L, stored?.createdAt)
    }

    @Test
    fun getReadyToDrainFiltersByRetryCountAndOrdersByCreatedAt() = runTest {
        val newest = dao.insert(action(actionType = "a", createdAt = 300, retryCount = 0))
        val oldest = dao.insert(action(actionType = "b", createdAt = 100, retryCount = 0))
        dao.insert(action(actionType = "c", createdAt = 200, retryCount = 3))

        val ready = dao.getReadyToDrain(maxRetries = 3)

        assertEquals(listOf(oldest, newest), ready.map { it.id })
    }

    @Test
    fun updateRetryPersistsRetryCountAndError() = runTest {
        val id = dao.insert(action(actionType = "a", createdAt = 1))

        dao.updateRetry(id, retryCount = 2, lastError = "boom")

        val stored = dao.getById(id)
        assertEquals(2, stored?.retryCount)
        assertEquals("boom", stored?.lastError)
    }

    @Test
    fun getAllIdsReturnsEveryRowId() = runTest {
        val id1 = dao.insert(action(actionType = "a", createdAt = 1))
        val id2 = dao.insert(action(actionType = "b", createdAt = 2))

        assertEquals(setOf(id1, id2), dao.getAllIds().toSet())
    }

    @Test
    fun deleteRemovesOnlyTheTargetedRow() = runTest {
        val id1 = dao.insert(action(actionType = "a", createdAt = 1))
        val id2 = dao.insert(action(actionType = "b", createdAt = 2))

        dao.delete(id1)

        assertNull(dao.getById(id1))
        assertEquals(id2, dao.getById(id2)?.id)
    }

    @Test
    fun deleteAllClearsTheTable() = runTest {
        dao.insert(action(actionType = "a", createdAt = 1))
        dao.insert(action(actionType = "b", createdAt = 2))

        dao.deleteAll()

        assertTrue(dao.getAll().isEmpty())
    }

    private fun action(actionType: String, createdAt: Long, retryCount: Int = 0) = PendingActionEntity(
        actionType = actionType,
        courseId = null,
        payload = "{}",
        retryCount = retryCount,
        createdAt = createdAt,
    )
}
