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
import com.instructure.canvasapi2.models.*
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class SubmissionDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var submissionDao: SubmissionDao
    private lateinit var userDao: UserDao
    private lateinit var groupDao: GroupDao
    private lateinit var courseDao: CourseDao
    private lateinit var assignmentGroupDao: AssignmentGroupDao
    private lateinit var assignmentDao: AssignmentDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        submissionDao = db.submissionDao()
        userDao = db.userDao()
        groupDao = db.groupDao()
        courseDao = db.courseDao()
        assignmentGroupDao = db.assignmentGroupDao()
        assignmentDao = db.assignmentDao()

        runBlocking {
            courseDao.insert(CourseEntity(Course(1L)))
            assignmentGroupDao.insert(AssignmentGroupEntity(AssignmentGroup(1L), 1L))
            assignmentDao.insert(AssignmentEntity(Assignment(1L, assignmentGroupId = 1L), null, null, null, null))
            assignmentDao.insert(AssignmentEntity(Assignment(2L, assignmentGroupId = 1L), null, null, null, null))
            assignmentDao.insert(AssignmentEntity(Assignment(3L, assignmentGroupId = 1L), null, null, null, null))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindById() = runTest {
        val entities = listOf(
            SubmissionEntity(Submission(id = 1, body = "Body 1", attempt = 2, assignmentId = 1), null, null),
            SubmissionEntity(Submission(id = 1, body = "Body 2", attempt = 1, assignmentId = 1), null, null),
            SubmissionEntity(Submission(id = 2, body = "Body 3", assignmentId = 1), null, null)
        )
        entities.forEach {
            submissionDao.insert(it)
        }

        val result = submissionDao.findById(1)

        val expected = listOf(
            SubmissionEntity(Submission(id = 1, body = "Body 2", attempt = 1, assignmentId = 1), null, null),
            SubmissionEntity(Submission(id = 1, body = "Body 1", attempt = 2, assignmentId = 1), null, null)
        )

        Assert.assertEquals(expected, result)
    }

    @Test
    fun testFindByAssignmentIds() = runTest {
        val entities = listOf(
            SubmissionEntity(Submission(id = 1, body = "Body 1", assignmentId = 1), null, null),
            SubmissionEntity(Submission(id = 2, body = "Body 2", assignmentId = 2), null, null),
            SubmissionEntity(Submission(id = 3, body = "Body 3", assignmentId = 3), null, null)
        )
        entities.forEach {
            submissionDao.insert(it)
        }

        val result = submissionDao.findByAssignmentIds(listOf(1, 3))

        val expected = listOf(
            SubmissionEntity(Submission(id = 1, body = "Body 1", assignmentId = 1), null, null),
            SubmissionEntity(Submission(id = 3, body = "Body 3", assignmentId = 3), null, null)
        )

        Assert.assertEquals(expected, result)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testGroupForeignKey() = runTest {
        val submissionEntity = SubmissionEntity(Submission(id = 1, body = "Body 1", assignmentId = 1), 1, null)

        submissionDao.insert(submissionEntity)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testUserForeignKey() = runTest {
        val submissionEntity = SubmissionEntity(Submission(id = 1, body = "Body 1", assignmentId = 1, userId = 1), null, null)

        submissionDao.insert(submissionEntity)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testAssignmentForeignKey() = runTest {
        val submissionEntity = SubmissionEntity(Submission(id = 1, body = "Body 1", assignmentId = 4, userId = 1), null, null)

        submissionDao.insert(submissionEntity)
    }

    @Test
    fun testGroupCascadeOnDelete() = runTest {
        groupDao.insert(GroupEntity(Group(id = 1)))

        val submissionEntity = SubmissionEntity(Submission(id = 1, body = "Body 1", assignmentId = 1), 1, null)

        submissionDao.insert(submissionEntity)

        groupDao.delete(GroupEntity(Group(id = 1)))

        val result = submissionDao.findById(1)

        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun testUserSetNullOnDelete() = runTest {
        userDao.insert(UserEntity(User(id = 1)))

        val submissionEntity = SubmissionEntity(Submission(id = 1, body = "Body 1", assignmentId = 1, userId = 1), null, null)

        submissionDao.insert(submissionEntity)

        userDao.delete(UserEntity(User(id = 1)))

        val result = submissionDao.findById(1)

        Assert.assertEquals(listOf(submissionEntity.copy(userId = null)), result)
    }

    @Test
    fun testFindByAssignmentId() = runTest {
        val entities = listOf(
            SubmissionEntity(Submission(id = 1, body = "Body 1", assignmentId = 1), null, null),
            SubmissionEntity(Submission(id = 2, body = "Body 2", assignmentId = 2), null, null),
        )
        entities.forEach {
            submissionDao.insert(it)
        }

        val result = submissionDao.findByAssignmentId(1)

        Assert.assertEquals(entities.first(), result)
    }
}
