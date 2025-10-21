/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
import com.instructure.canvasapi2.models.*
import java.util.*

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
data class PlannerItemEntity(
    @PrimaryKey
    val id: Long,
    val courseId: Long?,
    val groupId: Long?,
    val userId: Long?,
    val contextType: String?,
    val contextName: String?,
    val plannableType: String,
    val plannableId: Long,
    val plannableTitle: String?,
    val plannableDetails: String?,
    val plannableTodoDate: String?,
    val plannableEndAt: Long?,
    val plannableAllDay: Boolean?,
    val plannableCourseId: Long?,
    val plannableGroupId: Long?,
    val plannableUserId: Long?,
    val plannableDate: Long,
    val htmlUrl: String?,
    val submissionStateSubmitted: Boolean?,
    val submissionStateExcused: Boolean?,
    val submissionStateGraded: Boolean?,
    val newActivity: Boolean?,
    val plannerOverrideId: Long?,
    val plannerOverrideMarkedComplete: Boolean?
) {
    constructor(plannerItem: PlannerItem, courseId: Long?) : this(
        id = plannerItem.plannable.id,
        courseId = courseId,
        groupId = plannerItem.groupId,
        userId = plannerItem.userId,
        contextType = plannerItem.contextType,
        contextName = plannerItem.contextName,
        plannableType = plannerItem.plannableType.name,
        plannableId = plannerItem.plannable.id,
        plannableTitle = plannerItem.plannable.title,
        plannableDetails = plannerItem.plannable.details,
        plannableTodoDate = plannerItem.plannable.todoDate,
        plannableEndAt = plannerItem.plannable.endAt?.time,
        plannableAllDay = plannerItem.plannable.allDay,
        plannableCourseId = plannerItem.plannable.courseId,
        plannableGroupId = plannerItem.plannable.groupId,
        plannableUserId = plannerItem.plannable.userId,
        plannableDate = plannerItem.plannableDate.time,
        htmlUrl = plannerItem.htmlUrl,
        submissionStateSubmitted = plannerItem.submissionState?.submitted,
        submissionStateExcused = plannerItem.submissionState?.excused,
        submissionStateGraded = plannerItem.submissionState?.graded,
        newActivity = plannerItem.newActivity,
        plannerOverrideId = plannerItem.plannerOverride?.id,
        plannerOverrideMarkedComplete = plannerItem.plannerOverride?.markedComplete
    )

    fun toApiModel(): PlannerItem {
        val plannable = Plannable(
            id = plannableId,
            title = plannableTitle ?: "",
            courseId = plannableCourseId,
            groupId = plannableGroupId,
            userId = plannableUserId,
            pointsPossible = null,
            dueAt = null,
            assignmentId = null,
            todoDate = plannableTodoDate,
            startAt = null,
            endAt = plannableEndAt?.let { Date(it) },
            details = plannableDetails,
            allDay = plannableAllDay
        )

        val submissionState = if (submissionStateSubmitted != null || submissionStateExcused != null || submissionStateGraded != null) {
            SubmissionState(
                submitted = submissionStateSubmitted ?: false,
                excused = submissionStateExcused ?: false,
                graded = submissionStateGraded ?: false
            )
        } else null

        val plannerOverride = if (plannerOverrideId != null) {
            PlannerOverride(
                id = plannerOverrideId,
                plannableType = PlannableType.valueOf(plannableType),
                plannableId = plannableId,
                markedComplete = plannerOverrideMarkedComplete ?: false
            )
        } else null

        return PlannerItem(
            courseId = courseId,
            groupId = groupId,
            userId = userId,
            contextType = contextType,
            contextName = contextName,
            plannableType = PlannableType.valueOf(plannableType),
            plannable = plannable,
            plannableDate = Date(plannableDate),
            htmlUrl = htmlUrl,
            submissionState = submissionState,
            newActivity = newActivity,
            plannerOverride = plannerOverride
        )
    }
}