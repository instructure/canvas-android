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
package com.instructure.horizon.features.inbox.navigation

import com.instructure.horizon.features.inbox.HorizonInboxItemType
import com.instructure.horizon.features.inbox.navigation.HorizonInboxRoute.InboxDetails.Companion.COURSE_ID
import com.instructure.horizon.features.inbox.navigation.HorizonInboxRoute.InboxDetails.Companion.ID

sealed class HorizonInboxRoute(val route: String) {
    data object InboxList : HorizonInboxRoute("inbox_list")

    data class InboxDetails(
        val type: HorizonInboxItemType,
        val id: Long,
        val courseId: Long? = null,
    ) : HorizonInboxRoute("inbox_details") {
        companion object {
            const val TYPE: String = "type"
            const val ID: String = "id"
            const val COURSE_ID: String = "courseId"
            const val route: String = "inbox_details/{$TYPE}/{$ID}?$COURSE_ID={$COURSE_ID}"
            fun route(id: Long, type: HorizonInboxItemType, courseId: Long?): String {
                return if (courseId == null) {
                    "inbox_details/${type.navigationValue}/$id"
                } else {
                    "inbox_details/${type.navigationValue}/$id?$COURSE_ID=${courseId}"
                }
            }
        }
    }

    data object InboxCompose : HorizonInboxRoute("inbox_compose")
    data object InboxDetailsDeepLink : HorizonInboxRoute("conversations/{$ID}")
    data object CourseAnnouncementDetailsDeepLink : HorizonInboxRoute("/courses/{$COURSE_ID}/discussion_topics/{$ID}")
}