/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 *
 */
package com.instructure.pandautils.room.offline.facade

import androidx.room.withTransaction
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.pandautils.room.offline.OfflineDatabase
import com.instructure.pandautils.room.offline.daos.AssignmentOverrideDao
import com.instructure.pandautils.room.offline.daos.ScheduleItemAssignmentOverrideDao
import com.instructure.pandautils.room.offline.daos.ScheduleItemDao
import com.instructure.pandautils.room.offline.entities.AssignmentOverrideEntity
import com.instructure.pandautils.room.offline.entities.ScheduleItemAssignmentOverrideEntity
import com.instructure.pandautils.room.offline.entities.ScheduleItemEntity

class ScheduleItemFacade(
    private val scheduleItemDao: ScheduleItemDao,
    private val assignmentOverrideDao: AssignmentOverrideDao,
    private val scheduleItemAssignmentOverrideDao: ScheduleItemAssignmentOverrideDao,
    private val assignmentFacade: AssignmentFacade,
    private val offlineDatabase: OfflineDatabase
) {
    suspend fun insertScheduleItems(scheduleItems: List<ScheduleItem>, courseId: Long) {
        offlineDatabase.withTransaction {
            deleteAllByCourseId(courseId)

            scheduleItems.forEach { scheduleItem ->
                scheduleItemDao.insert(ScheduleItemEntity(scheduleItem, courseId))

                scheduleItem.subAssignment?.let { subAssignment ->
                    assignmentFacade.insertAssignment(subAssignment)
                }

                scheduleItem.assignmentOverrides?.let { assignmentOverrides ->
                    assignmentOverrides.forEach { assignmentOverride ->
                        assignmentOverride?.let {
                            assignmentOverrideDao.insert(AssignmentOverrideEntity(it))
                            scheduleItemAssignmentOverrideDao.insert(
                                ScheduleItemAssignmentOverrideEntity(
                                    it.id,
                                    scheduleItem.itemId
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    suspend fun findByItemType(contextCodes: List<String>, itemType: String): List<ScheduleItem> {
        val entities = scheduleItemDao.findByItemType(contextCodes, itemType)
        return entities.map { scheduleItemEntity ->
            val assignment = scheduleItemEntity.assignmentId?.let { assignmentId -> assignmentFacade.getAssignmentById(assignmentId) }
            val subAssignment = scheduleItemEntity.subAssignmentId?.let { subAssignmentId -> assignmentFacade.getAssignmentById(subAssignmentId) }
            val assignmentOverrideIds = scheduleItemAssignmentOverrideDao.findByScheduleItemId(scheduleItemEntity.id).map { it.assignmentOverrideId }
            val assignmentOverrides = assignmentOverrideDao.findByIds(assignmentOverrideIds).map { it.toApiModel() }
            scheduleItemEntity.toApiModel(assignmentOverrides, assignment, subAssignment)
        }
    }

    suspend fun deleteAllByCourseId(courseId: Long) {
        scheduleItemDao.deleteAllByCourseId(courseId)
    }
}