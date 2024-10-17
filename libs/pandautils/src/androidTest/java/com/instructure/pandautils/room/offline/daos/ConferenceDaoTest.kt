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
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.ConferenceEntity
import com.instructure.pandautils.room.offline.entities.CourseEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConferenceDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var conferenceDao: ConferenceDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        conferenceDao = db.conferenceDao()
        courseDao = db.courseDao()

        runBlocking {
            courseDao.insert(CourseEntity(Course(1)))
            courseDao.insert(CourseEntity(Course(2)))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindEntityByCourseId() = runTest {
        val conferenceEntities = listOf(
            ConferenceEntity(Conference(1), 1),
            ConferenceEntity(Conference(2), 2)
        )
        conferenceDao.insertAll(conferenceEntities)

        val result = conferenceDao.findByCourseId(1)

        Assert.assertEquals(conferenceEntities.filter { it.courseId == 1L }, result)
    }

    @Test
    fun testCourseCascade() = runTest {
        val conferenceEntity = ConferenceEntity(Conference(1), 1)

        conferenceDao.insert(conferenceEntity)

        courseDao.delete(CourseEntity(Course(1)))

        val result = conferenceDao.findByCourseId(1)

        assert(result.isEmpty())
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testCourseForeignKey() = runTest {
        val conferenceEntity = ConferenceEntity(Conference(1), 3)

        conferenceDao.insert(conferenceEntity)
    }

    @Test
    fun testDeleteAllByCourseId() = runTest {
        val conferenceEntity = ConferenceEntity(Conference(1), 1)

        conferenceDao.insert(conferenceEntity)

        val result = conferenceDao.findByCourseId(1)

        Assert.assertEquals(listOf(conferenceEntity), result)

        conferenceDao.deleteAllByCourseId(1L)

        val deletedResult = conferenceDao.findByCourseId(1L)

        assert(deletedResult.isEmpty())
    }
}
