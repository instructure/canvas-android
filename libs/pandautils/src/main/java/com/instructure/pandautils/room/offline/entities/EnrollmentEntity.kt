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
 */

package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.Enrollment
import java.util.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["observedUserId"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = SectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseSectionId"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EnrollmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val role: String,
    val type: String,
    val courseId: Long?,
    val courseSectionId: Long?,
    val enrollmentState: String?,
    val userId: Long,
    val computedCurrentScore: Double?,
    val computedFinalScore: Double?,
    val computedCurrentGrade: String?,
    val computedFinalGrade: String?,
    val multipleGradingPeriodsEnabled: Boolean,
    val totalsForAllGradingPeriodsOption: Boolean,
    val currentPeriodComputedCurrentScore: Double?,
    val currentPeriodComputedFinalScore: Double?,
    val currentPeriodComputedCurrentGrade: String?,
    val currentPeriodComputedFinalGrade: String?,
    val currentGradingPeriodId: Long,
    val currentGradingPeriodTitle: String?,
    val associatedUserId: Long,
    val lastActivityAt: Date?,
    val limitPrivilegesToCourseSection: Boolean,
    val observedUserId: Long?
) {
    constructor(enrollment: Enrollment, courseId: Long? = null, courseSectionId: Long? = null, observedUserId: Long?) : this(
        enrollment.id,
        enrollment.role?.apiRoleString ?: Enrollment.EnrollmentType.NoEnrollment.apiRoleString,
        enrollment.type?.apiTypeString ?: Enrollment.EnrollmentType.NoEnrollment.apiTypeString,
        if (enrollment.courseId != 0L) enrollment.courseId else courseId,
        if (enrollment.courseSectionId != 0L) enrollment.courseSectionId else courseSectionId,
        enrollment.enrollmentState,
        enrollment.userId,
        enrollment.computedCurrentScore,
        enrollment.computedFinalScore,
        enrollment.computedCurrentGrade,
        enrollment.computedFinalGrade,
        enrollment.multipleGradingPeriodsEnabled,
        enrollment.totalsForAllGradingPeriodsOption,
        enrollment.currentPeriodComputedCurrentScore,
        enrollment.currentPeriodComputedFinalScore,
        enrollment.currentPeriodComputedCurrentGrade,
        enrollment.currentPeriodComputedFinalGrade,
        enrollment.currentGradingPeriodId,
        enrollment.currentGradingPeriodTitle,
        enrollment.associatedUserId,
        enrollment.lastActivityAt,
        enrollment.limitPrivilegesToCourseSection,
        observedUserId
    )
}