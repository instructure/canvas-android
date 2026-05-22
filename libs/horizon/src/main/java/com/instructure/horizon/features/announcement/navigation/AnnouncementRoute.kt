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
 */
package com.instructure.horizon.features.announcement.navigation

import com.instructure.horizon.model.HorizonAnnouncementKind

sealed class AnnouncementRoute(val route: String) {
    data object Details : AnnouncementRoute("announcement_details/{$KIND}/{$ID}?$COURSE_ID={$COURSE_ID}") {
        fun route(kind: HorizonAnnouncementKind, id: Long, courseId: Long?): String {
            val base = "announcement_details/${kind.name.lowercase()}/$id"
            return if (courseId == null) base else "$base?$COURSE_ID=$courseId"
        }
    }

    companion object {
        const val KIND: String = "kind"
        const val ID: String = "id"
        const val COURSE_ID: String = "courseId"
    }
}
