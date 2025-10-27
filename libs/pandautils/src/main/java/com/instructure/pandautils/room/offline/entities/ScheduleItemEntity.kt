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

package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentOverride
import com.instructure.canvasapi2.models.ScheduleItem

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ScheduleItemEntity(
    @PrimaryKey
    val id: String,
    val title: String?,
    val description: String?,
    val startAt: String?,
    val endAt: String?,
    val isAllDay: Boolean,
    val allDayAt: String?,
    val locationAddress: String?,
    val locationName: String?,
    val htmlUrl: String?,
    val contextCode: String?,
    val effectiveContextCode: String?,
    val isHidden: Boolean,
    val importantDates: Boolean,
    val assignmentId: Long?,
    val subAssignmentId: Long?,
    val type: String,
    val itemType: String?,
    val courseId: Long
) {

    constructor(scheduleItem: ScheduleItem, courseId: Long) : this(
        scheduleItem.itemId,
        scheduleItem.title,
        scheduleItem.description,
        scheduleItem.startAt,
        scheduleItem.endAt,
        scheduleItem.isAllDay,
        scheduleItem.allDayAt,
        scheduleItem.locationAddress,
        scheduleItem.locationName,
        scheduleItem.htmlUrl,
        scheduleItem.contextCode,
        scheduleItem.effectiveContextCode,
        scheduleItem.isHidden,
        scheduleItem.importantDates,
        scheduleItem.assignment?.id,
        scheduleItem.subAssignment?.id,
        scheduleItem.type,
        scheduleItem.itemType?.name,
        courseId
    )

    fun toApiModel(assignmentOverrides: List<AssignmentOverride>?, assignment: Assignment?, subAssignment: Assignment?): ScheduleItem {
        return ScheduleItem(
            itemId = id,
            title = title,
            description = description,
            startAt = startAt,
            endAt = endAt,
            isAllDay = isAllDay,
            allDayAt = allDayAt,
            locationAddress = locationAddress,
            locationName = locationName,
            htmlUrl = htmlUrl,
            contextCode = contextCode,
            effectiveContextCode = effectiveContextCode,
            isHidden = isHidden,
            assignmentOverrides = assignmentOverrides,
            importantDates = importantDates,
            itemType = itemType?.let { ScheduleItem.Type.valueOf(it) } ?: ScheduleItem.Type.TYPE_CALENDAR,
            assignment = assignment,
            subAssignment = subAssignment
        )
    }
}