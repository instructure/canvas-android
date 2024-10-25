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
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionParticipant
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.CourseEntity
import com.instructure.pandautils.room.offline.entities.DiscussionParticipantEntity
import com.instructure.pandautils.room.offline.entities.DiscussionTopicHeaderEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.OffsetDateTime
import java.util.*

@RunWith(AndroidJUnit4::class)
class DiscussionTopicHeaderDaoTest {

    private lateinit var db: OfflineDatabase

    private lateinit var discussionTopicHeaderDao: DiscussionTopicHeaderDao
    private lateinit var courseDao: CourseDao
    private lateinit var discussionParticipantDao: DiscussionParticipantDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        discussionTopicHeaderDao = db.discussionTopicHeaderDao()
        courseDao = db.courseDao()
        discussionParticipantDao = db.discussionParticipantDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertReplace() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))

        val discussionTopicHeaderEntity = DiscussionTopicHeaderEntity(DiscussionTopicHeader(id = 1L, title = "Discussion"), 1)
        discussionTopicHeaderDao.insert(discussionTopicHeaderEntity)

        val updated = discussionTopicHeaderEntity.copy(title = "updated")
        discussionTopicHeaderDao.insert(updated)

        val result = discussionTopicHeaderDao.findById(1L)

        Assert.assertEquals(updated.title, result?.title)
    }

    @Test
    fun testFindById() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))

        val discussionTopicHeaderEntity = DiscussionTopicHeaderEntity(DiscussionTopicHeader(id = 1L, title = "Discussion"), 1)
        val discussionTopicHeaderEntity2 = DiscussionTopicHeaderEntity(DiscussionTopicHeader(id = 2L, title = "Discussion 2"), 1)
        discussionTopicHeaderDao.insertAll(listOf(discussionTopicHeaderEntity, discussionTopicHeaderEntity2))

        val result = discussionTopicHeaderDao.findById(1L)

        Assert.assertEquals(discussionTopicHeaderEntity.title, result?.title)
    }

    @Test
    fun findAllDiscussionsOnlyReturnsDiscussionsForSpecificCourse() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))
        courseDao.insert(CourseEntity(Course(id = 2)))

        val discussion = DiscussionTopicHeaderEntity(DiscussionTopicHeader(id = 1L, title = "Discussion"), 1)
        val discussion2 = DiscussionTopicHeaderEntity(DiscussionTopicHeader(id = 2L, title = "Discussion 2"), 1)
        val discussion3 = DiscussionTopicHeaderEntity(DiscussionTopicHeader(id = 3L, title = "Discussion 3"), 2)
        val announcement = DiscussionTopicHeaderEntity(DiscussionTopicHeader(id = 4L, title = "Announcement", announcement = true), 1)
        discussionTopicHeaderDao.insertAll(listOf(discussion, discussion2, discussion3, announcement))

        val result = discussionTopicHeaderDao.findAllDiscussionsForCourse(1)

        Assert.assertEquals(2, result.size)
        Assert.assertEquals(discussion.title, result[0].title)
        Assert.assertEquals(discussion2.title, result[1].title)
    }

    @Test
    fun findAllAnnouncementsOnlyReturnsAnnouncementsForSpecificCourseAndOrdersByDate() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))
        courseDao.insert(CourseEntity(Course(id = 2)))

        val discussion = DiscussionTopicHeaderEntity(DiscussionTopicHeader(id = 1L, title = "Discussion"), 1)
        val announcement = DiscussionTopicHeaderEntity(DiscussionTopicHeader(id = 2L, title = "Announcement", announcement = true), 2)
        val date1 = Date(OffsetDateTime.now().toEpochSecond())
        val date2 = Date(OffsetDateTime.now().plusDays(2).toEpochSecond())
        val announcement2 =
            DiscussionTopicHeaderEntity(DiscussionTopicHeader(id = 3L, title = "Announcement 2", announcement = true, postedDate = date1), 1)
        val announcement3 =
            DiscussionTopicHeaderEntity(DiscussionTopicHeader(id = 4L, title = "Announcement 3", announcement = true, postedDate = date2), 1)
        discussionTopicHeaderDao.insertAll(listOf(discussion, announcement, announcement2, announcement3))

        val result = discussionTopicHeaderDao.findAllAnnouncementsForCourse(1)

        Assert.assertEquals(2, result.size)
        Assert.assertEquals(announcement3.title, result[0].title)
        Assert.assertEquals(announcement2.title, result[1].title)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testCourseForeignKeyIsMandatory() = runTest {
        val discussion = DiscussionTopicHeaderEntity(DiscussionTopicHeader(id = 1L, title = "Discussion"), 1)

        discussionTopicHeaderDao.insert(discussion)
    }

    @Test
    fun testDeletingTheAssociatedCourseDeletesTheDiscussion() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))
        val discussion = DiscussionTopicHeaderEntity(DiscussionTopicHeader(id = 1L, title = "Discussion"), 1)
        discussionTopicHeaderDao.insert(discussion)

        courseDao.delete(CourseEntity(Course(id = 1)))

        val result = discussionTopicHeaderDao.findAllDiscussionsForCourse(1)

        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun testDeletingTheAssociatedParticipantSetsItToNull() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))
        val discussion = DiscussionTopicHeaderEntity(DiscussionTopicHeader(id = 1L, title = "Discussion", author = DiscussionParticipant(id = 1)), 1)
        val participant = DiscussionParticipantEntity(DiscussionParticipant(id = 1, displayName = "Participant"))

        discussionParticipantDao.insert(participant)
        discussionTopicHeaderDao.insert(discussion)

        val resultBeforeDelete = discussionTopicHeaderDao.findById(1)
        Assert.assertEquals(1L, resultBeforeDelete?.authorId)

        discussionParticipantDao.delete(participant)

        val result = discussionTopicHeaderDao.findById(1)
        Assert.assertNull(result?.authorId)
    }

    @Test
    fun testDeleteAllByCourseId() = runTest {
        courseDao.insert(CourseEntity(Course(id = 1)))

        val discussionTopicHeaderEntity = DiscussionTopicHeaderEntity(DiscussionTopicHeader(id = 1L, title = "Discussion"), 1)
        val discussionTopicHeaderEntity2 = DiscussionTopicHeaderEntity(DiscussionTopicHeader(id = 2L, title = "Discussion 2"), 1)
        discussionTopicHeaderDao.insertAll(listOf(discussionTopicHeaderEntity, discussionTopicHeaderEntity2))

        val result = discussionTopicHeaderDao.findAllDiscussionsForCourse(1L)

        Assert.assertEquals(listOf(discussionTopicHeaderEntity, discussionTopicHeaderEntity2), result)

        discussionTopicHeaderDao.deleteAllByCourseId(1L, false)

        val deletedResult = discussionTopicHeaderDao.findAllDiscussionsForCourse(1L)

        Assert.assertTrue(deletedResult.isEmpty())
    }
}