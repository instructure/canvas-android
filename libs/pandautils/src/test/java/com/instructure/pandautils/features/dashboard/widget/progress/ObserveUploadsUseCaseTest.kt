/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.progress

import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.features.file.upload.worker.FileUploadWorker
import com.instructure.pandautils.room.appdatabase.daos.DashboardFileUploadDao
import com.instructure.pandautils.room.appdatabase.entities.DashboardFileUploadEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveUploadsUseCaseTest {

    private val dashboardFileUploadDao: DashboardFileUploadDao = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    private lateinit var useCase: ObserveUploadsUseCase

    @Before
    fun setup() {
        every { apiPrefs.user } returns User(id = 123L)
        useCase = ObserveUploadsUseCase(dashboardFileUploadDao, workManager, apiPrefs)
    }

    @Test
    fun `Returns empty list when no uploads exist`() = runTest {
        every { dashboardFileUploadDao.getAllForUserAsFlow(123L) } returns flowOf(emptyList())

        val result = useCase().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `Returns empty list when all entities have invalid worker IDs`() = runTest {
        val entities = listOf(
            DashboardFileUploadEntity(
                workerId = "invalid-uuid",
                userId = 123L,
                title = "Test",
                subtitle = null,
                courseId = null,
                assignmentId = null,
                attemptId = null,
                folderId = null
            )
        )
        every { dashboardFileUploadDao.getAllForUserAsFlow(123L) } returns flowOf(entities)

        val result = useCase().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `Maps RUNNING work state to UPLOADING`() = runTest {
        val workerId = UUID.randomUUID()
        val entity = createEntity(workerId)
        val workInfo = createWorkInfo(workerId, WorkInfo.State.RUNNING, progress = 50, fullSize = 100)

        every { dashboardFileUploadDao.getAllForUserAsFlow(123L) } returns flowOf(listOf(entity))
        every { workManager.getWorkInfoByIdFlow(workerId) } returns flowOf(workInfo)

        val result = useCase().first()

        assertEquals(1, result.size)
        assertEquals(UploadState.UPLOADING, result[0].state)
        assertEquals(50, result[0].progress)
        assertEquals(R.drawable.ic_upload, result[0].icon)
        assertEquals(R.color.backgroundInfo, result[0].iconBackground)
    }

    @Test
    fun `Maps SUCCEEDED work state to SUCCEEDED`() = runTest {
        val workerId = UUID.randomUUID()
        val entity = createEntity(workerId)
        val workInfo = createWorkInfo(workerId, WorkInfo.State.SUCCEEDED)

        every { dashboardFileUploadDao.getAllForUserAsFlow(123L) } returns flowOf(listOf(entity))
        every { workManager.getWorkInfoByIdFlow(workerId) } returns flowOf(workInfo)

        val result = useCase().first()

        assertEquals(1, result.size)
        assertEquals(UploadState.SUCCEEDED, result[0].state)
        assertEquals(R.drawable.ic_check_white_24dp, result[0].icon)
        assertEquals(R.color.backgroundSuccess, result[0].iconBackground)
    }

    @Test
    fun `Maps FAILED work state to FAILED`() = runTest {
        val workerId = UUID.randomUUID()
        val entity = createEntity(workerId)
        val workInfo = createWorkInfo(workerId, WorkInfo.State.FAILED)

        every { dashboardFileUploadDao.getAllForUserAsFlow(123L) } returns flowOf(listOf(entity))
        every { workManager.getWorkInfoByIdFlow(workerId) } returns flowOf(workInfo)

        val result = useCase().first()

        assertEquals(1, result.size)
        assertEquals(UploadState.FAILED, result[0].state)
        assertEquals(R.drawable.ic_exclamation_mark, result[0].icon)
        assertEquals(R.color.backgroundDanger, result[0].iconBackground)
    }

    @Test
    fun `Calculates progress correctly`() = runTest {
        val workerId = UUID.randomUUID()
        val entity = createEntity(workerId)
        val workInfo = createWorkInfo(workerId, WorkInfo.State.RUNNING, progress = 75, fullSize = 100)

        every { dashboardFileUploadDao.getAllForUserAsFlow(123L) } returns flowOf(listOf(entity))
        every { workManager.getWorkInfoByIdFlow(workerId) } returns flowOf(workInfo)

        val result = useCase().first()

        assertEquals(75, result[0].progress)
    }

    @Test
    fun `Progress is clamped to 100`() = runTest {
        val workerId = UUID.randomUUID()
        val entity = createEntity(workerId)
        val workInfo = createWorkInfo(workerId, WorkInfo.State.RUNNING, progress = 150, fullSize = 100)

        every { dashboardFileUploadDao.getAllForUserAsFlow(123L) } returns flowOf(listOf(entity))
        every { workManager.getWorkInfoByIdFlow(workerId) } returns flowOf(workInfo)

        val result = useCase().first()

        assertEquals(100, result[0].progress)
    }

    @Test
    fun `Progress is 0 when full size is 0`() = runTest {
        val workerId = UUID.randomUUID()
        val entity = createEntity(workerId)
        val workInfo = createWorkInfo(workerId, WorkInfo.State.RUNNING, progress = 50, fullSize = 0)

        every { dashboardFileUploadDao.getAllForUserAsFlow(123L) } returns flowOf(listOf(entity))
        every { workManager.getWorkInfoByIdFlow(workerId) } returns flowOf(workInfo)

        val result = useCase().first()

        assertEquals(0, result[0].progress)
    }

    @Test
    fun `Maps entity fields correctly to UploadProgressItem`() = runTest {
        val workerId = UUID.randomUUID()
        val entity = DashboardFileUploadEntity(
            workerId = workerId.toString(),
            userId = 123L,
            title = "Test Title",
            subtitle = "Test Subtitle",
            courseId = 456L,
            assignmentId = 789L,
            attemptId = 1L,
            folderId = null
        )
        val workInfo = createWorkInfo(workerId, WorkInfo.State.RUNNING)

        every { dashboardFileUploadDao.getAllForUserAsFlow(123L) } returns flowOf(listOf(entity))
        every { workManager.getWorkInfoByIdFlow(workerId) } returns flowOf(workInfo)

        val result = useCase().first()

        assertEquals("Test Title", result[0].title)
        assertEquals("Test Subtitle", result[0].subtitle)
        assertEquals(456L, result[0].courseId)
        assertEquals(789L, result[0].assignmentId)
        assertEquals(1L, result[0].attemptId)
        assertEquals(null, result[0].folderId)
        assertEquals(workerId, result[0].workerId)
    }

    @Test
    fun `Uses default user ID when user is null`() = runTest {
        every { apiPrefs.user } returns null
        every { dashboardFileUploadDao.getAllForUserAsFlow(0L) } returns flowOf(emptyList())

        val result = useCase().first()

        assertTrue(result.isEmpty())
    }

    private fun createEntity(workerId: UUID) = DashboardFileUploadEntity(
        workerId = workerId.toString(),
        userId = 123L,
        title = "Test Upload",
        subtitle = "Uploading...",
        courseId = null,
        assignmentId = null,
        attemptId = null,
        folderId = null
    )

    private fun createWorkInfo(
        workerId: UUID,
        state: WorkInfo.State,
        progress: Long = 0,
        fullSize: Long = 1
    ): WorkInfo {
        val progressData = Data.Builder()
            .putLong(FileUploadWorker.PROGRESS_DATA_UPLOADED_SIZE, progress)
            .putLong(FileUploadWorker.PROGRESS_DATA_FULL_SIZE, fullSize)
            .build()

        return WorkInfo(
            id = workerId,
            state = state,
            tags = emptySet(),
            outputData = Data.EMPTY,
            progress = progressData,
            runAttemptCount = 0,
            generation = 0,
            constraints = mockk(relaxed = true),
            initialDelayMillis = 0,
            periodicityInfo = null,
            nextScheduleTimeMillis = Long.MAX_VALUE,
            stopReason = WorkInfo.STOP_REASON_NOT_STOPPED
        )
    }
}