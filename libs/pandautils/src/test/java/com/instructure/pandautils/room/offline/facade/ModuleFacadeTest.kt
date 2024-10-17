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
import com.instructure.canvasapi2.models.MasteryPath
import com.instructure.canvasapi2.models.ModuleCompletionRequirement
import com.instructure.canvasapi2.models.ModuleContentDetails
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.daos.ModuleCompletionRequirementDao
import com.instructure.pandautils.room.offline.daos.ModuleContentDetailsDao
import com.instructure.pandautils.room.offline.daos.ModuleItemDao
import com.instructure.pandautils.room.offline.daos.ModuleObjectDao
import com.instructure.pandautils.room.offline.entities.ModuleCompletionRequirementEntity
import com.instructure.pandautils.room.offline.entities.ModuleContentDetailsEntity
import com.instructure.pandautils.room.offline.entities.ModuleItemEntity
import com.instructure.pandautils.room.offline.entities.ModuleObjectEntity
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ModuleFacadeTest {

    private val moduleObjectDao: ModuleObjectDao = mockk(relaxed = true)
    private val moduleItemDao: ModuleItemDao = mockk(relaxed = true)
    private val completionRequirementDao: ModuleCompletionRequirementDao = mockk(relaxed = true)
    private val moduleContentDetailsDao: ModuleContentDetailsDao = mockk(relaxed = true)
    private val lockInfoFacade: LockInfoFacade = mockk(relaxed = true)
    private val masteryPathFacade: MasteryPathFacade = mockk(relaxed = true)
    private val offlineDatabase: OfflineDatabase = mockk(relaxed = true)

    private val moduleFacade = ModuleFacade(
        moduleObjectDao,
        moduleItemDao,
        completionRequirementDao,
        moduleContentDetailsDao,
        lockInfoFacade,
        masteryPathFacade,
        offlineDatabase
    )

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
    fun `insertModules inserts all the ModuleObject related entities into the database`() = runTest {
        val moduleObject = ModuleObject(
            id = 1, position = 1, name = "Module 1", items = listOf(
                ModuleItem(id = 2, position = 1, title = "Module 1 Item 1"),
                ModuleItem(
                    id = 3, position = 2, title = "Module 1 Item 2",
                    completionRequirement = ModuleCompletionRequirement(minScore = 10.0),
                    moduleDetails = ModuleContentDetails(lockAt = "2020-01-01T00:00:00Z", lockInfo = LockInfo(unlockAt = "2020-01-01T00:00:00Z")),
                    masteryPaths = MasteryPath(isLocked = true)
                )
            )
        )

        moduleFacade.insertModules(listOf(moduleObject), 1)

        val moduleObjectEntity = slot<ModuleObjectEntity>()
        coVerify { moduleObjectDao.insert(capture(moduleObjectEntity)) }
        Assert.assertEquals(moduleObject.id, moduleObjectEntity.captured.id)
        Assert.assertEquals(moduleObject.position, moduleObjectEntity.captured.position)
        Assert.assertEquals(moduleObject.name, moduleObjectEntity.captured.name)

        coVerify(exactly = 2) { moduleItemDao.insert(any()) }

        val completionRequirement = slot<ModuleCompletionRequirementEntity>()
        coVerify { completionRequirementDao.insert(capture(completionRequirement)) }
        Assert.assertEquals(moduleObject.items[1].completionRequirement?.minScore, completionRequirement.captured.minScore)

        val moduleContentDetails = slot<ModuleContentDetailsEntity>()
        coVerify { moduleContentDetailsDao.insert(capture(moduleContentDetails)) }
        Assert.assertEquals(moduleObject.items[1].moduleDetails?.lockAt, moduleContentDetails.captured.lockAt)

        coVerify { lockInfoFacade.insertLockInfoForModule(eq(LockInfo(unlockAt = "2020-01-01T00:00:00Z")), any()) }

        val masteryPath = slot<MasteryPath>()
        coVerify { masteryPathFacade.insertMasteryPath(capture(masteryPath), any()) }
        Assert.assertEquals(moduleObject.items[1].masteryPaths?.isLocked, masteryPath.captured.isLocked)
    }

    @Test
    fun `Build and return ModuleObjects for course from related database entities`() = runTest {
        coEvery { moduleObjectDao.findByCourseId(1) } returns listOf(ModuleObjectEntity(ModuleObject(id = 1, name = "Module"), 1))
        coEvery { moduleItemDao.findByModuleId(1) } returns listOf(ModuleItemEntity(ModuleItem(id = 2, title = "Item"), 1))
        coEvery { completionRequirementDao.findById(2) } returns ModuleCompletionRequirementEntity(ModuleCompletionRequirement(minScore = 10.0), 1, 1)
        coEvery { lockInfoFacade.getLockInfoByModuleId(2) } returns LockInfo(unlockAt = "2020-01-01T00:00:00Z")
        coEvery { moduleContentDetailsDao.findById(2) } returns ModuleContentDetailsEntity(
            ModuleContentDetails(pointsPossible = "10"), 2
        )
        coEvery { masteryPathFacade.getMasteryPath(2) } returns MasteryPath(isLocked = true)

        val result = moduleFacade.getModuleObjects(1)

        Assert.assertEquals(1, result.size)
        Assert.assertEquals(1, result.first().id)
        Assert.assertEquals("Module", result.first().name)
        Assert.assertEquals("Item", result.first().items.first().title)
        Assert.assertEquals(10.0, result.first().items.first().completionRequirement?.minScore)
        Assert.assertEquals("2020-01-01T00:00:00Z", result.first().items.first().moduleDetails?.lockInfo?.unlockAt)
        Assert.assertEquals("10", result.first().items.first().moduleDetails?.pointsPossible)
        Assert.assertEquals(true, result.first().items.first().masteryPaths?.isLocked)
    }

    @Test
    fun `Build and return ModuleObject for id from related database entities`() = runTest {
        coEvery { moduleObjectDao.findById(1) } returns ModuleObjectEntity(ModuleObject(id = 1, name = "Module"), 1)
        coEvery { moduleItemDao.findByModuleId(1) } returns listOf(ModuleItemEntity(ModuleItem(id = 2, title = "Item"), 1))
        coEvery { completionRequirementDao.findById(2) } returns ModuleCompletionRequirementEntity(ModuleCompletionRequirement(minScore = 10.0), 1, 1)
        coEvery { lockInfoFacade.getLockInfoByModuleId(2) } returns LockInfo(unlockAt = "2020-01-01T00:00:00Z")
        coEvery { moduleContentDetailsDao.findById(2) } returns ModuleContentDetailsEntity(
            ModuleContentDetails(pointsPossible = "10"), 2
        )
        coEvery { masteryPathFacade.getMasteryPath(2) } returns MasteryPath(isLocked = true)

        val result = moduleFacade.getModuleObjectById(1)

        Assert.assertEquals(1, result!!.id)
        Assert.assertEquals("Module", result.name)
        Assert.assertEquals("Item", result.items.first().title)
        Assert.assertEquals(10.0, result.items.first().completionRequirement?.minScore)
        Assert.assertEquals("2020-01-01T00:00:00Z", result.items.first().moduleDetails?.lockInfo?.unlockAt)
        Assert.assertEquals("10", result.items.first().moduleDetails?.pointsPossible)
        Assert.assertEquals(true, result.items.first().masteryPaths?.isLocked)
    }

    @Test
    fun `Build and return ModuleItems for module from related database entities`() = runTest {
        coEvery { moduleItemDao.findByModuleId(1) } returns listOf(ModuleItemEntity(ModuleItem(id = 2, title = "Item"), 1))
        coEvery { completionRequirementDao.findById(2) } returns ModuleCompletionRequirementEntity(ModuleCompletionRequirement(minScore = 10.0), 1, 1)
        coEvery { lockInfoFacade.getLockInfoByModuleId(2) } returns LockInfo(unlockAt = "2020-01-01T00:00:00Z")
        coEvery { moduleContentDetailsDao.findById(2) } returns ModuleContentDetailsEntity(
            ModuleContentDetails(pointsPossible = "10"), 2
        )
        coEvery { masteryPathFacade.getMasteryPath(2) } returns MasteryPath(isLocked = true)

        val result = moduleFacade.getModuleItems(1)

        Assert.assertEquals(1, result.size)
        Assert.assertEquals(2, result.first().id)
        Assert.assertEquals("Item", result.first().title)
        Assert.assertEquals(10.0, result.first().completionRequirement?.minScore)
        Assert.assertEquals("2020-01-01T00:00:00Z", result.first().moduleDetails?.lockInfo?.unlockAt)
        Assert.assertEquals("10", result.first().moduleDetails?.pointsPossible)
        Assert.assertEquals(true, result.first().masteryPaths?.isLocked)
    }

    @Test
    fun `Build and return ModuleItem for id from related database entities`() = runTest {
        coEvery { moduleItemDao.findById(2) } returns ModuleItemEntity(ModuleItem(id = 2, title = "Item"), 1)
        coEvery { completionRequirementDao.findById(2) } returns ModuleCompletionRequirementEntity(ModuleCompletionRequirement(minScore = 10.0), 1, 1)
        coEvery { lockInfoFacade.getLockInfoByModuleId(2) } returns LockInfo(unlockAt = "2020-01-01T00:00:00Z")
        coEvery { moduleContentDetailsDao.findById(2) } returns ModuleContentDetailsEntity(
            ModuleContentDetails(pointsPossible = "10"), 2
        )
        coEvery { masteryPathFacade.getMasteryPath(2) } returns MasteryPath(isLocked = true)

        val result = moduleFacade.getModuleItemById(2)

        Assert.assertEquals(2, result!!.id)
        Assert.assertEquals("Item", result.title)
        Assert.assertEquals(10.0, result.completionRequirement?.minScore)
        Assert.assertEquals("2020-01-01T00:00:00Z", result.moduleDetails?.lockInfo?.unlockAt)
        Assert.assertEquals("10", result.moduleDetails?.pointsPossible)
        Assert.assertEquals(true, result.masteryPaths?.isLocked)
    }

    @Test
    fun `Build and return ModuleItem for asset id and type from related database entities`() = runTest {
        coEvery { moduleItemDao.findByTypeAndContentId("Assignment", 1) } returns ModuleItemEntity(ModuleItem(id = 2, title = "Item"), 1)
        coEvery { completionRequirementDao.findById(2) } returns ModuleCompletionRequirementEntity(ModuleCompletionRequirement(minScore = 10.0), 1, 1)
        coEvery { lockInfoFacade.getLockInfoByModuleId(2) } returns LockInfo(unlockAt = "2020-01-01T00:00:00Z")
        coEvery { moduleContentDetailsDao.findById(2) } returns ModuleContentDetailsEntity(
            ModuleContentDetails(pointsPossible = "10"), 2
        )
        coEvery { masteryPathFacade.getMasteryPath(2) } returns MasteryPath(isLocked = true)

        val result = moduleFacade.getModuleItemByAssetIdAndType("Assignment", 1)

        Assert.assertEquals(2, result!!.id)
        Assert.assertEquals("Item", result.title)
        Assert.assertEquals(10.0, result.completionRequirement?.minScore)
        Assert.assertEquals("2020-01-01T00:00:00Z", result.moduleDetails?.lockInfo?.unlockAt)
        Assert.assertEquals("10", result.moduleDetails?.pointsPossible)
        Assert.assertEquals(true, result.masteryPaths?.isLocked)
    }

    @Test
    fun `Build and return ModuleItem for page from related database entities`() = runTest {
        coEvery { moduleItemDao.findByPageUrl("instructure.com") } returns ModuleItemEntity(ModuleItem(id = 2, title = "Item"), 1)
        coEvery { completionRequirementDao.findById(2) } returns ModuleCompletionRequirementEntity(ModuleCompletionRequirement(minScore = 10.0), 1, 1)
        coEvery { lockInfoFacade.getLockInfoByModuleId(2) } returns LockInfo(unlockAt = "2020-01-01T00:00:00Z")
        coEvery { moduleContentDetailsDao.findById(2) } returns ModuleContentDetailsEntity(
            ModuleContentDetails(pointsPossible = "10"), 2
        )
        coEvery { masteryPathFacade.getMasteryPath(2) } returns MasteryPath(isLocked = true)

        val result = moduleFacade.getModuleItemForPage("instructure.com")

        Assert.assertEquals(2, result!!.id)
        Assert.assertEquals("Item", result.title)
        Assert.assertEquals(10.0, result.completionRequirement?.minScore)
        Assert.assertEquals("2020-01-01T00:00:00Z", result.moduleDetails?.lockInfo?.unlockAt)
        Assert.assertEquals("10", result.moduleDetails?.pointsPossible)
        Assert.assertEquals(true, result.masteryPaths?.isLocked)
    }
}