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

package com.instructure.pandautils.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.SET_NULL
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.Course

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = TermEntity::class,
            parentColumns = ["id"],
            childColumns = ["termId"],
            onDelete = SET_NULL
        )
    ]
)
data class CourseEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val originalName: String?,
    val courseCode: String?,
    val startAt: String?,
    val endAt: String?,
    var syllabusBody: String?,
    val hideFinalGrades: Boolean,
    val isPublic: Boolean,
    val license: String,
    val termId: Long?,
    val needsGradingCount: Long,
    val isApplyAssignmentGroupWeights: Boolean,
    val currentScore: Double?,
    val finalScore: Double?,
    val currentGrade: String?,
    val finalGrade: String?,
    val isFavorite: Boolean,
    val accessRestrictedByDate: Boolean,
    val imageUrl: String?,
    val bannerImageUrl: String?,
    val isWeightedGradingPeriods: Boolean,
    val hasGradingPeriods: Boolean,
    val homePage: String?,
    val restrictEnrollmentsToCourseDate: Boolean,
    val workflowState: String?,
    val homeroomCourse: Boolean,
    val courseColor: String?
) {
    constructor(course: Course) : this(
        course.id,
        course.name,
        course.originalName,
        course.courseCode,
        course.startAt,
        course.endAt,
        course.syllabusBody,
        course.hideFinalGrades,
        course.isPublic,
        course.license?.apiString ?: Course.License.PRIVATE_COPYRIGHTED.apiString,
        course.term?.id,
        course.needsGradingCount,
        course.isApplyAssignmentGroupWeights,
        course.currentScore,
        course.finalScore,
        course.currentGrade,
        course.finalGrade,
        course.isFavorite,
        course.accessRestrictedByDate,
        course.imageUrl,
        course.bannerImageUrl,
        course.isWeightedGradingPeriods,
        course.hasGradingPeriods,
        course.homePage?.apiString,
        course.restrictEnrollmentsToCourseDate,
        course.workflowState?.apiString,
        course.homeroomCourse,
        course.courseColor
    )
}