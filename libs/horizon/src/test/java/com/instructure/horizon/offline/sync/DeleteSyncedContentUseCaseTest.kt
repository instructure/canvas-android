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
package com.instructure.horizon.offline.sync

import android.content.Context
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.database.dao.HorizonAnnouncementDao
import com.instructure.horizon.database.dao.HorizonCourseSyncPlanDao
import com.instructure.horizon.database.dao.HorizonFileSyncPlanDao
import com.instructure.horizon.database.dao.HorizonLocalImageDao
import com.instructure.horizon.database.dao.HorizonNotificationStreamItemDao
import com.instructure.horizon.database.entity.HorizonCourseSyncPlanEntity
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DeleteSyncedContentUseCaseTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private val context: Context = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)

    @Before
    fun setUp() {
        every { context.filesDir } returns tempFolder.root
    }
    private val syncHelper: HorizonOfflineSyncHelper = mockk(relaxed = true)
    private val courseSyncPlanDao: HorizonCourseSyncPlanDao = mockk(relaxed = true)
    private val fileSyncPlanDao: HorizonFileSyncPlanDao = mockk(relaxed = true)
    private val localImageDao: HorizonLocalImageDao = mockk(relaxed = true)
    private val notificationStreamItemDao: HorizonNotificationStreamItemDao = mockk(relaxed = true)
    private val announcementDao: HorizonAnnouncementDao = mockk(relaxed = true)
    private val courseCleanupHelper: CourseCleanupHelper = mockk(relaxed = true)

    private val useCase = DeleteSyncedContentUseCase(
        context = context,
        apiPrefs = apiPrefs,
        syncHelper = syncHelper,
        courseSyncPlanDao = courseSyncPlanDao,
        fileSyncPlanDao = fileSyncPlanDao,
        localImageDao = localImageDao,
        notificationStreamItemDao = notificationStreamItemDao,
        announcementDao = announcementDao,
        courseCleanupHelper = courseCleanupHelper,
    )

    @Test
    fun `awaits sync stop before deleting any data`() = runTest {
        every { apiPrefs.user } returns User(id = 1L)
        coEvery { courseSyncPlanDao.findAll() } returns emptyList()

        useCase()

        // fullStop() must be the very first operation — otherwise a still-running worker
        // could race against the DAO deletes and produce orphan rows.
        coVerifyOrder {
            syncHelper.fullStop()
            courseSyncPlanDao.deleteAll()
        }
    }

    @Test
    fun `cleans up each planned course before wiping the plan table`() = runTest {
        every { apiPrefs.user } returns User(id = 1L)
        coEvery { courseSyncPlanDao.findAll() } returns listOf(
            plan(11L), plan(22L), plan(33L),
        )

        useCase()

        coVerifyOrder {
            courseCleanupHelper.cleanupCourseContent(11L)
            courseCleanupHelper.cleanupCourseContent(22L)
            courseCleanupHelper.cleanupCourseContent(33L)
            courseSyncPlanDao.deleteAll()
        }
    }

    @Test
    fun `wipes all per-table caches after stopping the worker`() = runTest {
        every { apiPrefs.user } returns User(id = 1L)
        coEvery { courseSyncPlanDao.findAll() } returns emptyList()

        useCase()

        coVerify { fileSyncPlanDao.deleteAll() }
        coVerify { localImageDao.deleteAll() }
        coVerify { notificationStreamItemDao.deleteAll() }
        coVerify { announcementDao.deleteAll() }
        coVerify { announcementDao.deleteAllComments() }
    }

    private fun plan(courseId: Long) = HorizonCourseSyncPlanEntity(
        courseId = courseId,
        courseName = "Course $courseId",
    )
}
