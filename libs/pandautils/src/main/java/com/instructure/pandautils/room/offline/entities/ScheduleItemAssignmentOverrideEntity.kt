package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["assignmentOverrideId", "scheduleItemId"],
    foreignKeys = [
        ForeignKey(
            entity = AssignmentOverrideEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignmentOverrideId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ScheduleItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ScheduleItemAssignmentOverrideEntity(
    val assignmentOverrideId: Long,
    val scheduleItemId: Long
)