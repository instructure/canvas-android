/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.pandautils.room.offline.daos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.DiscussionParticipantEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiscussionParticipantDaoTest {

    private lateinit var db: OfflineDatabase

    private lateinit var discussionParticipantDao: DiscussionParticipantDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        discussionParticipantDao = db.discussionParticipantDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertReplace() = runTest {
        val discussionParticipantEntity = DiscussionParticipantEntity(DiscussionParticipant(id = 1L, displayName = "Participant"))
        discussionParticipantDao.insert(discussionParticipantEntity)

        val updated = discussionParticipantEntity.copy(displayName = "updated")
        discussionParticipantDao.insert(updated)

        val result = discussionParticipantDao.findById(1L)

        Assert.assertEquals(updated.displayName, result?.displayName)
    }

    @Test
    fun testFindById() = runTest {
        val discussionParticipantEntity = DiscussionParticipantEntity(DiscussionParticipant(id = 1L, displayName = "Participant"))
        val discussionParticipantEntity2 = DiscussionParticipantEntity(DiscussionParticipant(id = 2L, displayName = "Participant 2"))
        discussionParticipantDao.insertAll(listOf(discussionParticipantEntity, discussionParticipantEntity2))

        val result = discussionParticipantDao.findById(1L)

        Assert.assertEquals(discussionParticipantEntity.displayName, result?.displayName)
    }
}