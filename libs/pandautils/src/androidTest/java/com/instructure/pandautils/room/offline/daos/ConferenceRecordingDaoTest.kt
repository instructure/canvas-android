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
import com.instructure.canvasapi2.models.ConferenceRecording
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.entities.ConferenceEntity
import com.instructure.pandautils.room.offline.entities.ConferenceRecordingEntity
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
class ConferenceRecordingDaoTest {

    private lateinit var db: OfflineDatabase
    private lateinit var conferenceRecodingDao: ConferenceRecodingDao
    private lateinit var conferenceDao: ConferenceDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, OfflineDatabase::class.java).build()
        conferenceRecodingDao = db.conferenceRecordingDao()
        conferenceDao = db.conferenceDao()
        courseDao = db.courseDao()

        runBlocking {
            courseDao.insert(CourseEntity(Course(1)))
            conferenceDao.insertAll(
                listOf(
                    ConferenceEntity(Conference(1), 1),
                    ConferenceEntity(Conference(2), 1)
                )
            )
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFindEntityByCourseId() = runTest {
        val conferenceRecordingEntity = ConferenceRecordingEntity(ConferenceRecording(recordingId = "recording1"), 1)
        val conferenceRecordingEntity2 = ConferenceRecordingEntity(ConferenceRecording(recordingId = "recording2"), 2)
        conferenceRecodingDao.insert(conferenceRecordingEntity)
        conferenceRecodingDao.insert(conferenceRecordingEntity2)

        val result = conferenceRecodingDao.findByConferenceId(1)

        Assert.assertEquals(listOf(conferenceRecordingEntity), result)
    }

    @Test
    fun testConferenceCascade() = runTest {
        val conferenceRecordingEntity = ConferenceRecordingEntity(ConferenceRecording(recordingId = "recording1"), 1)

        conferenceRecodingDao.insert(conferenceRecordingEntity)

        conferenceDao.delete(ConferenceEntity(Conference(1), 1))

        val result = conferenceRecodingDao.findByConferenceId(1)

        assert(result.isEmpty())
    }

    @Test(expected = SQLiteConstraintException::class)
    fun testConferenceForeignKey() = runTest {
        val conferenceRecordingEntity = ConferenceRecordingEntity(ConferenceRecording(recordingId = "recording1"), 3)

        conferenceRecodingDao.insert(conferenceRecordingEntity)
    }
}
