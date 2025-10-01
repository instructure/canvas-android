/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.canvas.espresso.mockcanvas.endpoints

import com.instructure.canvas.espresso.mockcanvas.Endpoint
import com.instructure.canvas.espresso.mockcanvas.utils.successPaginatedResponse
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.models.DashboardCard
import java.util.Date

/**
 * Endpoint that can return a list of DashboardCards for the request user
 */
object DashboardCardsEndpoint : Endpoint(response = {
    GET {
        val now = Date()

        // Filter out concluded courses
        val currentCourses = data.courses.values.filter { it.endDate?.before(now) != true && it.enrollments?.all { it.enrollmentState != EnrollmentAPI.STATE_DELETED } ?: true }

        // Only show favorite courses. To match web behavior, if there are no favorites then we show all active courses.
        val favoriteCourses = data.courses.values.filter { it.isFavorite }.ifEmpty { currentCourses }

        val cards = favoriteCourses.map { DashboardCard(it.id, data.elementarySubjectPages) }
        request.successPaginatedResponse(cards)
    }
})
