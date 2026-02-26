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

import androidx.room.withTransaction
import com.instructure.pandautils.features.file.upload.FileUploadUtilsHelper
import com.instructure.pandautils.room.appdatabase.AppDatabase
import com.instructure.pandautils.room.appdatabase.daos.DashboardFileUploadDao
import com.instructure.pandautils.room.appdatabase.daos.FileUploadInputDao
import com.instructure.pandautils.room.appdatabase.entities.DashboardFileUploadEntity
import com.instructure.pandautils.room.appdatabase.entities.FileUploadInputEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID

class DismissUploadUseCaseTest {

    private val appDatabase: AppDatabase = mockk(relaxed = true)
    private val dashboardFileUploadDao: DashboardFileUploadDao = mockk(relaxed = true)
    private val fileUploadInputDao: FileUploadInputDao = mockk(relaxed = true)
    private val fileUploadUtilsHelper: FileUploadUtilsHelper = mockk(relaxed = true)

    private lateinit var useCase: DismissUploadUseCase

    @Before
    fun setup() {
        mockkStatic("androidx.room.RoomDatabaseKt")

        val transactionLambda = slot<suspend () -> Unit>()
        coEvery { appDatabase.withTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }

        useCase = DismissUploadUseCase(
            appDatabase,
            dashboardFileUploadDao,
            fileUploadInputDao,
            fileUploadUtilsHelper
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Deletes dashboard file upload entity when found`() = runTest {
        val workerId = UUID.randomUUID()
        val entity = DashboardFileUploadEntity(
            workerId = workerId.toString(),
            userId = 123L,
            title = "Test Upload",
            subtitle = "Uploading...",
            courseId = null,
            assignmentId = null,
            attemptId = null,
            folderId = null
        )
        coEvery { dashboardFileUploadDao.findByWorkerId(workerId.toString()) } returns entity
        coEvery { fileUploadInputDao.findByWorkerId(workerId.toString()) } returns null

        useCase(workerId)

        coVerify { dashboardFileUploadDao.delete(entity) }
    }

    @Test
    fun `Does not delete dashboard file upload entity when not found`() = runTest {
        val workerId = UUID.randomUUID()
        coEvery { dashboardFileUploadDao.findByWorkerId(workerId.toString()) } returns null
        coEvery { fileUploadInputDao.findByWorkerId(workerId.toString()) } returns null

        useCase(workerId)

        coVerify(exactly = 0) { dashboardFileUploadDao.delete(any()) }
    }

    @Test
    fun `Deletes cached files and file upload input when found`() = runTest {
        val workerId = UUID.randomUUID()
        val filePaths = listOf("/path/to/file1.txt", "/path/to/file2.txt")
        val inputEntity = FileUploadInputEntity(
            workerId = workerId.toString(),
            action = "UPLOAD",
            filePaths = filePaths
        )
        coEvery { dashboardFileUploadDao.findByWorkerId(workerId.toString()) } returns null
        coEvery { fileUploadInputDao.findByWorkerId(workerId.toString()) } returns inputEntity

        useCase(workerId)

        coVerify { fileUploadUtilsHelper.deleteCachedFiles(filePaths) }
        coVerify { fileUploadInputDao.delete(inputEntity) }
    }

    @Test
    fun `Deletes both dashboard upload and file input when both found`() = runTest {
        val workerId = UUID.randomUUID()
        val dashboardEntity = DashboardFileUploadEntity(
            workerId = workerId.toString(),
            userId = 123L,
            title = "Test Upload",
            subtitle = null,
            courseId = null,
            assignmentId = null,
            attemptId = null,
            folderId = null
        )
        val filePaths = listOf("/path/to/file.txt")
        val inputEntity = FileUploadInputEntity(
            workerId = workerId.toString(),
            action = "UPLOAD",
            filePaths = filePaths
        )
        coEvery { dashboardFileUploadDao.findByWorkerId(workerId.toString()) } returns dashboardEntity
        coEvery { fileUploadInputDao.findByWorkerId(workerId.toString()) } returns inputEntity

        useCase(workerId)

        coVerify { dashboardFileUploadDao.delete(dashboardEntity) }
        coVerify { fileUploadUtilsHelper.deleteCachedFiles(filePaths) }
        coVerify { fileUploadInputDao.delete(inputEntity) }
    }

    @Test
    fun `Operations are wrapped in transaction`() = runTest {
        val workerId = UUID.randomUUID()
        coEvery { dashboardFileUploadDao.findByWorkerId(workerId.toString()) } returns null
        coEvery { fileUploadInputDao.findByWorkerId(workerId.toString()) } returns null

        useCase(workerId)

        coVerify { appDatabase.withTransaction(any<suspend () -> Unit>()) }
    }
}