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

import com.instructure.pandautils.room.offline.daos.CourseSyncProgressDao
import com.instructure.pandautils.room.offline.daos.FileSyncProgressDao
import com.instructure.pandautils.room.offline.daos.StudioMediaProgressDao
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DismissSyncProgressUseCaseTest {

    private val fileSyncProgressDao: FileSyncProgressDao = mockk(relaxed = true)
    private val courseSyncProgressDao: CourseSyncProgressDao = mockk(relaxed = true)
    private val studioMediaProgressDao: StudioMediaProgressDao = mockk(relaxed = true)

    private lateinit var useCase: DismissSyncProgressUseCase

    @Before
    fun setup() {
        useCase = DismissSyncProgressUseCase(
            fileSyncProgressDao,
            courseSyncProgressDao,
            studioMediaProgressDao
        )
    }

    @Test
    fun `Deletes all file sync progress`() = runTest {
        useCase()

        coVerify { fileSyncProgressDao.deleteAll() }
    }

    @Test
    fun `Deletes all course sync progress`() = runTest {
        useCase()

        coVerify { courseSyncProgressDao.deleteAll() }
    }

    @Test
    fun `Deletes all studio media progress`() = runTest {
        useCase()

        coVerify { studioMediaProgressDao.deleteAll() }
    }

    @Test
    fun `Deletes all progress data in correct order`() = runTest {
        useCase()

        coVerify(ordering = io.mockk.Ordering.SEQUENCE) {
            fileSyncProgressDao.deleteAll()
            courseSyncProgressDao.deleteAll()
            studioMediaProgressDao.deleteAll()
        }
    }
}