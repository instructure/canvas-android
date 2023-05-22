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
import com.instructure.canvasapi2.models.Section
import com.instructure.canvasapi2.models.User
import com.instructure.pandautils.utils.orDefault

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
data class SectionEntity(
    @PrimaryKey
    val id: Long,
    var name: String,
    val courseId: Long?,
    val startAt: String?,
    val endAt: String?,
    val totalStudents: Int,
    val restrictEnrollmentsToSectionDates: Boolean
) {
    constructor(section: Section, courseId: Long? = null) : this(
        section.id,
        section.name,
        if (section.courseId != 0L) section.courseId else courseId,
        section.startAt,
        section.endAt,
        section.totalStudents,
        section.restrictEnrollmentsToSectionDates
    )

    fun toApiModel(
        students: List<User>? = null
    ) = Section(
        id = id,
        name = name,
        courseId = courseId.orDefault(),
        startAt = startAt,
        endAt = endAt,
        students = students,
        totalStudents = totalStudents,
        restrictEnrollmentsToSectionDates = restrictEnrollmentsToSectionDates
    )
}