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
package com.instructure.pandautils.room.offline.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.instructure.canvasapi2.models.Course

@Entity
data class EditDashboardItemEntity(
    @PrimaryKey
    val courseId: Long,
    val name: String,
    val isFavorite: Boolean,
    val enrollmentState: EnrollmentState,
    val position: Int
) {
    constructor(course: Course, enrollmentState: EnrollmentState, position: Int) : this(
        courseId = course.id,
        name = course.name,
        isFavorite = course.isFavorite,
        enrollmentState = enrollmentState,
        position = position
    )

    fun toCourse(): Course {
        return Course(
            id = courseId,
            name = name,
            isFavorite = isFavorite
        )
    }
}

enum class EnrollmentState {
    CURRENT, FUTURE, PAST
}