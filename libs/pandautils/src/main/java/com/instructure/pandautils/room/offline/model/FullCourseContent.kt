/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.pandautils.room.offline.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.instructure.pandautils.room.offline.entities.*

data class FullCourseContent(
    @Embedded
    val courseEntity: CourseEntity,

    @Relation(
        parentColumn = "termId",
        entityColumn = "id"
    )
    val termEntity: TermEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "courseId"
    )
    val enrollmentEntities: List<EnrollmentEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "courseId"
    )
    val sectionEntities: List<SectionEntity>,

    @Relation(
        parentColumn = "id",
        entity = GradingPeriodEntity::class,
        entityColumn = "id",
        associateBy = Junction(
            value = CourseGradingPeriodEntity::class,
            parentColumn = "courseId",
            entityColumn = "gradingPeriodId"
        )
    )
    val gradingPeriodEntities: List<GradingPeriodEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "courseId"
    )
    val tabEntities: List<TabEntity>
) {
    fun toApiModel() = courseEntity.toApiModel(
        term = termEntity.toApiModel(),
        enrollments = enrollmentEntities.map { it.toApiModel() }.toMutableList(),
        sections = sectionEntities.map { it.toApiModel() },
        gradingPeriods = gradingPeriodEntities.map { it.toApiModel() },
        tabs = tabEntities.map { it.toApiModel() }
    )
}
