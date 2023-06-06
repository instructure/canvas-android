package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.AssignmentOverride
import com.instructure.canvasapi2.models.ScheduleItem

@Entity
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
    val type: String,
    val itemType: String?
) {

    constructor(scheduleItem: ScheduleItem) :this(
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
        scheduleItem.type,
        scheduleItem.itemType?.name
    )

    fun toApiModel(assignmentOverrides: List<AssignmentOverride>?, assignment: Assignment?) : ScheduleItem {
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
            assignment = assignment
        )
    }
}