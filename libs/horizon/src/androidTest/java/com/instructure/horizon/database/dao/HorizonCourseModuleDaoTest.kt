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
import com.instructure.horizon.database.entity.HorizonCourseModuleItemEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HorizonCourseModuleDaoTest {
    private lateinit var db: HorizonDatabase
    private lateinit var dao: HorizonCourseModuleDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HorizonDatabase::class.java).build()
        dao = db.courseModuleDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun updateItemCompletionFlipsOnlyTheTargetedItem() = runTest {
        dao.insertItems(
            listOf(
                item(itemId = 1L, completed = false),
                item(itemId = 2L, completed = false),
            )
        )

        dao.updateItemCompletion(itemId = 1L, done = true)

        assertTrue(dao.getItemById(1L)!!.completionRequirementCompleted)
        assertFalse(dao.getItemById(2L)!!.completionRequirementCompleted)
    }

    @Test
    fun updateItemCompletionCanClearTheFlag() = runTest {
        dao.insertItems(listOf(item(itemId = 1L, completed = true)))

        dao.updateItemCompletion(itemId = 1L, done = false)

        assertEquals(false, dao.getItemById(1L)?.completionRequirementCompleted)
    }

    private fun item(itemId: Long, completed: Boolean) = HorizonCourseModuleItemEntity(
        itemId = itemId,
        moduleId = 20L,
        courseId = 10L,
        title = "Item $itemId",
        position = 0,
        indent = 0,
        type = "Assignment",
        htmlUrl = null,
        url = null,
        contentId = 0L,
        externalUrl = null,
        pageUrl = null,
        published = true,
        unpublishable = false,
        completionRequirementType = "must_mark_done",
        completionRequirementMinScore = 0.0,
        completionRequirementCompleted = completed,
        pointsPossible = null,
        dueAt = null,
        lockedForUser = false,
        lockExplanation = null,
        lockAt = null,
        unlockAt = null,
        hidden = null,
        locked = null,
        quizLti = false,
        estimatedDuration = null,
    )
}
