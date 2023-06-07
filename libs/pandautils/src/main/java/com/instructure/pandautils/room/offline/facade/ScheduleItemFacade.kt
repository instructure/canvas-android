package com.instructure.pandautils.room.offline.facade

import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.pandautils.room.offline.daos.AssignmentDao
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
    private val assignmentDao: AssignmentDao
) {

    suspend fun insertScheduleItems(scheduleItems: List<ScheduleItem>) {
        scheduleItems.forEach { scheduleItem ->
            scheduleItemDao.insert(ScheduleItemEntity(scheduleItem))

            scheduleItem.assignmentOverrides?.let { assignmentOverrides ->
                assignmentOverrides.forEach { assignmentOverride ->
                    assignmentOverride?.let {
                        assignmentOverrideDao.insert(AssignmentOverrideEntity(it))
                        scheduleItemAssignmentOverrideDao.insert(
                            ScheduleItemAssignmentOverrideEntity(
                                it.id,
                                scheduleItem.id
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun findByItemType(contextCodes: List<String>, itemType: String): List<ScheduleItem> {
        val entities = scheduleItemDao.findByItemType(contextCodes, itemType)
        return entities.map {
            val assignment = it.assignmentId?.let { assignmentId ->
                assignmentDao.findById(assignmentId)?.toApiModel()
            }
            val assignmentOverrideIds = scheduleItemAssignmentOverrideDao.findByScheduleItemId(it.id).map { it.assignmentOverrideId }
            val assignmentOverrides = assignmentOverrideDao.findByIds(assignmentOverrideIds).map { it.toApiModel() }
            it.toApiModel(assignmentOverrides, assignment)
        }
    }
}