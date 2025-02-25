package com.instructure.pandautils.room.offline.facade

import androidx.room.withTransaction
import com.instructure.canvasapi2.models.Conference
import com.instructure.canvasapi2.models.ConferenceRecording
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.daos.ConferenceDao
import com.instructure.pandautils.room.offline.daos.ConferenceRecodingDao
import com.instructure.pandautils.room.offline.entities.ConferenceEntity
import com.instructure.pandautils.room.offline.entities.ConferenceRecordingEntity
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ConferenceFacadeTest {

    private val conferenceDao: ConferenceDao = mockk(relaxed = true)
    private val conferenceRecordingDao: ConferenceRecodingDao = mockk(relaxed = true)
    private val offlineDatabase: OfflineDatabase = mockk(relaxed = true)

    private val facade = ConferenceFacade(conferenceDao, conferenceRecordingDao, offlineDatabase)

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        mockkStatic(
            "androidx.room.RoomDatabaseKt"
        )

        val transactionLambda = slot<suspend () -> Unit>()
        coEvery { offlineDatabase.withTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Calling insertConferences should insert conferences and related entities`() = runTest {
        val recordings = listOf(ConferenceRecording(recordingId = "recording 1"), ConferenceRecording(recordingId = "recording 2"))
        val conferences = listOf(Conference(id = 1, conferenceKey = "key 1", recordings = recordings), Conference(id = 2, conferenceKey = "key 2"))

        coEvery { conferenceDao.insert(any()) } just Runs
        coEvery { conferenceRecordingDao.insert(any()) } just Runs

        facade.insertConferences(conferences, 1)

        coVerify { conferenceDao.insertAll(conferences.map { ConferenceEntity(it, 1) }) }
        recordings.forEach {
            coVerify { conferenceRecordingDao.insert(ConferenceRecordingEntity(it, 1)) }
        }
    }

    @Test
    fun `Calling getConferencesByCourseId should return conferences by the specified CourseId`() = runTest {
        val recordings = listOf(ConferenceRecording(recordingId = "recording 1"), ConferenceRecording(recordingId = "recording 2"))
        val conferences = listOf(Conference(id = 1, conferenceKey = "key 1", recordings = recordings), Conference(id = 2, conferenceKey = "key 2"))

        coEvery { conferenceDao.findByCourseId(any()) } returns conferences.map { ConferenceEntity(it, 1) }
        coEvery { conferenceRecordingDao.findByConferenceId(1) } returns recordings.map { ConferenceRecordingEntity(it, 1) }
        coEvery { conferenceRecordingDao.findByConferenceId(2) } returns emptyList()

        val result = facade.getConferencesByCourseId(1)

        Assert.assertEquals(conferences, result)
    }
}
