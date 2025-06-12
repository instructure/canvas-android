/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.features.speedgrader.details.studentnotes

import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.ColumnDatum
import com.instructure.canvasapi2.models.CustomColumn


class StudentNotesRepository(
    private val courseApi: CourseAPI.CoursesInterface
) {
    suspend fun getCustomGradeBookColumns(courseId: Long, forceRefresh: Boolean): List<CustomColumn> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return courseApi.getCustomGradeBookColumns(courseId, params).dataOrThrow
    }

    suspend fun getCustomGradeBookColumnsEntries(courseId: Long, columnId: Long, forceRefresh: Boolean): List<ColumnDatum> {
        val params = RestParams(isForceReadFromNetwork = forceRefresh)
        return courseApi.getCustomGradeBookColumnsEntries(courseId, columnId, params).dataOrThrow
    }
}
