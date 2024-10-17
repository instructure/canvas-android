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

package com.instructure.pandautils.room.offline.facade

import androidx.room.withTransaction
import com.instructure.canvasapi2.models.LockInfo
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.daos.PageDao
import com.instructure.pandautils.room.offline.entities.PageEntity
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
import java.util.Date

class PageFacadeTest {

    private val pageDao: PageDao = mockk(relaxed = true)
    private val lockInfoFacade: LockInfoFacade = mockk(relaxed = true)
    private val offlineDatabase: OfflineDatabase = mockk(relaxed = true)

    private val facade = PageFacade(pageDao, lockInfoFacade, offlineDatabase)

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
    fun `Calling insertPages should insert pages and related entities`() = runTest {
        val lockInfo = LockInfo(unlockAt = Date().toApiString())
        val pages = listOf(Page(id = 1L, title = "Page 1", lockInfo = lockInfo), Page(id = 2L, title = "Page 2"))

        coEvery { pageDao.insertAll(any()) } just Runs
        coEvery { lockInfoFacade.insertLockInfoForPage(any(), any()) } just Runs

        facade.insertPages(pages, 1L)

        coVerify { pageDao.insertAll(pages.map { PageEntity(it, 1L) }) }
        coVerify { lockInfoFacade.insertLockInfoForPage(lockInfo, 1L) }
    }

    @Test
    fun `Calling insertPage should insert page and related entities`() = runTest {
        val lockInfo = LockInfo(unlockAt = Date().toApiString())
        val page = Page(id = 1L, title = "Page 1", lockInfo = lockInfo)

        facade.insertPage(page, 1L)

        coVerify { pageDao.insert(PageEntity(page, 1L)) }
        coVerify { lockInfoFacade.insertLockInfoForPage(lockInfo, 1L) }
    }

    @Test
    fun `Calling findByCourseId should return pages with the specified course ID`() = runTest {
        val lockInfo = LockInfo(unlockAt = Date().toApiString())
        val pages = listOf(Page(id = 1L, title = "Page 1", lockInfo = lockInfo), Page(id = 2L, title = "Page 2", lockInfo = lockInfo))

        coEvery { pageDao.findByCourseId(1L) } returns pages.map { PageEntity(it, 1L) }
        coEvery { lockInfoFacade.getLockInfoByPageId(any()) } returns lockInfo

        val result = facade.findByCourseId(1L)

        Assert.assertEquals(pages, result)
    }

    @Test
    fun `Calling getFrontPage should return front page with the specified course ID`() = runTest {
        val lockInfo = LockInfo(unlockAt = Date().toApiString())
        val page = Page(id = 1L, title = "Page 1", lockInfo = lockInfo)

        coEvery { pageDao.getFrontPage(1L) } returns PageEntity(page, 1L)
        coEvery { lockInfoFacade.getLockInfoByPageId(any()) } returns lockInfo

        val result = facade.getFrontPage(1L)

        Assert.assertEquals(page, result)
    }

    @Test
    fun `Calling getPageDetails should return page with the specified course ID`() = runTest {
        val lockInfo = LockInfo(unlockAt = Date().toApiString())
        val page = Page(id = 1L, title = "Page 1", lockInfo = lockInfo)

        coEvery { pageDao.getPageDetails(1L, any()) } returns PageEntity(page, 1L)
        coEvery { lockInfoFacade.getLockInfoByPageId(any()) } returns lockInfo

        val result = facade.getPageDetails(1L, "id")

        Assert.assertEquals(page, result)
    }
}
