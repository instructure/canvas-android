/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.navigation

sealed class LearnRoute {
    data class LearnCourseDetailsScreen(val courseId: Long): LearnRoute() {
        companion object {
            const val courseIdAttr = "courseId"
            private const val baseUrl = "learn/courses"
            const val route = "$baseUrl/{$courseIdAttr}"
            fun route(courseId: Long) = "$baseUrl/$courseId"
        }
    }

    data class LearnProgramDetailsScreen(val courseId: Long): LearnRoute() {
        companion object {
            const val programIdAttr = "programId"
            private const val baseUrl = "learn/programs"
            const val route = "$baseUrl/{$programIdAttr}"
            fun route(courseId: Long) = "$baseUrl/$courseId"
        }
    }
}