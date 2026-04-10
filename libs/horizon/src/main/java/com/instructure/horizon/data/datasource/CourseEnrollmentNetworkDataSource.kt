/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.data.datasource

import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.managers.graphql.horizon.DashboardEnrollment
import com.instructure.canvasapi2.managers.graphql.horizon.HorizonGetCoursesManager
import com.instructure.canvasapi2.utils.ApiPrefs
import javax.inject.Inject

class CourseEnrollmentNetworkDataSource @Inject constructor(
    private val horizonGetCoursesManager: HorizonGetCoursesManager,
    private val apiPrefs: ApiPrefs,
    private val enrollmentApi: EnrollmentAPI.EnrollmentInterface,
) {

    suspend fun getEnrollments(
        forceRefresh: Boolean,
    ): List<DashboardEnrollment> {
        return horizonGetCoursesManager.getDashboardEnrollments(
            userId = apiPrefs.user?.id ?: -1,
            forceNetwork = forceRefresh,
        ).dataOrThrow
    }

    suspend fun acceptInvite(courseId: Long, enrollmentId: Long) {
        enrollmentApi.acceptInvite(courseId, enrollmentId, RestParams()).dataOrThrow
    }
}
