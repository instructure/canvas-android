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
import com.instructure.canvasapi2.models.*

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = TermEntity::class,
            parentColumns = ["id"],
            childColumns = ["termId"],
            onDelete = ForeignKey.CASCADE
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
    val courseColor: String?,
    val gradingScheme: List<GradingSchemeRow>?,
    val pointsBasedGradingScheme: Boolean,
    val scalingFactor: Double
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
        course.license?.name ?: Course.License.PRIVATE_COPYRIGHTED.name,
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
        course.homePage?.name,
        course.restrictEnrollmentsToCourseDate,
        course.workflowState?.name,
        course.homeroomCourse,
        course.courseColor,
        course.gradingScheme,
        course.pointsBasedGradingScheme,
        course.scalingFactor
    )

    fun toApiModel(
        term: Term? = null,
        enrollments: MutableList<Enrollment>? = null,
        sections: List<Section> = emptyList(),
        gradingPeriods: List<GradingPeriod>? = null,
        tabs: List<Tab>? = null,
        settings: CourseSettings? = null
    ): Course {
        return Course(
            id = id,
            name = name,
            originalName = originalName,
            courseCode = courseCode,
            startAt = startAt,
            endAt = endAt,
            syllabusBody = syllabusBody,
            hideFinalGrades = hideFinalGrades,
            isPublic = isPublic,
            license = Course.License.valueOf(license),
            term = term,
            enrollments = enrollments,
            needsGradingCount = needsGradingCount,
            isApplyAssignmentGroupWeights = isApplyAssignmentGroupWeights,
            currentScore = currentScore,
            finalScore = finalScore,
            currentGrade = currentGrade,
            finalGrade = finalGrade,
            isFavorite = isFavorite,
            accessRestrictedByDate = accessRestrictedByDate,
            imageUrl = imageUrl,
            bannerImageUrl = bannerImageUrl,
            isWeightedGradingPeriods = isWeightedGradingPeriods,
            hasGradingPeriods = hasGradingPeriods,
            sections = sections,
            homePage = homePage?.let { Course.HomePage.valueOf(homePage) },
            restrictEnrollmentsToCourseDate = restrictEnrollmentsToCourseDate,
            workflowState = workflowState?.let { Course.WorkflowState.valueOf(it) },
            homeroomCourse = homeroomCourse,
            courseColor = courseColor,
            gradingPeriods = gradingPeriods,
            tabs = tabs,
            settings = settings,
            gradingSchemeRaw = gradingScheme?.map { listOf(it.name, it.value) },
            pointsBasedGradingScheme = pointsBasedGradingScheme,
            scalingFactor = scalingFactor
        )
    }
}