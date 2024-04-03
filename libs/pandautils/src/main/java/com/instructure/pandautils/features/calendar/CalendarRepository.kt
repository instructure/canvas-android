/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.calendar

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.DataResult

interface CalendarRepository {

    suspend fun getPlannerItems(
        startDate: String,
        endDate: String,
        contextCodes: List<String>,
        forceNetwork: Boolean
    ): List<PlannerItem>

    suspend fun getCanvasContexts(): DataResult<Map<CanvasContext.Type, List<CanvasContext>>>

    suspend fun getCalendarFilterLimit(): Int
}