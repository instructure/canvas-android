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
import com.instructure.canvasapi2.models.Grades
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.orDefault
import java.util.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["observedUserId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = SectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseSectionId"],
            onDelete = ForeignKey.SET_NULL
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
    @PrimaryKey
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
        enrollment.role?.name ?: Enrollment.EnrollmentType.NoEnrollment.name,
        enrollment.type?.name ?: Enrollment.EnrollmentType.NoEnrollment.name,
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

    fun toApiModel(
        grades: Grades? = null,
        observedUser: User? = null,
        user: User? = null
    ) = Enrollment(
        id = id,
        role = Enrollment.EnrollmentType.valueOf(role),
        type = Enrollment.EnrollmentType.valueOf(type),
        courseId = courseId.orDefault(),
        courseSectionId = courseSectionId.orDefault(),
        enrollmentState = enrollmentState,
        userId = userId,
        grades = grades,
        computedCurrentScore = computedCurrentScore,
        computedFinalScore = computedFinalScore,
        computedCurrentGrade = computedCurrentGrade,
        computedFinalGrade = computedFinalGrade,
        multipleGradingPeriodsEnabled = multipleGradingPeriodsEnabled,
        totalsForAllGradingPeriodsOption = totalsForAllGradingPeriodsOption,
        currentPeriodComputedCurrentScore = currentPeriodComputedCurrentScore,
        currentPeriodComputedFinalScore = currentPeriodComputedFinalScore,
        currentPeriodComputedCurrentGrade = currentPeriodComputedCurrentGrade,
        currentPeriodComputedFinalGrade = currentPeriodComputedFinalGrade,
        currentGradingPeriodId = currentGradingPeriodId,
        currentGradingPeriodTitle = currentGradingPeriodTitle,
        associatedUserId = associatedUserId,
        lastActivityAt = lastActivityAt,
        limitPrivilegesToCourseSection = limitPrivilegesToCourseSection,
        observedUser = observedUser,
        user = user
    )
}