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

import com.instructure.canvasapi2.models.MasteryPath
import com.instructure.pandautils.room.offline.daos.AssignmentSetDao
import com.instructure.pandautils.room.offline.daos.MasteryPathAssignmentDao
import com.instructure.pandautils.room.offline.daos.MasteryPathDao
import com.instructure.pandautils.room.offline.entities.AssignmentSetEntity
import com.instructure.pandautils.room.offline.entities.MasteryPathAssignmentEntity
import com.instructure.pandautils.room.offline.entities.MasteryPathEntity

class MasteryPathFacade(
    private val masteryPathDao: MasteryPathDao,
    private val masteryPathAssignmentDao: MasteryPathAssignmentDao,
    private val assignmentSetDao: AssignmentSetDao,
    private val assignmentFacade: AssignmentFacade
) {

    suspend fun insertMasteryPath(masteryPath: MasteryPath, moduleItemId: Long) {
        val masteryPathEntity = MasteryPathEntity(masteryPath, moduleItemId)
        masteryPathDao.insert(masteryPathEntity)
        masteryPath.assignmentSets?.filterNotNull()?.forEach { assignmentSet ->
            assignmentSetDao.insert(AssignmentSetEntity(assignmentSet, masteryPathEntity.id))
            assignmentSet.assignments.forEach { masteryPathAssignment ->
                masteryPathAssignmentDao.insert(MasteryPathAssignmentEntity(masteryPathAssignment))
                masteryPathAssignment.model?.let { assignment ->
                    assignmentFacade.insertAssignment(assignment)
                }
            }
        }
    }

    suspend fun getMasteryPath(moduleItemId: Long): MasteryPath? {
        val masteryPath = masteryPathDao.findById(moduleItemId)

        val assignmentSets = masteryPath?.let {
            val assignmentSets = assignmentSetDao.findByMasteryPathId(it.id)
            assignmentSets.map { assignmentSet ->
                val masteryPathAssignments = masteryPathAssignmentDao.findByAssignmentSetId(assignmentSet.id)
                    .map { masteryPathAssignment ->
                        val assignment = assignmentFacade.getAssignmentById(masteryPathAssignment.assignmentId)
                        masteryPathAssignment.toApiModel(assignment)
                    }
                assignmentSet.toApiModel(masteryPathAssignments)
            }
        }.orEmpty()

        return masteryPath?.toApiModel(assignmentSets)
    }
}