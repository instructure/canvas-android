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

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentSet
import com.instructure.canvasapi2.models.MasteryPath
import com.instructure.canvasapi2.models.MasteryPathAssignment
import com.instructure.pandautils.room.offline.daos.AssignmentSetDao
import com.instructure.pandautils.room.offline.daos.MasteryPathAssignmentDao
import com.instructure.pandautils.room.offline.daos.MasteryPathDao
import com.instructure.pandautils.room.offline.entities.AssignmentSetEntity
import com.instructure.pandautils.room.offline.entities.MasteryPathAssignmentEntity
import com.instructure.pandautils.room.offline.entities.MasteryPathEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class MasteryPathFacadeTest {

    private val masteryPathDao: MasteryPathDao = mockk(relaxed = true)
    private val masteryPathAssignmentDao: MasteryPathAssignmentDao = mockk(relaxed = true)
    private val assignmentSetDao: AssignmentSetDao = mockk(relaxed = true)
    private val assignmentFacade: AssignmentFacade = mockk(relaxed = true)

    private val masteryPathFacade = MasteryPathFacade(masteryPathDao, masteryPathAssignmentDao, assignmentSetDao, assignmentFacade)

    @Test
    fun `insertMasteryPath inserts all the mastery path related entities into the database`() = runTest {
        val masteryPath = MasteryPath(isLocked = true, assignmentSets = arrayOf(
            AssignmentSet(id = 1, createdAt = "tegnap", assignments = arrayOf(
                MasteryPathAssignment(id = 1, assignmentId = 1, model = null),
                MasteryPathAssignment(id = 2, assignmentId = 2, model = Assignment(id = 420, name = "Assignment"))
            )),
            AssignmentSet(id = 2, createdAt = "ma", assignments = arrayOf(
                MasteryPathAssignment(id = 3, assignmentId = 3, model = null)
            ))
        ))

        masteryPathFacade.insertMasteryPath(masteryPath, 1)

        val masteryPathEntity = slot<MasteryPathEntity>()
        coVerify { masteryPathDao.insert(capture(masteryPathEntity)) }
        Assert.assertTrue(masteryPathEntity.captured.isLocked)

        coVerify(exactly = 2) { assignmentSetDao.insert(any()) }
        coVerify(exactly = 3) { masteryPathAssignmentDao.insert(any()) }

        val assignment = slot<Assignment>()
        coVerify { assignmentFacade.insertAssignment(capture(assignment)) }
        Assert.assertEquals(420, assignment.captured.id)
        Assert.assertEquals("Assignment", assignment.captured.name)
    }

    @Test
    fun `Build and return MasteryPath for module item from related database entities`() = runTest {
        coEvery { masteryPathDao.findById(1) } returns MasteryPathEntity(MasteryPath(isLocked = true), 1)
        coEvery { assignmentSetDao.findByMasteryPathId(1) } returns listOf(
            AssignmentSetEntity(AssignmentSet(id = 1, createdAt = "tegnap"), 1)
        )
        coEvery { masteryPathAssignmentDao.findByAssignmentSetId(1) } returns listOf(
            MasteryPathAssignmentEntity(MasteryPathAssignment(id = 1, assignmentId = 420)),
        )
        coEvery { assignmentFacade.getAssignmentById(420) } returns Assignment(id = 420, name = "Assignment")


        val result = masteryPathFacade.getMasteryPath(1)

        Assert.assertTrue(result!!.isLocked)
        Assert.assertEquals(1, result.assignmentSets!!.size)
        Assert.assertEquals("tegnap", result.assignmentSets!!.first()!!.createdAt)
        Assert.assertEquals(1, result.assignmentSets!![0]!!.assignments.size)
        Assert.assertEquals(1, result.assignmentSets!![0]!!.assignments.first().id)
        Assert.assertEquals(420, result.assignmentSets!![0]!!.assignments.first().assignmentId)
        Assert.assertEquals(420, result.assignmentSets!![0]!!.assignments.first().assignment!!.id)
        Assert.assertEquals("Assignment", result.assignmentSets!![0]!!.assignments.first().assignment!!.name)
    }
}