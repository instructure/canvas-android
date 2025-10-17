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
package com.instructure.horizon.features.dashboard.widget.myprogress

import com.google.gson.annotations.SerializedName
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetWidgetsManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.util.deserializeDynamicObject
import java.util.Date
import javax.inject.Inject

data class MyProgressWidgetData(
    val data: List<MyProgressWidgetDataEntry>,
    val lastModifiedDate: Date?,
)

data class MyProgressWidgetDataEntry(
    @SerializedName("course_id")
    val courseId: Long?,

    @SerializedName("course_name")
    val courseName: String?,

    @SerializedName("user_id")
    val userId: Long?,

    @SerializedName("user_uuid")
    val userUUID: String?,

    @SerializedName("user_name")
    val userName: String?,

    @SerializedName("user_avatar_image_url")
    val userAvatarUrl: String?,

    @SerializedName("user_email")
    val userEmail: String?,

    @SerializedName("module_count_completed")
    val moduleCountCompleted: Int?,

    @SerializedName("module_count_started")
    val moduleCountStarted: Int?,

    @SerializedName("module_count_locked")
    val moduleCountLocked: Int?,

    @SerializedName("module_count_total")
    val moduleCountTotal: Int?,
)

class DashboardMyProgressRepository @Inject constructor(
    private val apiPrefs: ApiPrefs,
    private val getWidgetsManager: GetWidgetsManager,
    private val getCoursesManager: HorizonGetCoursesManager,
) {
    suspend fun getLearningStatusData(courseId: Long? = null, forceNetwork: Boolean): MyProgressWidgetData? {
        return getWidgetsManager.getLearningStatusWidgetData(courseId, forceNetwork).deserializeDynamicObject<MyProgressWidgetData>()
    }

    suspend fun getCourses(forceNetwork: Boolean): List<CourseWithProgress> {
        return getCoursesManager.getCoursesWithProgress(apiPrefs.user?.id ?: 0, forceNetwork).dataOrThrow
    }
}
