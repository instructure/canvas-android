/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.features.todolist

import com.instructure.canvasapi2.CanvasRestAdapter
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.models.PlannerOverride
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.canvasapi2.utils.depaginate
import javax.inject.Inject

class ToDoListRepository @Inject constructor(
    private val plannerApi: PlannerAPI.PlannerInterface,
    private val courseApi: CourseAPI.CoursesInterface
) {
    suspend fun getPlannerItems(
        startDate: String,
        endDate: String,
        forceRefresh: Boolean
    ): DataResult<List<PlannerItem>> {
        val restParams = RestParams(isForceReadFromNetwork = forceRefresh, usePerPageQueryParam = true)
        return plannerApi.getPlannerItems(
            startDate = startDate,
            endDate = endDate,
            contextCodes = emptyList(),
            restParams = restParams
        ).depaginate { nextUrl ->
            plannerApi.nextPagePlannerItems(nextUrl, restParams)
        }
    }

    suspend fun getCourses(forceRefresh: Boolean): DataResult<List<Course>> {
        val restParams = RestParams(isForceReadFromNetwork = forceRefresh)
        return courseApi.getFirstPageCourses(restParams).depaginate { nextUrl ->
            courseApi.next(nextUrl, restParams)
        }
    }

    suspend fun updatePlannerOverride(
        plannerOverrideId: Long,
        markedComplete: Boolean
    ): DataResult<PlannerOverride> {
        val restParams = RestParams(isForceReadFromNetwork = true)
        return plannerApi.updatePlannerOverride(
            plannerOverrideId = plannerOverrideId,
            complete = markedComplete,
            params = restParams
        )
    }

    suspend fun createPlannerOverride(
        plannableId: Long,
        plannableType: PlannableType,
        markedComplete: Boolean
    ): DataResult<PlannerOverride> {
        val restParams = RestParams(isForceReadFromNetwork = true)
        val override = PlannerOverride(
            plannableId = plannableId,
            plannableType = plannableType,
            markedComplete = markedComplete
        )
        return plannerApi.createPlannerOverride(override, restParams)
    }

    fun invalidateCachedResponses() {
        CanvasRestAdapter.clearCacheUrls("planner")
    }
}